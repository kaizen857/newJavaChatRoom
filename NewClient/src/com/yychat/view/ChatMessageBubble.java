package com.yychat.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.SimpleDateFormat;

class ChatMessageBubble extends JPanel {
    private final JTextPane messagePane;
    private final JLabel timeLabel;
    private final RoundedAvatar avatar;

    public ChatMessageBubble(ChatMessageData data) {
        boolean isSelf = data.senderType == ChatMessageData.SenderType.SELF;

        setLayout(new GridBagLayout());
        setOpaque(false);
        setBorder(new EmptyBorder(5, 10, 5, 10));

        avatar = new RoundedAvatar(30, data.avatarImagePath);

        messagePane = new JTextPane();
        messagePane.setContentType("text/html");
        messagePane.setEditable(false);
        messagePane.setOpaque(true);
        messagePane.setFocusable(true);

        if(data.isCommonMessage){
            String htmlContent = "<html><head><style type='text/css'>"
                    + "body { margin:0; padding:0; font-family: " + getFont().getFamily() + "; font-size: " + getFont().getSize() + "pt; }"
                    + "p { width: 200px; margin:0; padding:0; word-wrap: break-word; overflow-wrap: break-word; }"
                    + "</style></head><body><p>"
                    + escapeHtml(data.message)
                    + "</p></body></html>";
            messagePane.setText(htmlContent);
            messagePane.setMargin(new Insets(8, 10, 8, 10));
        }
        else{
            //图像
            messagePane.insertIcon(new ImageIcon(data.message));
            messagePane.setMargin(new Insets(8, 10, 8, 10));
        }


        timeLabel = new JLabel();
        timeLabel.setFont(UIManager.getFont("Label.smallFont"));
        timeLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
        // --- MOD: 设置时间并使其一直可见 ---
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm"); // 您可以根据需要更改日期格式，例如 "MM-dd HH:mm"
        timeLabel.setText(sdf.format(data.timestamp));
        timeLabel.setVisible(true); // 确保时间标签是可见的 (JLabel 默认可见, 所以这行其实可以省略)
        // --- END MOD ---

        GridBagConstraints gbc = new GridBagConstraints();
        if (isSelf) {
            messagePane.setBackground(new Color(0xAD, 0xD8, 0xE6, 180));
            gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1.0; gbc.anchor = GridBagConstraints.EAST;
            add(timeLabel, gbc); // timeLabel 在消息上方

            gbc.gridy = 1;
            add(messagePane, gbc);

            gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 0; gbc.insets = new Insets(0, 5, 0, 0); gbc.anchor = GridBagConstraints.NORTHEAST;
            add(avatar, gbc);
        } else {
            messagePane.setBackground(UIManager.getColor("TextPane.background"));
            gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0; gbc.insets = new Insets(0, 0, 0, 5); gbc.anchor = GridBagConstraints.NORTHWEST;
            add(avatar, gbc);

            gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0; gbc.anchor = GridBagConstraints.WEST;
            add(timeLabel, gbc); // timeLabel 在消息上方

            gbc.gridy = 1;
            add(messagePane, gbc);
        }
    }
    private String escapeHtml(String text) {
        if (text == null) return "";
        // 基本的HTML转义，确保纯文本正确显示
        // \n 转 <br> 用于在HTML中换行
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;")
                .replace("\n", "<br>");
    }
}