package com.guanghui.admonitor.syctrl;

import com.guanghui.admonitor.catalog_ui.msgservchannel.msgs.ChannelMatchMsg;
import com.guanghui.admonitor.catalog_ui.msgservchannel.msgs.MatchItem;
import com.guanghui.admonitor.catalog_ui.msgservchannel.msgs.MatchResult;
import com.mysql.cj.jdbc.result.ResultSetFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.sql.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class LogView {
    private static long lastTimeFileSize = 0;  //上次文件大小
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


    public static ChannelMatchMsg readMatchResult(File log){
        try{
            System.out.println("last: "+lastTimeFileSize);
            Connection con;

            String driver = "com.mysql.cj.jdbc.Driver";
            String url = "jdbc:mysql://localhost:3306/ads?serverTimezone=UTC";
            String user = "root";
            String password = "root";
            Class.forName(driver);
            con = DriverManager.getConnection(url,user,password);
            if(!con.isClosed())
                System.out.println("Succeeded connecting to the Database!");
            Statement statement = con.createStatement();

            //指定文件可读可写
            RandomAccessFile randomFile = new RandomAccessFile(log, "rw");
                        randomFile.seek(lastTimeFileSize);
                        ChannelMatchMsg res = new ChannelMatchMsg();
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
                              //  System.out.println(tmp);
                                mark = 1;
                                continue;
                            }
                            if(mark==1){
                                if (Pattern.matches(".*channelPath.*", new String(tmp.getBytes("ISO8859-1")))){
                             //       System.out.println(tmp);
                                    mark = 2;}
                                else mark = 0;
                            }
                            if(mark>=2){

                                if (Pattern.matches(".*adClipFrameNr.*", new String(tmp.getBytes("ISO8859-1")))){
                                 //   System.out.println(tmp);
                                    String[] s = StringUtils.substringsBetween(tmp,"Nr\" : ",",");
                               //     System.out.println(s.length);
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
                                    String name[] = StringUtils.substringsBetween(adUrl,"ts/","/refclips");
                                    String getChannelid = "SELECT id from channelinfo WHERE name = '"+name[0]+"'";
                                    ResultSet namers = statement.executeQuery(getChannelid);
                                    int channelid = -1;
                                    if(namers.next())
                                        channelid = namers.getInt("id");
                                    resitem.channelid = channelid;
                                    resitem.startTime = Long.parseLong(startMatchChannelTime);
                                    resitem.refurl = adUrl;
                                    resitem.endTime = resitem.startTime+matchFrameNr/24*1000;
                                    resitem.matchNr = matchFrameNr;
                                    resitem.frameNr = adClipFrameNr;
                                    resitem.loudness = matchFrameNr/adClipFrameNr;
                                    res.matchItems.add(resitem);
                                    System.out.println("adClipFrameNr:"+adClipFrameNr);
                                    System.out.println("adUrl:"+adUrl);
                                    System.out.println("matchFrameNr:"+matchFrameNr);
                                    System.out.println("startMatchChannelTime:"+startMatchChannelTime);
                                    System.out.println("endTime:"+resitem.endTime);
                                    num++;
                                }
                                else if(Pattern.matches(".*startTime.*", new String(tmp.getBytes("ISO8859-1"))))
                                    mark = 0;

                            }

                            }
                        System.out.println("num: "+num);

                        lastTimeFileSize = randomFile.length();
                        System.out.println(lastTimeFileSize);

            if(num>0) {
                String insertmatch = "INSERT INTO adclipinfo_tab (own_channelid,start_time,frame_nr,loudness,url) VALUES (?,?,?,?,?) ";
                PreparedStatement statement2 = con.prepareStatement(insertmatch);
                for (int i = 0; i < res.matchItems.size(); i++) {
                    statement2.setInt(1, res.matchItems.get(i).channelid);
                    statement2.setTimestamp(2, Timestamp.valueOf(
                            TimeTransform.timeStamp2Date(Long.toString(res.matchItems.get(i).startTime))));
                    statement2.setLong(3, res.matchItems.get(i).frameNr);
                    statement2.setFloat(4, res.matchItems.get(i).loudness);
                    statement2.setString(5,res.matchItems.get(i).refurl);
                    statement2.executeUpdate();
                }
                return res;
            }



            con.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

    return null;
    }



    public static void main(String[] args) throws Exception {
        LogView view = new LogView();
        final File tmpLogFile = new File("msgproxy2.log");
       // File f2 = new File("msgproxy2.log");
        while(true) {
            view.readMatchResult(tmpLogFile);
        }
    }

}