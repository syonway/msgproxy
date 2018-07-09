package com.guanghui.admonitor.catalog_ui.demo;

import com.guanghui.admonitor.backservice.SendMsg;
import com.guanghui.admonitor.backservice.ctrlcntmsg.send.CreateRefAdClip;
import com.guanghui.admonitor.backservice.ctrlcntmsg.send.response.CreateRefAdClipResponse;
import com.guanghui.admonitor.catalog_ui.msgservchannel.MsgProcessSurface;
import com.guanghui.admonitor.catalog_ui.msgservchannel.msgs.*;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Created by cuiju on 2017/1/12. 16 35
 */
public class MsgProcess implements MsgProcessSurface {
    @Override
    public UserInfo login(LoginMsg msg) {
        UserInfo info = h2Db.getUserInfo(msg.userName) ;
        if (info == null){
            return null;
        }
        if (info.passWd.equals(msg.passWd)){
            return info;
        }
        return null;
    }

    @Override
    public UserInfo changeUserInfo(UserInfo userInfo) {
        return null;
        //return new userinfo
    }

    @Override
    public UserTaskInfo getUserTaskInfo(String userName) {
        return h2Db.getUserTaskInfo(userName);
    }

    @Override
    public AdOwnerInfo getAdOwnerInfo(){return h2Db.getAdOwners();}

    @Override
    public UserProductInfo getUserProductInfo(int ownerid){return h2Db.getUserProductInfo(ownerid);}

    @Override
    public CommonResponse AddAdRecordInfo(){

        return null;
    }

    @Override
    public ChannelMatchMsg getChannelMatch(ReqChannelMatchMsg reqMsg, String username) {
        return h2Db.getChannelMatch(reqMsg);
        // return null;
    }

    @Override
    public CommonResponse addAdClipInfo(AdClipInfo info, String username, SendMsg sendmsg) {
      
        try{
        CreateRefAdClip createRefAdClip = new CreateRefAdClip(info.channelName);
        createRefAdClip.startTime = Long.toString(info.startTime);
        createRefAdClip.endTime = Long.toString(info.startTime+(int)(info.frameNr/24*1000));
        createRefAdClip.nasIp = "10.108.157.177";
        //如需要，查询空adclip monitor
        int monitorid = 3000;
        CreateRefAdClipResponse response = sendmsg.sendCreateRefAdClip(monitorid, createRefAdClip);
        String res = ReflectionToStringBuilder.toString(response, ToStringStyle.MULTI_LINE_STYLE);
        System.out.println("adclip response-- "+res);
        CommonResponse comre = new CommonResponse();
        if(res=="<null>") {
            comre.res.info = "add clip success";
            //数据库操作
        }
        else {
            comre.res.info = "add clip fail";
        }
        return comre;
      }catch(Exception e){
          e.printStackTrace();
      }
      return null;
    }

    @Override
    public AdClassfication getAdClassfication(String username) {
        return h2Db.getAdClassfication();
    }



    public H2Db h2Db;
}
