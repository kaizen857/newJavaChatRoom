package com.yychat.view;

import java.util.Date;

public class ChatMessageData {
    enum SenderType {SELF, OTHER}

    SenderType senderType;
    String message;
    Date timestamp;
    String avatarImagePath;

    public ChatMessageData(boolean senderType, String message, Date timestamp, String avatarImagePath) {
        this.senderType = senderType ? SenderType.SELF : SenderType.OTHER;
        this.message = message;
        this.timestamp = timestamp;
        this.avatarImagePath = avatarImagePath;
    }

    public String getMessage() {
        return message;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getAvatarImagePath() {
        return avatarImagePath;
    }

    public SenderType getSenderType() {
        return senderType;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public void setSenderType(SenderType senderType) {
        this.senderType = senderType;
    }

    public void setAvatarImagePath(String avatarImagePath) {
        this.avatarImagePath = avatarImagePath;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}