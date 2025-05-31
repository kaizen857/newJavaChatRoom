package com.yychat.view;

import cn.hutool.core.codec.Base64;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.yychat.control.ImageLoaderUtil;
import com.yychat.control.MessageHandler;
import com.yychat.model.UserInfoList;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ChatUI extends JFrame {

    private final ConcurrentHashMap<String, FriendListPanel.FriendEntry> friendEntryMap = new ConcurrentHashMap<>();
    private final File friendsInfoFile = new File("./friendsInfo.dat");
    private AccountPanel accountPanel;
    private FriendListPanel friendListPanel;
    private ChatPanel chatPanel;
    private FriendData currentUser;
    private UserInfoList userInfoList;
    private MessageHandler messageHandler;
    private ConcurrentHashMap<String, FriendData> myFriends;

    public ChatUI(UserInfoList userInfoList) {
        //UI初始化
        messageHandler = MessageHandler.getInstance(userInfoList.getLastUsedName());
        if(!messageHandler.getClientUserName().equals(userInfoList.getLastUsedName())) {
            messageHandler.setClientUserName(userInfoList.getLastUsedName());
        }
        messageHandler.setChatUI(this);
        String userAvatarPath = messageHandler.getFriendAvatar(userInfoList.getLastUsedName(), userInfoList.getLastUsedName());
        currentUser = new FriendData(userInfoList.getLastUsedName(), userAvatarPath, "", "");

        this.userInfoList = userInfoList;
        setTitle(currentUser.name);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700); // Slightly larger for better initial view
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        accountPanel = new AccountPanel(this, currentUser);
        friendListPanel = new FriendListPanel(this);
        chatPanel = new ChatPanel(this, currentUser);

        accountPanel.setMinimumSize(new Dimension(85, 0)); // Based on 60px avatar + 2*10px insets + border
        friendListPanel.setMinimumSize(new Dimension(200, 0));
        chatPanel.setMinimumSize(new Dimension(350, 0));

        JSplitPane leftSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, accountPanel, friendListPanel);
        leftSplitPane.setUI(new InvisibleSplitPaneUI());
        leftSplitPane.setContinuousLayout(true);
        leftSplitPane.setOneTouchExpandable(false);
        leftSplitPane.setDividerSize(7);
        leftSplitPane.setDividerLocation(accountPanel.getMinimumSize().width + 2);
        leftSplitPane.setResizeWeight(0.0);

        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftSplitPane, chatPanel);
        mainSplitPane.setUI(new InvisibleSplitPaneUI());
        mainSplitPane.setContinuousLayout(true);
        mainSplitPane.setOneTouchExpandable(false);
        mainSplitPane.setDividerSize(7);
        mainSplitPane.setDividerLocation((int) (getWidth() * 0.35));
        mainSplitPane.setResizeWeight(0.3);

        add(mainSplitPane, BorderLayout.CENTER);

        myFriends = new ConcurrentHashMap<>();
        List<String> allFriendsList = messageHandler.getAllFriendsName(currentUser.name);
        for (String friendName : allFriendsList) {
            String avatarPath = messageHandler.getFriendAvatar(currentUser.name, friendName);
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            List<ChatMessageData> history = messageHandler.getFriendChatHistory(currentUser.name, friendName);
            if (!history.isEmpty()) {
                String lastMessageContent = history.get(history.size() - 1).getMessage();
                String lastMessageTime = history.get(history.size() - 1).getTimestamp().toString();
                FriendData tmp = new FriendData(friendName, avatarPath, lastMessageContent, lastMessageTime);
                tmp.chatHistory = history;
                myFriends.put(friendName, tmp);

            } else {
                myFriends.put(friendName, new FriendData(friendName, avatarPath, "", ""));
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }


        //查看是否有本地缓存，没有就创建
//        if(friendsInfoFile.exists()) {
//            try(FileInputStream fis = new FileInputStream(friendsInfoFile)) {
//                ObjectInputStream ois = new ObjectInputStream(fis);
//                myFriends = (List<FriendData>) ois.readObject();
//            } catch (IOException | ClassNotFoundException e) {
//                throw new RuntimeException(e);
//            }
//        }
//        else{
//            try {
//                friendsInfoFile.createNewFile();
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        }
//
//        messageHandler = MessageHandler.getInstance(currentUser.name);
//        messageHandler.setChatUI(this);
//
//        if(myFriends == null){
//            myFriends = new ArrayList<>();
//            //TODO:从服务器上获取所有数据
//        }
//        else{
//            //从服务器获取所有好友名字，与本地存储的好友信息比对，查看是否有新增好友
//            //本地的好友名字
//            Set<String> myFriendNames = myFriends.stream()
//                    .map(FriendData::getName)
//                    .collect(Collectors.toSet());
//            //服务器上的好友名字
//            List<String> friendsListInServer = messageHandler.getAllFriendsName(currentUser.name);
//            //新的好友名字
//            List<String> newFriends = friendsListInServer.stream()
//                    .filter(name -> !myFriendNames.contains(name))
//                    .collect(Collectors.toList());
//            for(String name : newFriends){
//                //添加好友到本地缓存，同时向服务器请求好友头像
//                FriendData friend = new FriendData(name, "/avatars/default_avatar.png", "", "");
//                friend.avatarImagePath = messageHandler.getFriendAvatar(currentUser.name, name);
//                if(friend.avatarImagePath == null){
//                    System.err.println("服务器返回数据错误！（在好友头像）");
//                }
//            }
//        }

        //通过本地缓存的历史记录里的最后更新时间与服务器上的最后更新时间比较
        //来更新本地缓存


        friendListPanel.loadFriends(new ArrayList<>(myFriends.values()));//加载好友列表
        chatPanel.setActiveChat(null);
    }

    public static void main(String[] args) {
        // Apply FlatLaf theme
        try {
            // You can try other themes like FlatDarkLaf, FlatIntelliJLaf, FlatMacLightLaf, etc.
            // For example: com.formdev.flatlaf.themes.FlatMacLightLaf.setup();
            FlatLaf.setup(new FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("Failed to initialize FlatLaf: " + ex.getMessage());
            // Fallback to system L&F if FlatLaf fails
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        SwingUtilities.invokeLater(() -> {
            new ChatUI(null).setVisible(true);
        });
    }

    public void switchChatSession(FriendData friend) {
        this.chatPanel.setActiveChat(friend);
    }

    public FriendData getCurrentUser() {
        return currentUser;
    }

    public FriendData getCurrentChatFriend() {
        return this.chatPanel.getCurrentChatFriend();
    }

    public void newFriend(FriendData friend) {
        myFriends.put(friend.name, friend);
        friendListPanel.addFriendToFriendList(friend);
    }

    public void newChatMessage(String sender, String content, Date sendTime) {
        if (getCurrentChatFriend().getName().equals(sender)) {
            //是当前正在聊天的好友
            getCurrentChatFriend().getChatHistory().add(new ChatMessageData(false, true, content, sendTime, getCurrentChatFriend().getAvatarImagePath()));
            this.chatPanel.addMessageBubble(new ChatMessageData(false, true, content, sendTime, getCurrentChatFriend().getAvatarImagePath()));
        } else {
            //不是当前正在聊天的好友
            FriendData friendData = myFriends.get(sender);
            friendData.getChatHistory().add(new ChatMessageData(false, true, content, sendTime, getCurrentChatFriend().getAvatarImagePath()));
        }
        FriendListPanel.FriendEntry entry = friendEntryMap.get(sender);
        String lastMessageContent = content;
        if (content.length() > 10) {
            lastMessageContent = content.substring(0, 10);
        }
        entry.lastMessageLabel.setText(lastMessageContent);
    }

    public void newChatImageMessage(String sender, String contentBase64, Date sendTime) {
        byte[] imageBytes = Base64.decode(contentBase64);
        Path paths = Paths.get(System.getProperty("user.dir"), "images", sendTime.getTime() + ".png");
        try {
            Files.write(paths, imageBytes);
            if (getCurrentChatFriend().getName().equals(sender)) {
                getCurrentChatFriend().getChatHistory().add(new ChatMessageData(false, false, paths.toString(), sendTime, getCurrentChatFriend().getAvatarImagePath()));
                this.chatPanel.addMessageBubble(new ChatMessageData(false, false, paths.toString(), sendTime, getCurrentChatFriend().getAvatarImagePath()));
            }
            else{
                FriendData friendData = myFriends.get(sender);
                friendData.getChatHistory().add(new ChatMessageData(false, false, paths.toString(), sendTime, getCurrentChatFriend().getAvatarImagePath()));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    class AccountPanel extends JPanel {
        ChatUI parent;
        RoundedAvatar avatarLabel;

        public AccountPanel(ChatUI parent, FriendData user) {
            this.parent = parent;
            setLayout(new GridBagLayout());
            setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, UIManager.getColor("Component.borderColor")));

            GridBagConstraints c = new GridBagConstraints();

            avatarLabel = new RoundedAvatar(60, user.avatarImagePath);
            c.gridx = 0;
            c.gridy = 0;
            c.anchor = GridBagConstraints.NORTH;
            c.insets = new Insets(10, 10, 10, 10);
            avatarLabel.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        byte[] image = ImageLoaderUtil.loadImageAndProcess(parent, "选择新的头像");
                        if (image != null) {
                            String imageBase64 = Base64.encode(image);
                            if (messageHandler.changeUserAvatar(imageBase64)) {
                                JOptionPane.showMessageDialog(parent, "头像修改成功！");
                                Path avatarPath = Paths.get(System.getProperty("user.dir") + "/avatars/" + currentUser.name + ".png");
                                try {
                                    Files.write(avatarPath, image);
                                    avatarLabel.setImage(ImageIO.read(avatarPath.toFile()));
                                    avatarLabel.revalidate();
                                    avatarLabel.repaint();
                                } catch (IOException ex) {
                                    throw new RuntimeException(ex);
                                }
                            }
                        }
                    }
                }
            });
            add(avatarLabel, c);

            JButton settingsButton = new JButton("⚙"); // Consider UIManager.getIcon("FileChooser.detailsViewIcon") or similar
            settingsButton.putClientProperty("JButton.buttonType", "toolBarButton"); // FlatLaf style hint
            settingsButton.setToolTipText("Settings");
            settingsButton.addActionListener(e -> {
                System.out.println("Settings button clicked");
                JOptionPane.showMessageDialog(ChatUI.this, "TODO: Implement Settings");
            });
            c.gridy = 1;
            c.weighty = 1.0;
            c.anchor = GridBagConstraints.SOUTH;
            c.insets = new Insets(10, 10, 10, 10);
            add(settingsButton, c);
        }
    }

    class FriendListPanel extends JPanel {
        private final JPanel friendListContainer;
        private final ChatUI mainFrame; // Keep reference to outer class instance

        public FriendListPanel(ChatUI mainFrame) {
            this.mainFrame = mainFrame; // Store the passed ChatUI instance
            setLayout(new GridBagLayout());
            setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, UIManager.getColor("Component.borderColor")));

            GridBagConstraints gbc = new GridBagConstraints();

            JPanel topPanel = new JPanel(new GridBagLayout());
            JTextField searchField = new JTextField(); // Allow it to grow
            searchField.putClientProperty("JTextField.placeholderText", "Search Friends"); // FlatLaf placeholder
            searchField.addActionListener(e -> {
                System.out.println("Search initiated: " + searchField.getText());
                // TODO: Implement search functionality
            });

            JButton addFriendButton = new JButton("+");
            addFriendButton.putClientProperty("JButton.buttonType", "toolBarButton");
            addFriendButton.setToolTipText("Add friend");
            addFriendButton.addActionListener(e -> {
                //TODO: Implement Add Friend
                String friendName = JOptionPane.showInputDialog("请输入好友名字");
                if (friendName != null) {
                    if (messageHandler.sendFriendRequest(currentUser.name, friendName)) {
                        JOptionPane.showMessageDialog(ChatUI.this, "好友请求已发送");
                    } else {
                        JOptionPane.showMessageDialog(ChatUI.this, "好友请求发送失败！");
                    }
                }
            });

            GridBagConstraints topGbc = new GridBagConstraints();
            topGbc.gridx = 0;
            topGbc.weightx = 1.0;
            topGbc.fill = GridBagConstraints.HORIZONTAL;
            topGbc.insets = new Insets(5, 8, 5, 5);
            topPanel.add(searchField, topGbc);
            topGbc.gridx = 1;
            topGbc.weightx = 0;
            topGbc.fill = GridBagConstraints.NONE;
            topGbc.insets = new Insets(5, 0, 5, 8);
            topPanel.add(addFriendButton, topGbc);

            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 1.0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            add(topPanel, gbc);

            friendListContainer = new JPanel();
            friendListContainer.setLayout(new BoxLayout(friendListContainer, BoxLayout.Y_AXIS));

            JScrollPane scrollPane = new JScrollPane(friendListContainer);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane.setBorder(null); // Clean look, FlatLaf will style scrollbars

            gbc.gridy = 1;
            gbc.weighty = 1.0;
            gbc.fill = GridBagConstraints.BOTH;
            add(scrollPane, gbc);
        }

        public void loadFriends(List<FriendData> friends) {
            friendListContainer.removeAll();
            for (FriendData friend : friends) {
                FriendEntry entry = new FriendEntry(friend, this.mainFrame); // Pass stored mainFrame
                friendListContainer.add(entry);
                friendListContainer.add(new JSeparator(SwingConstants.HORIZONTAL));
                friendEntryMap.put(friend.name, entry);
            }
            friendListContainer.revalidate();
            friendListContainer.repaint();
        }

        public void addFriendToFriendList(FriendData friend) {
            FriendEntry entry = new FriendEntry(friend, this.mainFrame);
            friendListContainer.add(entry);
            friendListContainer.add(new JSeparator(SwingConstants.HORIZONTAL));
            friendEntryMap.put(friend.name, entry);
            friendListContainer.revalidate();
            friendListContainer.repaint();
        }

        class FriendEntry extends JPanel {
            private final Color defaultBg = UIManager.getColor("List.background");
            private final Color hoverBg = UIManager.getColor("List.selectionBackgroundInactive");
            private final Color selectedBg = UIManager.getColor("List.selectionBackground");
            private final Color selectedFg = UIManager.getColor("List.selectionForeground");
            private final Color defaultFg = UIManager.getColor("List.foreground");
            public String friendName;
            public JLabel lastMessageLabel;
            public JLabel dateLabel;
            private boolean selected = false;

            public FriendEntry(FriendData friend, ChatUI mainFrameArg) {
                setLayout(new GridBagLayout());
                setBorder(new EmptyBorder(8, 8, 8, 8));
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                setBackground(defaultBg); // Set initial background

                GridBagConstraints c = new GridBagConstraints();

                RoundedAvatar avatar = new RoundedAvatar(40, friend.avatarImagePath);
                c.gridx = 0;
                c.gridy = 0;
                c.gridheight = 2;
                c.anchor = GridBagConstraints.WEST;
                c.insets = new Insets(0, 0, 0, 10);
                add(avatar, c);

                JLabel nameLabel = new JLabel(friend.name);
                nameLabel.setForeground(defaultFg);
                c.gridx = 1;
                c.gridy = 0;
                c.gridheight = 1;
                c.weightx = 1.0;
                c.anchor = GridBagConstraints.NORTHWEST;
                add(nameLabel, c);

                lastMessageLabel = new JLabel(friend.lastMessage);
                lastMessageLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
                c.gridx = 1;
                c.gridy = 1;
                c.anchor = GridBagConstraints.SOUTHWEST;
                add(lastMessageLabel, c);

                dateLabel = new JLabel(friend.lastMessageDate);
                dateLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
                c.gridx = 2;
                c.gridy = 0;
                c.weightx = 0;
                c.anchor = GridBagConstraints.NORTHEAST;
                add(dateLabel, c);

                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        mainFrameArg.switchChatSession(friend);
                        for (Component comp : getParent().getComponents()) {
                            if (comp instanceof FriendEntry) {
                                ((FriendEntry) comp).setSelected(false);
                            }
                        }
                        setSelected(true);
                    }

                    @Override
                    public void mouseEntered(MouseEvent e) {
                        if (!selected) setBackground(hoverBg);
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        if (!selected) setBackground(defaultBg);
                    }
                });
            }

            public void setSelected(boolean selected) {
                this.selected = selected;
                if (selected) {
                    setBackground(selectedBg);
                    for (Component child : getComponents()) {
                        if (child instanceof JLabel) ((JLabel) child).setForeground(selectedFg);
                    }
                } else {
                    setBackground(defaultBg);
                    for (Component child : getComponents()) {
                        if (child instanceof JLabel) ((JLabel) child).setForeground(defaultFg);
                        // Restore specific colors if needed, e.g. for lastMessageLabel
                        if (child instanceof JLabel && ((JLabel) child).getText().equals(((JLabel) getComponent(2)).getText())) { // crude check for lastMessageLabel
                            ((JLabel) child).setForeground(UIManager.getColor("Label.disabledForeground"));
                        }
                        if (child instanceof JLabel && ((JLabel) child).getText().equals(((JLabel) getComponent(3)).getText())) { // crude check for dateLabel
                            ((JLabel) child).setForeground(UIManager.getColor("Label.disabledForeground"));
                        }
                    }
                }
            }
        }
    }

    class ChatPanel extends JPanel {
        private static final double DEFAULT_MESSAGE_AREA_PROPORTION = 0.75; // 75% height for messages
        private final JLabel friendNameLabel;
        private final JPanel messageDisplayArea;
        private final JScrollPane messageScrollPane;
        private final JTextArea inputTextArea;
        private final JButton sendButton;
        private final CardLayout cardLayout;
        private final JPanel contentPanel; // This will go into the TOP of the new JSplitPane
        private final FriendData currentUser;
        private final JPanel inputPanelContainer; // This will go into the BOTTOM of the new JSplitPane

        private final JSplitPane chatContentSplitPane; // New JSplitPane for messages/input
        private FriendData currentActiveChatFriend;
        private boolean inputAreaWasVisible = true; // Track if input area was visible before hiding
        private int lastInputDividerLocation = -1; // Store last divider location for input area


        public ChatPanel(ChatUI mainFrame, FriendData currentUser) {
            this.currentUser = currentUser;
            setLayout(new GridBagLayout());

            GridBagConstraints gbc = new GridBagConstraints(); // For ChatPanel's direct children

            friendNameLabel = new JLabel(" ");
            friendNameLabel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager.getColor("Component.borderColor")),
                    new EmptyBorder(10, 10, 10, 10)
            ));
            friendNameLabel.setHorizontalAlignment(SwingConstants.LEFT);
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 1.0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            add(friendNameLabel, gbc);

            // 1. Setup contentPanel (messages or logo) - this remains largely the same
            cardLayout = new CardLayout();
            contentPanel = new JPanel(cardLayout);

            JPanel initialEmptyPanel = new JPanel(new GridBagLayout()) { // Logo Panel
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setColor(UIManager.getColor("Panel.background"));
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                    int size = Math.min(getWidth(), getHeight()) / 3;
                    int x = (getWidth() - size) / 2;
                    int y = (getHeight() - size) / 2;
                    g2d.setColor(UIManager.getColor("Component.borderColor"));
                    g2d.drawRect(x, y, size - 1, size - 1);
                    g2d.setColor(UIManager.getColor("Label.foreground"));
                    g2d.setFont(UIManager.getFont("Label.font").deriveFont(Font.BOLD, (float) size / 5));
                    FontMetrics fm = g2d.getFontMetrics();
                    String text = "CHATTER";
                    g2d.drawString(text, x + (size - fm.stringWidth(text)) / 2, y + (size + fm.getAscent()) / 2 - fm.getDescent() / 2);
                    g2d.dispose();
                }
            };

            messageDisplayArea = new JPanel();
            messageDisplayArea.setLayout(new BoxLayout(messageDisplayArea, BoxLayout.Y_AXIS));
            messageDisplayArea.setBorder(new EmptyBorder(10, 10, 10, 10));

            JPanel chatMessagesPanelWrapper = new JPanel(new BorderLayout());
            messageScrollPane = new JScrollPane(messageDisplayArea);
            messageScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            messageScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            messageScrollPane.setBorder(null);
            chatMessagesPanelWrapper.add(messageScrollPane, BorderLayout.CENTER);

            contentPanel.add(initialEmptyPanel, "INITIAL");
            contentPanel.add(chatMessagesPanelWrapper, "CHAT");
            // contentPanel will be added to the JSplitPane later


            // 2. Setup inputPanelContainer - this also remains largely the same internally
            inputPanelContainer = new JPanel(new GridBagLayout());
            inputPanelContainer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIManager.getColor("Component.borderColor")));

            GridBagConstraints inputGbc = new GridBagConstraints();
            JPanel toolbarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
            JButton emojiButton = new JButton("😀");
            JButton fileButton = new JButton("📁");
            JButton imageButton = new JButton("🖼️");
            styleToolbarButton(emojiButton);
            styleToolbarButton(fileButton);
            styleToolbarButton(imageButton);
            emojiButton.addActionListener(e -> JOptionPane.showMessageDialog(ChatUI.this, "TODO: Emoji Picker"));
            fileButton.addActionListener(e -> JOptionPane.showMessageDialog(ChatUI.this, "TODO: File Sender"));
            imageButton.addActionListener(e -> {
                //TODO:发送图片
                byte[] image = ImageLoaderUtil.loadImageAndProcess(ChatUI.this, "选择图片");
                if (image != null) {
                    String imageBase64 = Base64.encode(image);
                    if (messageHandler.sendImageMessage(currentUser.name, currentActiveChatFriend.name, imageBase64, new Date())) {
                        try {
                            Path paths = Paths.get(System.getProperty("user.dir"), "images", new Date().getTime() + ".png");
                            Files.write(paths, image);
                            ChatMessageData msgData = new ChatMessageData(
                                    true, false, paths.toString(), new Date(), currentUser.avatarImagePath);
                            addMessageBubble(msgData);
                            currentActiveChatFriend.chatHistory.add(msgData);
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    } else {
                        JOptionPane.showMessageDialog(this, "发送失败！", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
            toolbarPanel.add(emojiButton);
            toolbarPanel.add(fileButton);
            toolbarPanel.add(imageButton);
            inputGbc.gridx = 0;
            inputGbc.gridy = 0;
            inputGbc.gridwidth = 2;
            inputGbc.fill = GridBagConstraints.HORIZONTAL;
            inputGbc.insets = new Insets(5, 8, 2, 8);
            inputPanelContainer.add(toolbarPanel, inputGbc);

            inputTextArea = new JTextArea();
            inputTextArea.setLineWrap(true);
            inputTextArea.setWrapStyleWord(true);
            inputTextArea.putClientProperty("JTextField.placeholderText", "Type a message...");


            JScrollPane inputScrollPane = new JScrollPane(inputTextArea);
            inputScrollPane.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createEmptyBorder(0, 8, 0, 8),
                    UIManager.getBorder("TextField.border")
            ));
            inputScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            inputGbc.gridy = 1;
            inputGbc.weightx = 1.0;
            inputGbc.weighty = 1.0;
            inputGbc.fill = GridBagConstraints.BOTH;
            inputGbc.insets = new Insets(2, 0, 5, 0);
            inputPanelContainer.add(inputScrollPane, inputGbc);

            sendButton = new JButton("Send");
            sendButton.setBackground(new Color(0x007AFF));
            sendButton.setForeground(Color.WHITE);
            sendButton.setEnabled(false);
            sendButton.setPreferredSize(new Dimension(80, 32));
            inputGbc.gridx = 1;
            inputGbc.gridy = 2;
            inputGbc.weightx = 0;
            inputGbc.weighty = 0;
            inputGbc.fill = GridBagConstraints.NONE;
            inputGbc.anchor = GridBagConstraints.SOUTHEAST;
            inputGbc.insets = new Insets(0, 5, 8, 8);
            inputPanelContainer.add(sendButton, inputGbc);
            // inputPanelContainer will be added to the JSplitPane later


            // 3. --- MOD: Create and configure the new JSplitPane for chat content and input ---
            chatContentSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
            chatContentSplitPane.setTopComponent(contentPanel);
            chatContentSplitPane.setBottomComponent(inputPanelContainer); // Initially has input area
            chatContentSplitPane.setUI(new InvisibleSplitPaneUI());
            chatContentSplitPane.setContinuousLayout(true);
            chatContentSplitPane.setOneTouchExpandable(false);
            chatContentSplitPane.setDividerSize(7); // Invisible draggable area height
            // Message area gets 80% of vertical resize changes, input area gets 20%
            chatContentSplitPane.setResizeWeight(0.80);


            // Add chatContentSplitPane to ChatPanel's main layout
            gbc.gridy = 1; // Takes the place of the old contentPanel + inputPanelContainer
            gbc.weighty = 1.0;
            gbc.fill = GridBagConstraints.BOTH;
            add(chatContentSplitPane, gbc);

            // Set initial divider location after components are potentially sized
            // We need to do this after the panel is displayed to get its actual height.
            // One way is to use a ComponentListener or invokeLater.
            // For simplicity now, we will set it in setActiveChat when input becomes visible first time.
            // Or set a proportional location:
            SwingUtilities.invokeLater(() -> { // Defer to allow component to get size
                chatContentSplitPane.setDividerLocation(DEFAULT_MESSAGE_AREA_PROPORTION);
                lastInputDividerLocation = chatContentSplitPane.getDividerLocation(); // Store initial
            });


            // Listeners
            inputTextArea.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    checkInput();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    checkInput();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    checkInput();
                }
            });
            sendButton.addActionListener(e -> sendMessage());
            inputTextArea.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        if ((e.getModifiersEx() & KeyEvent.SHIFT_DOWN_MASK) != 0) {
                            inputTextArea.append("\n");
                        } else {
                            e.consume();
                            if (sendButton.isEnabled()) {
                                sendMessage();
                            }
                        }
                    }
                }
            });
        }

        private void styleToolbarButton(JButton button) {
            button.putClientProperty("JButton.buttonType", "toolBarButton");
            button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        private void checkInput() {
            String text = inputTextArea.getText().trim();
            sendButton.setEnabled(!text.isEmpty());
        }

        private void sendMessage() {
            //TODO:向好友发送信息
            String messageText = inputTextArea.getText().trim();
            if (!messageText.isEmpty() && currentActiveChatFriend != null) {
                String friendName = currentActiveChatFriend.name;
                if (messageHandler.sendMessageToFriend(currentUser.name, friendName, messageText, new Date())) {
                    ChatMessageData msgData = new ChatMessageData(
                            true, true, messageText, new Date(), currentUser.avatarImagePath);
                    addMessageBubble(msgData);
                    currentActiveChatFriend.chatHistory.add(msgData);
                    inputTextArea.setText("");
                } else {
                    JOptionPane.showMessageDialog(this, "发送失败！", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }

        public void setActiveChat(FriendData friend) {
            this.currentActiveChatFriend = friend;
            if (friend == null) {
                friendNameLabel.setText(" ");
                cardLayout.show(contentPanel, "INITIAL"); // Show logo in top part

                // --- MOD: Hide input area using JSplitPane ---
                if (inputPanelContainer.isVisible() && chatContentSplitPane.getBottomComponent() == inputPanelContainer) {
                    // Store current divider location if input area was visible
                    lastInputDividerLocation = chatContentSplitPane.getDividerLocation();
                    inputAreaWasVisible = true;
                } else {
                    inputAreaWasVisible = false; // It was already hidden or not the bottom component
                }
                chatContentSplitPane.setBottomComponent(null); // Remove input panel from split pane
                inputPanelContainer.setVisible(false); // Also hide the panel itself
                // --- END MOD ---

                if (inputTextArea != null) inputTextArea.setEnabled(false);

            } else {
                friendNameLabel.setText(friend.name);
                messageDisplayArea.removeAll();
                for (ChatMessageData msg : friend.chatHistory) {
                    addMessageBubble(msg);
                }
                cardLayout.show(contentPanel, "CHAT"); // Show messages in top part

                // --- MOD: Show input area using JSplitPane ---
                inputPanelContainer.setVisible(true); // Make panel visible first
                chatContentSplitPane.setBottomComponent(inputPanelContainer); // Add it back

                // Restore divider location
                SwingUtilities.invokeLater(() -> { // Defer to allow UI updates
                    if (inputAreaWasVisible && lastInputDividerLocation != -1 && lastInputDividerLocation < chatContentSplitPane.getHeight()) {
                        chatContentSplitPane.setDividerLocation(lastInputDividerLocation);
                    } else {
                        // Set to default proportional location if no valid last location or first time
                        chatContentSplitPane.setDividerLocation(DEFAULT_MESSAGE_AREA_PROPORTION);
                    }
                    // Ensure a minimum reasonable height for input area when it reappears
                    int currentTopHeight = chatContentSplitPane.getDividerLocation();
                    int minInputHeight = inputPanelContainer.getPreferredSize().height;
                    if (minInputHeight < 100) minInputHeight = 100; // Absolute min for input area

                    if (chatContentSplitPane.getHeight() - currentTopHeight < minInputHeight &&
                            chatContentSplitPane.getHeight() > minInputHeight + chatContentSplitPane.getDividerSize()) {
                        chatContentSplitPane.setDividerLocation(chatContentSplitPane.getHeight() - minInputHeight - chatContentSplitPane.getDividerSize());
                    }
                });
                // --- END MOD ---

                if (inputTextArea != null) {
                    inputTextArea.setEnabled(true);
                    inputTextArea.requestFocusInWindow();
                }
            }
            checkInput(); // Update send button state
            // Revalidate the main chat panel or the split pane to reflect changes
            chatContentSplitPane.revalidate();
            chatContentSplitPane.repaint();
            this.revalidate(); // Revalidate ChatPanel itself
            this.repaint();
        }

        public FriendData getCurrentChatFriend() {
            return currentActiveChatFriend;
        }

        public void addMessageBubble(ChatMessageData messageData) {
            ChatMessageBubble bubble = new ChatMessageBubble(messageData);
            JPanel wrapper = new JPanel(new BorderLayout());
            wrapper.setOpaque(false);

            if (messageData.senderType == ChatMessageData.SenderType.SELF) {
                wrapper.add(bubble, BorderLayout.EAST);
            } else {
                wrapper.add(bubble, BorderLayout.WEST);
            }
            messageDisplayArea.add(wrapper);
            messageDisplayArea.add(Box.createRigidArea(new Dimension(0, 8)));
            messageDisplayArea.revalidate();
            if (messageScrollPane != null && messageScrollPane.getViewport() != null) {
                messageScrollPane.getViewport().revalidate();
                messageScrollPane.getViewport().repaint();
            } else {
                messageDisplayArea.repaint();
            }
            scrollToBottom();
        }

        private void scrollToBottom() {
            SwingUtilities.invokeLater(() -> {
                if (messageScrollPane == null) return;
                JScrollBar verticalBar = messageScrollPane.getVerticalScrollBar();
                if (verticalBar != null && verticalBar.isVisible()) {
                    verticalBar.setValue(verticalBar.getMaximum());
                }
            });
        }
    }
}
