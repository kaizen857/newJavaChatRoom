package com.yychat.model;

import java.util.Date;

public class ChatMessageData {
    private String sender;
    private String receiver;
    private String message;
    private Date timestamp;
    private String avatarImagePath;

    public ChatMessageData(String sender,String receiver,String message, Date timestamp, String avatarImagePath) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.timestamp = timestamp;
        this.avatarImagePath = avatarImagePath;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public void setAvatarImagePath(String avatarImagePath) {
        this.avatarImagePath = avatarImagePath;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public String getAvatarImagePath() {
        return avatarImagePath;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getMessage() {
        return message;
    }
}
