package com.guanghui.admonitor.backservice.ctrlcntmsg.recv;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.LinkedList;

/**
 * Created by cuiju on 2017/2/13. 20 36
 */
public class ReplayClip {
    public String fstartTime;
    public LinkedList<ReplayInfo> replayInfos;

    @JsonIgnore
    public long fstartTimeLong;

    public void string2long(){
        fstartTimeLong = Long.parseLong(fstartTime);
        replayInfos.forEach(ReplayInfo::string2long);
    }
}
