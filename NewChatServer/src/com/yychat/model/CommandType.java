package com.yychat.model;

public enum CommandType {
    // 用户登录请求 (1|userName|password)
    LOGIN_REQUEST(1),

    // 用户登录请求ACK (2|userName|status)
    LOGIN_RESPONSE(2),
    //注册请求 （11|userName|password）
    REGISTER_REQUEST(11),
    //注册请求ACK （16|userName|status）
    REGISTER_RESPONSE(16),

    // 发送新好友请求 (3|userName|friendName)
    FRIEND_REQUEST(3),

    // 好友同意请求 (4|userName|friendName|status|ImageIcon)
    NEW_FRIEND_CONFIRM(4),

    // 查询所有在线的好友 (5|userName)
    QUERY_ONLINE_FRIENDS(5),

    // 查询所有在线的好友ACK (6|userName|onlineFriend)
    QUERY_ONLINE_FRIENDS_RESPONSE(6),

    // 登出 (-1|userName)
    LOGOUT(-1),

    // 发送消息 (7|userName|friendName|content|time)
    SEND_MESSAGE(7),
    //发送图片消息（20|sender|receiver|Image）
    SEND_IMAGE_MESSAGE(20),

    QUERY_HAS_USER(23),

    QUERY_HAS_USER_ACK(24),

    CHANGE_PASSWORD(25),

    CHANGE_PASSWORD_RESPONSE(26),

    //发送信息ACK(13|userName|status|time)
    SEND_MESSAGE_RESPONSE(13),

    // 通用ACK信号 (8|userName|ack)
    GENERAL_ACK(8),

    // 请求聊天记录 (9|userName|friendName|lastTime)
    REQUEST_CHAT_HISTORY(9),
    //请求聊天记录回应(12|userName|contentNumber)
    REQUEST_CHAT_HISTORY_ACK(12),
    //聊天记录信息(17|sender|receiver|content|timestamp)
    REQUEST_CHAT_HISTORY_CONTENT(17),

    //请求好友头像资源(18|userName|friendName)
    QUERY_FRIEND_AVATAR(18),

    //请求好友头像资源应答(19|userName|image)
    QUERY_FRIEND_AVATAR_ACK(19),
    //更改用户头像(21|username|image)
    CHANGE_USER_AVATAR(21),
    //更改用户头像ACK(22|userName|status)
    CHANGE_USER_AVATAR_ACK(22),

    //查询该用户的所有好友(14|userName)
    QUERY_ALL_FRIENDS(14),

    //查询该用户的所有好友ACK(15|userName|friendList)
    QUERY_ALL_FRIENDS_RESPONSE(15),

    // 回应好友请求 (10|userName|friendName|status)
    FRIEND_RESPOND(10);



    private final int commandCode;

    CommandType(int commandCode) {
        this.commandCode = commandCode;
    }

    public int getCommandCode() {
        return commandCode;
    }

    /**
     * 根据命令码获取对应的枚举值
     * @param code 命令码
     * @return 对应的CommandType枚举
     * @throws IllegalArgumentException 如果找不到对应的命令码
     */
    public static CommandType fromCode(int code) {
        for (CommandType cmd : values()) {
            if (cmd.commandCode == code) {
                return cmd;
            }
        }
        throw new IllegalArgumentException("未知的命令码: " + code);
    }

    /**
     * 检查命令码是否有效
     * @param code 要检查的命令码
     * @return 如果有效返回true，否则返回false
     */
    public static boolean isValidCommand(int code) {
        for (CommandType cmd : values()) {
            if (cmd.commandCode == code) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取命令的最小参数数量(不包括命令码本身)
     * @param command 业务命令
     * @return 该命令所需的最小参数数量
     */
    public static int getMinParamCount(CommandType command) {
        switch (command) {
            case LOGIN_REQUEST:
            case LOGIN_RESPONSE:
            case FRIEND_REQUEST:
            case QUERY_ONLINE_FRIENDS:
                return 2;
            case LOGOUT:
            case QUERY_ONLINE_FRIENDS_RESPONSE:
            case GENERAL_ACK:
                return 0;
            case NEW_FRIEND_CONFIRM:
            case SEND_MESSAGE:
            case REQUEST_CHAT_HISTORY:
            case FRIEND_RESPOND:
                return 3;
            default:
                return 1;
        }
    }
}