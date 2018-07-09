package com.guanghui.admonitor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guanghui.admonitor.catalog_ui.demo.CmdMsg;
import com.guanghui.admonitor.catalog_ui.msgclientchannel.MsgChannelClient;
//import com.guanghui.admonitor.catalog_ui.msgclientchannel.calfeaturemsg.CalTaskMsg;

import java.io.File;
import java.io.IOException;

/**
 * Created by cuiju on 2017/1/19. 20 31
 */
public class ControlCalService {

    public static void main(String[] args){
         if (args.length < 3){
             System.out.println("input task_fileName calserviceip calserviceport");
             return;
         }
        ObjectMapper objectMapper = new ObjectMapper();
         File file = new File(args[0]);
        try {
            CmdMsg msg =  objectMapper.readValue(file, CmdMsg.class);
            MsgChannelClient client = new MsgChannelClient();
            client.conn2CalService(args[1], Integer.parseInt(args[2]));
            if ((msg.addMsg != null) && (!msg.addMsg.isEmpty())){
                msg.addMsg.forEach(client::addCalTask);
            }
            if ((msg.changeTaskMsg != null) && (!msg.changeTaskMsg.isEmpty())){
                msg.changeTaskMsg.forEach(client::changeCalTaskMask);
            }
            if ((msg.stopMsg) != null && (!msg.stopMsg.isEmpty())){
                msg.stopMsg.forEach(client::stopCalTask);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
