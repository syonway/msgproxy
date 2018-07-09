package com.guanghui.admonitor.backservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guanghui.admonitor.backservice.ctrlcnt_demo.TestDataBase;
import com.guanghui.admonitor.backservice.ctrlcntmsg.CommonRespose;
import com.guanghui.admonitor.backservice.ctrlcntmsg.MsgId;
import com.guanghui.admonitor.backservice.ctrlcntmsg.recv.*;
import com.guanghui.itvm.dcbase.IRecvData;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by cuiju on 2017/2/13. 09 55
 */
public class RecvMsg implements IRecvData {
    @Override
    public byte[] recvData(int monitorId, byte[] bytes, int dataid) {
        byte[] res = new byte[0];
        switch (dataid){
            //for match monitor start
            case MsgId.RPT_REPLAY_AD_CLIPS_MSG_ID:
                res = rptReplayAdClips(monitorId, bytes);
                break;
            case MsgId.RPT_REPLAY_CLIPS_MSG_ID:
                res = rptReplayClips(monitorId, bytes);
                break;
             //for match monitor end

            //for record monitor start
            case MsgId.REPORT_RECORD_STATUS:
                res = rptRecordStatus(monitorId, bytes);
                break;
            case MsgId.START_TASK_RESULT:
                res = rptStartTaskResult(monitorId, bytes);
                break;
             //for record monitor end

            //for cal feature monitor start
            case MsgId.REPORT_CAL_TASK_STATUS:
                res = rptCalTaskStatus(monitorId, bytes);
                break;
             //cal featrue monitor end
            default:
        }
        return res;
    }

    private byte[] rptCalTaskStatus(int monitorid, byte[] bytes){
        byte[] res = null;
        ObjectMapper objectMapper = new ObjectMapper();
        CommonRespose response = new CommonRespose();

        try {
            ReportCalTaskStatus calTaskStatus = objectMapper.readValue(bytes, ReportCalTaskStatus.class);
            calTaskStatus.str2long();
            //添加业务逻辑处理代码
           logger.info(calTaskStatus.toString());
           res = objectMapper.writeValueAsBytes(response);
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            response.code = -1;
            response.info = "format error";
        }
        return res;
    }

    private byte[] rptRecordStatus(int monitorid, byte[] bytes){
        byte[] res;
        ObjectMapper objectMapper = new ObjectMapper();
        CommonRespose respose = new CommonRespose();
        try {
            ReportRecordStatus reportRecordStatus = objectMapper.readValue(bytes, ReportRecordStatus.class);
            reportRecordStatus.string2long();

            //添加业务逻辑处理代码
            logger.info(reportRecordStatus.toString());
            res = objectMapper.writeValueAsBytes(respose);
        } catch (IOException e) {
            logger.error("parse to ReportRecordStatus failed:{}", ExceptionUtils.getStackTrace(e));
            respose.code = -1;
            respose.info = "format error";
            return null;
        }
        return res;
    }

    private byte[] rptStartTaskResult(int monitorid, byte[] bytes){
        byte[] res = null;
        ObjectMapper objectMapper = new ObjectMapper();
        CommonRespose response = new CommonRespose();
        try {
            AddDelRecvTaskResult startTaskResult = objectMapper.readValue(bytes, AddDelRecvTaskResult.class);

            //添加业务逻辑处理代码
            logger.info(startTaskResult.toString());
            res = objectMapper.writeValueAsBytes(response);
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            response.code = -1;
            response.info = "fomat error";
        }
        return res;
    }

    private byte[] rptReplayAdClips(int monitorid, byte[] bytes){
        byte[] res;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            ChnlReplayAdClipMsg msg = objectMapper.readValue(bytes, ChnlReplayAdClipMsg.class);
            msg.string2long();
            TestDataBase.getIns().addRptAdClips2Db(monitorid, msg);


            logger.info(ReflectionToStringBuilder.toString(msg, ToStringStyle.MULTI_LINE_STYLE));
            CommonRespose commonRespose = new CommonRespose();
            res = objectMapper.writeValueAsBytes(commonRespose);
        } catch (IOException e) {
            logger.error("parse ChnlReplayAdClipMsg failed:{}",
                    ExceptionUtils.getStackTrace(e));
            res = null;
        }
        return res;
    }

    private byte[] rptReplayClips(int monitorid, byte[] bytes){
        byte[] res = null;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            ReplayClipsMsg msg = objectMapper.readValue(bytes, ReplayClipsMsg.class);
            msg.string2long();

            TestDataBase.getIns().addReplayClips(monitorid, msg);

            logger.info(ReflectionToStringBuilder.toString(msg, ToStringStyle.MULTI_LINE_STYLE));
            CommonRespose commonRespose = new CommonRespose();
            res = objectMapper.writeValueAsBytes(commonRespose);
        } catch (IOException e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
        return res;
    }

    private Logger logger = LoggerFactory.getLogger("RecvMsg");
}
