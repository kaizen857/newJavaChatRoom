import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;


class InvisibleSplitPaneDivider extends BasicSplitPaneDivider {
    public InvisibleSplitPaneDivider(BasicSplitPaneUI ui) {
        super(ui);
        // We don't want any border on the divider itself
        setBorder(null);
        // The background color can be set if needed, but paint() override is key
        // setBackground(new Color(0,0,0,0)); // Transparent background
    }

    @Override
    public void paint(Graphics g) {
        // Override paint to do nothing, making the divider visually disappear.
        // The area will still be interactive due to the dividerSize.
        // If you want to ensure it's "transparent" by painting the parent's background:
        // Graphics2D g2d = (Graphics2D) g.create();
        // g2d.setColor(splitPane.getBackground()); // Or a specific transparent color
        // g2d.fillRect(0, 0, getWidth(), getHeight());
        // g2d.dispose();
        // For true invisibility, just leave this method empty.
    }

    // Optional: Ensure the divider has a minimum size for interaction if desired,
    // though JSplitPane's dividerSize property is the main controller.
}

// Custom UI that uses the InvisibleSplitPaneDivider
class InvisibleSplitPaneUI extends BasicSplitPaneUI {
    @Override
    public BasicSplitPaneDivider createDefaultDivider() {
        return new InvisibleSplitPaneDivider(this);
    }
}

class RoundedAvatar extends JPanel {
    private Image image;
    private Color placeholderColor;
    private int diameter;

    public RoundedAvatar(int diameter, String imagePath) {
        this.diameter = Math.min(diameter, 80); // Max 80px
        setPreferredSize(new Dimension(this.diameter, this.diameter));
        setOpaque(false);
        try {
            if (imagePath != null) {
                this.image = new ImageIcon(imagePath).getImage();
            }
        } catch (Exception e) {
            this.image = null; // Fallback to color if image fails
        }
        if (this.image == null) {
            this.placeholderColor = new Color((int)(Math.random() * 0xFFFFFF));
        }
    }

    public RoundedAvatar(int diameter, Color placeholderColor) {
        this.diameter = Math.min(diameter, 80);
        this.placeholderColor = placeholderColor;
        setPreferredSize(new Dimension(this.diameter, this.diameter));
        setOpaque(false);
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Ellipse2D.Float ellipse = new Ellipse2D.Float(0, 0, diameter - 1, diameter - 1);
        g2d.setClip(ellipse);

        if (image != null) {
            g2d.drawImage(image, 0, 0, diameter, diameter, this);
        } else {
            g2d.setColor(placeholderColor);
            g2d.fillRect(0, 0, diameter, diameter);
        }
        g2d.dispose();
    }
}

class FriendData {
    String id;
    String name;
    String avatarPath; // Placeholder for image path or color
    Color avatarColor;
    String lastMessage;
    String lastMessageDate;
    List<ChatMessageData> chatHistory;

    public FriendData(String id, String name, Color avatarColor, String lastMessage, String lastMessageDate) {
        this.id = id;
        this.name = name;
        this.avatarColor = avatarColor;
        this.lastMessage = lastMessage;
        this.lastMessageDate = lastMessageDate;
        this.chatHistory = new ArrayList<>();
    }
}

class ChatMessageData {
    enum SenderType { SELF, OTHER }
    SenderType senderType;
    String message;
    Date timestamp;
    Color avatarColor; // For the sender of this message

    public ChatMessageData(SenderType senderType, String message, Date timestamp, Color avatarColor) {
        this.senderType = senderType;
        this.message = message;
        this.timestamp = timestamp;
        this.avatarColor = avatarColor;
    }
}

class ChatMessageBubble extends JPanel {
    private JLabel messageLabel;
    private JLabel timeLabel;
    private RoundedAvatar avatar;
    private ChatMessageData data;
    private boolean isSelf;

