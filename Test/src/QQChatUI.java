import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class QQChatUI extends JFrame {

    private JPanel accountInfoPanel;
    private JPanel friendListPanel;
    private JPanel chatPanel;

    public QQChatUI() {
        setTitle("QQ Chat - Java Swing");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null); // Center the window

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // --- Account Info Panel ---
        accountInfoPanel = createAccountInfoPanel();
        gbc.gridx = 0; // Column 0
        gbc.gridy = 0; // Row 0
        gbc.weightx = 0.05; // Small horizontal space
        gbc.weighty = 1.0;  // Takes full vertical space
        gbc.fill = GridBagConstraints.BOTH;
        add(accountInfoPanel, gbc);

        // --- Friend List Panel ---
        friendListPanel = createFriendListPanel();
        gbc.gridx = 1; // Column 1
        gbc.weightx = 0.25; // Medium horizontal space
        add(friendListPanel, gbc);

        // --- Chat Panel ---
        chatPanel = createChatPanel();
        gbc.gridx = 2; // Column 2
        gbc.weightx = 0.70; // Large horizontal space
        add(chatPanel, gbc);
    }

    // Methods to create each panel (details below)
    private JPanel createAccountInfoPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(60, 63, 65)); // Dark background
        GridBagConstraints gbc = new GridBagConstraints();

        // Account Avatar (placeholder)
        JLabel avatarLabel = new JLabel();
        ImageIcon originalIcon = new ImageIcon("./assert/0.jpg"); // Ensure this path is correctgetClass().getResource("assert/0.jpg")
        Image originalImage = originalIcon.getImage();
        Image scaledImage = originalImage.getScaledInstance(70, 70, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(scaledImage);
        avatarLabel.setIcon(scaledIcon);
        avatarLabel.setPreferredSize(new Dimension(80, 80)); // Allocate space for circular avatar
        // To make it truly circular, you'd typically draw it in a custom JPanel or use a library.
        // For simplicity with JLabel, we'll just display a square image.
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(10, 0, 10, 0); // Top/Bottom padding
        panel.add(avatarLabel, gbc);

        // Settings Button
        JButton settingsButton = new JButton("Settings");
        settingsButton.setBackground(new Color(80, 80, 80));
        settingsButton.setForeground(Color.WHITE);
        settingsButton.setFocusPainted(false);
        settingsButton.setBorder(new EmptyBorder(5, 10, 5, 10));
        gbc.gridy = 1;
        gbc.weighty = 1.0; // Pushes button to the bottom
        gbc.anchor = GridBagConstraints.SOUTH;
        gbc.insets = new Insets(0, 0, 10, 0); // Bottom padding
        panel.add(settingsButton, gbc);

        return panel;
    }

    private JPanel createFriendListPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(70, 73, 75)); // Slightly lighter dark
        GridBagConstraints gbc = new GridBagConstraints();

        // Search Box and Add Button
        JPanel searchPanel = new JPanel(new GridBagLayout());
        searchPanel.setOpaque(false); // Make it transparent
        JTextField searchField = new JTextField("Search friends...");
        searchField.setPreferredSize(new Dimension(150, 30));
        searchField.setForeground(Color.LIGHT_GRAY);
        searchField.setBackground(new Color(90, 93, 95));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(100, 100, 100)),
                new EmptyBorder(5, 5, 5, 5)
        ));

        JButton addButton = new JButton("+");
        addButton.setPreferredSize(new Dimension(30, 30));
        addButton.setBackground(new Color(50, 150, 250)); // Blue color
        addButton.setForeground(Color.WHITE);
        addButton.setFocusPainted(false);
        addButton.setBorder(new EmptyBorder(0,0,0,0));

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 0);
        searchPanel.add(searchField, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.0;
        gbc.insets = new Insets(5, 5, 5, 5);
        searchPanel.add(addButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = GridBagConstraints.REMAINDER; // Span across columns
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel.add(searchPanel, gbc);

        // Friend List (wrapped in JScrollPane)
        JPanel friendsContainer = new JPanel();
        friendsContainer.setLayout(new BoxLayout(friendsContainer, BoxLayout.Y_AXIS));
        friendsContainer.setBackground(new Color(70, 73, 75));

        // Add some dummy friends
        for (int i = 1; i <= 10; i++) {
            friendsContainer.add(createFriendListItem("Friend " + i, "Hello there!", "yesterday"));
        }

        JScrollPane scrollPane = new JScrollPane(friendsContainer);
        scrollPane.setBorder(BorderFactory.createEmptyBorder()); // No border for the scroll pane
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        // Hide scrollbar when mouse not over, show when over
        scrollPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                scrollPane.getVerticalScrollBar().setVisible(true);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                // Don't hide if dragging
                //if (!scrollPane.getVerticalScrollBar().isDragging())
                    scrollPane.getVerticalScrollBar().setVisible(false);
            }
        });
        // Initial state: hidden
        scrollPane.getVerticalScrollBar().setVisible(false);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(scrollPane, gbc);

        return panel;
    }

    private JPanel createFriendListItem(String name, String latestMessage, String date) {
        JPanel itemPanel = new JPanel(new GridBagLayout());
        itemPanel.setBackground(new Color(85, 88, 90)); // Slightly darker for item background
        itemPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        itemPanel.setMaximumSize(new Dimension(Short.MAX_VALUE, 70)); // Limit height for uniform items
        itemPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        GridBagConstraints gbc = new GridBagConstraints();

        // Friend Avatar
        JLabel avatar = new JLabel();
        ImageIcon originalIcon = new ImageIcon("./assert/0.jpg"); // Replace with actual friend avatargetClass().getResource("/images/friend_avatar.png")
        Image originalImage = originalIcon.getImage();
        Image scaledImage = originalImage.getScaledInstance(50, 50, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(scaledImage);
        avatar.setIcon(scaledIcon);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridheight = 2; // Spans two rows
        gbc.insets = new Insets(0, 0, 0, 10);
        itemPanel.add(avatar, gbc);

        // Friend Name
        JLabel nameLabel = new JLabel(name);
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 2, 0); // Padding below name
        itemPanel.add(nameLabel, gbc);

        // Latest Message
        JLabel messageLabel = new JLabel(latestMessage);
        messageLabel.setForeground(Color.LIGHT_GRAY);
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 0, 0);
        itemPanel.add(messageLabel, gbc);

        // Latest Message Date
        JLabel dateLabel = new JLabel(date);
        dateLabel.setForeground(Color.GRAY);
        dateLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.gridheight = 1; // Resets height to 1
        gbc.weightx = 0.0;
        gbc.anchor = GridBagConstraints.NORTHEAST;
        itemPanel.add(dateLabel, gbc);

        itemPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                System.out.println("Clicked on friend: " + name);
                // Here you would update the chat panel to show this friend's conversation
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                itemPanel.setBackground(new Color(100, 103, 105)); // Highlight on hover
            }
            @Override
            public void mouseExited(MouseEvent e) {
                itemPanel.setBackground(new Color(85, 88, 90)); // Restore original color
            }
        });

        return itemPanel;
    }


    private JPanel createChatPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(240, 240, 240)); // Light background for chat area
        GridBagConstraints gbc = new GridBagConstraints();

        // Friend Name (at the top)
        JLabel currentFriendName = new JLabel("Current Friend Name");
        currentFriendName.setFont(new Font("Arial", Font.BOLD, 18));
        currentFriendName.setBorder(new EmptyBorder(10, 10, 10, 10));
        currentFriendName.setHorizontalAlignment(SwingConstants.CENTER);
        currentFriendName.setOpaque(true);
        currentFriendName.setBackground(new Color(230, 230, 230));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(currentFriendName, gbc);

        // Chat Display Area
        JPanel chatDisplayArea = new JPanel();
        chatDisplayArea.setLayout(new BoxLayout(chatDisplayArea, BoxLayout.Y_AXIS));
        chatDisplayArea.setBackground(new Color(240, 240, 240)); // Match panel background
        chatDisplayArea.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Add some dummy messages
        chatDisplayArea.add(createMessageBubble("Hello!", true, "10:00 AM")); // My message
        chatDisplayArea.add(createMessageBubble("Hi there!", false, "10:01 AM")); // Friend's message
        chatDisplayArea.add(createMessageBubble("How are you?", true, "10:02 AM"));
        chatDisplayArea.add(createMessageBubble("I'm good, thanks!", false, "10:03 AM"));

        JScrollPane chatScrollPane = new JScrollPane(chatDisplayArea);
        chatScrollPane.setBorder(BorderFactory.createEmptyBorder());
        chatScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        chatScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        // Hide scrollbar when mouse not over, show when over
        chatScrollPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                chatScrollPane.getVerticalScrollBar().setVisible(true);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                //if (!chatScrollPane.getVerticalScrollBar().isDragging())
                    chatScrollPane.getVerticalScrollBar().setVisible(false);
            }
        });
        chatScrollPane.getVerticalScrollBar().setVisible(false); // Initial state: hidden

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0; // Takes most vertical space
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(chatScrollPane, gbc);

        // Input Area (bottom)
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBackground(new Color(220, 220, 220));
        inputPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

        // Buttons above input
        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        buttonRow.setOpaque(false);
        JButton emojiButton = new JButton("😊");
        JButton fileButton = new JButton("📁");
        JButton imageButton = new JButton("🖼️");

        styleInputActionButton(emojiButton);
        styleInputActionButton(fileButton);
        styleInputActionButton(imageButton);

        buttonRow.add(emojiButton);
        buttonRow.add(fileButton);
        buttonRow.add(imageButton);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 5, 0); // Padding below buttons
        inputPanel.add(buttonRow, gbc);

        JTextArea messageInput = new JTextArea(3, 20); // 3 rows, 20 columns wide
        messageInput.setLineWrap(true);
        messageInput.setWrapStyleWord(true);
        JScrollPane inputScrollPane = new JScrollPane(messageInput);
        inputScrollPane.setBorder(new LineBorder(new Color(180, 180, 180)));

        JButton sendButton = new JButton("发送");
        sendButton.setBackground(new Color(150, 200, 250)); // Light blue when disabled
        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false);
        sendButton.setBorder(new EmptyBorder(8, 15, 8, 15));
        sendButton.setEnabled(false); // Initially disabled

        // Enable/disable send button based on text input
        messageInput.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                checkSendButtonStatus();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                checkSendButtonStatus();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                checkSendButtonStatus();
            }

            private void checkSendButtonStatus() {
                boolean hasText = !messageInput.getText().trim().isEmpty();
                sendButton.setEnabled(hasText);
                if (hasText) {
                    sendButton.setBackground(new Color(50, 150, 250)); // Blue when enabled
                } else {
                    sendButton.setBackground(new Color(150, 200, 250)); // Light blue when disabled
                }
            }
        });

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0; // Don't let it expand vertically much
        gbc.fill = GridBagConstraints.BOTH;
        inputPanel.add(inputScrollPane, gbc);

        // Send button in the bottom-right of the input panel
        gbc.gridx = 0;
        gbc.gridy = 2; // New row for send button
        gbc.weightx = 0.0; // Don't take extra horizontal space
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.SOUTHEAST; // Align to bottom right
        gbc.insets = new Insets(5, 0, 0, 0); // Padding above button
        inputPanel.add(sendButton, gbc);


        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weighty = 0.0; // Fix input panel height
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(inputPanel, gbc);

        return panel;
    }

    private void styleInputActionButton(JButton button) {
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setForeground(new Color(80, 80, 80));
        button.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20)); // For emojis
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }


    private JPanel createMessageBubble(String message, boolean isMyMessage, String timestamp) {
        JPanel bubblePanel = new JPanel(new BorderLayout()); // Use BorderLayout for the bubble content and timestamp
        bubblePanel.setOpaque(false); // Make transparent to see parent background
        // Set AlignmentX for BoxLayout parent (chatDisplayArea) to position the entire bubble
        bubblePanel.setAlignmentX(isMyMessage ? Component.RIGHT_ALIGNMENT : Component.LEFT_ALIGNMENT);

        JPanel contentPanel = new JPanel(new GridBagLayout()); // This panel holds avatar and message text
        contentPanel.setBackground(isMyMessage ? new Color(150, 230, 150) : new Color(200, 200, 200)); // Green for me, gray for friend
        contentPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(isMyMessage ? new Color(120, 200, 120) : new Color(170, 170, 170), 1),
                new EmptyBorder(8, 12, 8, 12)
        ));
        contentPanel.setOpaque(true); // Ensure background is painted
        // contentPanel.setMaximumSize(new Dimension(500, Short.MAX_VALUE)); // This might cause issues with BoxLayout if not careful, better to let GridBagLayout manage internal size

        GridBagConstraints gbc = new GridBagConstraints();

        // Avatar
        JLabel avatarLabel = new JLabel();
        ImageIcon originalIcon = new ImageIcon("./assert/1.jpg");//
        Image originalImage = originalIcon.getImage();
        Image scaledImage = originalImage.getScaledInstance(40, 40, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(scaledImage);
        avatarLabel.setIcon(scaledIcon);

        // Content Text (JLabel to support image display if needed)
        JLabel messageContent = new JLabel("<html>" + message + "</html>");
        messageContent.setFont(new Font("Arial", Font.PLAIN, 14));
        messageContent.setForeground(Color.BLACK);
        messageContent.setCursor(new Cursor(Cursor.HAND_CURSOR)); // To indicate hover for timestamp

        // Timestamp label (initially hidden)
        JLabel timeLabel = new JLabel(timestamp);
        timeLabel.setFont(new Font("Arial", Font.PLAIN, 9));
        timeLabel.setForeground(Color.GRAY);
        timeLabel.setVisible(false); // Initially hidden

        // Mouse listener for timestamp visibility
        messageContent.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                timeLabel.setVisible(true);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                timeLabel.setVisible(false);
            }
        });


        if (isMyMessage) {
            // My message: Avatar on right, text on left within contentPanel
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 1.0; // Message content takes available space
            gbc.insets = new Insets(0, 0, 0, 5);
            contentPanel.add(messageContent, gbc);

            gbc.gridx = 1;
            gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.EAST;
            gbc.weightx = 0.0; // Avatar doesn't expand
            gbc.insets = new Insets(0, 5, 0, 0);
            contentPanel.add(avatarLabel, gbc);

            bubblePanel.add(timeLabel, BorderLayout.NORTH); // Timestamp above the bubble content
            bubblePanel.add(contentPanel, BorderLayout.EAST); // Content bubble aligned to the EAST (right)
        } else {
            // Friend's message: Avatar on left, text on right within contentPanel
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 0.0; // Avatar doesn't expand
            gbc.insets = new Insets(0, 0, 0, 5);
            contentPanel.add(avatarLabel, gbc);

            gbc.gridx = 1;
            gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.EAST;
            gbc.weightx = 1.0; // Message content takes available space
            gbc.insets = new Insets(0, 5, 0, 0);
            contentPanel.add(messageContent, gbc);

            bubblePanel.add(timeLabel, BorderLayout.NORTH); // Timestamp above the bubble content
            bubblePanel.add(contentPanel, BorderLayout.WEST); // Content bubble aligned to the WEST (left)
        }

        return bubblePanel;
    }


    public static void main(String[] args) {
        // Set an appealing look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            QQChatUI ui = new QQChatUI();
            ui.setVisible(true);
        });
    }
}