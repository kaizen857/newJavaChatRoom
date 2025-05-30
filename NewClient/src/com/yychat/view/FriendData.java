package com.yychat.view;

import java.util.ArrayList;
import java.util.List;

public class FriendData {
    String name;
    String avatarImagePath;
    String lastMessage;
    String lastMessageDate;
    List<ChatMessageData> chatHistory;

    public FriendData() {
        chatHistory = new ArrayList<>();
    }

    public FriendData(String name, String avatarImagePath, String lastMessage, String lastMessageDate) {
        this.name = name;
        this.avatarImagePath = avatarImagePath;
        this.lastMessage = lastMessage;
        this.lastMessageDate = lastMessageDate;
        this.chatHistory = new ArrayList<>();
    }

    public FriendData(String name, String avatarImagePath, String lastMessage, String lastMessageDate,List<ChatMessageData> chatHistory) {
        this.name = name;
        this.avatarImagePath = avatarImagePath;
        this.lastMessage = lastMessage;
        this.lastMessageDate = lastMessageDate;
        this.chatHistory = chatHistory;
    }

    public String getName(){
        return name;
    }
    public void setName(String name){
        this.name = name;
    }

    public void setAvatarImagePath(String avatarImagePath) {
        this.avatarImagePath = avatarImagePath;
    }

    public void setChatHistory(List<ChatMessageData> chatHistory) {
        this.chatHistory = chatHistory;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public void setLastMessageDate(String lastMessageDate) {
        this.lastMessageDate = lastMessageDate;
    }

    public String getAvatarImagePath() {
        return avatarImagePath;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public String getLastMessageDate() {
        return lastMessageDate;
    }

    public List<ChatMessageData> getChatHistory() {
        return chatHistory;
    }
}
