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
import com.guanghui.admonitor.backservice.ctrlcntmsg.send.CalTaskMsg;
import com.guanghui.admonitor.backservice.ctrlcntmsg.send.CreateRefAdClip;
import com.guanghui.admonitor.backservice.ctrlcntmsg.send.FindReplayClipTaskMsg;
import com.guanghui.admonitor.backservice.ctrlcntmsg.send.RecvTasks;
import com.guanghui.admonitor.backservice.ctrlcntmsg.send.response.CreateRefAdClipResponse;
import com.guanghui.admonitor.backservice.ctrlcntmsg.send.response.MonitorType;
import com.guanghui.admonitor.backservice.ctrlcntmsg.send.response.NasMountInfo;
import com.guanghui.admonitor.backservice.ctrlcntmsg.send.response.NasMountInfos;
import com.guanghui.admonitor.catalog_ui.demo.H2Db;
import com.guanghui.admonitor.catalog_ui.demo.MsgProcess;
import com.guanghui.admonitor.catalog_ui.msgservchannel.MsgChannelServer;
import com.guanghui.itvm.dcbase.DataChannelMng;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Created by cuiju on 2017/1/12. 16 34
 */
public class DemoMain {
    private static DemoMain demoMain;
    public static DemoMain getInstance(){
        return demoMain;
    }
    public static void main(String[] args){
        demoMain = new DemoMain();
        demoMain.mainLoop(args);
    }