    private String buildHtmlContent(String text) {
        // 处理特殊字符
        text = text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");

        // 自动检测内容类型
        if (text.startsWith("<image>")) {
            // 图片消息处理
            String imgPath = text.substring(7);
            return String.format("<html><img src='file:%s' width='%d'></html>",
                    imgPath, 100-30);
        } else if (text.startsWith("<file>")) {
            // 文件消息处理
            return "<html><div style='width:"+(100-20)+"px'>"+
                    "📄 "+text.substring(6)+"</div></html>";
        } else {
            // 文本消息处理（关键改进点）
            return "<html><div style='"+
                    "width:"+100+"px;"+
                    "word-wrap:break-word;"+
                    "white-space:pre-wrap;"+  // 保留换行符
                    "font-family:微软雅黑;"+
                    "font-size:14pt;"+
                    "'>"+text+"</div></html>";
        }
    }

    public ChatMessageBubble(ChatMessageData data) {
        this.data = data;
        this.isSelf = data.senderType == ChatMessageData.SenderType.SELF;

        setLayout(new GridBagLayout());
        setOpaque(false); // Important for bubble-like appearance against chat background
        setBorder(new EmptyBorder(5, 10, 5, 10));

        avatar = new RoundedAvatar(30, data.avatarColor);
        String htmlContent = buildHtmlContent(data.message);
        messageLabel = new JLabel("<html><body style='width: 100px;'>" + data.message + "</body></html>"); // Basic HTML for wrapping
        messageLabel.setOpaque(true);
        messageLabel.setBorder(new EmptyBorder(8, 12, 8, 12));



        timeLabel = new JLabel();
        timeLabel.setFont(new Font("微软雅黑", Font.PLAIN, 9));
        timeLabel.setForeground(Color.GRAY);
        timeLabel.setVisible(false); // Initially hidden

        GridBagConstraints gbc = new GridBagConstraints();

        // Configure based on sender
        if (isSelf) {
            messageLabel.setBackground(new Color(0xC9E6FF)); // Light blue for self
            // Message first, then avatar
            gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1.0; gbc.anchor = GridBagConstraints.EAST;
            add(timeLabel, gbc); // Time above message
            gbc.gridy = 1;
            add(messageLabel, gbc);
            gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 0; gbc.insets = new Insets(0, 5, 0, 0); gbc.anchor = GridBagConstraints.NORTHEAST;
            add(avatar, gbc);
        } else {
            messageLabel.setBackground(Color.WHITE); // White for other
            // Avatar first, then message
            gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0; gbc.insets = new Insets(0, 0, 0, 5); gbc.anchor = GridBagConstraints.NORTHWEST;
            add(avatar, gbc);
            gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0; gbc.anchor = GridBagConstraints.WEST;
            add(timeLabel, gbc); // Time above message
            gbc.gridy = 1;
            add(messageLabel, gbc);
        }

        // Make the bubble focusable to show time
        setFocusable(true);
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                timeLabel.setText(sdf.format(data.timestamp));
                timeLabel.setVisible(true);
                revalidate();
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                timeLabel.setVisible(false);
                revalidate();
                repaint();
            }
        });
        // Allow click to gain focus
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                requestFocusInWindow();
            }
        });
    }
}


public class QQLikeUI extends JFrame {

    private AccountPanel accountPanel;
    private FriendListPanel friendListPanel;
    private ChatPanel chatPanel;

    private FriendData currentUser; // Currently logged-in user
    private FriendData currentChatFriend; // Currently chatting with

