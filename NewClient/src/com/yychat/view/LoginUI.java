package com.yychat.view;

import cn.hutool.crypto.digest.DigestUtil;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;
import com.yychat.control.MessageHandler;
import com.yychat.control.ShutdownHandler;
import com.yychat.model.UserInfoList;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.Arrays;
import javax.imageio.ImageIO;

public class LoginUI extends JFrame {

    private static final int ANIMATION_DURATION = 400; // 动画持续时间（毫秒）
    private static final int PANEL_WIDTH = 400; // 面板宽度

    private JPanel mainContainerPanel; // 用于容纳 imagePanel 和 animatingPanel
    private JPanel imagePanel;
    private JPanel animatingPanel; // 用于承载登录/注册面板，实现动画
    private JPanel loginContentPanel; // 登录面板的实际内容
    private JPanel registerContentPanel; // 注册面板的实际内容

    private Timer animationTimer;

    private int animationStep;
    private boolean isLoginState; // 当前是否处于登录状态
    private boolean transitioningToLogin; // 动画目标：true为登录，false为注册

    private BufferedImage originalImage;
    private Image scaledImage;

    private UserInfoList userInfoList = new UserInfoList();
    private final File userInfoFile = new File("./userInfo.dat");

    public LoginUI() {
        if(userInfoFile.exists()){
            try (FileInputStream fileIn = new FileInputStream(userInfoFile);
                 ObjectInputStream in = new ObjectInputStream(fileIn)) {
                userInfoList = (UserInfoList) in.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        else{
            userInfoList = new UserInfoList();
            try {
                userInfoFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        setTitle("登录聊天室");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 500);
        setLocationRelativeTo(null); // 居中显示
        setResizable(false);

        // 设置 FlatLaf 主题
        FlatLaf.setup(new FlatMacLightLaf()); // 可以尝试 FlatLightLaf, FlatDarkLaf 等

        isLoginState = true; // 初始状态为登录
        createUI();
        initAnimation();


    }

    private void createUI() {
        mainContainerPanel = new JPanel(new BorderLayout());

        imagePanel = new ImagePanel();
        try {
            URL imageUrl = getClass().getResource("/image.png"); // 尝试从资源加载
            if (imageUrl != null) {
                originalImage = ImageIO.read(imageUrl);
            } else {
                originalImage = ImageIO.read(new java.io.File("images/image.png")); // 尝试从文件系统加载
            }
            if (originalImage != null) {
                scaledImage = originalImage.getScaledInstance(PANEL_WIDTH, getHeight(), Image.SCALE_SMOOTH);
            } else {
                System.err.println("Error: Image not found. Please ensure 'image.png' is in the correct path or resources.");
                imagePanel.setBackground(Color.LIGHT_GRAY);
                JLabel noImageLabel = new JLabel("No Image Found", SwingConstants.CENTER);
                noImageLabel.setForeground(Color.RED);
                imagePanel.add(noImageLabel);
            }
        } catch (IOException e) {
            e.printStackTrace();
            imagePanel.setBackground(Color.LIGHT_GRAY);
            JLabel errorLabel = new JLabel("Error loading image", SwingConstants.CENTER);
            errorLabel.setForeground(Color.RED);
            imagePanel.add(errorLabel);
        }
        imagePanel.setOpaque(false); // Make image panel transparent for alpha blending (although alpha blending is now constant 1.0f)
        imagePanel.setPreferredSize(new Dimension(PANEL_WIDTH, getHeight()));


        animatingPanel = new JPanel(null);
        animatingPanel.setPreferredSize(new Dimension(PANEL_WIDTH, getHeight()));
        animatingPanel.setOpaque(false);

        loginContentPanel = createLoginPanel();
        registerContentPanel = createRegisterPanel();

        animatingPanel.add(loginContentPanel);
        animatingPanel.add(registerContentPanel);

        loginContentPanel.setBounds(0, 0, PANEL_WIDTH, getHeight());
        registerContentPanel.setBounds(-PANEL_WIDTH, 0, PANEL_WIDTH, getHeight());
        registerContentPanel.setVisible(false); // 注册面板初始不可见

        mainContainerPanel.add(imagePanel, BorderLayout.WEST);
        mainContainerPanel.add(animatingPanel, BorderLayout.CENTER);

        add(mainContainerPanel);

        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                if (originalImage != null) {
                    scaledImage = originalImage.getScaledInstance(PANEL_WIDTH, getHeight(), Image.SCALE_SMOOTH);
                    imagePanel.setPreferredSize(new Dimension(PANEL_WIDTH, getHeight()));
                    imagePanel.revalidate();
                    imagePanel.repaint();
                }
                animatingPanel.setPreferredSize(new Dimension(PANEL_WIDTH, getHeight()));
                animatingPanel.setBounds(animatingPanel.getX(), animatingPanel.getY(), PANEL_WIDTH, getHeight());
                loginContentPanel.setBounds(loginContentPanel.getX(), 0, PANEL_WIDTH, getHeight());
                registerContentPanel.setBounds(registerContentPanel.getX(), 0, PANEL_WIDTH, getHeight());
                animatingPanel.revalidate();
            }
        });
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.putClientProperty(FlatClientProperties.STYLE, "background: #f0f0f0; arc: 20");
        panel.setPreferredSize(new Dimension(PANEL_WIDTH, getHeight()));

        GridBagConstraints gbc = new GridBagConstraints();
        // Default insets for components
        gbc.insets = new Insets(10, 10, 10, 10);

        // --- Title ---
        JLabel titleLabel = new JLabel("登录", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3; // Span all 3 columns
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0; // Allow title area to use full width
        panel.add(titleLabel, gbc);

        // Reset defaults for subsequent rows
        gbc.gridwidth = 1;
        gbc.weightx = 0.0; // Default: columns do not expand unless specified

        // --- Username Row ---
        JLabel usernameLabel = new JLabel("用户名:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE; // Labels don't fill their cell
        gbc.anchor = GridBagConstraints.LINE_START; // Align labels to the left
        gbc.weightx = 0.0; // Column 0 (labels) should not expand
        panel.add(usernameLabel, gbc);

        JTextField usernameField = new JTextField(15);
        if (userInfoList != null && userInfoList.getLastUsedName() != null) {
            usernameField.setText(userInfoList.getLastUsedName());
        }
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 2; // Username field spans column 1 and 2
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0; // Columns 1 & 2 together will take available horizontal space
        panel.add(usernameField, gbc);

        // --- Password Row ---
        // Reset common properties for the new row components
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.LINE_START;

        JLabel passwordLabel = new JLabel("密码:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0; // Column 0 (labels)
        panel.add(passwordLabel, gbc);

        JPasswordField passwordField = new JPasswordField(15);
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0; // Column 1 (password field) should expand
        gbc.insets = new Insets(10, 10, 10, 5); // Adjust right inset to be closer to forgot button
        panel.add(passwordField, gbc);

        JButton forgotPasswordButton = new JButton("忘记密码");
        forgotPasswordButton.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_BORDERLESS);
        Color linkColor = UIManager.getColor("Component.linkColor");
        if (linkColor == null) {
            linkColor = new Color(0, 102, 204);
        }
        forgotPasswordButton.setForeground(linkColor);
        forgotPasswordButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        // Optional: Make button more compact
        // forgotPasswordButton.setMargin(new Insets(0, 0, 0, 0));
        // forgotPasswordButton.setFont(forgotPasswordButton.getFont().deriveFont(Font.PLAIN, forgotPasswordButton.getFont().getSize() - 1f));


        gbc.gridx = 2;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE; // Button takes its preferred size
        gbc.anchor = GridBagConstraints.LINE_START; // Align button to the left of its cell
        gbc.weightx = 0.0; // Column 2 (button) should not expand significantly
        gbc.insets = new Insets(10, 5, 10, 10); // Left inset 5px (from password field), right inset 10px
        panel.add(forgotPasswordButton, gbc);

        forgotPasswordButton.addActionListener(e -> {
            // TODO: 实现忘记密码逻辑
            JOptionPane.showMessageDialog(panel, "忘记密码功能暂未开放。", "提示", JOptionPane.INFORMATION_MESSAGE);
//            String userName = JOptionPane.showInputDialog(panel,"请输入用户名","忘记密码",JOptionPane.PLAIN_MESSAGE);
//            if(userName != null) {
//                MessageHandler messageHandler = MessageHandler.getInstance(userName);
//                if(messageHandler.confirmHasUser(userName)){
//                    //TODO:修改密码
//                }
//                else{
//                    JOptionPane.showMessageDialog(panel, "没有该用户！", "", JOptionPane.ERROR_MESSAGE);
//                }
//            }
        });

        // Reset insets to default for subsequent components
        gbc.insets = new Insets(10, 10, 10, 10);

        // --- Login Button ---
        JButton loginButton = new JButton("登录");
        loginButton.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_ROUND_RECT);
        loginButton.putClientProperty(FlatClientProperties.STYLE, "font: bold; background: #60A5FA; foreground: #FFFFFF;");
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 3; // Span all 3 columns
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0; // Allow button to use full width
        panel.add(loginButton, gbc);

        passwordField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    loginButton.doClick();
                }
            }
        });

        // --- Register Button ---
        JButton registerButton = new JButton("注册");
        registerButton.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_ROUND_RECT);
        registerButton.putClientProperty(FlatClientProperties.STYLE, "background: #818CF8; foreground: #FFFFFF;");
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 3; // Span all 3 columns
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0; // Allow button to use full width
        panel.add(registerButton, gbc);

        // Action Listeners (ensure password retrieval is correct)
        loginButton.addActionListener(e -> {
            //TODO:登录
            String username = usernameField.getText();
            String passwordInput = new String(passwordField.getPassword());
            String passwordSHA256 = DigestUtil.sha256Hex(passwordInput);
            Arrays.fill(passwordField.getPassword(), '0'); // Clear password from memory

            MessageHandler tmp = MessageHandler.getInstance(username);
            if(!tmp.hasStart()){
                tmp.start();
            }
            if(tmp.login(username, passwordSHA256)){
                try(ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(userInfoFile))) {
                    userInfoList.setLastUsedName(username);
                    out.writeObject(userInfoList);
                    JOptionPane.showMessageDialog(rootPane, "登录成功！");
                    this.dispose();
                    FlatLaf.setup(new FlatLightLaf()); // Re-apply or ensure theme consistency
                    SwingUtilities.invokeLater(() -> {
                        new ChatUI(userInfoList).setVisible(true);
                    });
                } catch (IOException ex) {
                    // Log error or show user-friendly message
                    ex.printStackTrace(); // For debugging
                    JOptionPane.showMessageDialog(rootPane, "处理用户信息时出错: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
            else{
                JOptionPane.showMessageDialog(rootPane, "登录失败！");
            }
        });

        registerButton.addActionListener(e -> {
            if (isLoginState) {
                transitioningToLogin = false;
                startAnimation();
            }
        });

        return panel;
    }

    private JPanel createRegisterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.putClientProperty(FlatClientProperties.STYLE, "background: #f0f0f0; arc: 20");
        panel.setPreferredSize(new Dimension(PANEL_WIDTH, getHeight()));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("注册", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);

        JLabel usernameLabel = new JLabel("用户名:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        panel.add(usernameLabel, gbc);

        JTextField usernameField = new JTextField(15);
        gbc.gridx = 1;
        gbc.gridy = 1;
        panel.add(usernameField, gbc);

        JLabel passwordLabel = new JLabel("密码:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(passwordLabel, gbc);

        JPasswordField passwordField = new JPasswordField(15);
        gbc.gridx = 1;
        gbc.gridy = 2;
        panel.add(passwordField, gbc);

        JLabel confirmPasswordLabel = new JLabel("确认密码:");
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(confirmPasswordLabel, gbc);

        JPasswordField confirmPasswordField = new JPasswordField(15);
        gbc.gridx = 1;
        gbc.gridy = 3;
        panel.add(confirmPasswordField, gbc);

        JButton registerButton = new JButton("注册");
        registerButton.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_ROUND_RECT);
        registerButton.putClientProperty(FlatClientProperties.STYLE, "font: bold; background: #60A5FA; foreground: #FFFFFF;");
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        panel.add(registerButton, gbc);

        JButton loginButton = new JButton("返回登录");
        loginButton.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_ROUND_RECT);
        loginButton.putClientProperty(FlatClientProperties.STYLE, "background: #818CF8; foreground: #FFFFFF;");
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        panel.add(loginButton, gbc);

        registerButton.addActionListener(e -> {
            String userName = usernameField.getText();
            String password = new String(passwordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());
            if(password.equals(confirmPassword)){
                String passwordSHA256 = DigestUtil.sha256Hex(password);
                MessageHandler tmp = MessageHandler.getInstance(userName);
                tmp.start();
                if(tmp.register(userName, passwordSHA256)){
                    JOptionPane.showMessageDialog(rootPane, "注册成功！");
                    userInfoList.addUserInfo(userName,password,"/avatars/default_avatar.png");
                    try(ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(userInfoFile))) {
                        userInfoList.setLastUsedName(userName);
                        out.writeObject(userInfoList);
                    } catch (FileNotFoundException ex) {
                        throw new RuntimeException(ex);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
                else{
                    JOptionPane.showMessageDialog(rootPane, "注册失败！");
                }
            }
            else{
                JOptionPane.showMessageDialog(rootPane, "密码不一致！");
            }
        });
        loginButton.addActionListener(e -> {
            if (!isLoginState) {
                transitioningToLogin = true;
                startAnimation();
            }
        });

        return panel;
    }

    private void initAnimation() {
        animationTimer = new Timer(10, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                animationStep++;
                double progress = (double) animationStep / (ANIMATION_DURATION / 10);

                if (progress > 1.0) {
                    progress = 1.0;
                    animationTimer.stop();
                    // 动画结束，根据目标状态切换面板的可见性
                    if (transitioningToLogin) {
                        loginContentPanel.setVisible(true);
                        registerContentPanel.setVisible(false);
                        isLoginState = true;
                    } else {
                        loginContentPanel.setVisible(false);
                        registerContentPanel.setVisible(true);
                        isLoginState = false;
                    }
                    return;
                }
                // 缓动函数 (easeInOutQuad)
                double easedProgress;
                if (progress < 0.5) {
                    easedProgress = 2 * progress * progress;
                } else {
                    easedProgress = 1 - Math.pow(-2 * progress + 2, 2) / 2;
                }
                int loginPanelX;
                int registerPanelX;
                if (transitioningToLogin) { // 从注册到登录
                    registerPanelX = (int) (PANEL_WIDTH * easedProgress);
                    loginPanelX = (int) (-PANEL_WIDTH + PANEL_WIDTH * easedProgress);
                } else { // 从登录到注册
                    loginPanelX = (int) (-PANEL_WIDTH * easedProgress);
                    registerPanelX = (int) (-PANEL_WIDTH + PANEL_WIDTH * easedProgress);
                }
                loginContentPanel.setLocation(loginPanelX, 0);
                registerContentPanel.setLocation(registerPanelX, 0);
                animatingPanel.revalidate();
                animatingPanel.repaint();
            }
        });
    }

    // 启动动画
    private void startAnimation() {
        animationStep = 0;
        if (transitioningToLogin) {
            loginContentPanel.setLocation(-PANEL_WIDTH, 0);
            registerContentPanel.setLocation(0, 0);
            loginContentPanel.setVisible(true);
            registerContentPanel.setVisible(true);
        } else {
            loginContentPanel.setLocation(0, 0);
            registerContentPanel.setLocation(PANEL_WIDTH, 0);
            loginContentPanel.setVisible(true);
            registerContentPanel.setVisible(true);
        }
        animationTimer.start();
    }

    // 自定义 JPanel 来绘制图像
    private class ImagePanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (scaledImage != null) {
                // 直接绘制图像，不再设置 AlphaComposite
                g.drawImage(scaledImage, 0, 0, getWidth(), getHeight(), this);
            }
        }

        @Override
        public Dimension getPreferredSize() {
            if (scaledImage != null) {
                return new Dimension(scaledImage.getWidth(this), scaledImage.getHeight(this));
            }
            return super.getPreferredSize();
        }
    }

    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new ShutdownHandler());
        SwingUtilities.invokeLater(() -> {
            LoginUI frame = new LoginUI();
            frame.setVisible(true);
        });
    }
}