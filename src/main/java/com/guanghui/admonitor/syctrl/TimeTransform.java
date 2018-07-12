package com.guanghui.admonitor.syctrl;

import org.apache.commons.lang3.StringUtils;

import java.sql.DatabaseMetaData;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.text.SimpleDateFormat;

public class TimeTransform {
    public static void main(String[] args) throws Exception {
        System.out.println(timeStamp2Date(1527686650000L));
        System.out.println(systemAdd(1527686650000L));
        System.out.println(timeStamp2Date(systemAdd(1527686650000L)));
        System.out.println(timeStamp2file(1527686650000L,1527686690000L));
        System.out.println(sysStamp2date(1527715450000L));

        System.out.println();

        System.out.println(Date2UnixStamp("2018-05-30 21:24:10.000"));
        System.out.println();
        System.out.println(getStartTimeFromFile("2018_04_31_5_24_10__2018_04_31_6_25_7:918_v.ts.ftr"));


        long tryi = Date2UnixStamp("2018-07-05 01:43:20.99");
        Boolean t = tryi>Date2UnixStamp(getStartTimeFromFile("2018_07_05_01_41_20_2018_07_05_01_51_20:89_")) &&
                tryi<=Date2UnixStamp(getEndTimeFromFile("2018_07_05_01_41_20_2018_07_05_01_51_20:89_"));
        System.out.println(t);
    }

    public static long systemAdd(long timestamp){
        return (timestamp+8*3600*1000);
    }

    public static String timeStamp2Date(long timestamp){
        String format = "yyyy-MM-dd HH:mm:ss.SSS";
        String date = new java.text.SimpleDateFormat(format).format(new java.util.Date(timestamp));
        return date;
    }

    public static String timeStamp2file(long start,long end) {
        String date = timeStamp2Date(systemAdd(start));
        String date2 = timeStamp2Date(systemAdd(end));
        System.out.println(date);
        String format = "yyyy-MM-dd HH:mm:ss.SSS";
        String res = "none";
        try {
            Date dat = new SimpleDateFormat(format).parse(date);
            Calendar calendar = Calendar.getInstance();//日历对象
            calendar.setTime(dat);
            String yearStr = calendar.get(Calendar.YEAR)+"";//获取年份
            int month = calendar.get(Calendar.MONTH) + 1;//获取月份
            int day = calendar.get(Calendar.DATE);//获取日
            month = month-1;
            String mon = Integer.toString(month);
            if(month<10)
                mon = "0"+mon;
            int h = calendar.get(Calendar.HOUR);
            int m = calendar.get(Calendar.MINUTE);
            int s = calendar.get(Calendar.SECOND);
            int ss = calendar.get(Calendar.MILLISECOND);

            Date dat2 = new SimpleDateFormat(format).parse(date2);
            Calendar calendar2 = Calendar.getInstance();//日历对象
            calendar.setTime(dat2);
            String yearStr2 = calendar.get(Calendar.YEAR)+"";//获取年份
            int month2 = calendar.get(Calendar.MONTH) + 1;//获取月份
            int day2 = calendar.get(Calendar.DATE);//获取日
            month2 = month2-1;
            String mon2 = Integer.toString(month2);
            if(month2<10)
                mon2 = "0"+mon2;
            int h2 = calendar2.get(Calendar.HOUR);
            int m2 = calendar2.get(Calendar.MINUTE);
            int s2 = calendar2.get(Calendar.SECOND);
            int ss2 = calendar2.get(Calendar.MILLISECOND);

            System.out.println(yearStr+"  "+month+"  "+day+" "+h+"  "+m+" "+s+" "+ss);
            res = yearStr+"_"+mon+""+"_"+day+"_"+h+"_"+m+"_"+s+"__"+yearStr2+"_"+mon2+"_"+day2+"_"+h2+"_"+m2+"_"+s2+":"+ss2+"_v.ts.ftr";
        }catch (Exception e){
            e.printStackTrace();
        }

        return res;
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

    public static String sysStamp2date(long stamp){
        long truestamp = stamp-8*3600*1000;
        return timeStamp2Date(truestamp);
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