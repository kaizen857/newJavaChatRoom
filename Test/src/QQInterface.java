import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.Objects;

public class QQInterface {
    private JFrame frame;
    private JTextArea inputArea;
    private JButton sendButton;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new QQInterface().createUI());
    }

    private void createUI() {
        frame = new JFrame("QQ Chat");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 800);
        frame.setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);

        // 账号信息面板
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.1;
        gbc.weighty = 1;
        mainPanel.add(createAccountPanel(), gbc);

        // 好友列表面板
        gbc.gridx = 1;
        gbc.weightx = 0.25;
        mainPanel.add(createFriendListPanel(), gbc);

        // 聊天面板
        gbc.gridx = 2;
        gbc.weightx = 0.65;
        mainPanel.add(createChatPanel(), gbc);

        frame.add(mainPanel);
        frame.setVisible(true);
    }

    private JPanel createAccountPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEtchedBorder());

        // 头像
        JLabel avatar = new JLabel(createCircularIcon("./assert/0.jpg", 80));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 10, 10, 10);
        panel.add(avatar, gbc);

        // 设置按钮
        JButton settings = new JButton("设置");
        gbc.gridy = 1;
        panel.add(settings, gbc);

        return panel;
    }

    private JPanel createFriendListPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // 搜索框和按钮
        JPanel topPanel = new JPanel(new BorderLayout());
        JTextField searchField = new JTextField();
        JButton addButton = new JButton("+");
        topPanel.add(searchField, BorderLayout.CENTER);
        topPanel.add(addButton, BorderLayout.EAST);

        // 好友列表
        JList<String> friendList = new JList<>(new String[]{"好友1", "好友2", "好友3"});
        JScrollPane scrollPane = new JScrollPane(friendList);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        friendList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
            }
        });

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createChatPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // 好友名称
        JLabel friendName = new JLabel("当前好友", SwingConstants.CENTER);
        panel.add(friendName, BorderLayout.NORTH);

        // 聊天记录区域
        JPanel chatArea = new JPanel();
        chatArea.setLayout(new BoxLayout(chatArea, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        // 输入区域
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputArea = new JTextArea(3, 20);
        inputArea.setLineWrap(true);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        sendButton = new JButton("发送");
        sendButton.setBackground(new Color(0, 120, 215));
        sendButton.setForeground(Color.WHITE);

        inputArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { update(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { update(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { update(); }
            private void update() {
                sendButton.setEnabled(inputArea.getText().trim().length() > 0);
            }
        });

        buttonPanel.add(new JButton("😊"));
        buttonPanel.add(new JButton("📁"));
        buttonPanel.add(new JButton("🖼️"));
        buttonPanel.add(sendButton);

        inputPanel.add(new JScrollPane(inputArea), BorderLayout.CENTER);
        inputPanel.add(buttonPanel, BorderLayout.SOUTH);

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(inputPanel, BorderLayout.SOUTH);

        return panel;
    }

    private ImageIcon createCircularIcon(String path, int size) {
        try {
            BufferedImage src = javax.imageio.ImageIO.read(Objects.requireNonNull(getClass().getResource(path)));
            BufferedImage dest = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = dest.createGraphics();

            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setClip(new RoundRectangle2D.Float(0, 0, size, size, size, size));
            g2.drawImage(src.getScaledInstance(size, size, Image.SCALE_SMOOTH), 0, 0, null);
            g2.dispose();

            return new ImageIcon(dest);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}