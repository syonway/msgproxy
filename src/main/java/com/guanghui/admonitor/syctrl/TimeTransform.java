package com.guanghui.admonitor.syctrl;

import org.apache.commons.lang3.StringUtils;

import java.sql.DatabaseMetaData;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.text.SimpleDateFormat;

public class TimeTransform {
    public static void main(String[] args) throws Exception {
        int time = 1528110574-31*24*3600;
        System.out.println(timeStamp2file(Integer.toString(1530726080)));
        System.out.println(Date2UnixStamp("2018-07-05 01:41:20.351"));
        System.out.println(getStartTimeFromFile("2018_07_05_01_41_20_2018_07_05_01_51_20:453_"));
        System.out.println();
        System.out.println(Date2UnixStamp(getStartTimeFromFile("2018_07_05_01_41_20_2018_07_05_01_51_20:67_")));
        long tryi = Date2UnixStamp("2018-07-05 01:43:20.99");
        Boolean t = tryi>Date2UnixStamp(getStartTimeFromFile("2018_07_05_01_41_20_2018_07_05_01_51_20:89_")) &&
                tryi<=Date2UnixStamp(getEndTimeFromFile("2018_07_05_01_41_20_2018_07_05_01_51_20:89_"));
        System.out.println(t);
    }

    public static String timeStamp2Date1(String timestampString){
        String format = "yyyy-MM-dd HH:mm:ss";
        Long timestamp = Long.parseLong(timestampString)*1000;
        String date = new java.text.SimpleDateFormat(format).format(new java.util.Date(timestamp));
        return date;
    }

    public static String timeStamp2Date(String timestampString){
        String format = "yyyy-MM-dd HH:mm:ss.SSS";
        Long timestamp = Long.parseLong(timestampString);
        String date = new java.text.SimpleDateFormat(format).format(new java.util.Date(timestamp));
        return date;
    }

    public static String timeStamp2file(String timestamp) {
        String date = timeStamp2Date1(timestamp);
        String end = timeStamp2Date(Integer.toString(Integer.parseInt(timestamp)+600));

        return date+"_"+end;
    }

    public static long Date2UnixStamp(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        Date dat = null;
        long stamp = 0;
        try {
            dat = sdf.parse(date);
            stamp = dat.getTime();
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return stamp;
    }

    public static String getStartTimeFromFile(String name) {
        String st = null;
        String[] sti = name.split("_");
        int n = 0;
        String[] ms = StringUtils.substringsBetween(name,":","_");
       // String[] min = StringUtils.substringsBetween(name,"_",":");
        //System.out.println(sti.length);
        st = sti[n]+"-"+sti[n+1]+"-"+sti[n+2]+" "+sti[n+3]+":"+sti[n+4]+":"+sti[n+5]+"."+ms[0];
        return st;
    }

    public static String getEndTimeFromFile(String name) {
        String st = null;
        String[] sti = name.split("_");
        int n = 6;
        String[] ms = StringUtils.substringsBetween(name,":","_");
        st = sti[n]+"-"+sti[n+1]+"-"+sti[n+2]+" "+sti[n+3]+":"+sti[n+4]+":"+sti[n-1]+"."+ms[0];
        return st;
    }
}