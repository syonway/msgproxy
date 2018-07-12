package com.guanghui.admonitor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.guanghui.admonitor.backservice.RecvMsg;
import com.guanghui.admonitor.backservice.SendMsg;
import com.guanghui.admonitor.backservice.ctrlcnt_demo.ProcessTaskTask;
import com.guanghui.admonitor.backservice.ctrlcnt_demo.TestDataBase;
import com.guanghui.admonitor.backservice.ctrlcntmsg.CommonRespose;
import com.guanghui.admonitor.backservice.ctrlcntmsg.RecvTask;
import com.guanghui.admonitor.backservice.ctrlcntmsg.recv.AddDelRecvTaskResult;
import com.guanghui.admonitor.backservice.ctrlcntmsg.send.*;
import com.guanghui.admonitor.backservice.ctrlcntmsg.send.response.CreateRefAdClipResponse;
import com.guanghui.admonitor.backservice.ctrlcntmsg.send.response.MonitorType;
import com.guanghui.admonitor.backservice.ctrlcntmsg.send.response.NasMountInfo;
import com.guanghui.admonitor.backservice.ctrlcntmsg.send.response.NasMountInfos;
import com.guanghui.admonitor.catalog_ui.demo.H2Db;
import com.guanghui.admonitor.catalog_ui.demo.MsgProcess;
import com.guanghui.admonitor.catalog_ui.msgservchannel.MsgChannelServer;
import com.guanghui.admonitor.catalog_ui.msgservchannel.msgs.ChannelMatchMsg;
import com.guanghui.admonitor.catalog_ui.msgservchannel.msgs.ReqChannelMatchMsg;
import com.guanghui.itvm.dcbase.DataChannelMng;
import com.guanghui.admonitor.syctrl.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.sql.*;
import java.util.HashMap;

import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.guanghui.admonitor.syctrl.logThread;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.json.JSONObject;

/**
 * Created by cuiju on 2017/1/12. 16 34
 */
public class DemoLoop {
    public static void main(String[] args){
        DemoLoop demoMain = DemoLoop.getInstance();
        demoMain.mainLoop(args);
      //  demoMain.initService(args);
    }

    private static DemoLoop demoLoop = new DemoLoop();
    public static DemoLoop getInstance(){
        return demoLoop;
    }

    private Connection con;
    private Statement statement;
    private void connSql(){
        String driver = "com.mysql.cj.jdbc.Driver";
        String url = "jdbc:mysql://115.28.61.129:3306/syads?serverTimezone=UTC";
        String user = "root";
        String password = "123456";
        Class.forName(driver);
        con = DriverManager.getConnection(url,user,password);
        if(!con.isClosed())
            System.out.println("Succeeded connecting to the Database!");
        statement = con.createStatement();
    }
    
    public Connection getConnection(){
        return DemoLoop.getInstance().con;
    }

