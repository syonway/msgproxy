package com.guanghui.admonitor.syctrl;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

public class filetest {
    public static void main(String[] args) {
        try{
        String taskfile="testjson\\recvtask.json";
        String channel = "hbtv";
        String path ="ts\\hbtc";
        File file = new File(taskfile);
        String recontent = FileUtils.readFileToString(file, "UTF-8");
        JSONObject recordtask = new JSONObject(recontent);
        recordtask.put("channelName", channel);
        recordtask.put("path", path);
        System.out.println("姓名是：" + recordtask.getString("channelName") + recordtask.getString("path"));
        BufferedWriter output = new BufferedWriter(new FileWriter(file));
        output.write(recordtask.toString());
        output.close();
    }catch (Exception e){
            e.printStackTrace();
        }
}}

