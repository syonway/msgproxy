package com.guanghui.admonitor.syctrl;

import com.guanghui.admonitor.DemoLoop;
import com.guanghui.admonitor.backservice.SendMsg;

import java.awt.Taskbar.State;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.management.monitor.Monitor;

public class startMatch implements Runnable{
    
    @Override
    public void run(){
        DemoLoop dm = DemoLoop.getInstance();
        SendMsg sendms = dm.getSendMsg();
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        System.out.println(dateFormat.format( now ));

        //读取频道，获取monitor信息，分配
        try{
        String sqlmc = "select * FROM channelinfo";
        Connection con = dm.getConnection();
        Statement statement = con.createStatement();
        ResultSet channelrs = statement.executeQuery(sqlmc);
        List<String> channellist = new ArrayList<>();
        List<String> channelMatch = new ArrayList<>();
        while (channelrs.next()){
            channellist.add(channelrs.getString("name"));
        }    
        String queryEmptyMonitor = "SELECT * FROM monitor WHERE task=0 AND type = 'matchclip' ";
        String queryMatchTaskMonitor = "SELECT * FROM monitor WHERE task=1 AND type = 'matchclip' ";
        ResultSet monitorrs = statement.executeQuery(queryMatchTaskMonitor);
        ResultSet emonitorrs = statement.executeQuery(queryEmptyMonitor);
        List<Integer> emmonitor = new ArrayList<>();
        while(monitorrs.next()){
            channelMatch.add(monitorrs.getString("task_on"));            
        }
        while(emonitorrs.next()){
            emmonitor.add(emonitorrs.getInt("id"));
        }
        for(int i=0;i<channellist.size();i++){
            if(!channelMatch.contains(channellist.get(i))){
                String addChannelMatch = "UPDATE";
            }


        }
        //对应频道获取ref url，无则不发送指令
        //定时发送
        //读取log结果

        //ps明天要看addclip信息，添加数据（自行拼凑文件名，减去八小时插入·）
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
