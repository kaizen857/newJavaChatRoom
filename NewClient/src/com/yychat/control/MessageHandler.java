package com.yychat.control;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.io.BufferUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.socket.nio.NioClient;
import com.yychat.model.CommandType;
import com.yychat.view.ChatMessageData;
import com.yychat.view.ChatUI;
import com.yychat.view.FriendData;

import javax.swing.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class MessageHandler {

    // 单例相关
    private static volatile MessageHandler instance;
    private String clientUserName;
    private NioClient client;
    private final AtomicBoolean hasLogin = new AtomicBoolean(false);
    private final AtomicBoolean hasStart = new AtomicBoolean(false);
    private final AtomicBoolean hasStartReceived = new AtomicBoolean(false);
    private final AtomicBoolean logoutFromServer = new AtomicBoolean(false);
    private final ExecutorService messageProcessor = new ThreadPoolExecutor(
            Runtime.getRuntime().availableProcessors() * 2,
            Runtime.getRuntime().availableProcessors() * 2,
            60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(1000),
            new ThreadFactory() {
                private final AtomicInteger count = new AtomicInteger(1);
                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, "message-handler-" + count.getAndIncrement());
                }
            },
            new ThreadPoolExecutor.CallerRunsPolicy()
    );
    // 锁对象：用于Socket写操作线程安全
    private final Object writeLock = new Object();
    //暂存好友名字队列
    //暂存在线的好友名字队列
    private List<ChatMessageData> messageList;
    private String friendAvatarPath;
    private ChatUI chatUI;
    private final String projectRoot = System.getProperty("user.dir");
    // CompletableFuture
    private volatile CompletableFuture<Boolean> loginFuture;
    private volatile CompletableFuture<Boolean> registerFuture;
    private volatile CompletableFuture<List<String>> friendListWaitAckFuture;
    private volatile CompletableFuture<List<String>> onlineFriendListWaitAckFuture;
    private volatile CompletableFuture<String> getFriendAvatarWaitAckFuture;
    private volatile CompletableFuture<Boolean> getFriendChatHistoryWaitAckFuture;
    private volatile CompletableFuture<Boolean> getSendMessageCpltWaitAckFuture;
    private volatile CompletableFuture<Boolean> getChangeUserAvatarCpltWaitAckFuture;
    private volatile CompletableFuture<Boolean> getQueryHasUserWaitAckFuture;

    private final AtomicInteger chatHistorySize = new AtomicInteger(0);

    private MessageHandler(String clientUserName) {
        this.clientUserName = clientUserName;
    }

    public void setClientUserName(String clientUserName) {
        this.clientUserName = clientUserName;
    }

    public String getClientUserName(){
        return clientUserName;
    }

    public boolean isLogoutFromServer(){
        return logoutFromServer.get();
    }

    public static MessageHandler getInstance(String userName) {
        if (instance == null) {
            synchronized (MessageHandler.class) {
                if (instance == null) {
                    instance = new MessageHandler(userName);
                }
            }
        }
        return instance;
    }

    public void start() {
        if (!hasStart.get()) {
            this.client = new NioClient("127.0.0.1", 3456);
            this.client.setChannelHandler(this::DecodeMessage);
            client.listen();
            hasStart.set(true);
        }
    }

    public boolean hasStart() {
        return hasStart.get();
    }

    public void shutdown() {
        try {
            messageProcessor.shutdown();
            if (!messageProcessor.awaitTermination(5, TimeUnit.SECONDS)) {
                messageProcessor.shutdownNow();
            }
            if(client != null) {
                client.close();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            messageProcessor.shutdownNow();
        }
    }


    private boolean sendMessage(SocketChannel sc, String msg) {
        try {
            synchronized (writeLock) {
                sc.write(BufferUtil.createUtf8(msg));
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void setChatUI(ChatUI chatUI) {
        this.chatUI = chatUI;
    }

    private boolean sendCommand(CommandType command, String userName, String password) {
        String msg = command.getCommandCode() + "|" + userName + "|" + password;
        return sendMessage(client.getChannel(), msg);
    }

    public boolean login(String userName, String password) {
        //TODO:登录
        hasLogin.set(false);
        loginFuture = new CompletableFuture<>();

        sendCommand(CommandType.LOGIN_REQUEST, userName, password);

        try {
            return loginFuture.get(10, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return false;
        }
    }

    public boolean register(String userName, String password) {
        //TODO:注册
        registerFuture = new CompletableFuture<>();

        sendCommand(CommandType.REGISTER_REQUEST, userName, password);

        try {
            return registerFuture.get(10, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return false;
        }
    }

    public List<String> getAllFriendsName(String userName) {
        //TODO:获取所有好友名字
        friendListWaitAckFuture = new CompletableFuture<>();
        String request = CommandType.QUERY_ALL_FRIENDS.getCommandCode() + "|" + userName;
        sendMessage(client.getChannel(), request);
        try {
            return friendListWaitAckFuture.get(30, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return Collections.emptyList();
        }
    }

    public void logout() {
        logoutFromServer.set(true);
        if(hasLogin.get()) {
            String message = CommandType.LOGOUT.getCommandCode() + "|" + clientUserName;
            sendMessage(client.getChannel(), message);
        }
        else{
            System.exit(0);
        }
    }

    public boolean getHasLogin(){
        return hasLogin.get();
    }

    public List<String> getOnlineFriendList(String userName) {
        //TODO:获取在线好友列表
        onlineFriendListWaitAckFuture = new CompletableFuture<>();
        String request = CommandType.QUERY_ONLINE_FRIENDS.getCommandCode() + "|" + userName;
        sendMessage(client.getChannel(), request);
        try {
            return onlineFriendListWaitAckFuture.get(30, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return Collections.emptyList();
        }
    }

    public String getFriendAvatar(String userName, String friendName) {
        //TODO:获取好友头像（保存到本地后返回路径）
        getFriendAvatarWaitAckFuture = new CompletableFuture<>();
        String request = CommandType.QUERY_FRIEND_AVATAR.getCommandCode() + "|" + userName + "|" + friendName;
        sendMessage(client.getChannel(), request);
        try {

            return getFriendAvatarWaitAckFuture.get(30, TimeUnit.SECONDS);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean changeUserAvatar(String imageBase64){
        getChangeUserAvatarCpltWaitAckFuture = new CompletableFuture<>();
        String message = CommandType.CHANGE_USER_AVATAR.getCommandCode() + "|" + clientUserName + "|" + imageBase64;
        System.out.println(message.length());
        sendMessage(client.getChannel(), message);
        try{
            return getChangeUserAvatarCpltWaitAckFuture.get(30,TimeUnit.SECONDS);
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }



    public List<ChatMessageData> getFriendChatHistory(String userName, String friendName) {
        //TODO:获取与好友的聊天记录
        getFriendChatHistoryWaitAckFuture = new CompletableFuture<>();
        String query = CommandType.REQUEST_CHAT_HISTORY.getCommandCode() + "|" + userName + "|" + friendName + "|0";
        sendMessage(client.getChannel(), query);
        try {
            if (getFriendChatHistoryWaitAckFuture.get(30, TimeUnit.SECONDS)) {
                return messageList;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    public List<ChatMessageData> getFriendChatHistory(String userName, String friendName, Date time) {
        //TODO:获取与好友的在time之后的聊天记录
        getFriendChatHistoryWaitAckFuture = new CompletableFuture<>();
        String query = CommandType.REQUEST_CHAT_HISTORY.getCommandCode() + "|" + userName + "|" + friendName + "|" + time.toString();
        sendMessage(client.getChannel(), query);
        try {
            if (getFriendChatHistoryWaitAckFuture.get(30, TimeUnit.SECONDS)) {
                return messageList;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    public boolean sendMessageToFriend(String userName, String friendName, String content, Date time) {
        //TODO:发送聊天信息  7|userName|friendName|content|time
        getSendMessageCpltWaitAckFuture = new CompletableFuture<>();
        String message = CommandType.SEND_MESSAGE.getCommandCode() + "|" + userName + "|" + friendName +"|"+Base64.encode(content)+"|"+time.getTime();
        sendMessage(client.getChannel(),message);
        try {
            return getSendMessageCpltWaitAckFuture.get(60,TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean sendImageMessage(String userName,String friendName,String imageBase64,Date time){
        //TODO:发送图像信息
        getSendMessageCpltWaitAckFuture = new CompletableFuture<>();
        String message = CommandType.SEND_IMAGE_MESSAGE.getCommandCode() +"|"+userName+"|"+friendName+"|"+imageBase64;
        sendMessage(client.getChannel(),message);
        try {
            return getSendMessageCpltWaitAckFuture.get(60,TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean confirmHasUser(String userName){
        getQueryHasUserWaitAckFuture = new CompletableFuture<>();
        String query = CommandType.QUERY_HAS_USER.getCommandCode() + "|" + userName;
        sendMessage(client.getChannel(), query);
        try {
            return getQueryHasUserWaitAckFuture.get(10,TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean sendFriendRequest(String userName,String friendName){
        String message = CommandType.FRIEND_REQUEST.getCommandCode() + "|" + userName + "|" + friendName;
        return sendMessage(client.getChannel(),message);
    }

    private void DecodeMessage(SocketChannel channel) {
        try {
            ByteBuffer buffer = ByteBuffer.allocate(100*1024*1024);
            int readBytes = channel.read(buffer);
            if (readBytes > 0) {
                buffer.flip();
                byte[] bytes = new byte[buffer.remaining()];
                buffer.get(bytes);
                final String msg = StrUtil.utf8Str(bytes);

                messageProcessor.submit(() -> processMessage(channel, msg));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processMessage(SocketChannel channel, String msg) {
        try {
            String[] params = msg.split("\\|");
            int commandCode = Integer.parseInt(params[0]);
            CommandType command = CommandType.fromCode(commandCode);

            if (params.length - 1 < CommandType.getMinParamCount(command)) {
                String response = CommandType.GENERAL_ACK.getCommandCode() + "|server|invalid_param_count";
                sendMessage(channel, response);
                return;
            }
            if(command == CommandType.LOGOUT){
                if(params[1].equals("all")){
                    logoutFromServer.set(true);
                }
                System.exit(0);
            }
            if (!hasLogin.get()) {
                handleUnauthenticatedMessage(command, params);
            } else {
//                if (params[1].equals(clientUserName) || (command == CommandType.FRIEND_REQUEST && params[2].equals(clientUserName))) {
                    handleAuthenticatedMessage(channel, command, params);
//                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            String errorResponse = CommandType.GENERAL_ACK.getCommandCode() + "|server|processing_error";
            sendMessage(channel, errorResponse);
        }
    }

    private void handleUnauthenticatedMessage(CommandType command, String[] params) {
        if (command == CommandType.LOGIN_RESPONSE) {
            boolean success = params.length >= 3 && params[1].equals(clientUserName) && params[2].equals("success");
            hasLogin.set(success);
            if (loginFuture != null) {
                loginFuture.complete(success);
            }
        } else if (command == CommandType.REGISTER_RESPONSE) {
            boolean success = params.length >= 3 && params[2].equals("success");
            if (registerFuture != null) {
                registerFuture.complete(success);
            }
        }
    }

    private void handleAuthenticatedMessage(SocketChannel sc, CommandType command, String[] params) {
        // 登录后的命令逻辑
        switch (command) {
            case QUERY_ALL_FRIENDS_RESPONSE: {//TODO:查询该用户的所有好友ACK
                List<String> friendList = new ArrayList<>(Collections.emptyList());
                friendList.addAll(Arrays.asList(params).subList(2, params.length));
                friendListWaitAckFuture.complete(friendList);

                break;
            }
            case QUERY_ONLINE_FRIENDS_RESPONSE: {//TODO:查询所有在线的好友ACK
                List<String> onlineFriendList = new ArrayList<>(Collections.emptyList());
                onlineFriendList.addAll(Arrays.asList(params).subList(2, params.length));
                if (onlineFriendListWaitAckFuture != null) {
                    onlineFriendListWaitAckFuture.complete(onlineFriendList);
                }
                break;
            }
            case SEND_MESSAGE:{
                //TODO:收到好友发来的聊天信息
                String sender = params[1];
                String message = Base64.decodeStr(params[3]);
                Date sendTime = new Date(Long.parseLong(params[4]));
                chatUI.newChatMessage(sender, message, sendTime);
                break;
            }
            case SEND_IMAGE_MESSAGE:{//20|sender|receiver|Image
                String sender = params[1];
                String receiver = params[2];
                String message = params[3];
                Date sendTime = new Date(Long.parseLong(params[4]));
                chatUI.newChatImageMessage(sender,message,sendTime);
                break;
            }
            case SEND_MESSAGE_RESPONSE: {//TODO:发送信息ACK 13|userName|status|time
                if(params[2].equals("success")){
                    if(getSendMessageCpltWaitAckFuture != null) {
                        getSendMessageCpltWaitAckFuture.complete(true);
                    }
                }
                else{
                    if(getSendMessageCpltWaitAckFuture != null) {
                        getSendMessageCpltWaitAckFuture.complete(false);
                    }
                }
                break;
            }
            case REQUEST_CHAT_HISTORY_ACK: {//TODO:请求聊天记录回应  12|userName|contentNumber
                chatHistorySize.set(Integer.parseInt(params[2]));
                messageList = new ArrayList<>();
                if(chatHistorySize.get() == 0){
                    hasStartReceived.set(false);
                    getFriendChatHistoryWaitAckFuture.complete(true);
                }
                else{
                    hasStartReceived.set(true);
                }
                break;
            }
            case REQUEST_CHAT_HISTORY_CONTENT: {//TODO:聊天记录信息回传  17|sender|receiver|isImage|content|sendTime
                if(hasStartReceived.get() && chatHistorySize.get() > 0){
                    int isImage = Integer.parseInt(params[3]);
                    if(isImage == 0){
                        String content = Base64.decodeStr(params[4]);
                        Date time = new Date(Long.parseLong(params[5]));
                        String avatarImagePath;
                        boolean isMyself = params[1].equals(clientUserName);
                        avatarImagePath = Paths.get(System.getProperty("user.dir"),"avatars/" + params[1] + ".png").toString();
                        messageList.add(new ChatMessageData(isMyself,true,content,time,avatarImagePath));
                        chatHistorySize.set(chatHistorySize.get() - 1);
                    }
                    else{
                        String imageBase64 = params[4];
                        byte[] imageBytes = Base64.decode(imageBase64);
                        Date time = new Date(Long.parseLong(params[5]));
                        boolean isMyself = params[1].equals(clientUserName);
                        String avatarImagePath = Paths.get(System.getProperty("user.dir"),"avatars/" + params[1] + ".png").toString();
                        Path paths = Paths.get(System.getProperty("user.dir"), "images", time.getTime() + ".png");
                        try {
                            Files.write(paths, imageBytes);
                            messageList.add(new ChatMessageData(isMyself,false,paths.toString(),time,avatarImagePath));
                            chatHistorySize.set(chatHistorySize.get() - 1);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    if(chatHistorySize.get() == 0){
                        hasStartReceived.set(false);
                        getFriendChatHistoryWaitAckFuture.complete(true);
                    }
                }
                break;
            }
            case QUERY_FRIEND_AVATAR_ACK: {//TODO:请求好友头像资源应答  19|userName|friendName|image
                if (params[1].equals(clientUserName)) {
                    byte[] imageByte = Base64.decode(params[3]);
                    friendAvatarPath = "avatars/" + params[2] + ".png";
                    try {
                        Path path = Paths.get(System.getProperty("user.dir"),friendAvatarPath);
                        Files.write(path, imageByte);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    if (getFriendAvatarWaitAckFuture != null) {
                        getFriendAvatarWaitAckFuture.complete(friendAvatarPath);
                    }
                }
                break;
            }
            case FRIEND_REQUEST: {//TODO:他人发给你的好友请求 3|userName|friendName
                String friendName = params[1];
                String query = friendName + "向你发来添加好友请求，是否同意？";
                int option = JOptionPane.showConfirmDialog(chatUI, query, "我;"+ clientUserName + "好友请求", JOptionPane.YES_NO_OPTION);
                StringBuilder builder = new StringBuilder();
                //消息回传
                //10|userName|friendName|status
                builder.append(CommandType.FRIEND_RESPOND.getCommandCode()).append("|").append(clientUserName).append("|").append(friendName).append("|");
                if (option == JOptionPane.YES_OPTION) {
                    //用户同意
                    builder.append("true|");
                } else {
                    //用户不同意
                    builder.append("false|");
                }
                sendMessage(sc, builder.toString());
                if(option == JOptionPane.YES_OPTION){
                    this.getFriendAvatar(clientUserName, friendName);
                    String path = Paths.get(projectRoot, "avatars", friendName + ".png").toString();
                    chatUI.newFriend(new FriendData(friendName, path, "", ""));
                }
                break;
            }
            case NEW_FRIEND_CONFIRM:{//TODO:发送的好友请求的回应(4|userName|friendName|status|ImageIcon)
                String friendName = params[2];
                String status = params[3];
                switch (status) {
                    case "true":
                        byte[] imageByte = Base64.decode(params[4]);
                        try {
                            String path = Paths.get(projectRoot, "avatars", friendName + ".png").toString();
                            Files.write(Paths.get(path), imageByte);
                            chatUI.newFriend(new FriendData(friendName, path, "", ""));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        JOptionPane.showMessageDialog(chatUI, "好友添加成功！", "好友请求", JOptionPane.INFORMATION_MESSAGE);
                        break;
                    case "offline":
                        JOptionPane.showMessageDialog(chatUI, "对方不在线上，等对方在线了再发送吧", "好友请求", JOptionPane.INFORMATION_MESSAGE);
                        break;
                    case "no user":
                        JOptionPane.showMessageDialog(chatUI, "没有这个用户", "好友请求", JOptionPane.INFORMATION_MESSAGE);
                        break;
                    case "already friend":
                        JOptionPane.showMessageDialog(chatUI, "已经是朋友了", "好友请求", JOptionPane.INFORMATION_MESSAGE);
                        break;
                    default:
                        JOptionPane.showMessageDialog(chatUI, "请求被拒绝", "好友请求", JOptionPane.INFORMATION_MESSAGE);
                        break;
                }
                break;
            }
            case CHANGE_USER_AVATAR_ACK:{
                if(getChangeUserAvatarCpltWaitAckFuture != null) {
                    if(params[2].equals("success")){
                        getChangeUserAvatarCpltWaitAckFuture.complete(true);
                    }
                    else{
                        getChangeUserAvatarCpltWaitAckFuture.complete(false);
                    }
                }
                break;
            }
            case QUERY_HAS_USER_ACK:{
                if(params[2].equals("true")) {
                    if(getQueryHasUserWaitAckFuture != null) {
                        getQueryHasUserWaitAckFuture.complete(true);
                    }
                }
                else{
                    if(getQueryHasUserWaitAckFuture != null) {
                        getQueryHasUserWaitAckFuture.complete(false);
                    }
                }
            }
            default:
                break;
        }
    }
}
