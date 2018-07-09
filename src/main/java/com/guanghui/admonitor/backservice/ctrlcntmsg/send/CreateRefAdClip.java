package com.guanghui.admonitor.backservice.ctrlcntmsg.send;

/**
 * Created by cuiju on 2017/4/3. 10 06
 */


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 std::string channelPath;
 std::string nasIp;
 int64_t startTime;
 int64_t endTime;
 */
public class CreateRefAdClip {
    public String channelPath;
    public String nasIp;
    public String startTime;
    public String endTime;

    public CreateRefAdClip(@JsonProperty("channelPath") String channelPath){
        this.channelPath = channelPath;
    }

    @JsonIgnore
    public long startTimeLong;

    @JsonIgnore
    public long endTimeLong;

    @JsonIgnore
    public void long2str(){
        startTime = Long.toString(startTimeLong);
        endTime = Long.toString(endTimeLong);
    }
}
