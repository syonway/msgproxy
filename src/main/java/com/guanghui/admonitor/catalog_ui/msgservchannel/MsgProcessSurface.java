package com.guanghui.admonitor.catalog_ui.msgservchannel;

import com.guanghui.admonitor.backservice.SendMsg;
import com.guanghui.admonitor.catalog_ui.msgservchannel.msgs.*;

/**
 * Created by cuiju on 2017/1/11. 21 33
 */
public interface MsgProcessSurface {
    UserInfo login(LoginMsg msg);//if login success return UserInfo, or return null
    UserInfo changeUserInfo(UserInfo userInfo);
    UserTaskInfo getUserTaskInfo(String userName);
    UserProductInfo getUserProductInfo(int ownerid);
    CommonResponse addAdClipInfo(AdClipInfo info, String username, SendMsg sendmsg);
    CommonResponse addAdRecordInfo();
    AdClassfication getAdClassfication(String username);
    ChannelMatchMsg getChannelMatch(ReqChannelMatchMsg reqMsg, String username);
    AdOwnerInfo getAdOwnerInfo();
}
