package com.guanghui.admonitor.backservice.ctrlcntmsg.recv;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Created by cuiju on 2017/2/13. 20 25
 */
public class AdClipReplay {
    public String nasInfo;
    public String adUrl;
    public  int adoffset;
    public int matchFrameNr;
    public int adClipFrameNr;
    public String startMatchChannelTime;

    @JsonIgnore
    public long startMatchChannelTimeLong;

    @JsonIgnore
    public void string2long(){
        startMatchChannelTimeLong = Long.parseLong(startMatchChannelTime);
    }
}
