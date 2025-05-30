import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.border.*;
import java.util.ArrayList;

public class FixedQQChatApp extends JFrame {
    // 颜色常量
    private static final Color MAIN_BG_COLOR = new Color(240, 240, 240);
    private static final Color FRIEND_LIST_BG = new Color(230, 230, 230);
    private static final Color CHAT_WINDOW_BG = Color.WHITE;
    private static final Color ONLINE_STATUS = new Color(0, 180, 0);
    private static final Color OFFLINE_STATUS = Color.GRAY;
    private static final Color SENDER_BUBBLE_COLOR = new Color(0, 153, 255);
    private static final Color RECEIVER_BUBBLE_COLOR = new Color(229, 229, 229);

    // 数据模型
    private Map<String, Friend> friends = new HashMap<>();
    private String currentChatFriend = "张三";
    private JPanel chatContentPanel;
    private JLabel chatHeaderLabel;
    private JLabel myAvatarLabel;
    private JScrollPane chatScrollPane;

    // 统一头像尺寸
    private static final int AVATAR_SIZE = 40;
    private static final Dimension AVATAR_DIMENSION = new Dimension(AVATAR_SIZE, AVATAR_SIZE);

    public FixedQQChatApp() {
        initializeFriendsData();

        setTitle("QQ风格聊天应用 - 修复版");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 650);
        setLocationRelativeTo(null);

        // 主面板使用GridBagLayout
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(MAIN_BG_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();

        // 左侧好友列表面板
        JPanel friendListPanel = createFriendListPanel();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridheight = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0.35;
        gbc.weighty = 1.0;
        mainPanel.add(friendListPanel, gbc);

        // 右侧上部 - 聊天对象信息
        JPanel chatHeaderPanel = createChatHeaderPanel();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridheight = 1;
        gbc.weightx = 0.65;
        gbc.weighty = 0.07;
        mainPanel.add(chatHeaderPanel, gbc);

        // 右侧中部 - 聊天内容区域
        chatContentPanel = createChatContentPanel();
        chatScrollPane = new JScrollPane(chatContentPanel);
        chatScrollPane.setBorder(null);
        chatScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        chatScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weighty = 0.8;
        mainPanel.add(chatScrollPane, gbc);

        // 右侧下部 - 消息输入区域
        JPanel messageInputPanel = createMessageInputPanel();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.weighty = 0.13;
        mainPanel.add(messageInputPanel, gbc);

        add(mainPanel);

        // 创建自己的头像
        myAvatarLabel = createAvatarLabel('我');

        // 初始显示第一个好友的聊天记录
        updateChatDisplay(currentChatFriend);
    }

    private void initializeFriendsData() {
        // 使用ArrayList替代Arrays.asList创建的不可变列表
        friends.put("张三", new Friend("张三", ONLINE_STATUS, new ArrayList<>(Arrays.asList(
                new ChatMessage("张三", "你好，最近怎么样？", "10:30", false),
                new ChatMessage("我", "还不错，你呢？", "10:31", true),
                new ChatMessage("张三", "我也挺好的，周末有空一起吃饭吗？", "10:32", false)
        ))));

        friends.put("李四", new Friend("李四", OFFLINE_STATUS, new ArrayList<>(Arrays.asList(
                new ChatMessage("李四", "明天会议别忘了", "昨天", false),
                new ChatMessage("我", "已经准备好了PPT", "昨天", true)
        ))));

        friends.put("王五", new Friend("王五", ONLINE_STATUS, new ArrayList<>(Arrays.asList(
                new ChatMessage("王五", "文件已发送", "15:20", false),
                new ChatMessage("我", "收到了，谢谢", "15:21", true)
        ))));
    }

    private JPanel createFriendListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(FRIEND_LIST_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // 搜索框
        JTextField searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(0, 35));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        searchField.putClientProperty("JTextField.placeholderText", "搜索好友、群聊");

        // 好友列表
        JPanel friendList = new JPanel();
        friendList.setLayout(new BoxLayout(friendList, BoxLayout.Y_AXIS));
        friendList.setBackground(FRIEND_LIST_BG);

        // 添加好友项
        for (Friend friend : friends.values()) {
            addFriendItem(friendList, friend);
        }

