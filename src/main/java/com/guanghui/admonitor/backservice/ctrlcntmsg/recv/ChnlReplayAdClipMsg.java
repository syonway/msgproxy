package com.guanghui.admonitor.backservice.ctrlcntmsg.recv;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.LinkedList;

/**
 * Created by cuiju on 2017/2/13. 20 29
 */
public class ChnlReplayAdClipMsg {
    public int taskId;
    public String channelPath;
    public String startTime;
    public String endTime;
    public LinkedList<AdClipReplay> replayAdClips;

    @JsonIgnore
    public long startTimeLong;
    @JsonIgnore
    public long endTimeLong;

    @JsonIgnore
    public void string2long(){
        startTimeLong = Long.parseLong(startTime);
        endTimeLong = Long.parseLong(endTime);
        if (replayAdClips != null) {
            replayAdClips.forEach(AdClipReplay::string2long);
        }
    }
}
