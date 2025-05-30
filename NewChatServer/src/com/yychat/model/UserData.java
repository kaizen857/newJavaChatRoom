package com.yychat.model;

import java.util.HashMap;
import java.util.List;

public class UserData {
    private String userName;                                        //用户名
    private String userAvatarPath;                                  //用户头像位置
    private List<String> friendsName;                               //好友名字
    private HashMap<String,String> friendsAvatar;                   //好友头像位置
    private HashMap<String,List<ChatMessageData>> friendsMessage;   //聊天记录

    UserData(){}

    UserData(String userName,String userAvatarPath,List<String> friendsName,HashMap<String,String> friendsAvatar){
        this.userName=userName;
        this.userAvatarPath=userAvatarPath;
        this.friendsName=friendsName;
        this.friendsAvatar=friendsAvatar;
        this.friendsMessage=new HashMap<>();
    }

    public String getUserAvatarPath() {
        return userAvatarPath;
    }

    public String getUserName() {
        return userName;
    }

    public List<String> getFriendsName() {
        return friendsName;
    }

    public HashMap<String, String> getFriendsAvatar() {
        return friendsAvatar;
    }

    public HashMap<String, List<ChatMessageData>> getFriendsMessage() {
        return friendsMessage;
    }

    public void setFriendsAvatar(HashMap<String, String> friendsAvatar) {
        this.friendsAvatar = friendsAvatar;
    }

    public void setFriendsMessage(HashMap<String, List<ChatMessageData>> friendsMessage) {
        this.friendsMessage = friendsMessage;
    }

    public void setFriendsName(List<String> friendsName) {
        this.friendsName = friendsName;
    }

    public void setUserAvatarPath(String userAvatarPath) {
        this.userAvatarPath = userAvatarPath;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