    @SuppressWarnings("InfiniteLoopStatement")
    private void mainLoop(String[] args){
        Cfg cfg = Cfg.getInstance();
        if (args.length < 1){
            System.out.println("paramete: cfgfile");
        }

        ////for catalog ui msgproxy demo start
        DemoMain backEndServiceDemo = new DemoMain();


        String bindip = "0.0.0.0";
        int port = cfg.getCatalogUiListenPort();
        int dataWebPort = cfg.getDbWebPort();
        backEndServiceDemo.startDb(dataWebPort, cfg.getDatabaseUrl(), cfg.getDbDriver(),
                cfg.getDbuser(), cfg.getDbPasswd());
      //  backEndServiceDemo.startDb(dataWebPort,"jdbc:h2:~/ad;IFEXISTS=TRUE", "org.h2.Driver",
      //          "sa", "hello1234");
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
                System.out.print("cmd>");
                String line = reader.readLine();
                commandProcess(line);
            }catch (Exception e){
                logger.error(ExceptionUtils.getStackTrace(e));
            }
        }
    }

    private Logger logger = LoggerFactory.getLogger("DemoMain");
    private void help(){
        String help = "get onlinemonitor|runingtask|nasinfo|monitortype [monitorid]\n" +
                "send recordtask|add_calfeaturetask|del_calfeaturetask|matchadreplaytask" +
                "|matchselfreplaytask|create_refad_clip" +
                " monitorid cmdfile\n"+
                "help";
        System.out.println(help);
    }

    private void getCommandProcess(String[] cmds){
        if (cmds.length < 2){
            help();
            return;
        }
        ObjectMapper objectMapper = new ObjectMapper();
        switch (cmds[1]) {
            case "onlinemonitor":
                List<Integer> online = dataChannelMng.getOnlineMonitorForSend();
                if (online.isEmpty()) {
                    System.out.println("no online monitor");
                } else {
                    for (int mid : online) {
                        System.out.print(mid);
                        System.out.print(" ");
                    }
                    System.out.println();
                }
                break;
            case "runingtask":
                if (cmds.length < 3) {
                    help();
                    return;
                }
                int monitorid = Integer.parseInt(cmds[2]);
                RecvTasks recvTasks = sendMsg.sendGetRuningTask(monitorid);
                for (RecvTask task : recvTasks.tasks) {
                    System.out.println(task.toString());
                }
                break;
            case "nasinfo": {
                if (cmds.length < 3) {
                    help();
                    return;
                }
                int monitor = Integer.parseInt(cmds[2]);
                NasMountInfos nasMountInfos = sendMsg.sendGetNasInfo(monitor);
                for (NasMountInfo nasMountInfo : nasMountInfos.nasMountInfos) {
                    System.out.println(nasMountInfo.toString());
                }
                break;
            }
            case "monitortype": {
                if (cmds.length < 3) {
                    help();
                    return;
                }
                int monitor = Integer.parseInt(cmds[2]);
                MonitorType monitorType = sendMsg.getMonitorType(monitor);
                if (monitorType == null) {
                    System.out.println("get monitor type return null!");
                } else {
                    try {
                        String str = objectMapper.writeValueAsString(monitorType);
                        System.out.println(str);
                    } catch (JsonProcessingException e) {
                        logger.error(ExceptionUtils.getStackTrace(e));
                    }
                }
                break;
            }
            default:
                help();
                break;
        }
    }

    private void sendCmdsProcess(String[] cmds){
        if (cmds.length < 4) {
            help();
            return;
        }
        ObjectMapper objectMapper = new ObjectMapper();
        int monitorid = Integer.parseInt(cmds[2]);
        File file = new File(cmds[3]);
        try {
            switch (cmds[1]) {
                case "recordtask": {
                    RecvTask recvTask = objectMapper.readValue(file, RecvTask.class);
                    AddDelRecvTaskResult respose = sendMsg.sendAddRecvTask(monitorid, recvTask);
                    String resStr = objectMapper.writeValueAsString(respose);
                    System.out.println(resStr);
                    break;
                }
                case "add_calfeaturetask": {
                    CalTaskMsg calTaskMsg = objectMapper.readValue(file, CalTaskMsg.class);
                    CommonRespose respose = sendMsg.sendStartCalFeatureTask(monitorid, calTaskMsg);
                    String str = objectMapper.writeValueAsString(respose);
                    System.out.println(str);
                    break;
                }
                case "del_calfeaturetask": {
                    CalTaskMsg calTaskMsg = objectMapper.readValue(file, CalTaskMsg.class);
                    CommonRespose respose = sendMsg.sendStopCalFeatureTask(monitorid, calTaskMsg);
                    String str = objectMapper.writeValueAsString(respose);
                    System.out.println(str);
                    break;
                }
                case "matchadreplaytask": {
                    FindReplayClipTaskMsg findReplayClipTaskMsg = objectMapper.readValue(file,
                            FindReplayClipTaskMsg.class);
                    findReplayClipTaskMsg.string2Long();
                    CommonRespose respose = sendMsg.sendFindAdClipTask(monitorid, findReplayClipTaskMsg);
                    String resStr = objectMapper.writeValueAsString(respose);
                    System.out.println(resStr);
                    break;
                }
                case "matchselfreplaytask": {
                    FindReplayClipTaskMsg selfFindTaskMsg = objectMapper.readValue(file, FindReplayClipTaskMsg.class);
                    selfFindTaskMsg.string2Long();
                    CommonRespose respose = sendMsg.sendSelFindTaskMsg(monitorid, selfFindTaskMsg);
                    String resStr = objectMapper.writeValueAsString(respose);
                    System.out.println(resStr);
                    break;
                }
                case "create_refad_clip":
                    CreateRefAdClip createRefAdClip = objectMapper.readValue(file, CreateRefAdClip.class);
                    CreateRefAdClipResponse response = sendMsg.sendCreateRefAdClip(monitorid, createRefAdClip);
                    System.out.println(ReflectionToStringBuilder.toString(response, ToStringStyle.MULTI_LINE_STYLE));
                    System.out.println(ReflectionToStringBuilder.toString(response, ToStringStyle.MULTI_LINE_STYLE)=="<null>");
                    break;
                default:
                    help();
                    break;
            }
        }catch (Exception e){
           logger.error(ExceptionUtils.getStackTrace(e));
           e.printStackTrace();
        }
    }

    public void commandProcess(String cmd){
        String[] f = cmd.split(" ");
        for(int i = 0; i < f.length; i++){
            f[i] = f[i].trim();
        }
        if (f.length == 0) {
            return;
        }
        if (f[0].equals("get")){
            getCommandProcess(f);
            return;
        }else if (f[0].equals("send")){
            sendCmdsProcess(f);
           return;
        }
        help();
        System.out.println();
    }
    private DataChannelMng dataChannelMng;
    private SendMsg sendMsg;
    private MsgProcess msgProcess = new MsgProcess();
    public void startDb(int weport, String dburl, String dbDriver, String usr, String passwd){
        H2Db db = new H2Db();
        msgProcess.h2Db = db;
        db.startDb(weport, dburl, dbDriver, usr, passwd);
    }
    public SendMsg getSendmsg(){
        return this.sendMsg;
    }
    public void startMsgServer(String bindip, int port){
        MsgChannelServer msgChannelServer = new MsgChannelServer(bindip, port, msgProcess);
        msgChannelServer.startWork();
    }

}
