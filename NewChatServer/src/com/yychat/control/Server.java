package com.yychat.control;
import cn.hutool.core.codec.Base64;
import cn.hutool.core.io.BufferUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.socket.nio.NioServer;
import com.yychat.control.DBUtil;
import com.yychat.control.PasswordUtil;
import com.yychat.model.CommandType;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static com.yychat.model.CommandType.*;

public class Server extends Thread {
    private final String projectRoot = System.getProperty("user.dir");
    private HashMap<String, SocketChannel> clients = new HashMap<>();
    private NioServer server;

    public Server() {

    }

    @Override
    public void run() {
        server = new NioServer(3456);
        server.setChannelHandler(this::taskHandler);
        server.listen();
    }

    @Override
    public void interrupt() {
        //TODO:处理线程关闭
        String message = LOGOUT.getCommandCode() +
                "|" +
                "all";
        for (SocketChannel c : clients.values()) {
            sendMessage(c, message);
            try {
                c.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        super.interrupt();
    }

    private boolean sendMessage(SocketChannel sc, String msg) {
        try {
            sc.write(BufferUtil.createUtf8(msg));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
            //throw new RuntimeException(e);
        }
    }

    private void taskHandler(SocketChannel channel) {
        ByteBuffer buffer = ByteBuffer.allocate(10 * 1024 * 1024);//2MB
        try {
            int readBytes = channel.read(buffer);
            if (readBytes > 0) {
                buffer.flip();
                byte[] bytes = new byte[buffer.remaining()];
                buffer.get(bytes);
                String msg = new String(bytes);
                String[] parts = msg.split("\\|");//分割
                try {
                    int commandCode = Integer.parseInt(parts[0]);
                    CommandType command = CommandType.fromCode(commandCode);
                    if (parts.length - 1 < CommandType.getMinParamCount(command)) {
                        String message = CommandType.GENERAL_ACK.getCommandCode() + "|server|invalid_param_count";
                        sendMessage(channel, message);
                        return;
                    }
                    StringBuilder messageBuilder = new StringBuilder();
                    switch (command) {
                        case LOGIN_REQUEST://登录请求
                        {
                            if (handleLogin(parts)) {
                                //登录成功
                                messageBuilder.append(LOGIN_RESPONSE.getCommandCode())
                                        .append("|")
                                        .append(parts[1])
                                        .append("|")
                                        .append("success");
                                System.out.println("login success");
                                clients.put(parts[1], channel);
                            } else {
                                //登录失败
                                messageBuilder.append(LOGIN_RESPONSE.getCommandCode())
                                        .append("|")
                                        .append(parts[1])
                                        .append("|")
                                        .append("fail");
                                System.out.println("login fail");
                            }
                            sendMessage(channel, messageBuilder.toString());
                            break;
                        }
                        case REGISTER_REQUEST://注册请求
                        {
                            //TODO:注册请求
                            if (handleRegister(parts)) {
                                messageBuilder.append(REGISTER_RESPONSE.getCommandCode())
                                        .append("|")
                                        .append(parts[1])
                                        .append("|")
                                        .append("success");
                                System.out.println("register success");
                            } else {
                                messageBuilder.append(REGISTER_RESPONSE.getCommandCode())
                                        .append("|")
                                        .append(parts[1])
                                        .append("|")
                                        .append("fail");
                                System.out.println("register fail");
                            }
                            sendMessage(channel, messageBuilder.toString());
                            break;
                        }
                        case FRIEND_REQUEST://新好友请求
                        {//TODO:新好友请求
                            handleFriendRequest(channel, parts);
                            break;
                        }
                        case QUERY_ONLINE_FRIENDS://查询所有在线的好友
                        {
                            String result = queryAllOnlineFriend(parts[1]);
                            messageBuilder.append(QUERY_ONLINE_FRIENDS_RESPONSE.getCommandCode())
                                    .append("|")
                                    .append(parts[1])
                                    .append("|")
                                    .append(result);
                            sendMessage(channel, messageBuilder.toString());
                            break;
                        }
                        case LOGOUT://登出
                        {
                            if (parts.length > 1) {
                                handleLogout(channel, parts[1]);
                            } else {
                                handleLogout(channel, null);
                            }
                            break;
                        }
                        case SEND_MESSAGE://发送信息
                        {
                            handleSendMessage(channel, parts);
                            break;
                        }
                        case REQUEST_CHAT_HISTORY://请求聊天记录
                        {
                            handleQueryChatHistory(channel, parts);
                            break;
                        }
                        case FRIEND_RESPOND://好友回应发给他的好友请求
                        {
                            handleFriendRequestResponse(channel, parts);
                            break;
                        }
                        case QUERY_ALL_FRIENDS://获取好友列表
                        {
                            String allFriends = DBUtil.getAllFriends(parts[1], 1);
                            messageBuilder.append(QUERY_ALL_FRIENDS_RESPONSE.getCommandCode())
                                    .append("|")
                                    .append(parts[1])
                                    .append("|")
                                    .append(allFriends);
                            sendMessage(channel, messageBuilder.toString());
                            break;
                        }
                        case QUERY_FRIEND_AVATAR://获取头像 18|userName|friendName
                        {
                            String path = DBUtil.getUserAvatarPath(parts[1]);
                            String imageBase64 = Base64.encode(new File(path));
                            //19|userName|friendName|image
                            String message = QUERY_FRIEND_AVATAR_ACK.getCommandCode() + "|" + parts[1] + "|" + parts[2] + "|" + imageBase64;
                            sendMessage(channel, message);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    throw e;
                }

            } else if (readBytes < 0) {
                IoUtil.close(channel);
            }
        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    private boolean handleLogin(String[] params) {

        String userName = params[1];
        String password = params[2];
        return DBUtil.loginValidate(userName, password);
    }

    private boolean handleRegister(String[] params) {
        String userName = params[1];
        String password = params[2];
        if (DBUtil.hasUser(userName)) {
            return false;
        } else {
            byte[] salt = PasswordUtil.generateSalt();
            if (DBUtil.addNewUser(userName, password, salt) == 1) {
                String defaultAvatarPath = Paths.get("avatars", "default_avatar.png").toString();
                if (DBUtil.addNewUserAvatarPath(userName, defaultAvatarPath)) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }
    }

    private boolean handleFriendRequest(SocketChannel sc, String[] params) {
        //TODO:转发好友申请 (3|userName|friendName)
        String userName = params[1];
        String friendName = params[2];
        if (DBUtil.hasUser(friendName)) {
            if (clients.containsKey(friendName)) {
                //在线就转发申请
                String message = params[0] + "|" + params[1] + "|" + params[2];
                return sendMessage(clients.get(friendName), message);
            } else {
                //离线就驳回
                String message = NEW_FRIEND_CONFIRM.getCommandCode() + "|" + userName + "|" + friendName + "|offline";
                sendMessage(sc, message);
                try {
                    sleep(1);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        } else {
            String message = NEW_FRIEND_CONFIRM.getCommandCode() + "|" + userName + "|" + friendName + "|no user";
            sendMessage(sc, message);
            try {
                sleep(1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return false;
    }

    private String queryAllOnlineFriend(String userName) {
        //TODO:查询该用户所有在线的好友
        String[] allFriends = DBUtil.getAllFriends(userName, 1).split("\\|");
        StringBuilder result = new StringBuilder();
        for (String friend : allFriends) {
            if (clients.containsKey(friend)) {
                result.append(friend).append("|");
            }
        }
        result.deleteCharAt(result.length() - 1);
        return result.toString();
    }

    private void handleQueryChatHistory(SocketChannel sc, String[] params) {
        //TODO:处理请求聊天记录(9|userName|friendName|lastTime)
        List<String> history;
        if (params[3].equals("0")) {
            //获取全部的聊天记录
            history = DBUtil.getChatMessage(params[1], params[2]);
        } else {
            //获取指定时间往后的聊天记录
            history = DBUtil.getChatMessage(params[1], params[2], new Date(Long.parseLong(params[3])));
        }
        String resultHead = CommandType.REQUEST_CHAT_HISTORY_ACK.getCommandCode() + "|" + params[1] + "|" + history.size();
        sendMessage(sc, resultHead);
        StringBuilder result = new StringBuilder();
        for (String message : history) {//17|sender|receiver|content|timestamp
            result.append(CommandType.REQUEST_CHAT_HISTORY_CONTENT.getCommandCode())
                    .append("|")
                    .append(message);
            sendMessage(sc, result.toString());
            try {
                sleep(1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private boolean handleSendMessage(SocketChannel sc, String[] params) {
        //TODO:处理消息发送(7|userName|friendName|content|time)//Content为base64编码
        String userName = params[1];
        String friendName = params[2];
        String message = params[3];
        if (clients.containsKey(friendName)) {
            Date now = new java.util.Date();
            StringBuilder builder = new StringBuilder();
            builder.append(SEND_MESSAGE.getCommandCode())
                    .append("|")
                    .append(userName)
                    .append("|")
                    .append(friendName)
                    .append("|")
                    .append(message)
                    .append("|")
                    .append(now.getTime());
            boolean result = sendMessage(clients.get(friendName), builder.toString());
            result |= DBUtil.insertChatMessage(userName, friendName, Base64.decodeStr(message), now);
            //发送信息ACK(13|userName|status|time)
            if (result) {
                builder.append(SEND_MESSAGE_RESPONSE.getCommandCode())
                        .append("|")
                        .append(userName)
                        .append("|")
                        .append("success")
                        .append("|")
                        .append(now.getTime());
            } else {
                builder.append(SEND_MESSAGE_RESPONSE.getCommandCode())
                        .append("|")
                        .append(userName)
                        .append("|")
                        .append("failure")
                        .append("|")
                        .append(now.getTime());
            }
            sendMessage(sc, builder.toString());
            return result;
        } else {
            return false;
        }
    }

    private void handleLogout(SocketChannel sc, String userName) {
        //TODO:处理登出请求
        generateAck(sc, userName);
        IoUtil.close(sc);
        System.out.println("logout success");
        if (userName != null) {
            clients.remove(userName);
        }
    }

    private void handleFriendRequestResponse(SocketChannel sc, String[] params) {
        //TODO:处理好友同意请求(4|sender|receiver|status|ImageIcon)
        String userName = params[1];
        String friendName = params[2];
        boolean status = Boolean.parseBoolean(params[3]);
        StringBuilder builder = new StringBuilder();
        if (status) {
            if (clients.containsKey(friendName)) {
                //请求者在线，直接发送同意请求通知
                builder.append(NEW_FRIEND_CONFIRM.getCommandCode())
                        .append("|")
                        .append(friendName)
                        .append("|")
                        .append(userName)
                        .append("|")
                        .append("true");
                try {
                    byte[] imageBytes = Files.readAllBytes(Paths.get(System.getProperty("user.dir"), DBUtil.getUserAvatarPath(friendName)));
                    String base64Img = Base64.encode(imageBytes);
                    builder.append("|").append(base64Img);
                } catch (IOException e) {
                    e.printStackTrace();
                    builder.append("|use default");
                }
                sendMessage(clients.get(friendName), builder.toString());
            }
            DBUtil.insertIntoFriend(userName, friendName, 1);
            DBUtil.insertIntoFriend(friendName, userName, 1);
        } else {
            //
            builder.append(NEW_FRIEND_CONFIRM.getCommandCode())
                    .append("|")
                    .append(userName)
                    .append("|")
                    .append(friendName)
                    .append("|")
                    .append("denial");
            sendMessage(sc, builder.toString());
        }

    }

    private void generateAck(SocketChannel sc, String userName) {
        String builder = GENERAL_ACK.getCommandCode() +
                "|" +
                userName;
        sendMessage(sc, builder);
    }
}