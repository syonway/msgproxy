package com.guanghui.admonitor.backservice.ctrlcntmsg.recv;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.LinkedList;

/**
 * Created by cuiju on 2017/2/13. 20 39
 */
public class ReplayClipsMsg {
    public String nasIp;
    public String channelPath;
    public String startTime;
    public String endTime;
    public LinkedList<ReplayClip> replayClips;

    @JsonIgnore
    public long startTimeLong;
    @JsonIgnore
    public long endTimeLong;

    @JsonIgnore
    public void string2long(){
        startTimeLong = Long.parseLong(startTime);
        endTimeLong = Long.parseLong(endTime);
        if (replayClips != null) {
            replayClips.forEach(ReplayClip::string2long);
        }
    }
}
