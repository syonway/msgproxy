package com.guanghui.admonitor.backservice.ctrlcntmsg.send;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.LinkedList;

/**
 * Created by cuiju on 2017/2/13. 18 04
 */
public class FindReplayClipTaskMsg {
    public int taskId =100;
    public String channelPath;
    public String nasIp;
    public String startTime;
    public String endTime;
    public double threshold = 0.9;

    public LinkedList<RefAdClips> refAdClips = new LinkedList<>();

    @JsonIgnore
    public long startTimeLong;
    @JsonIgnore
    public long endTimeLong;

    @JsonIgnore
    public void long2String(){
        startTime = Long.toString(startTimeLong);
        endTime = Long.toString(endTimeLong);
    }

    @JsonIgnore
    public void string2Long(){
        startTimeLong = Long.parseLong(startTime);
        endTimeLong = Long.parseLong(endTime);
    }
}
