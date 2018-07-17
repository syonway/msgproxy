package com.guanghui.admonitor.syctrl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guanghui.admonitor.DemoLoop;
import com.guanghui.admonitor.backservice.SendMsg;
import com.guanghui.admonitor.backservice.ctrlcntmsg.send.FindReplayClipTaskMsg;
import com.guanghui.admonitor.backservice.ctrlcntmsg.send.RefAdClips;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.management.monitor.Monitor;
import javax.swing.plaf.nimbus.State;

public class startMatch implements Runnable{
    private long starttime = System.currentTimeMillis();
    @Override
    public void run(){
       // DemoLoop dm = DemoLoop.getInstance();
      //  SendMsg sendms = dm.getSendMsg();
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        System.out.println(dateFormat.format( now ));

        //读取频道，获取monitor信息，分配
        try{
        String sqlmc = "select * FROM channelinfo";
        //Connection con = dm.getConnection();

            String driver = "com.mysql.cj.jdbc.Driver";
            String url = "jdbc:mysql://115.28.61.129:3306/syads?serverTimezone=UTC";
            String user = "root";
            String password = "123456";
            Class.forName(driver);
            Connection con = DriverManager.getConnection(url, user, password);
            if (!con.isClosed())
                System.out.println("Succeeded connecting to the Database!");

        Statement statement = con.createStatement();
        ResultSet channelrs = statement.executeQuery(sqlmc);
        List<String> channellist = new ArrayList<>();
        List<String> channelMatch = new ArrayList<>();
        List<Integer> emmonitor = new ArrayList<>();
        while (channelrs.next()){
            channellist.add(channelrs.getString("name"));
        }
        String queryEmptyMonitor = "SELECT * FROM monitor WHERE task=0 AND type = 'matchclip' ";
        String queryMatchTaskMonitor = "SELECT * FROM monitor WHERE task=1 AND type = 'matchclip' ";
        ResultSet monitorrs = statement.executeQuery(queryMatchTaskMonitor);
        while(monitorrs.next()){
            channelMatch.add(monitorrs.getString("task_on"));
        }
        ResultSet emonitorrs = statement.executeQuery(queryEmptyMonitor);
        while(emonitorrs.next()){
            emmonitor.add(emonitorrs.getInt("id"));
        }
        for(int i=0;i<channellist.size();i++){
            if(!channelMatch.contains(channellist.get(i))){
                String addChannelMatch = "UPDATE monitor SET task = 1 AND task_on= '"+channellist.get(i)+"'WHERE id = ";
                if(emmonitor.size()>0) {
                    int monitorid = emmonitor.get(emmonitor.size() - 1);
                    if (statement.executeUpdate(addChannelMatch + monitorid) > 0) {
                        emmonitor.remove(emmonitor.size() - 1);
                        System.out.println(channellist.get(i) + " has match monitor now");
                    } else {
                        System.out.println(channellist.get(i) + " fail to get match monitor ");
                    }
                }else{
                    System.out.println("need more match monitor");
                    break;
                }

            }else{
                System.out.println(channellist.get(i)+" has already been matching ");
            }

        }

        //对应频道获取ref url，无则不发送指令
        Statement statement2 = con.createStatement();
        ResultSet monitorAndChannelrs = statement2.executeQuery(queryMatchTaskMonitor);
        while(monitorAndChannelrs.next()){
            FindReplayClipTaskMsg matask=new FindReplayClipTaskMsg();
            matask.channelPath = "ts/"+monitorAndChannelrs.getString("task_on");
            int moid = monitorAndChannelrs.getInt("id");

            String getRefSql = "SELECT *  FROM adinfo INNER JOIN channelinfo on adinfo.channelid=channelinfo.id WHERE url = ? and channelinfo.name='"+monitorAndChannelrs.getString("task_on")+"'";
            String getRefUrl = "SELECT DISTINCT url FROM adinfo INNER JOIN channelinfo on adinfo.channelid=channelinfo.id WHERE channelinfo.name='"+monitorAndChannelrs.getString("task_on")+"'";
            String getRefNumOfNasIp = "SELECT count(*) cou FROM adinfo INNER JOIN channelinfo on adinfo.channelid=channelinfo.id WHERE url = ? and channelinfo.name='"+monitorAndChannelrs.getString("task_on")+"'";
            String getRecordIp = "SELECT pathIp from channel_tab WHERE name = '"+monitorAndChannelrs.getString("task_on")+"'";
            ResultSet recordIprs = statement.executeQuery(getRecordIp);
            if(recordIprs.next())
                matask.nasIp = recordIprs.getString("pathIp");

            PreparedStatement refstatement= con.prepareStatement(getRefSql);
            PreparedStatement refnum = con.prepareStatement(getRefNumOfNasIp);
            ResultSet urlrs = statement.executeQuery(getRefUrl);
            List<String> urllist = new ArrayList<String>();

            int urltotalnum = 0;

            while (urlrs.next()){
                urllist.add(urlrs.getString("url"));
            }
            for(int n=0;n<urllist.size();n++){
                RefAdClips ref = new RefAdClips();
                ref.nasIp = urllist.get(n);
                refstatement.setString(1,urllist.get(n));
                refnum.setString(1,urllist.get(n));
                ResultSet refrs = refstatement.executeQuery();
                ResultSet refnumrs = refnum.executeQuery();
                int num = 0;
                while(refnumrs.next()) {
                    num = refnumrs.getInt("cou");
                }
                urltotalnum += num;
                while(refrs.next()){
                    if(num>0) {
                        // String mn2 = refrs.getString("lambdaFile");
                        String u = refrs.getString("url");

                        ref.adClipUrls.add(refrs.getString("lambdaFile"));
                    }
                }
                matask.refAdClips.add(ref);
            }
            if(urltotalnum>0) {
                matask.startTime = Long.toString(System.currentTimeMillis());
                matask.endTime = Long.toString(Long.parseLong(matask.startTime)+60*60*1000);
                String mtask = (new ObjectMapper()).writeValueAsString(matask);
                File matchf = new File("testjson\\findreplayclip.json");
                BufferedWriter output3 = new BufferedWriter(new FileWriter(matchf));
                output3.write(mtask);
                output3.close();


                DemoLoop demoLoop = DemoLoop.getInstance();
                String getMonitor = "SELECT id from monitor where task=0 and problem=0 and type = 'matchclip'";
                Statement mstatement = con.createStatement();
                ResultSet mrs = mstatement.executeQuery(getMonitor);
                if(mrs.next()) {
                    int id  = mrs.getInt("id");
                    String res = demoLoop.sendmsg(new String[]{"send", "matchadreplaytask", Integer.toString(id), "testjson\\findreplayclip.json"});
                    if (res.equals("null")) {
                        System.out.println(id + " wrong");
                        String monitorWrong = "UPDATE monitor SET problem = 1 WHERE id=" + id;
                        int i = statement.executeUpdate(monitorWrong);
                    } else {
                        if ((new JSONObject(res)).getInt("code") ==0) {
                            if(mstatement.executeUpdate("UPDATE monitor SET task =1,task_on = '"+monitorAndChannelrs.getString("")+"' WHERE id=" + id)>0)
                                ;
                        } else if((new JSONObject(res)).getInt("code") == 0) {
                            System.out.println("send match to " + moid + " fail");
                            String monitorStop = "UPDATE monitor SET task = 0,task_on='',task_plan='' WHERE id=" + id;
                        }
                    }
                }
                else{
                    System.out.println("need more match monitor");
                }


            }
            con.close();
        }


        //读取log结果

        //ps明天要看addclip信息，添加数据（自行拼凑文件名）
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
