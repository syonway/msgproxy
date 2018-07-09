package com.guanghui.admonitor.backservice.ctrlcntmsg.recv;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Created by cuiju on 2017/2/13. 20 33
 */
public class ReplayInfo {
    public String replayStartTime;
    public int replayFrameNr;

    @JsonIgnore
    public long replayStartTimeLong;

    @JsonIgnore
    public void string2long(){
        replayStartTimeLong = Long.parseLong(replayStartTime);
    }
}
