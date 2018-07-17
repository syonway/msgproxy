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

public class startMatch implements Runnable {
    private long starttime = System.currentTimeMillis();

    @Override
    public void run() {
        // DemoLoop dm = DemoLoop.getInstance();
        // SendMsg sendms = dm.getSendMsg();
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        System.out.println(dateFormat.format(now));

        // 读取频道，获取monitor信息，分配
        try {
            String sqlmc = "select * FROM channelinfo";
            // Connection con = dm.getConnection();

            String driver = "com.mysql.cj.jdbc.Driver";
            String url = "jdbc:mysql://115.28.61.129:3306/syads?serverTimezone=UTC";
            String user = "root";
            String password = "123456";
            Class.forName(driver);
            Connection con = DriverManager.getConnection(url, user, password);
            if (!con.isClosed())
                System.out.println("Succeeded connecting to the Database!");

            Statement statement = con.createStatement();
            

            // 对应频道获取ref url，无则不发送指令
            // String queryMatchTaskMonitor = "SELECT * FROM monitor WHERE task=1 AND type =
            // 'matchclip' ";
            String queryMatchTaskMonitor = "SELECT * FROM monitor WHERE task=1 AND type = 'matchclip' ";
            String queryNotStopMatch = "SELECT count(*) cou FROM monitor WHERE task=2 AND type = 'matchclip' ";
            Statement statement2 = con.createStatement();
            ResultSet notStoprs = statement2.executeQuery(queryNotStopMatch);
            int notstopnum = 0;
            while(notStoprs.next()){
                notstopnum = notStoprs.getInt("cou");
            }
            if(notstopnum>0) {
                ResultSet monitorAndChannelrs = statement2.executeQuery(queryMatchTaskMonitor);
                while (monitorAndChannelrs.next()) {
                    FindReplayClipTaskMsg matask = new FindReplayClipTaskMsg();
                    matask.channelPath = "ts/" + monitorAndChannelrs.getString("task_on");
                    int moid = monitorAndChannelrs.getInt("id");

                    String getRefSql = "SELECT *  FROM adinfo INNER JOIN channelinfo on adinfo.channelid=channelinfo.id WHERE url = ? and channelinfo.name='"
                            + monitorAndChannelrs.getString("task_on") + "'";
                    String getRefUrl = "SELECT DISTINCT url FROM adinfo INNER JOIN channelinfo on adinfo.channelid=channelinfo.id WHERE channelinfo.name='"
                            + monitorAndChannelrs.getString("task_on") + "'";
                    String getRefNumOfNasIp = "SELECT count(*) cou FROM adinfo INNER JOIN channelinfo on adinfo.channelid=channelinfo.id WHERE url = ? and channelinfo.name='"
                            + monitorAndChannelrs.getString("task_on") + "'";
                    String getRecordIp = "SELECT pathIp from channel_tab WHERE name = '"
                            + monitorAndChannelrs.getString("task_on") + "'";
                    ResultSet recordIprs = statement.executeQuery(getRecordIp);
                    if (recordIprs.next())
                        matask.nasIp = recordIprs.getString("pathIp");

                    PreparedStatement refstatement = con.prepareStatement(getRefSql);
                    PreparedStatement refnum = con.prepareStatement(getRefNumOfNasIp);
                    ResultSet urlrs = statement.executeQuery(getRefUrl);
                    List<String> urllist = new ArrayList<String>();

                    int urltotalnum = 0;

                    while (urlrs.next()) {
                        urllist.add(urlrs.getString("url"));
                    }
                    for (int n = 0; n < urllist.size(); n++) {
                        RefAdClips ref = new RefAdClips();
                        ref.nasIp = urllist.get(n);
                        refstatement.setString(1, urllist.get(n));
                        refnum.setString(1, urllist.get(n));
                        ResultSet refrs = refstatement.executeQuery();
                        ResultSet refnumrs = refnum.executeQuery();
                        int num = 0;
                        while (refnumrs.next()) {
                            num = refnumrs.getInt("cou");
                        }
                        urltotalnum += num;
                        while (refrs.next()) {
                            if (num > 0) {
                                // String mn2 = refrs.getString("lambdaFile");
                                String u = refrs.getString("url");

                                ref.adClipUrls.add(refrs.getString("lambdaFile"));
                            }
                        }
                        matask.refAdClips.add(ref);
                    }
                    if (urltotalnum > 0) {
                        matask.startTime = Long.toString(System.currentTimeMillis());
                        matask.endTime = Long.toString(Long.parseLong(matask.startTime) + 60 * 60 * 1000);
                        starttime = Long.parseLong(matask.endTime);
                        String mtask = (new ObjectMapper()).writeValueAsString(matask);
                        File matchf = new File("testjson\\findreplayclip.json");
                        BufferedWriter output3 = new BufferedWriter(new FileWriter(matchf));
                        output3.write(mtask);
                        output3.close();

                        DemoLoop demoLoop = DemoLoop.getInstance();
                        String res = demoLoop.sendmsg(new String[] { "send", "matchadreplaytask",
                                Integer.toString(moid), "testjson\\findreplayclip.json" });
                        if (res.equals("null")) {
                            System.out.println(moid + " wrong");
                            String monitorWrong = "UPDATE monitor SET problem = 1 WHERE id=" + moid;
                            int i = statement.executeUpdate(monitorWrong);
                        }
                        // 统一发送指令检验，-1则task=2，但只有没有task=2 monitor时，改变starttime统一发送
                        else {
                            if ((new JSONObject(res)).getInt("code") == 0) {
                                if (statement.executeUpdate("UPDATE monitor SET task =1,task_on = '"
                                        + monitorAndChannelrs.getString("") + "' WHERE id=" + moid) > 0)
                                    System.out.print(moid + " finished task successfully");

                            } else if ((new JSONObject(res)).getInt("code") == -1) {
                                System.out.println(moid + " send new task fail ");
                                String monitorNotStop = "UPDATE monitor SET task = 2 WHERE id=" + moid;
                                if (statement.executeUpdate(monitorNotStop) > 0) {
                                    System.out.print(moid + " has not finished task");
                                }
                            }
                        }
                    }

                }
            }
            
            con.close();

            // 读取log结果

            // ps明天要看addclip信息，添加数据（自行拼凑文件名）
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
