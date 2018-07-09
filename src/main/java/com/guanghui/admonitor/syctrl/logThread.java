package com.guanghui.admonitor.syctrl;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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

public class logThread{
   /** public static void main(String[] args){
        logThread th = new logThread();
        Thread s = new Thread(th);
        s.start();
    }

    public void run(){
        while (true){
        readStarttimeFromLog(tmpLogFile,new int[]{1001,1002,4000,4001,4002});
        // File f2 = new File("msgproxy2.log");
        //view.readMatchResult(tmpLogFile);
        try {
            Thread.sleep(1000);
        }catch(Exception e){
            e.printStackTrace();
        }
        }
    }**/
    public File logFile = new File("msgproxy.log");
    private long lastTimeFileSize = 0;  //上次文件大小

    public void readStarttimeFromLog(String[] monitorlist,Connection con)  {
        try {
            //连接数据库i
            //System.out.println("first");


            if (!con.isClosed())
                System.out.println("Succeeded connecting to the Database!");
            Statement statement = con.createStatement();

            RandomAccessFile randomFile = new RandomAccessFile(logFile, "rw");
                //获得变化部分的
                randomFile.seek(lastTimeFileSize);
                String tmp = "";
                String connpattern = ".*monitor " + "[0-9]+" + ".*authen ok.*";
                String updatepattern =".*INFO.*";
                int monitorid=3000;
                String starttime = "";
                String lasttime="";
                String initmonitor = "";
                String updateLasttime = "";

                while ((tmp = randomFile.readLine()) != null) {
                    //   System.out.println("not transfer:  "+tmp);
                    if (Pattern.matches(connpattern, new String(tmp.getBytes("ISO8859-1")))) {
                        System.out.println("match success: " + new String(tmp.getBytes("ISO8859-1")));
                        String[] info = (new String(tmp.getBytes("ISO8859-1"))).split(" ");
                        String[] timetmp = info[1].split(",");
                        monitorid = Integer.parseInt(info[7]);

                        starttime = info[0] + "_" + timetmp[0];
                        initmonitor = "INSERT INTO monitor (id,starttime) VALUES(" + monitorid + ",'" + starttime + "')"+"ON DUPLICATE KEY UPDATE starttime='"+ starttime + "'";
                        System.out.println(initmonitor);
                        if (statement.executeUpdate(initmonitor) >= 0)
                            System.out.println("monitor " + monitorid + " has update starttime");
                    }

                    for(int i=0;i<monitorlist.length;i++){
                        updatepattern = ".*INFO.*"+monitorlist[i];
                        //System.out.println("upp:  "+updatepattern);
                        if(Pattern.matches(updatepattern,new String(tmp.getBytes("ISO8859-1")))){
                            String[] info2 = (new String(tmp.getBytes("ISO8859-1"))).split(" ");
                            String[] timetmp2 = info2[1].split(",");
                            lasttime = info2[0] + "_" + timetmp2[0];
                            updateLasttime = "UPDATE monitor  SET "+"lasttime='"+lasttime+"' WHERE id="+monitorid;
                            System.out.println(updateLasttime);
                            if (statement.executeUpdate(updateLasttime) >= 0)
                                System.out.println("monitor " + monitorid + " has update lasttime");
                        }
                    }}

                lastTimeFileSize = randomFile.length();
                System.out.println(lastTimeFileSize);



          //  con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}