    @SuppressWarnings("InfiniteLoopStatement")
    private void mainLoop(String[] args){
        Cfg cfg = Cfg.getInstance();
        if (args.length < 1){
            System.out.println("paramete: cfgfile");
        }

        connSql();;
        ////for catalog ui msgproxy demo start
        DemoLoop backEndServiceDemo = new DemoLoop();
        String bindip = "0.0.0.0";
        int port = cfg.getCatalogUiListenPort();
        int dataWebPort = cfg.getDbWebPort();
        backEndServiceDemo.startDb(dataWebPort, cfg.getDatabaseUrl(), cfg.getDbDriver(),
                cfg.getDbuser(), cfg.getDbPasswd());
        backEndServiceDemo.startMsgServer(bindip, port);
        ////for catalog ui msgproxy demo end

        ////for backservice demo start
        TestDataBase testDataBase = TestDataBase.getIns();
        testDataBase.connet2Db(cfg.getDatabaseUrl(), cfg.getDbuser(), cfg.getDbPasswd());
        dataChannelMng = new DataChannelMng();
        dataChannelMng.startCnt2Monitor("0.0.0.0", cfg.getServCnt2MonitorPort());
        dataChannelMng.startMonitor2Cnt("0.0.0.0", cfg.getServMonitor2CntPort(), new RecvMsg());
        Thread sendProcessthread = new Thread(new ProcessTaskTask(dataChannelMng));
        sendMsg = new SendMsg(dataChannelMng);
        sendProcessthread.setDaemon(true);
        ///for backservice demo end
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while (true){
            try {
              //  String ress = sendmsg(new String[]{"send","add_calfeaturetask","2000","testjson\\caltask.json"});
                System.out.print("cmd>");
                String line = reader.readLine();

                DemoMain dm1 = new DemoMain();
                dm1.commandProcess(line);

              //  String n=sendmsg(new String[]{"send","del_calfeaturetask","2000","testjson\\caltask.json"});

                //获取所有monitorid
                System.out.println("1:");

                String mstr = getmsg(new String[]{"get","onlinemonitor"}); //id
                System.out.println(StringUtils.isNumeric(mstr));
                String[] monitorlist = new String[]{};
                while(mstr.equals("no online monitor")){
                    Thread.currentThread().sleep(3000);
                    mstr = getmsg(new String[]{"get","onlinemonitor"});
                }
                if(StringUtils.isNumeric(mstr)){
                    //  monitorid = mstr;
                    monitorlist[0] = mstr;
                }
                else{
                    monitorlist = mstr.split(" ");
                }
                for(int i=0;i<monitorlist.length;i++) System.out.print(monitorlist[i]+" ");
                System.out.println("ml:"+monitorlist.length);

            



                //插入新建monitor
                String sqlm = "select type,count(*) totalCount FROM monitor where id =  ";
                String sqlm2 = "INSERT INTO monitor (id,type,ip) VALUES (?,?,?) ";
                //PreparedStatement pstatement1 = con.prepareStatement(sqlm);
                for(int i=0;i<monitorlist.length;i++){
                    ResultSet rs = statement.executeQuery(sqlm+monitorlist[i]);
                    if(rs.next()){
                        if(rs.getInt("totalCount")==0) {

                            JSONObject typers = new JSONObject(getmsg(new String[]{"get", "monitortype", monitorlist[i]}));
                            String type = typers.getString("type");
                            String[] ipadd = StringUtils.substringsBetween(typers.get("hostIps").toString(), "\",\"", "\"]");
                            System.out.println(typers.get("hostIps").toString());
                            //re = statement.executeUpdate(sqlm2);
                            PreparedStatement pstatement12 = con.prepareStatement(sqlm2);
                            pstatement12.setInt(1, Integer.parseInt(monitorlist[i]));
                            pstatement12.setString(2, type);
                            pstatement12.setString(3, ipadd[0]);
                            int rs2 = pstatement12.executeUpdate();
                        }
                    }

                }


                monitorlist = new String[]{"4000","4001","4002"};
                //获取四种空monitor
                String queryEmptyMonitor = "SELECT * FROM monitor WHERE task=0 ";
                ResultSet rsEMmonitor = statement.executeQuery(queryEmptyMonitor);
                List<MonitorItem> recoMonitorlist = new ArrayList();
                List<MonitorItem> calMonitorlist = new ArrayList();
                List<MonitorItem> refMonitorlist = new ArrayList();
                List<MonitorItem> matchMonitorlist = new ArrayList();
                List<MonitorItem> problemMonitorlist = new ArrayList();
                while (rsEMmonitor.next()){
                    if(Arrays.asList(monitorlist).contains(Integer.toString(rsEMmonitor.getInt("id")))) {
                        MonitorItem empty = new MonitorItem(rsEMmonitor.getInt("id"));
                        empty.setIp(rsEMmonitor.getString("ip"));
                        empty.settype(rsEMmonitor.getString("type"));
                        empty.setaskinf(rsEMmonitor.getInt("task"));
                        empty.setTaskon(rsEMmonitor.getString("task_on"));
                        empty.setTaskplan(rsEMmonitor.getString("task_plan"));
                        empty.setLasttime(rsEMmonitor.getString("lasttime"));
                        switch(rsEMmonitor.getString("type")){
                            case "tsh264":
                                recoMonitorlist.add(empty);
                                break;
                            case "calfeature":
                                calMonitorlist.add(empty);
                                break;
                            case "refclip":
                                refMonitorlist.add(empty);
                                break;
                            case "matchclip":
                                matchMonitorlist.add(empty);
                                break;
                        }
                    }
                    else{
                        MonitorItem pro = new MonitorItem(rsEMmonitor.getInt(("id")));
                        pro.setIp(rsEMmonitor.getString("ip"));
                        pro.settype(rsEMmonitor.getString("type"));
                        pro.setLasttime(rsEMmonitor.getString("lasttime"));
                        problemMonitorlist.add(pro);
                    }
                }

                //获取channelname
                String sqlmc = "select * FROM channelinfo";
                ResultSet channelrs = statement.executeQuery(sqlmc);
                List<String> channellist = new ArrayList<>();
                while (channelrs.next()){
                    channellist.add(channelrs.getString("name"));
                }


                //获取运行任务monitor信息
                String queryTaskMonitor = "SELECT * FROM monitor WHERE task=1";
                ResultSet rsTAmonitor = statement.executeQuery(queryTaskMonitor);
                List<MonitorItem> taskMonitorlist = new ArrayList();
                while(rsTAmonitor.next()){
                    MonitorItem taskmonitor = new MonitorItem(rsTAmonitor.getInt("id"));
                    taskmonitor.setIp(rsTAmonitor.getString("ip"));
                    taskmonitor.settype(rsTAmonitor.getString("type"));
                    taskmonitor.setaskinf(rsTAmonitor.getInt("task"));
                    taskmonitor.setTaskon(rsTAmonitor.getString("task_on"));
                    taskmonitor.setTaskplan(rsTAmonitor.getString("task_plan"));
                    taskMonitorlist.add(taskmonitor);
                }

                //channel,taskmonitor 匹配，匹配结果在数组显示
                int[][] channelMatch = new int[channellist.size()][3];
                for(int i=0;i<channellist.size();i++){
                    String chan = channellist.get(i);
                    for(int j=0;j<taskMonitorlist.size();j++){
                        String chaname = taskMonitorlist.get(j).getTask_plan();
                        String tasktype = taskMonitorlist.get(j).gettype();
                        if(chaname.equals(chan)){
                            switch (tasktype){
                                case "tsh264":
                                    channelMatch[i][0] = 1;
                                    break;
                                case "calfeature":
                                    channelMatch[i][1] = 1;
                                    break;
                                case "matchclip":
                                    channelMatch[i][2] = 1;
                                    break;
                            }
                        }
                    }
                }

                System.out.println("matcgh");
                for(int i=0;i<channelMatch.length;i++){
                    for(int j=0;j<channelMatch[0].length;j++)
                      System.out.print(channelMatch[i][j]+" ");
                    System.out.println("");
                }

                //channel配置
                File recvf = new File("testjson\\recvtask.json");
                File calf = new File("testjson\\caltask.json");
                File createf = new File("testjson\\create_refadclip.json");
                File matchf = new File("testjson\\findreplayclip.json");
                String recontent= FileUtils.readFileToString(recvf,"UTF-8");
                String cacontent= FileUtils.readFileToString(calf,"UTF-8");
                String crcontent= FileUtils.readFileToString(createf,"UTF-8");
                String macontent= FileUtils.readFileToString(matchf,"UTF-8");
                JSONObject rectask=new JSONObject(recontent);
                JSONObject caltask=new JSONObject(cacontent);
                JSONObject cretask=new JSONObject(crcontent);
                FindReplayClipTaskMsg matask=new FindReplayClipTaskMsg();
               
                // BufferedWriter output2 = new BufferedWriter(new FileWriter(calf));
               // BufferedWriter output3 = new BufferedWriter(new FileWriter(createf));
               // BufferedWriter output4 = new BufferedWriter(new FileWriter(matchf));

                for(int i=0;i<channelMatch.length;i++){
                    int moid;
                    if(channelMatch[i][0]!=1){
                        if(recoMonitorlist.size()<=0){
                            System.out.println("no record monitor left");
                            break;
                        }
                        //配置recvjson
                        moid = recoMonitorlist.get(0).getMonitorid();
                        rectask.put("channelName",channellist.get(i));
                        rectask.put("recordPath","/ts/"+channellist.get(i));
                        BufferedWriter output1 = new BufferedWriter( new FileWriter(recvf));
                        output1.write(rectask.toString());
                        output1.close();
                        System.out.println(rectask.toString());
                        recontent= FileUtils.readFileToString(recvf,"UTF-8");
                        System.out.println(recontent);

                        String res1 = sendmsg(new String[]{"send","recordtask",Integer.toString(moid),"testjson\\recvtask.json"});
                        if((new JSONObject(res1)).getJSONObject("response").getInt("code")==0){
                            String updateTask = "UPDATE monitor SET task=1,task_on='"+channellist.get(i)+"',task_plan='"+channellist.get(i)+"' WHERE id="+moid;
                            if(statement.executeUpdate(updateTask)>0)
                                System.out.println(channellist.get(i)+" record run on "+moid);
                            else System.out.println("monitor"+moid+" sql change fail");
                            recoMonitorlist.remove(0);
                        }else {
                            System.out.println("send"+ channellist.get(i)+"record to "+moid+" fail");
                        }

                        //如果成功,更改数据库monitor表
                        //如果不成功，上报运维人员
                    }
                }

                //同一个calmonitor不能send重复任务
                for(int i=0;i<channelMatch.length;i++){
                    int moid;
                    if(channelMatch[i][1]!=1){
                        if(calMonitorlist.size()<=0){
                            System.out.println("no cal monitor left");
                            break;
                        }
                        moid = calMonitorlist.get(0).getMonitorid();
                        caltask.put("calPath","/ts/"+channellist.get(i));
                        BufferedWriter output2 = new BufferedWriter(new FileWriter(calf));
                        output2.write(caltask.toString());
                        output2.close();
                        String res2 = sendmsg(new String[]{"send","add_calfeaturetask",Integer.toString(moid),"testjson\\caltask.json"});
                        System.out.println(res2);
                        if((new JSONObject(res2).getInt("code"))==0){
                            String updateTask2 = "UPDATE monitor SET task=1,task_on='"+channellist.get(i)+"',task_plan='"+channellist.get(i)+"' WHERE id="+moid;
                            if(statement.executeUpdate(updateTask2)>0)
                                System.out.println(channellist.get(i)+" cal run on "+moid);
                            else System.out.println("monitor sql change fail");
                            calMonitorlist.remove(0);
                        }else {
                            System.out.println("send cal to "+moid+" fail");
                        }
                    }
                }

                for(int i=0;i<channelMatch.length;i++){
                    int moid;
                    if(channelMatch[i][2]!=1){
                        if(matchMonitorlist.size()<=0){
                            System.out.println("no match monitor left");
                            break;
                        }
                        moid = matchMonitorlist.get(0).getMonitorid();
                        matask.channelPath = "ts/"+channellist.get(i);
                        String getRefSql = "SELECT *  FROM adinfo INNER JOIN channelinfo on adinfo.channelid=channelinfo.id WHERE url = ? and channelinfo.name='"+channellist.get(i)+"'";
                        String getRefUrl = "SELECT DISTINCT url FROM adinfo INNER JOIN channelinfo on adinfo.channelid=channelinfo.id WHERE url = ? and channelinfo.name='"+channellist.get(i)+"'"";
                        String getRefNumOfNasIp = "SELECT count(*) cou FROM adinfo INNER JOIN channelinfo on adinfo.channelid=channelinfo.id WHERE url = ? and channelinfo.name='"+channellist.get(i)+"'";
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
                                String chan = channellist.get(i);
                                if(num>0) {
                                   // String mn2 = refrs.getString("lambdaFile");
                                    String u = refrs.getString("url");

                                    ref.adClipUrls.add(refrs.getString("lambdaFile"));
                                }
                            }
                            matask.refAdClips.add(ref);
                        }
                        String getRecordIp = "SELECT pathIp from channel_tab WHERE name = '"+channellist.get(i)+"'";
                        ResultSet recordIprs = statement.executeQuery(getRecordIp);
                        if(recordIprs.next())
                            matask.nasIp = recordIprs.getString("pathIp");
                        matask.startTime =  Long.toString(System.currentTimeMillis());
                        matask.endTime = Long.toString(Long.parseLong(matask.startTime)+3600000);
                        if(urltotalnum==0)
                            System.out.println(channellist.get(i)+" has no ref");
                        String mtask = (new ObjectMapper()).writeValueAsString(matask);



                        BufferedWriter output3 = new BufferedWriter(new FileWriter(matchf));
                        output3.write(mtask);
                        output3.close();
                        String res3 = sendmsg(new String[]{"send","matchadreplaytask",Integer
                                .toString(moid),"testjson\\findreplayclip.json"});

                        if((new JSONObject(res3)).getInt("code")==0){
                            String updateTask4 = "UPDATE monitor SET task=1,task_on='"+channellist.get(i)+"',task_plan='"+channellist.get(i)+"' WHERE id="+moid;
                            String getAdRefUrl = "SELECT  * FROM channel_tab WHERE name = '"+channellist.get(i)+"'";
                            if(statement.executeUpdate(updateTask4)>0)
                                System.out.println(channellist.get(i)+" match run on "+moid);
                            else System.out.println("monitor sql change fail");
                            matchMonitorlist.remove(0);
                        }else {
                            System.out.println("send match to "+moid+" fail");
                        }
                    }
                }

                //检查所有已运行任务运行情况
                String queryRunningTaskMonitor = "SELECT * FROM monitor WHERE task=1";
                ResultSet rsRunning = statement.executeQuery(queryRunningTaskMonitor);
                while(rsRunning.next()){
                    int id = rsRunning.getInt("id");
                    String mtype = rsRunning.getString("type");
                    String mtask = rsRunning.getString("task_plan");
                    System.out.println(id+"  "+mtype);
                    int resCheck=0;
                    HashMap<String,String> promap = new HashMap();
                    switch (mtype){
                        case "tsh264":
                            promap.put("channelName",mtask);
                            promap.put("recordPath","/ts/"+mtask);
                            ChangeFileData("testjson\\recvtask.json",promap);
                            resCheck = (new JSONObject(sendmsg(new String[]{"send","recordtask",Integer.toString(id),"testjson\\recvtask.json"})))
                                        .getJSONObject("response").getInt("code");
                            if(resCheck==-1)
                                System.out.println(id+" normal");
                            else
                                System.out.println(id+" stop");
                            break;
                        case "calfeature":
                            promap.put("calPath","/ts/"+mtask);
                            ChangeFileData("testjson\\caltask.json",promap);
                            resCheck = (new JSONObject(sendmsg(new String[]{"send","add_calfeaturetask",Integer.toString(id),"testjson\\caltask.json"})))
                                        .getInt("code");
                            if(resCheck==-1)
                                System.out.println(id+" normal");
                            else
                                System.out.println(id+" stop");
                            break;
                        case "matchclip":
                            /**
                            //?match是一直运行吗？
                            promap.put("channelPath","/ts/"+mtask);
                            ChangeFileData("testjson\\findreplayclip.json",promap);
                            if(sendmsg(new String[]{"send","matchadreplaytask",Integer.toString(id),"testjson\\findreplayclip.json"}).equals("null")){
                                System.out.println(id+" wrong");
                                String monitorWrong = "UPDATE monitor SET task=1,task_on='wrong' WHERE id="+id;
                                statement.executeUpdate(monitorWrong);
                            }
                            else System.out.println(id+" stop");**/
                            break;

                    }
                    //设一下rescheck检验

                }

                lo.readStarttimeFromLog(monitorlist,con);






            }catch (Exception e){
                logger.error(ExceptionUtils.getStackTrace(e));
                e.printStackTrace();
            }
        }
    }

    private logThread lo = new logThread();

    private Logger logger = LoggerFactory.getLogger("DemoMain");
    private void help(){
        String help = "get onlinemonitor|runingtask|nasinfo|monitortype [monitorid]\n" +
                "send recordtask|add_calfeaturetask|del_calfeaturetask|matchadreplaytask" +
                "|matchselfreplaytask|create_refad_clip" +
                " monitorid cmdfile\n"+
                "help";
        System.out.println(help);
    }



    private String sendmsg(String[] cmds){
        if (cmds.length < 4) {
            help();
            return "cmds.length < 4";
        }
        ObjectMapper objectMapper = new ObjectMapper();
        int monitorid = Integer.parseInt(cmds[2]);
        File file = new File(cmds[3]);
        String resStr = "no match";
        try {
            if (cmds[1].equals("recordtask"))   {
                RecvTask recvTask = objectMapper.readValue(file, RecvTask.class) ;
                AddDelRecvTaskResult  respose = sendMsg.sendAddRecvTask(monitorid, recvTask);
                System.out.println("respose: ");
                resStr = objectMapper.writeValueAsString(respose);
                System.out.println(resStr);
                JSONObject jsonr = new JSONObject(resStr);
                JSONObject response = jsonr.getJSONObject("response");
                System.out.println("code:"+response.getInt("code"));//return resStr;
            }else if(cmds[1].equals("del")) {
                RecvTask recvTask = objectMapper.readValue(file, RecvTask.class) ;
                AddDelRecvTaskResult  respose = sendMsg.sendDelRecvTask(monitorid, recvTask);
                resStr = objectMapper.writeValueAsString(respose);
                System.out.println("del"+resStr);
                // return resStr;
            }else if (cmds[1].equals("add_calfeaturetask")){
                CalTaskMsg calTaskMsg = objectMapper.readValue(file, CalTaskMsg.class);
                CommonRespose respose = sendMsg.sendStartCalFeatureTask(monitorid, calTaskMsg);
                resStr = objectMapper.writeValueAsString(respose) ;
                System.out.println(resStr);
                //return resStr;
            }else if (cmds[1].equals("del_calfeaturetask")){
                CalTaskMsg calTaskMsg = objectMapper.readValue(file, CalTaskMsg.class);
                CommonRespose respose = sendMsg.sendStopCalFeatureTask(monitorid, calTaskMsg);
                resStr = objectMapper.writeValueAsString(respose) ;
                System.out.println(resStr);
            } else if (cmds[1].equals("matchadreplaytask")){
                FindReplayClipTaskMsg findReplayClipTaskMsg = objectMapper.readValue(file,
                        FindReplayClipTaskMsg.class);
                findReplayClipTaskMsg.string2Long();
                CommonRespose respose = sendMsg.sendFindAdClipTask(monitorid, findReplayClipTaskMsg);
                resStr = objectMapper.writeValueAsString(respose);
                System.out.println(resStr);

            }else if (cmds[1].equals("matchselfreplaytask")){
                FindReplayClipTaskMsg selfFindTaskMsg = objectMapper.readValue(file, FindReplayClipTaskMsg.class) ;
                selfFindTaskMsg.string2Long();
                CommonRespose respose = sendMsg.sendSelFindTaskMsg(monitorid, selfFindTaskMsg);
                resStr = objectMapper.writeValueAsString(respose);
                System.out.println(resStr);

            }else if (cmds[1].equals("create_refad_clip")){
                CreateRefAdClip createRefAdClip = objectMapper.readValue(file, CreateRefAdClip.class);
                CreateRefAdClipResponse response = sendMsg.sendCreateRefAdClip(monitorid, createRefAdClip);
                System.out.println(ReflectionToStringBuilder.toString(response, ToStringStyle.MULTI_LINE_STYLE));
                //  return ReflectionToStringBuilder.toString(response, ToStringStyle.MULTI_LINE_STYLE);
                resStr = ReflectionToStringBuilder.toString(response, ToStringStyle.MULTI_LINE_STYLE);
                //return ReflectionToStringBuilder.toString(response, ToStringStyle.MULTI_LINE_STYLE;

            } else {
                help();
                // return "no match";
            }
        }catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            e.printStackTrace();
        }
        return resStr;

    }

    private String getmsg(String[] cmds){
        if (cmds.length < 2){
            help();
            return "help";
        }
        String res = "";
        ObjectMapper objectMapper = new ObjectMapper();
        if(cmds[1].equals("onlinemonitor")){
            List<Integer> online = dataChannelMng.getOnlineMonitorForSend();
            // int[] onlinemonitor = (int[]) online.toArray();
            System.out.println("onlineList: "+online.toString()+online.size());

            if (online.isEmpty() ){
                System.out.println("no online monitor");
                return "no online monitor";
            }else{for (int mid : online){
                res = res+mid;
                System.out.print(" ");
                if(online.indexOf(mid)!=(online.size()-1))
                    res=res+" ";
            }
                System.out.println();
                /** for(Integer s:online){
                 res.concat(Integer.toString(s));
                 res.concat(" ");
                 }**/
                return res;
            }
        }else if(cmds[1].equals("runingtask")) {
            if (cmds.length < 3) {
                help();
                return "help";
            }
            int monitorid = Integer.parseInt(cmds[2]);

            RecvTasks recvTasks = sendMsg.sendGetRuningTask(monitorid);
            if(recvTasks==null){
                res = "notask";
            }
            else{
                for (RecvTask task : recvTasks.tasks){
                    res = res+task.toString();
                }
            }
            System.out.println("res: "+res);

            return res;
        }else if (cmds[1].equals("nasinfo")){
            if (cmds.length < 3) {
                help();
                return "help";
            }
            int monitor = Integer.parseInt(cmds[2]);
            NasMountInfos nasMountInfos = sendMsg.sendGetNasInfo(monitor);
            for(NasMountInfo nasMountInfo : nasMountInfos.nasMountInfos){
                System.out.println(nasMountInfo.toString());
                res = res+nasMountInfo.toString();
                //res = res.concat(nasMountInfo.toString());
                //res.concat(" ");
            }
            return res;
        } else if (cmds[1].equals("monitortype")){
            if (cmds.length < 3) {
                help();
                return "help";

            }
            int monitor = Integer.parseInt(cmds[2]);
            MonitorType monitorType = sendMsg.getMonitorType(monitor);
            if (monitorType == null) {
                System.out.println("get monitor type return null!");
                return "null";
            }else {
                try {
                    String str = objectMapper.writeValueAsString(monitorType);
                    res = res+str;
                    System.out.println(res);
                    return res;
                } catch (JsonProcessingException e) {
                    logger.error(ExceptionUtils.getStackTrace(e));
                }
            }
        } else {
            help();
            return "help";
        }
        return res;
    }

    private void ChangeFileData(String path, HashMap<String,String> map){
        File f = new File(path);
        try{
            String fs = FileUtils.readFileToString(f,"UTF-8");
            JSONObject json = new JSONObject(fs);
            for(String pro:map.keySet()){
                json.put(pro,map.get(pro));
            }
            BufferedWriter ou = new BufferedWriter(new FileWriter(f));
            ou.write(json.toString());
            ou.close();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private DataChannelMng dataChannelMng;
    private SendMsg sendMsg;
    public SendMsg getSendMsg(){
        return this.sendMsg;
    }
    private MsgProcess msgProcess = new MsgProcess();
    private void startDb(int weport, String dburl, String dbDriver, String usr, String passwd){
        H2Db db = new H2Db();
        msgProcess.h2Db = db;
        db.startDb(weport, dburl, dbDriver, usr, passwd);

/**        ReqChannelMatchMsg rsq = new ReqChannelMatchMsg();
        rsq.channelid = 17;
        rsq.startTime = 1527715447280L;
        rsq.endTime = 1527715670040L;
        ChannelMatchMsg rs = db.getChannelMatch(rsq);
   **/
    }
    private void startMsgServer(String bindip, int port){
        MsgChannelServer msgChannelServer = new MsgChannelServer(bindip, port, msgProcess);
        msgChannelServer.startWork();
    }

}
