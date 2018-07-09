package com.guanghui.admonitor.backservice.ctrlcntmsg;

/**
 * Created by cuiju on 2017/2/13. 10 10
 */

/*

static const uint32_t DELETE_RECV_TASK = 1002;
static const uint32_t ADD_RECV_TASK = 1001;

static const uint32_t GET_LOCAL_IP_LIST = 1100;
static const uint32_t GET_NAS_MOUNT_INFO = 1101;
static const uint32_t GET_RUNING_TASK = 1102;

static const uint32_t REPORT_RECORD_STATUS = 1011;
static const uint32_t START_TASK_RESULT = 1010;

*/
public interface MsgId {
    //for comm cmd
    int GET_MONITOR_TYPE = 5000;

    ///for match monitor
    int SELF_FIND_TASK_MSG_ID = 1;
    int ADD_AD_CLIPS_MSG_ID = 2;
    int DEL_AD_CLIPS_MSG_ID = 3;
    int FIND_REPLAY_TASK_MSG_ID = 4;
    int RPT_REPLAY_AD_CLIPS_MSG_ID = 100;
    int RPT_REPLAY_CLIPS_MSG_ID = 200;

    //for record monitor
    int DELETE_RECV_TASK = 1002;
    int ADD_RECV_TASK = 1001;
    int GET_LOCAL_IP_LIST = 1100;
    int GET_NAS_MOUNT_INFO = 1101;
    int GET_RUNING_TASK = 1102;
    int REPORT_RECORD_STATUS = 1011;
    int START_TASK_RESULT = 1010;


    ///for cal feature monitor
    int START_CAL_TASK_MSG_ID = 2101;
    int CHANGE_CAL_MASK_MSG_ID = 2102;
    int STOP_CAL_TASK_MSG_ID = 2201;
    int GET_CALING_TASKS_MSG_ID = 2301;
    int REPORT_CAL_TASK_STATUS = 2000;

    int CREATE_REF_AD_CLIP = 6001;
}


