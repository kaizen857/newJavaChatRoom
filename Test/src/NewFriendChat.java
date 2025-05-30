import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.Date;

public class NewFriendChat extends JFrame {
    private JPanel mainPanel;
    private JList<String> friendList;
    private JPanel chatPanel;
    private JScrollPane chatScrollPane;
    private JTextArea inputArea;
    private JButton sendButton;

    public NewFriendChat() {
        setTitle("优化版聊天室");
        setSize(900, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
        setVisible(true);
    }

    private void initComponents() {
        mainPanel = new JPanel(new BorderLayout(5, 5));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(new Color(240, 242, 245));

        // 左侧好友列表
        String[] friends = {"张三", "李四", "王五", "赵六", "钱七"};
        friendList = new JList<>(friends);
        friendList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        friendList.setFixedCellHeight(60);
        friendList.setCellRenderer(new FriendListRenderer());

        JScrollPane friendScrollPane = new JScrollPane(friendList);
        friendScrollPane.setPreferredSize(new Dimension(180, 0));
        friendScrollPane.setBorder(BorderFactory.createEmptyBorder());

        // 右侧聊天区域
        chatPanel = new JPanel() {
            @Override
            public Dimension getPreferredSize() {
                // 确保面板足够宽以显示最大气泡
                Dimension d = super.getPreferredSize();
                d.width = getParent().getWidth();
                return d;
            }
        };
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        chatPanel.setBackground(new Color(240, 242, 245));

        chatScrollPane = new JScrollPane(chatPanel);
        chatScrollPane.setBorder(BorderFactory.createEmptyBorder());
        chatScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        chatScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // 底部输入区域
        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        inputPanel.setBackground(new Color(240, 242, 245));

        inputArea = new JTextArea(4, 20);
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        inputArea.setFont(new Font("微软雅黑", Font.PLAIN, 14));

        JScrollPane inputScrollPane = new JScrollPane(inputArea);
        inputScrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));

        sendButton = new JButton("发送");
        sendButton.setPreferredSize(new Dimension(80, 60));
        sendButton.setBackground(new Color(0, 191, 255));
        sendButton.setForeground(Color.WHITE);
        sendButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        sendButton.setFocusPainted(false);
        sendButton.addActionListener(e -> sendMessage());

        inputPanel.add(inputScrollPane, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        //mainPanel.add(friendScrollPane, BorderLayout.WEST);
        mainPanel.add(chatScrollPane, BorderLayout.CENTER);
        mainPanel.add(inputPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    // 好友列表渲染器
    class FriendListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            // 设置头像和文本布局
            setIcon(createRoundAvatar(getRandomColorAvatar(value.toString()), 50));
            setText("<html><b>" + value + "</b><br/><small>最后消息...</small></html>");
            setVerticalTextPosition(SwingConstants.BOTTOM);
            setHorizontalTextPosition(SwingConstants.RIGHT);
            setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

            if (isSelected) {
                setBackground(new Color(220, 240, 255));
                setForeground(Color.BLACK);
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }

            return this;
        }
    }

    // 创建圆形头像
    private ImageIcon createRoundAvatar(ImageIcon icon, int diameter) {
        BufferedImage image = new BufferedImage(diameter, diameter, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 创建圆形裁剪区域
        Ellipse2D.Double circle = new Ellipse2D.Double(0, 0, diameter, diameter);
        g2.setClip(circle);

        // 绘制缩放后的图像
        g2.drawImage(icon.getImage(), 0, 0, diameter, diameter, null);

        // 添加边框
        g2.setStroke(new BasicStroke(1.5f));
        g2.setColor(new Color(200, 200, 200));
        g2.drawOval(0, 0, diameter-1, diameter-1);

        g2.dispose();
        return new ImageIcon(image);
    }

    // 生成随机颜色头像(模拟)
    private ImageIcon getRandomColorAvatar(String name) {
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();

        // 根据名字生成确定性颜色
        int hash = name.hashCode();
        Color bgColor = new Color(
                Math.abs(hash % 200 + 55),
                Math.abs((hash >> 8) % 200 + 55),
                Math.abs((hash >> 16) % 200 + 55)
        );

        g2.setColor(bgColor);
        g2.fillRect(0, 0, 100, 100);

        // 绘制名字首字母
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("微软雅黑", Font.BOLD, 40));
        String initial = name.substring(0, 1).toUpperCase();

        FontMetrics fm = g2.getFontMetrics();
        int x = (100 - fm.stringWidth(initial)) / 2;
        int y = ((100 - fm.getHeight()) / 2) + fm.getAscent();

        g2.drawString(initial, x, y);
        g2.dispose();

        return new ImageIcon(image);
    }

    // 发送消息
    private void sendMessage() {
        String message = inputArea.getText().trim();
        if (!message.isEmpty()) {
            ImageIcon selfAvatar = getRandomColorAvatar("我");
            addMessage(createRoundAvatar(selfAvatar, 40), message, true, new Date());
            inputArea.setText("");

            // 模拟回复
            if (Math.random() > 0.3) {
                String[] replies = {"好的", "明白了", "嗯嗯", "OK", "收到", "谢谢", "知道了"};
                String reply = replies[(int)(Math.random() * replies.length)];

                Timer timer = new Timer(1000 + (int)(Math.random() * 2000), e -> {
                    ImageIcon friendAvatar = getRandomColorAvatar(friendList.getSelectedValue());
                    addMessage(createRoundAvatar(friendAvatar, 40), reply, false, new Date());
                    ((Timer)e.getSource()).stop();
                });
                timer.setRepeats(false);
                timer.start();
            }
        }
    }

    // 添加消息到聊天区域
    public void addMessage(ImageIcon avatar, String message, boolean isSelf, Date time) {
        MessageBubble bubble = new MessageBubble(avatar, message, isSelf, time);

        // 使用Glue来确保新消息添加到底部
        if (chatPanel.getComponentCount() > 0) {
            chatPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        if (isSelf) {
            bubble.setAlignmentX(Component.LEFT_ALIGNMENT);
        } else {
            bubble.setAlignmentX(Component.RIGHT_ALIGNMENT);
        }

        chatPanel.add(bubble);

        // 强制重新计算布局
        chatPanel.revalidate();
        chatPanel.repaint();

        // 滚动到底部
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = chatScrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new NewFriendChat());
    }
}