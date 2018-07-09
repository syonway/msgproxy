package com.guanghui.admonitor.syctrl;

import com.guanghui.admonitor.catalog_ui.msgservchannel.msgs.MatchItem;
import com.guanghui.admonitor.catalog_ui.msgservchannel.msgs.MatchResult;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class LogView {
    private long lastTimeFileSize = 0;  //上次文件大小
    /**
     * 实时输出日志信息
     /* @param logFile 日志文件
     * @throws IOException
     */

    public int readSendResult(File logfile,int monitorid,String channel) throws IOException {
       int code = -1;
        try{
        RandomAccessFile randomFile = new RandomAccessFile(logfile, "r");
        randomFile.seek(lastTimeFileSize);
        String tmp = "";
        String adclippattern = "send to monitor "+monitorid;
        int mark = 0;
        String channelres="";
        while ((tmp = randomFile.readLine()) != null) {
            if (mark == 1 && Pattern.matches(new String(".*\"code.*"), new String(tmp.getBytes("ISO8859-1")))) {
                System.out.println(tmp);
                String[] coderes = StringUtils.substringsBetween(new String(tmp.getBytes("ISO8859-1")), "code\" : ", ",");
                if(channel.equals(channelres+"tv")) {
                    code = Integer.parseInt(coderes[0]);
                    System.out.println(channelres+"tv");
                }
                System.out.println(monitorid+" code: "+ code);
                mark = 0;
            }
            if (Pattern.matches(adclippattern, new String(tmp.getBytes("ISO8859-1"))) ) {
                System.out.println(new String(tmp.getBytes("ISO8859-1")));
                String[] channelresg = StringUtils.substringsBetween(new String(tmp.getBytes("ISO8859-1")),"ts/","tc");
                channelres = channelresg[0];
                System.out.println("cres  "+channelresg[0]);
                mark = 1;
            }

        }}catch (Exception e) {
           e.printStackTrace();
       }
       return code;

    }

    private JSONArray res = new JSONArray();
    public String getMatchResult(){
        return res.toString();
    }

    public MatchResult readMatchResult(File log){
        MatchResult res = new MatchResult();
        try{
            //指定文件可读可写
            RandomAccessFile randomFile = new RandomAccessFile(log, "rw");
                        randomFile.seek(lastTimeFileSize);
                        String tmp = "";
                        String pattern1 = ".*recv from monitor.*";
                        int mark = 0;
                        int adClipFrameNr = 0;
                        String adUrl = "";
                        int matchFrameNr = 0;
                        String startMatchChannelTime = "";
                        int num = 0;
                        while ((tmp = randomFile.readLine()) != null) {
                            //   System.out.println("not transfer:  "+tmp);
                            if (Pattern.matches(pattern1, new String(tmp.getBytes("ISO8859-1")))) {
                                System.out.println(tmp);
                                mark = 1;
                                continue;
                            }
                            if(mark==1){
                                if (Pattern.matches(".*channelPath.*", new String(tmp.getBytes("ISO8859-1")))){
                                    System.out.println(tmp);
                                    mark = 2;}
                                else mark = 0;
                            }
                            if(mark>=2){

                                if (Pattern.matches(".*adClipFrameNr.*", new String(tmp.getBytes("ISO8859-1")))){
                                    System.out.println(tmp);
                                    String[] s = StringUtils.substringsBetween(tmp,"Nr\" : ",",");
                                    System.out.println(s.length);
                                    adClipFrameNr = Integer.parseInt(s[0]);
                                }
                                else if(Pattern.matches(".*adUrl.*", new String(tmp.getBytes("ISO8859-1")))){
                                    String[] s2 = StringUtils.substringsBetween(tmp,"rl\" : \"",",");
                                    adUrl = s2[0];
                                }
                                else if(Pattern.matches(".*matchFrameNr.*", new String(tmp.getBytes("ISO8859-1")))){
                                    String[] s3 = StringUtils.substringsBetween(tmp,"meNr\" : ",",");
                                    matchFrameNr = Integer.parseInt(s3[0]);
                                }
                                else if(Pattern.matches(".*startMatchChannelTime.*", new String(tmp.getBytes("ISO8859-1")))){
                                    String[] s4 = StringUtils.substringsBetween(tmp,"\"","\"");
                                    startMatchChannelTime = s4[1];
                                    MatchItem resitem = new MatchItem();
                                    resitem.startTime = Long.parseLong(startMatchChannelTime);
                                    resitem.refurl = adUrl;
                                    resitem.endTime = resitem.startTime+matchFrameNr/24*1000;
                                    res.items.add(resitem);
                                    System.out.println(adClipFrameNr);
                                    System.out.println(adUrl);
                                    System.out.println(matchFrameNr);
                                    System.out.println(startMatchChannelTime);
                                    num++;
                                }
                                else if(Pattern.matches(".*startTime.*", new String(tmp.getBytes("ISO8859-1"))))
                                    mark = 0;

                            }

                            }
                        System.out.println("num: "+num);

                        lastTimeFileSize = randomFile.length();
                        System.out.println(lastTimeFileSize);


                    } catch (Exception e) {
                        e.printStackTrace();
                    }

    return res;
    }



    public static void main(String[] args) throws Exception {
        LogView view = new LogView();
        final File tmpLogFile = new File("msgproxy2.log");
       // File f2 = new File("msgproxy2.log");
        view.readMatchResult(tmpLogFile);
    }

}