        JScrollPane scrollPane = new JScrollPane(friendList);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        panel.add(searchField, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void addFriendItem(JPanel panel, Friend friend) {
        JPanel itemPanel = new JPanel();
        itemPanel.setLayout(new BorderLayout());
        itemPanel.setBackground(FRIEND_LIST_BG);
        itemPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        itemPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        // 头像
        JLabel avatar = createAvatarLabel(friend.name.charAt(0));

        // 名称和最新消息
        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setBackground(FRIEND_LIST_BG);
        GridBagConstraints gbc = new GridBagConstraints();

        // 名称
        JLabel nameLabel = new JLabel(friend.name);
        nameLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1.0;
        infoPanel.add(nameLabel, gbc);

        // 时间标签
        String lastTime = friend.messages.isEmpty() ? "" : friend.messages.get(friend.messages.size()-1).time;
        JLabel timeLabel = new JLabel(lastTime);
        timeLabel.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        timeLabel.setForeground(Color.GRAY);
        gbc.gridx = 1;
        gbc.weightx = 0.0;
        gbc.insets = new Insets(0, 10, 0, 0);
        infoPanel.add(timeLabel, gbc);

        // 最新消息预览
        String lastMsg = friend.messages.isEmpty() ? "" : friend.messages.get(friend.messages.size()-1).content;
        JLabel previewLabel = new JLabel(lastMsg);
        previewLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        previewLabel.setForeground(Color.DARK_GRAY);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(5, 0, 0, 0);
        infoPanel.add(previewLabel, gbc);

        // 状态指示器
        JPanel statusIndicator = new JPanel();
        statusIndicator.setPreferredSize(new Dimension(10, 10));
        statusIndicator.setBackground(friend.statusColor);
        statusIndicator.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(FRIEND_LIST_BG);
        rightPanel.add(statusIndicator, BorderLayout.NORTH);

        itemPanel.add(avatar, BorderLayout.WEST);
        itemPanel.add(infoPanel, BorderLayout.CENTER);
        itemPanel.add(rightPanel, BorderLayout.EAST);

        // 添加鼠标点击事件
        itemPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // 切换当前聊天好友
                currentChatFriend = friend.name;
                chatHeaderLabel.setText(friend.name);
                updateChatDisplay(friend.name);

                // 更新选中状态
                for (Component comp : panel.getComponents()) {
                    if (comp instanceof JPanel) {
                        comp.setBackground(FRIEND_LIST_BG);
                    }
                }
                itemPanel.setBackground(new Color(210, 210, 210));
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                itemPanel.setBackground(new Color(210, 210, 210));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!friend.name.equals(currentChatFriend)) {
                    itemPanel.setBackground(FRIEND_LIST_BG);
                }
            }
        });

        panel.add(itemPanel);
    }

    private JPanel createChatHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CHAT_WINDOW_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));

        // 聊天对象信息
        chatHeaderLabel = new JLabel(currentChatFriend);
        chatHeaderLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));

        panel.add(chatHeaderLabel, BorderLayout.WEST);
        return panel;
    }

    private JPanel createChatContentPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(CHAT_WINDOW_BG);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        return panel;
    }

    private void updateChatDisplay(String friendName) {
        chatContentPanel.removeAll();

        // 添加历史消息
        Friend friend = friends.get(friendName);
        if (friend != null) {
            for (ChatMessage message : friend.messages) {
                addMessageBubble(chatContentPanel, message.sender, message.content, message.isMe);
            }
        }

        // 添加弹性空间使消息从底部开始
        chatContentPanel.add(Box.createVerticalGlue());

        chatContentPanel.revalidate();
        chatContentPanel.repaint();

        // 滚动到底部
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = chatScrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    private void addMessageBubble(JPanel panel, String sender, String message, boolean isMe) {
        JPanel messagePanel = new JPanel();
        messagePanel.setLayout(new BorderLayout());
        messagePanel.setBackground(CHAT_WINDOW_BG);
        messagePanel.setBorder(new EmptyBorder(5, 5, 5, 5));

        // 头像面板 (严格固定大小)
        JPanel avatarPanel = new JPanel(new BorderLayout());
        avatarPanel.setPreferredSize(AVATAR_DIMENSION);
        avatarPanel.setMaximumSize(AVATAR_DIMENSION);
        avatarPanel.setBackground(CHAT_WINDOW_BG);

        JLabel avatar = isMe ? myAvatarLabel : createAvatarLabel(sender.charAt(0));
        avatarPanel.add(avatar, BorderLayout.CENTER);

        // 使用JTextArea实现消息气泡
        JTextArea bubble = new JTextArea(message);
        bubble.setLineWrap(true);
        bubble.setWrapStyleWord(true);
        bubble.setEditable(false);
        bubble.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        bubble.setBorder(BorderFactory.createCompoundBorder(
                new RoundBorder(isMe ? SENDER_BUBBLE_COLOR : RECEIVER_BUBBLE_COLOR, 1, 15),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        bubble.setBackground(isMe ? SENDER_BUBBLE_COLOR : RECEIVER_BUBBLE_COLOR);
        bubble.setForeground(isMe ? Color.WHITE : Color.BLACK);

        // 设置气泡样式
        if (isMe) {
            // 发送者消息（右侧）
            JPanel rightPanel = new JPanel(new BorderLayout());
            rightPanel.setBackground(CHAT_WINDOW_BG);
            rightPanel.add(bubble, BorderLayout.CENTER);

            messagePanel.add(rightPanel, BorderLayout.CENTER);
            messagePanel.add(avatarPanel, BorderLayout.EAST);
        } else {
            // 接收者消息（左侧）
            JPanel leftPanel = new JPanel(new BorderLayout());
            leftPanel.setBackground(CHAT_WINDOW_BG);
            leftPanel.add(bubble, BorderLayout.CENTER);

            messagePanel.add(avatarPanel, BorderLayout.WEST);
            messagePanel.add(leftPanel, BorderLayout.CENTER);
        }

        panel.add(messagePanel);
    }

    private JLabel createAvatarLabel(char initial) {
        JLabel avatar = new JLabel(String.valueOf(initial), SwingConstants.CENTER);
        avatar.setPreferredSize(AVATAR_DIMENSION);
        avatar.setMinimumSize(AVATAR_DIMENSION);
        avatar.setMaximumSize(AVATAR_DIMENSION);
        avatar.setOpaque(true);
        avatar.setBackground(getRandomColor());
        avatar.setForeground(Color.WHITE);
        avatar.setFont(new Font("微软雅黑", Font.BOLD, 16));
        avatar.setBorder(new RoundBorder(Color.WHITE, 2, AVATAR_SIZE/2));
        return avatar;
    }

    private Color getRandomColor() {
        Color[] colors = {
                new Color(0, 153, 255),  // 蓝色
                new Color(255, 102, 0),  // 橙色
                new Color(51, 153, 51),   // 绿色
                new Color(153, 51, 153),  // 紫色
                new Color(255, 51, 51)     // 红色
        };
        return colors[(int)(Math.random() * colors.length)];
    }

    private JPanel createMessageInputPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CHAT_WINDOW_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        // 工具栏
        JPanel toolbarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        toolbarPanel.setBackground(CHAT_WINDOW_BG);

        JButton emojiBtn = createToolButton("😊");
        JButton imageBtn = createToolButton("📷");
        JButton fileBtn = createToolButton("📁");
        JButton screenShotBtn = createToolButton("✂️");

        toolbarPanel.add(emojiBtn);
        toolbarPanel.add(imageBtn);
        toolbarPanel.add(fileBtn);
        toolbarPanel.add(screenShotBtn);

        // 输入框
        JTextArea inputArea = new JTextArea();
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        inputArea.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        inputArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        JScrollPane inputScroll = new JScrollPane(inputArea);
        inputScroll.setBorder(null);

        // 发送按钮
        JButton sendBtn = new JButton("发送");
        sendBtn.setFont(new Font("微软雅黑", Font.BOLD, 14));
        sendBtn.setForeground(Color.WHITE);
        sendBtn.setBackground(new Color(0, 120, 255));
        sendBtn.setFocusPainted(false);
        sendBtn.setBorder(new RoundBorder(new Color(0, 100, 220), 1, 15));
        sendBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // 底部面板
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(CHAT_WINDOW_BG);
        bottomPanel.add(toolbarPanel, BorderLayout.WEST);
        bottomPanel.add(sendBtn, BorderLayout.EAST);

        panel.add(inputScroll, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        // 发送按钮事件
        sendBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String message = inputArea.getText().trim();
                if (!message.isEmpty()) {
                    // 更新当前好友的消息记录
                    Friend friend = friends.get(currentChatFriend);
                    if (friend != null) {
                        // 使用ArrayList确保可以添加新消息
                        friend.messages.add(new ChatMessage("我", message, "刚刚", true));

                        // 添加消息到聊天区域
                        addMessageBubble(chatContentPanel, "我", message, true);

                        inputArea.setText("");

                        // 滚动到底部
                        SwingUtilities.invokeLater(() -> {
                            JScrollBar vertical = chatScrollPane.getVerticalScrollBar();
                            vertical.setValue(vertical.getMaximum());
                        });
                    }
                }
            }
        });

        return panel;
    }

    private JButton createToolButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(230, 230, 230));
                button.setOpaque(true);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(null);
                button.setOpaque(false);
            }
        });

        return button;
    }

    // 圆角边框类
    class RoundBorder extends AbstractBorder {
        private Color color;
        private int thickness;
        private int radius;

        public RoundBorder(Color color, int thickness, int radius) {
            this.color = color;
            this.thickness = thickness;
            this.radius = radius;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(thickness));
            g2.drawRoundRect(x, y, width-1, height-1, radius, radius);
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(radius, radius, radius, radius);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.left = insets.right = insets.bottom = insets.top = radius;
            return insets;
        }
    }

    // 好友数据类
    class Friend {
        String name;
        Color statusColor;
        ArrayList<ChatMessage> messages;

        Friend(String name, Color statusColor, ArrayList<ChatMessage> messages) {
            this.name = name;
            this.statusColor = statusColor;
            this.messages = messages;
        }
    }

    // 聊天消息类
    class ChatMessage {
        String sender;
        String content;
        String time;
        boolean isMe;

        ChatMessage(String sender, String content, String time, boolean isMe) {
            this.sender = sender;
            this.content = content;
            this.time = time;
            this.isMe = isMe;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // 设置系统外观
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            FixedQQChatApp app = new FixedQQChatApp();
            app.setVisible(true);
        });
    }
}