package com.yychat.control;

import javax.swing.*;

public class ShutdownHandler extends Thread {

    @Override
    public void run() {
        System.out.println("退出中");
        MessageHandler messageHandler = MessageHandler.getInstance("shutdown");
        if(messageHandler != null) {
            if(messageHandler.isLogoutFromServer()){
                JOptionPane.showMessageDialog(null, "服务器关闭！客户端关闭中");
                messageHandler.shutdown();
            }
            else if(messageHandler.getHasLogin()){
                messageHandler.logout();
                messageHandler.shutdown();
            }
        }
    }
}
