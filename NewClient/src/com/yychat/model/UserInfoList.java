package com.yychat.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UserInfoList implements Serializable {
    private final List<String> userNames = new ArrayList<>();
    private final List<String> passwords = new ArrayList<>();
    private final HashMap<String,String> userIconPath =  new HashMap<>();
    private String lastUsedName;

    public void addUserInfo(String userName,String password,String userIconPath){
        userNames.add(userName);
        passwords.add(password);
        this.userIconPath.put(userName,userIconPath);
    }

    public String getUserIconPath(String userName){
        return userIconPath.get(userName);
    }

    public String getLastUsedName() {
        return lastUsedName;
    }

    public void setLastUsedName(String lastUsedName) {
        this.lastUsedName = lastUsedName;
    }
}