    public QQLikeUI() {
        currentUser = new FriendData("user0", "MySelf", Color.ORANGE, "", "");

        setTitle("Java Swing QQ-Like Chat - Invisible Dividers");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(850, 650);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        accountPanel = new AccountPanel(currentUser);
        friendListPanel = new FriendListPanel(this);
        chatPanel = new ChatPanel(this, currentUser);

        accountPanel.setMinimumSize(new Dimension(80, 0));
        friendListPanel.setMinimumSize(new Dimension(100, 0)); // Adjusted min width
        chatPanel.setMinimumSize(new Dimension(150, 0));    // Adjusted min width


        // --- Configure Left Split Pane ---
        JSplitPane leftSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, accountPanel, friendListPanel);
        leftSplitPane.setUI(new InvisibleSplitPaneUI()); // Apply custom UI
        leftSplitPane.setContinuousLayout(true);       // Ensure real-time resizing (default)
        leftSplitPane.setOneTouchExpandable(false);    // No expander buttons
        leftSplitPane.setDividerSize(7);               // Width of the invisible draggable area
        leftSplitPane.setDividerLocation(90);
        leftSplitPane.setResizeWeight(0.0);            // AccountPanel size prioritized

        // --- Configure Main Split Pane ---
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftSplitPane, chatPanel);
        mainSplitPane.setUI(new InvisibleSplitPaneUI()); // Apply custom UI
        mainSplitPane.setContinuousLayout(true);        // Ensure real-time resizing
        mainSplitPane.setOneTouchExpandable(false);     // No expander buttons
        mainSplitPane.setDividerSize(7);                // Width of the invisible draggable area
        mainSplitPane.setDividerLocation(280);
        mainSplitPane.setResizeWeight(0.3);             // ChatPanel gets 70% of resize delta

        add(mainSplitPane, BorderLayout.CENTER);

        friendListPanel.loadFriends(createDummyFriends());
        chatPanel.setActiveChat(null); // Hides input area initially
    }

    private List<FriendData> createDummyFriends() {
        List<FriendData> friends = new ArrayList<>();
        friends.add(new FriendData("friend1", "Alice", Color.PINK, "Hey there!", "10:30"));
        friends.add(new FriendData("friend2", "Bob", Color.CYAN, "See you soon.", "09:15"));
        friends.add(new FriendData("friend3", "Charlie", Color.GREEN, "Lunch tomorrow?", "Yesterday"));
        friends.add(new FriendData("friend4", "Diana", Color.MAGENTA, "Good night!", "02/05"));
        for(int i=5; i<15; i++){
            friends.add(new FriendData("friend"+i, "User "+i, new Color((int)(Math.random() * 0xFFFFFF)), "Random msg "+i, "01/05"));
        }
        return friends;
    }

    public void switchChatSession(FriendData friend) {
        this.currentChatFriend = friend;
        chatPanel.setActiveChat(friend);
    }

    public FriendData getCurrentUser() {
        return currentUser;
    }

    public FriendData getCurrentChatFriend() {
        return currentChatFriend;
    }


    // --- Inner Panels ---

    class AccountPanel extends JPanel {
        private RoundedAvatar avatarLabel;
        private JButton settingsButton;

        public AccountPanel(FriendData user) {
            setLayout(new GridBagLayout());
            setBackground(new Color(0xF0F0F0));
            setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.LIGHT_GRAY));

            GridBagConstraints c = new GridBagConstraints();

            avatarLabel = new RoundedAvatar(60, user.avatarColor);
            c.gridx = 0;
            c.gridy = 0;
            c.anchor = GridBagConstraints.NORTH;
            c.insets = new Insets(10, 10, 10, 10);
            add(avatarLabel, c);

            settingsButton = new JButton("⚙");
            settingsButton.setToolTipText("Settings");
            settingsButton.setFont(new Font("SansSerif", Font.PLAIN, 20));
            settingsButton.setMargin(new Insets(2, 2, 2, 2));
            settingsButton.addActionListener(e -> {
                System.out.println("Settings button clicked");
                JOptionPane.showMessageDialog(this, "TODO: Implement Settings");
            });
            c.gridx = 0;
            c.gridy = 1;
            c.weighty = 1.0;
            c.anchor = GridBagConstraints.SOUTH;
            c.insets = new Insets(10, 10, 10, 10);
            add(settingsButton, c);
        }
    }

    class FriendListPanel extends JPanel {
        private JTextField searchField;
        private JButton addFriendButton;
        private JPanel friendListContainer;
        private JScrollPane scrollPane;
        private QQLikeUI mainFrame;

        public FriendListPanel(QQLikeUI mainFrame) {
            this.mainFrame = mainFrame;
            setLayout(new GridBagLayout());
            setBackground(new Color(0xF8F8F8));
            setBorder(BorderFactory.createMatteBorder(0,0,0,1, Color.LIGHT_GRAY));

            GridBagConstraints gbc = new GridBagConstraints();

            // Top: Search and Add button
            JPanel topPanel = new JPanel(new GridBagLayout());
            searchField = new JTextField(10);
            searchField.setToolTipText("Search friends");
            searchField.addActionListener(e -> {
                // TODO: Implement search functionality
                System.out.println("Search initiated: " + searchField.getText());
            });

            addFriendButton = new JButton("+");
            addFriendButton.setToolTipText("Add friend");
            addFriendButton.addActionListener(e -> {
                // TODO: Implement add friend functionality
                System.out.println("Add friend button clicked");
                JOptionPane.showMessageDialog(this, "TODO: Implement Add Friend");
            });

            GridBagConstraints topGbc = new GridBagConstraints();
            topGbc.gridx = 0; topGbc.gridy = 0; topGbc.weightx = 1.0; topGbc.fill = GridBagConstraints.HORIZONTAL; topGbc.insets = new Insets(5,5,2,5);
            topPanel.add(searchField, topGbc);
            topGbc.gridx = 1; topGbc.weightx = 0; topGbc.fill = GridBagConstraints.NONE; topGbc.insets = new Insets(5,0,2,5);
            topPanel.add(addFriendButton, topGbc);

            gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
            add(topPanel, gbc);


            // Bottom: Friend List
            friendListContainer = new JPanel();
            friendListContainer.setLayout(new BoxLayout(friendListContainer, BoxLayout.Y_AXIS));
            friendListContainer.setBackground(Color.WHITE);

            scrollPane = new JScrollPane(friendListContainer);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane.setBorder(null);

            // Scrollbar hide/show on hover (Basic - AS_NEEDED is usually sufficient)
            // For more advanced hiding, custom UI or listeners on JScrollBar visibility are needed.
            // This is a simplified version of the request.
            scrollPane.getVerticalScrollBar().addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    // scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    // if (!scrollPane.getBounds().contains(e.getPoint())) { // check if still over scrollpane
                    //    scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
                    //}
                }
            });


            gbc.gridx = 0; gbc.gridy = 1; gbc.weighty = 1.0; gbc.fill = GridBagConstraints.BOTH;
            add(scrollPane, gbc);
        }

        public void loadFriends(List<FriendData> friends) {
            friendListContainer.removeAll();
            for (FriendData friend : friends) {
                FriendEntry entry = new FriendEntry(friend, mainFrame);
                friendListContainer.add(entry);
                friendListContainer.add(Box.createRigidArea(new Dimension(0,1))); // Small separator
            }
            friendListContainer.revalidate();
            friendListContainer.repaint();
        }

        class FriendEntry extends JPanel {
            private FriendData friend;
            private QQLikeUI mainFrame;

            public FriendEntry(FriendData friend, QQLikeUI mainFrame) {
                this.friend = friend;
                this.mainFrame = mainFrame;
                setLayout(new GridBagLayout());
                setBorder(new EmptyBorder(5, 5, 5, 5));
                setBackground(Color.WHITE);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

                GridBagConstraints c = new GridBagConstraints();

                RoundedAvatar avatar = new RoundedAvatar(40, friend.avatarColor);
                c.gridx = 0; c.gridy = 0; c.gridheight = 2; c.anchor = GridBagConstraints.WEST; c.insets = new Insets(0,0,0,10);
                add(avatar, c);

                JLabel nameLabel = new JLabel(friend.name);
                nameLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
                c.gridx = 1; c.gridy = 0; c.gridheight = 1; c.weightx = 1.0; c.anchor = GridBagConstraints.NORTHWEST; c.insets = new Insets(0,0,0,0);
                add(nameLabel, c);

                JLabel lastMessageLabel = new JLabel(friend.lastMessage);
                lastMessageLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
                lastMessageLabel.setForeground(Color.GRAY);
                c.gridx = 1; c.gridy = 1; c.anchor = GridBagConstraints.SOUTHWEST;
                add(lastMessageLabel, c);

                JLabel dateLabel = new JLabel(friend.lastMessageDate);
                dateLabel.setFont(new Font("微软雅黑", Font.PLAIN, 10));
                dateLabel.setForeground(Color.LIGHT_GRAY);
                c.gridx = 2; c.gridy = 0; c.weightx = 0; c.anchor = GridBagConstraints.NORTHEAST;
                add(dateLabel, c);

                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        // TODO: Implement friend click to open chat
                        System.out.println("Clicked on friend: " + friend.name);
                        mainFrame.switchChatSession(friend);
                        // Highlight selected friend (optional)
                        for(Component comp : getParent().getComponents()){
                            if(comp instanceof FriendEntry){
                                comp.setBackground(Color.WHITE);
                            }
                        }
                        setBackground(new Color(0xE0E0E0)); // Light selection color
                    }
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        if(getBackground() != new Color(0xE0E0E0)) // Don't change if selected
                            setBackground(new Color(0xF0F0F0));
                    }
                    @Override
                    public void mouseExited(MouseEvent e) {
                        if(getBackground() != new Color(0xE0E0E0))
                            setBackground(Color.WHITE);
                    }
                });
            }
        }
    }

    class ChatPanel extends JPanel {
        private JLabel friendNameLabel;
        private JPanel messageDisplayArea;
        private JScrollPane messageScrollPane;
        private JTextArea inputTextArea;
        private JButton emojiButton, fileButton, imageButton, sendButton;
        private JPanel initialEmptyPanel;
        private CardLayout cardLayout;
        private JPanel contentPanel;
        private QQLikeUI mainFrame;
        private FriendData currentUser;
        private JPanel inputPanelContainer; // Field for the input area container


        public ChatPanel(QQLikeUI mainFrame, FriendData currentUser) {
            this.mainFrame = mainFrame;
            this.currentUser = currentUser;
            setLayout(new GridBagLayout());
            setBackground(new Color(0xF5F5F5));

            GridBagConstraints gbc = new GridBagConstraints();

            friendNameLabel = new JLabel(" ");
            friendNameLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
            friendNameLabel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0,0,1,0,Color.LIGHT_GRAY),
                    new EmptyBorder(10,10,10,10)
            ));
            friendNameLabel.setHorizontalAlignment(SwingConstants.LEFT);
            gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
            add(friendNameLabel, gbc);

            cardLayout = new CardLayout();
            contentPanel = new JPanel(cardLayout);
            initialEmptyPanel = new JPanel(new GridBagLayout()) {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2d = (Graphics2D) g.create();
                    int size = Math.min(getWidth(), getHeight()) / 2;
                    int x = (getWidth() - size) / 2;
                    int y = (getHeight() - size) / 2;
                    g2d.setColor(new Color(0xFFE0E0));
                    g2d.fillRect(0,0, getWidth(), getHeight());
                    g2d.setColor(Color.RED);
                    g2d.fillRect(x, y, size, size);
                    g2d.setColor(Color.WHITE);
                    g2d.setFont(new Font("微软雅黑", Font.BOLD, size/4));
                    FontMetrics fm = g2d.getFontMetrics();
                    String text = "LOGO";
                    g2d.drawString(text, x + (size - fm.stringWidth(text))/2, y + (size + fm.getAscent())/2);
                    g2d.dispose();
                }
            };
            initialEmptyPanel.setBackground(new Color(0xF5F5F5));

            JPanel chatMessagesPanel = new JPanel(new BorderLayout());
            chatMessagesPanel.setOpaque(false);
            messageDisplayArea = new JPanel();
            messageDisplayArea.setLayout(new BoxLayout(messageDisplayArea, BoxLayout.Y_AXIS));
            messageDisplayArea.setBackground(new Color(0xF5F5F5));
            messageDisplayArea.setBorder(new EmptyBorder(10,10,10,10));
            messageScrollPane = new JScrollPane(messageDisplayArea);
            messageScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            messageScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            messageScrollPane.setBorder(null);
            messageScrollPane.getViewport().setBackground(new Color(0xF5F5F5));
            chatMessagesPanel.add(messageScrollPane, BorderLayout.CENTER);
            contentPanel.add(initialEmptyPanel, "INITIAL");
            contentPanel.add(chatMessagesPanel, "CHAT");
            gbc.gridx = 0; gbc.gridy = 1; gbc.weighty = 1.0; gbc.fill = GridBagConstraints.BOTH;
            add(contentPanel, gbc);

            inputPanelContainer = new JPanel(new GridBagLayout());
            inputPanelContainer.setBorder(BorderFactory.createMatteBorder(1,0,0,0,Color.LIGHT_GRAY));
            inputPanelContainer.setBackground(Color.WHITE);

            GridBagConstraints inputGbc = new GridBagConstraints();

            JPanel toolbarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
            toolbarPanel.setOpaque(false);
            emojiButton = new JButton("😀"); fileButton = new JButton("📁"); imageButton = new JButton("🖼️");
            emojiButton.addActionListener(e -> { System.out.println("Emoji clicked"); });
            fileButton.addActionListener(e -> { System.out.println("File clicked"); });
            imageButton.addActionListener(e -> { System.out.println("Image clicked"); });
            styleToolbarButton(emojiButton); styleToolbarButton(fileButton); styleToolbarButton(imageButton);
            toolbarPanel.add(emojiButton); toolbarPanel.add(fileButton); toolbarPanel.add(imageButton);
            inputGbc.gridx = 0; inputGbc.gridy = 0; inputGbc.gridwidth = 2; inputGbc.fill = GridBagConstraints.HORIZONTAL; inputGbc.insets = new Insets(5,5,5,5);
            inputPanelContainer.add(toolbarPanel, inputGbc);

            inputTextArea = new JTextArea(String.valueOf(3));
            inputTextArea.setFont(new Font("微软雅黑", Font.PLAIN, 14));
            inputTextArea.setLineWrap(true); inputTextArea.setWrapStyleWord(true);
            JScrollPane inputScrollPane = new JScrollPane(inputTextArea);
            inputScrollPane.setBorder(BorderFactory.createEmptyBorder());
            inputScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            inputGbc.gridx = 0; inputGbc.gridy = 1; inputGbc.gridwidth = 2; inputGbc.weightx = 1.0; inputGbc.weighty = 1.0; inputGbc.fill = GridBagConstraints.BOTH; inputGbc.insets = new Insets(0,10,5,10);
            inputPanelContainer.add(inputScrollPane, inputGbc);

            sendButton = new JButton("发送");
            sendButton.setBackground(new Color(0x007AFF)); sendButton.setForeground(Color.WHITE);
            sendButton.setEnabled(false); sendButton.setOpaque(true); sendButton.setBorderPainted(false);
            sendButton.setFont(new Font("微软雅黑", Font.PLAIN, 13)); sendButton.setPreferredSize(new Dimension(70, 30));
            inputGbc.gridx = 1; inputGbc.gridy = 2; inputGbc.gridwidth = 1; inputGbc.weightx = 0; inputGbc.weighty = 0; inputGbc.fill = GridBagConstraints.NONE; inputGbc.anchor = GridBagConstraints.SOUTHEAST; inputGbc.insets = new Insets(5,5,10,10);
            inputPanelContainer.add(sendButton, inputGbc);

            gbc.gridx = 0; gbc.gridy = 2; gbc.weighty = 0; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
            add(inputPanelContainer, gbc);

            inputTextArea.getDocument().addDocumentListener(new DocumentListener() {
                @Override public void insertUpdate(DocumentEvent e) { checkInput(); }
                @Override public void removeUpdate(DocumentEvent e) { checkInput(); }
                @Override public void changedUpdate(DocumentEvent e) { checkInput(); }
            });
            sendButton.addActionListener(e -> sendMessage());
            inputTextArea.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        if (e.isShiftDown()) {
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
            button.setOpaque(false);
            button.setContentAreaFilled(false);
            button.setBorderPainted(false);
            button.setFont(new Font("微软雅黑", Font.PLAIN, 18));
            button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }


        private void checkInput() {
            String text = inputTextArea.getText().trim();
            if (text.isEmpty()) {
                sendButton.setEnabled(false);
                sendButton.setForeground(Color.GRAY);
            } else {
                sendButton.setEnabled(true);
                sendButton.setForeground(Color.WHITE);
            }
        }

        private void sendMessage() {
            String messageText = inputTextArea.getText().trim();
            if (!messageText.isEmpty() && mainFrame.getCurrentChatFriend() != null) {
                FriendData currentChat = mainFrame.getCurrentChatFriend();
                System.out.println("Sending to " + currentChat.name + ": " + messageText);

                ChatMessageData msgData = new ChatMessageData(
                        ChatMessageData.SenderType.SELF,
                        messageText,
                        new Date(),
                        currentUser.avatarColor
                );
                addMessageBubble(msgData);
                currentChat.chatHistory.add(msgData);

                inputTextArea.setText("");
                Timer timer = new Timer(1000, ae -> {
                    ChatMessageData replyData = new ChatMessageData(
                            ChatMessageData.SenderType.OTHER,
                            "Got it: \"" + messageText + "\"",
                            new Date(),
                            currentChat.avatarColor
                    );
                    addMessageBubble(replyData);
                    currentChat.chatHistory.add(replyData);
                });
                timer.setRepeats(false);
                timer.start();
            }
        }

        public void setActiveChat(FriendData friend) {
            if (friend == null) {
                friendNameLabel.setText(" ");
                cardLayout.show(contentPanel, "INITIAL");
                inputTextArea.setEnabled(false);
                checkInput();
                if (inputPanelContainer != null) {
                    inputPanelContainer.setVisible(false);
                }
            } else {
                friendNameLabel.setText(friend.name);
                messageDisplayArea.removeAll();
                for (ChatMessageData msg : friend.chatHistory) {
                    addMessageBubble(msg);
                }
                cardLayout.show(contentPanel, "CHAT");
                if (inputPanelContainer != null) {
                    inputPanelContainer.setVisible(true);
                }
                inputTextArea.setEnabled(true);
                messageDisplayArea.revalidate();
                messageDisplayArea.repaint();
                scrollToBottom();
                inputTextArea.requestFocusInWindow();
            }
            this.revalidate();
            this.repaint();
        }

        private void addMessageBubble(ChatMessageData messageData) {
            ChatMessageBubble bubble = new ChatMessageBubble(messageData);
            JPanel wrapper = new JPanel(new BorderLayout());
            wrapper.setOpaque(false);

            if (messageData.senderType == ChatMessageData.SenderType.SELF) {
                wrapper.add(bubble, BorderLayout.EAST);
            } else {
                wrapper.add(bubble, BorderLayout.WEST);
            }
            messageDisplayArea.add(wrapper);
            messageDisplayArea.add(Box.createRigidArea(new Dimension(0, 5)));

            messageDisplayArea.revalidate();
            messageDisplayArea.repaint();
            scrollToBottom();
        }

        private void scrollToBottom() {
            SwingUtilities.invokeLater(() -> {
                JScrollBar verticalBar = messageScrollPane.getVerticalScrollBar();
                if (verticalBar != null) {
                    verticalBar.setValue(verticalBar.getMaximum());
                }
            });
        }
    }


    public static void main(String[] args) {
        // Set Look and Feel (Optional, for better aesthetics)
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            new QQLikeUI().setVisible(true);
        });
    }
}