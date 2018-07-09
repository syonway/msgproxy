package com.guanghui.admonitor.backservice.ctrlcnt_demo;

import com.guanghui.admonitor.backservice.SendMsg;
import com.guanghui.admonitor.backservice.ctrlcntmsg.CommonRespose;
import com.guanghui.admonitor.backservice.ctrlcntmsg.send.FindReplayClipTaskMsg;
import com.guanghui.itvm.dcbase.DataChannelMng;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by cuiju on 2017/2/21. 21 42
 */
public class ProcessTaskTask implements Runnable {
    public ProcessTaskTask(DataChannelMng mng){
        dataChannelMng = mng;
        sendMsg = new SendMsg(mng);
    }
    @Override
    public void run() {
        TestDataBase db = TestDataBase.getIns();
        FindReplayClipTaskMsg selfFindTaskMsg = new FindReplayClipTaskMsg();
        FindReplayClipTaskMsg findReplayClipTaskMsg = new FindReplayClipTaskMsg();
        while (true) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                logger.error(ExceptionUtils.getStackTrace(e));
            }
            List<Integer> monitors = dataChannelMng.getOnlineMonitorForSend();
            if (monitors.isEmpty()){
                continue;
            }
            int monitorid = monitors.get(0);
            int taskid = db.getSelFindTaskMsg(selfFindTaskMsg);
            if (taskid > 0){
                CommonRespose res = sendMsg.sendSelFindTaskMsg(monitorid, selfFindTaskMsg);
                if (res.code == 0){
                    db.changeSelfSearchReplay(taskid, 1);
                }else {
                    logger.error("send task failed:{}", res.info);
                }
            }

            taskid = db.getFindReplayAdClipTaskMsg(findReplayClipTaskMsg);
            if (taskid > 0){
                CommonRespose res = sendMsg.sendFindAdClipTask(monitorid, findReplayClipTaskMsg);
                if (res.code == 0){
                    db.changeSearchAdClipsTask(monitorid, 1);
                    logger.info("send search replay adclip task success");
                }else {
                    logger.error("send search relay adclip task faild:{}", res.info);
                }
            }
        }
    }

    private SendMsg sendMsg = null;
    private Logger logger = LoggerFactory.getLogger("ProcessTaskTask");
    private DataChannelMng dataChannelMng = null;
}
