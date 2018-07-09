package com.guanghui.admonitor.backservice.ctrlcntmsg.send.response;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.LinkedList;

/**
 * Created by cuiju on 2017/3/10. 08 10
 */
public class LocalIpList {
    public LinkedList<String> iplist;

    @JsonIgnore
    public String toString(){
        String res = "";
        for(String ip : iplist){
            res += ip + ",";
        }
        return res;
    }
}
