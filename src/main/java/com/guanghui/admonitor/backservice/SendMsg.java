package com.guanghui.admonitor.backservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guanghui.admonitor.backservice.ctrlcntmsg.CommonRespose;
import com.guanghui.admonitor.backservice.ctrlcntmsg.MsgId;
import com.guanghui.admonitor.backservice.ctrlcntmsg.RecvTask;
import com.guanghui.admonitor.backservice.ctrlcntmsg.recv.AddDelRecvTaskResult;
import com.guanghui.admonitor.backservice.ctrlcntmsg.send.*;
import com.guanghui.admonitor.backservice.ctrlcntmsg.send.response.CreateRefAdClipResponse;
import com.guanghui.admonitor.backservice.ctrlcntmsg.send.response.LocalIpList;
import com.guanghui.admonitor.backservice.ctrlcntmsg.send.response.MonitorType;
import com.guanghui.admonitor.backservice.ctrlcntmsg.send.response.NasMountInfos;
import com.guanghui.itvm.dcbase.DataChannelMng;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static com.guanghui.admonitor.backservice.ctrlcntmsg.MsgId.GET_RUNING_TASK;

/**
 * Created by cuiju on 2017/2/14. 19 58
 */
public class SendMsg {
    public SendMsg(DataChannelMng dataChannelMng) {
        this.dataChannelMng = dataChannelMng;
    }

    //for common cmd start
    public MonitorType getMonitorType(int monitor){
        MonitorType monitorType = null;
        ObjectMapper objectMapper = new ObjectMapper();
        byte[] sendData = new byte[1];
        byte[] res = dataChannelMng.sendData(monitor, sendData, MsgId.GET_MONITOR_TYPE);
        try {
            monitorType = objectMapper.readValue(res, MonitorType.class);
        } catch (IOException e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
        return monitorType;
    }
    //for common cmd end

    ///for match start
    private CommonRespose sendFindReplayClipsTaskMsg(int monitorid, FindReplayClipTaskMsg msg, int msgid){
        msg.long2String();
        return sendCmdRecvRes(monitorid, msg, msgid, CommonRespose.class);
    }
    public CommonRespose sendSelFindTaskMsg(int monitorid, FindReplayClipTaskMsg msg){
        return sendFindReplayClipsTaskMsg(monitorid, msg, MsgId.SELF_FIND_TASK_MSG_ID);
    }


    public CommonRespose sendFindAdClipTask(int monitorid, FindReplayClipTaskMsg msg){
        return sendFindReplayClipsTaskMsg(monitorid, msg, MsgId.FIND_REPLAY_TASK_MSG_ID);
    }

    /*public CommonRespose addRefAdClips(int monitorid, RefAdClips msg){
        return addOrDelRefAdClips(monitorid, msg, MsgId.ADD_AD_CLIPS_MSG_ID);
    }

    public CommonRespose delRefAdClips(int monitorid, RefAdClips msg){
        return addOrDelRefAdClips(monitorid, msg, MsgId.DEL_AD_CLIPS_MSG_ID);
    }

    private CommonRespose addOrDelRefAdClips(int monitor, RefAdClips clipsMsg, int msgid){
        CommonRespose respose = null;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            byte[] sendData = objectMapper.writeValueAsBytes(clipsMsg);
            byte[] res = dataChannelMng.sendData(monitor, sendData, msgid);
            respose = objectMapper.readValue(res, CommonRespose.class);
        } catch (Exception e) {
           logger.error(ExceptionUtils.getStackTrace(e));
        }
        return respose;
    }*/
    //for match end

    //for record start
    public RecvTasks sendGetRuningTask(int monitor){
        byte[] data = new byte[1];
        byte[] res = dataChannelMng.sendData(monitor, data, GET_RUNING_TASK);
        RecvTasks respose = null;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            respose = objectMapper.readValue(res, RecvTasks.class) ;
        } catch (IOException e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
        return respose;
    }

    public LocalIpList getLocalIpList(int monitor){
        byte[] data = new byte[1];
        byte[] res = dataChannelMng.sendData(monitor, data, MsgId.GET_LOCAL_IP_LIST);
        LocalIpList respose = null;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            respose = objectMapper.readValue(res, LocalIpList.class) ;
        } catch (IOException e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
        return respose;
    }

    public NasMountInfos sendGetNasInfo(int monitor){
        byte[] data = new byte[1];
        byte[] res = dataChannelMng.sendData(monitor, data, MsgId.GET_NAS_MOUNT_INFO);
        NasMountInfos respose = null;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            respose = objectMapper.readValue(res, NasMountInfos.class) ;
        } catch (IOException e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
        return respose;
    }

    public AddDelRecvTaskResult sendAddRecvTask(int monitor, RecvTask task){
        return sendRecvTask(monitor, task, MsgId.ADD_RECV_TASK);
    }

    public AddDelRecvTaskResult  sendDelRecvTask(int monitor, RecvTask task){
        return sendRecvTask(monitor, task, MsgId.DELETE_RECV_TASK);
    }

    private AddDelRecvTaskResult sendRecvTask(int monitorid, RecvTask task, int msgid){
        return sendCmdRecvRes(monitorid, task, msgid, AddDelRecvTaskResult.class);
    }
    //for record end

    //for calfeature start
    public CommonRespose sendStartCalFeatureTask(int monitorid, CalTaskMsg msg){
        return sendCalTask(monitorid, msg, MsgId.START_CAL_TASK_MSG_ID);
    }

    public CommonRespose sendStopCalFeatureTask(int monitorid, CalTaskMsg msg){
        return sendCalTask(monitorid, msg, MsgId.STOP_CAL_TASK_MSG_ID);
    }
    private CommonRespose sendCalTask(int monitor, CalTaskMsg msg, int msgid){
        return sendCmdRecvRes(monitor, msg, msgid, CommonRespose.class);
    }
    //for calfeature end

    private <T,R> R sendCmdRecvRes(int monitor, T cmd, int msgid, Class<R> resType){
        ObjectMapper objectMapper = new ObjectMapper();
        R response = null;
        try {
            byte[] data = objectMapper.writeValueAsBytes(cmd);
            byte[] res = dataChannelMng.sendData(monitor, data, msgid);
            response = objectMapper.readValue(res, resType);
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
        return response;
    }

    //for refadcreate start
    public CreateRefAdClipResponse sendCreateRefAdClip(int monitor, CreateRefAdClip msg){
        return  sendCmdRecvRes(monitor, msg, MsgId.CREATE_REF_AD_CLIP, CreateRefAdClipResponse.class);
    }
    //for refadcreate end

    private DataChannelMng dataChannelMng;
    private Logger logger = LoggerFactory.getLogger("SendMsg");
}
