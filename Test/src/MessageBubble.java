import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

class MessageBubble extends JPanel {
    private static final int ARC_WIDTH = 20;
    private static final int ARC_HEIGHT = 20;
    private static final int BUBBLE_PADDING = 12;

    private boolean isSelf;
    private ImageIcon avatar;
    private String message;
    private Date time;
    private JTextArea textArea;

    public MessageBubble(ImageIcon avatar, String message, boolean isSelf, Date time) {
        this.avatar = avatar;
        this.message = message;
        this.isSelf = isSelf;
        this.time = time;
        setOpaque(false);
        setLayout(new BorderLayout(8, 0));

        initComponents();
    }

    private void initComponents() {
        // 头像
        JLabel avatarLabel = new JLabel(avatar);
        avatarLabel.setPreferredSize(new Dimension(40, 40));

        // 消息文本和时间
        JPanel textPanel = new JPanel(new BorderLayout()) {
            @Override
            public Dimension getPreferredSize() {
                // 确保文本区域有足够的空间显示完整内容
                Dimension d = super.getPreferredSize();
                d.width = Math.max(d.width, textArea.getPreferredSize().width + 24);
                return d;
            }
        };
        textPanel.setOpaque(false);

        textArea = new JTextArea(message);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        textArea.setOpaque(false);
        textArea.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        textArea.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        JLabel timeLabel = new JLabel(sdf.format(time), SwingConstants.RIGHT);
        timeLabel.setFont(new Font("微软雅黑", Font.PLAIN, 10));
        timeLabel.setForeground(new Color(150, 150, 150));
        timeLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 3, 5));

        textPanel.add(textArea, BorderLayout.CENTER);
        textPanel.add(timeLabel, BorderLayout.SOUTH);

        // 气泡背景
        JPanel bubble = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (isSelf) {
                    // 自己的消息 - 右侧绿色气泡
                    g2.setColor(new Color(154, 205, 50));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight()-5, ARC_WIDTH, ARC_HEIGHT);

                    // 气泡小三角
                    int[] xPoints = {getWidth()-10, getWidth()-10, getWidth()};
                    int[] yPoints = {getHeight()-15, getHeight()-5, getHeight()-10};
                    g2.fillPolygon(xPoints, yPoints, 3);
                } else {
                    // 好友的消息 - 左侧白色气泡
                    g2.setColor(Color.WHITE);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight()-5, ARC_WIDTH, ARC_HEIGHT);

                    // 气泡小三角
                    int[] xPoints = {10, 10, 0};
                    int[] yPoints = {getHeight()-15, getHeight()-5, getHeight()-10};
                    g2.fillPolygon(xPoints, yPoints, 3);
                }

                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                // 气泡大小由文本区域决定
                Dimension textPref = textPanel.getPreferredSize();
                return new Dimension(
                        textPref.width,
                        textPref.height + 5  // 加上小三角的高度
                );
            }
        };

        bubble.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        bubble.add(textPanel, BorderLayout.CENTER);

        if (isSelf) {
            // 自己的消息 - 右侧
            add(bubble, BorderLayout.CENTER);
            add(avatarLabel, BorderLayout.EAST);
        } else {
            // 好友的消息 - 左侧
            add(avatarLabel, BorderLayout.WEST);
            add(bubble, BorderLayout.CENTER);
        }
    }

    @Override
    public Dimension getPreferredSize() {
        // 整个消息气泡的优选大小由内容和头像决定
        Dimension bubbleSize = ((JPanel)getComponent(isSelf ? 0 : 1)).getPreferredSize();
        return new Dimension(
                bubbleSize.width + 48,  // 头像宽度 + 间距
                Math.max(bubbleSize.height, 40)  // 至少和头像一样高
        );
    }

    @Override
    public Dimension getMaximumSize() {
        // 限制最大宽度，但保持高度由内容决定
        Dimension pref = getPreferredSize();
        return new Dimension(
                Math.min(pref.width, 600),  // 最大宽度限制
                pref.height
        );
    }

    @Override
    public Insets getInsets() {
        return new Insets(5, 5, 5, 5);
    }
}