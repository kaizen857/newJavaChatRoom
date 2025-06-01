package com.yychat.control;

import cn.hutool.core.codec.Base64;
import cn.hutool.crypto.digest.DigestUtil;

import javax.xml.bind.DatatypeConverter;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class DBUtil {
    private static final String db_url = "jdbc:mysql://localhost:3306/yychat2025s?useUnicode=true&characterEncoding=utf-8";
    private static final String db_user = "root";
    private static final String db_pass = "Xwl20050219";
    private static Connection dataBase = connectDB();

    private static Connection connectDB() {
        Connection tmp = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            tmp = DriverManager.getConnection(db_url, db_user, db_pass);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tmp;
    }

    public static boolean loginValidate(String userName, String passwordSHA256) {
        boolean loginSuccess = false;
        String query = "select password_hash,salt from user where username=?";
        PreparedStatement statement = null;
        try {
            statement = dataBase.prepareStatement(query);
            statement.setString(1, userName);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                byte[] storedSHA256 = rs.getBytes("password_hash");
                byte[] storedSalt = rs.getBytes("salt");
                byte[] passwordHash = DatatypeConverter.parseHexBinary(passwordSHA256);
                byte[] mixedPassword = new byte[storedSalt.length + passwordHash.length];
                System.arraycopy(storedSalt, 0, mixedPassword, 0, storedSalt.length);
                System.arraycopy(passwordHash, 0, mixedPassword, storedSalt.length, passwordHash.length);
                byte[] userPasswordHash = DigestUtil.sha256(mixedPassword);
                if (Arrays.equals(storedSHA256, userPasswordHash)) {
                    loginSuccess = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return loginSuccess;
    }

    public static boolean hasUser(String user) {
        boolean hasUser = false;
        String query = "select * from user where username=?";
        PreparedStatement statement = null;
        try {
            statement = dataBase.prepareStatement(query);
            statement.setString(1, user);
            ResultSet rs = statement.executeQuery();
            hasUser = rs.next();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return hasUser;
    }

    //添加新用户
    public static int addNewUser(String userName, String password, byte[] salt) {
        int result = -1;
        String insert = "insert into user(username,password_hash,salt) values(?,?,?)";
        PreparedStatement statement;
        try {
            statement = dataBase.prepareStatement(insert);
            statement.setString(1, userName);
            byte[] passwordBytes = DatatypeConverter.parseHexBinary(password);
            byte[] passwordMixSaltBytes = new byte[salt.length + passwordBytes.length];
            System.arraycopy(salt, 0, passwordMixSaltBytes, 0, salt.length);
            System.arraycopy(passwordBytes, 0, passwordMixSaltBytes, salt.length, passwordBytes.length);
            byte[] passwordHash = DigestUtil.sha256(passwordMixSaltBytes);
            statement.setBytes(2, passwordHash);
            statement.setBytes(3, salt);
            result = statement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String getAllFriends(String userName, int friendType) {
        StringBuilder builder = new StringBuilder();
        String query = "SELECT slaveUser FROM userRelation WHERE masterUser=? AND relation=?";

        try (PreparedStatement statement = dataBase.prepareStatement(query)) {
            statement.setString(1, userName);
            statement.setInt(2, friendType);

            try (ResultSet rs = statement.executeQuery()) {
                boolean isFirst = true; // 标记是否为第一个元素（避免开头多一个"|"）

                while (rs.next()) { // 直接通过 rs.next() 控制循环
                    if (!isFirst) {
                        builder.append("|");
                    }
                    builder.append(rs.getString("slaveUser"));
                    isFirst = false;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return ""; // 返回空字符串或抛出自定义异常
        }

        String result = builder.toString();
        System.out.println(userName + "的全部好友: " + result);
        return result;
    }

    public static boolean isUsersFriend(String userName, String userFriend, int friendType) {
        boolean result = false;
        String query = "select * from userRelation where masterUser=? and slaveUser=? and relation=?";
        PreparedStatement statement = null;
        try {
            statement = dataBase.prepareStatement(query);
            statement.setString(1, userName);
            statement.setString(2, userFriend);
            statement.setInt(3, friendType);
            ResultSet rs = statement.executeQuery();
            result = rs.next();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static int insertIntoFriend(String user, String friendOfUser, int friendType) {
        int count = 0;
        String query = "insert into userRelation(masterUser,slaveUser,relation) values(?,?,?)";
        PreparedStatement statement = null;
        try {
            statement = dataBase.prepareStatement(query);
            statement.setString(1, user);
            statement.setString(2, friendOfUser);
            statement.setInt(3, friendType);
            count = statement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;
    }

    public static boolean insertChatMessage(String from, String to, int isImage, String content, Date time) {
        boolean result = false;
        String query = "insert into message(isImage,from_user,to_user,content,sendtime) values(?,?,?,?,?)";
        PreparedStatement statement = null;
        try {
            statement = dataBase.prepareStatement(query);
            statement.setString(2, from);
            statement.setString(3, to);
            statement.setInt(1, isImage);
            statement.setString(4, content);
            statement.setTimestamp(5, new java.sql.Timestamp(time.getTime()));
            result = (statement.executeUpdate() > 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static List<String> getChatMessage(String userName, String friendName, Date time) {
        // SQL 查询：获取两人在指定时间之后的聊天记录（按时间升序排列）
        String sql = "SELECT from_user, to_user, content, sendtime FROM message " +
                "WHERE ((from_user = ? AND to_user = ?) OR (from_user = ? AND to_user = ?)) " +
                "AND sendtime >= ? " +
                "ORDER BY sendtime ASC";

        List<String> messages = new ArrayList<>();

        try {
            PreparedStatement stmt = dataBase.prepareStatement(sql);

            stmt.setString(1, userName);
            stmt.setString(2, friendName);
            stmt.setString(3, friendName);
            stmt.setString(4, userName);
            stmt.setTimestamp(5, new Timestamp(time.getTime())); // 设置时间条件
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String fromUser = rs.getString("from_user");
                    String toUser = rs.getString("to_user");
                    String content = rs.getString("content");
                    Timestamp sendTime = rs.getTimestamp("sendTime");

                    // 格式化为 "userName|friendName|content|sendTime"
                    String formattedMessage = String.format("%s|%s|%s|%s",
                            fromUser, toUser, content, sendTime.toString());
                    messages.add(formattedMessage);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return new ArrayList<>(); // 出错返回空数组
            }


            return messages;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<String> getChatMessage(String userName, String friendName) {
        // SQL 查询：获取两人之间的所有聊天记录（按时间升序排列）
        String sql = "SELECT isImage, from_user, to_user, content, sendtime FROM message " +
                "WHERE (from_user = ? AND to_user = ?) OR (from_user = ? AND to_user = ?) " +
                "ORDER BY sendtime ASC";
        List<String> messages = new ArrayList<>();
        try {
            PreparedStatement stmt = dataBase.prepareStatement(sql);
            stmt.setString(1, userName);
            stmt.setString(2, friendName);
            stmt.setString(3, friendName);
            stmt.setString(4, userName);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int isImage = rs.getInt("isImage");
                    String fromUser = rs.getString("from_user");
                    String toUser = rs.getString("to_user");
                    String content = rs.getString("content");
                    Timestamp sendTime = rs.getTimestamp("sendTime");
                    if (isImage == 0) {
                        String contentBase64 = Base64.encode(content);
                        // 格式化为 "sender|receiver|isImage|content|sendTime"
                        String formattedMessage = String.format("%s|%s|%d|%s|%d",
                                fromUser, toUser, isImage, contentBase64, sendTime.getTime());
                        messages.add(formattedMessage);
                    } else {
                        String formattedMessage = String.format("%s|%s|%d|%s|%d",
                                fromUser, toUser, isImage, content, sendTime.getTime());
                        messages.add(formattedMessage);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return new ArrayList<>(); // 出错返回空数组
            }
            return messages;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static Timestamp getLatestMessageTime(String userName, String friendName) {
        String sql = "SELECT MAX(sendtime) AS latest_time FROM message " +
                "WHERE (from_user = ? AND to_user = ?) OR (from_user = ? AND to_user = ?)";

        try {
            PreparedStatement stmt = dataBase.prepareStatement(sql);
            stmt.setString(1, userName);
            stmt.setString(2, friendName);
            stmt.setString(3, friendName);
            stmt.setString(4, userName);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getTimestamp("latest_time"); // 返回最新的时间戳
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null; // 无记录时返回 null
    }

    public static String getUserAvatarPath(String userName) {
        String sql = "SELECT avatarPath FROM userAvatarPath WHERE username = ?";
        try {
            PreparedStatement stmt = dataBase.prepareStatement(sql);
            stmt.setString(1, userName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("avatarPath");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return "";
    }

    public static boolean updateUserAvatarPath(String userName, String path) {
        String sql = "UPDATE userAvatarPath SET avatarPath = ? WHERE username = ?";
        try {
            PreparedStatement stmt = dataBase.prepareStatement(sql);
            stmt.setString(1, path);
            stmt.setString(2, userName);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean addNewUserAvatarPath(String userName, String path) {
        String sql = "INSERT INTO userAvatarPath(username, avatarPath) VALUES(?, ?)";
        try {
            PreparedStatement stmt = dataBase.prepareStatement(sql);
            stmt.setString(1, userName);
            stmt.setString(2, path);
            boolean result = stmt.executeUpdate() > 0;
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
