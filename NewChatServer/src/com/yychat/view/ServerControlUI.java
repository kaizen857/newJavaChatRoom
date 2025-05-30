package com.yychat.view;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.yychat.control.Server;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class ServerControlUI extends JFrame {

    private JButton startButton;
    private JButton stopButton;
    private JTextArea logArea;
    private PrintStream originalOut;
    private PrintStream originalErr;
    private boolean serverRunning = false;
    Server server;

    public ServerControlUI() {
        // 设置FlatLaf外观
        try {
            // 使用暗色主题
            FlatDarkLaf.setup();
            // 或者使用亮色主题
            // FlatLightLaf.setup();
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        // 初始化UI
        initUI();

        // 重定向控制台输出
        //redirectConsoleOutput();
    }

    private void initUI() {
        setTitle("服务器控制面板");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 主面板
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        startButton = new JButton("启动服务器");
        stopButton = new JButton("停止服务器");
        stopButton.setEnabled(false);

        // 设置按钮样式
        startButton.setFocusPainted(false);
        stopButton.setFocusPainted(false);
        startButton.setPreferredSize(new Dimension(120, 40));
        stopButton.setPreferredSize(new Dimension(120, 40));

        // 添加按钮监听
        startButton.addActionListener(this::startServer);
        stopButton.addActionListener(this::stopServer);

        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);

        // 日志区域
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setPreferredSize(new Dimension(780, 500));

        // 状态栏
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel statusLabel = new JLabel("服务器状态: 停止");
        statusPanel.add(statusLabel);

        // 更新状态标签
        startButton.addActionListener(e -> statusLabel.setText("服务器状态: 运行中"));
        stopButton.addActionListener(e -> statusLabel.setText("服务器状态: 停止"));

        // 添加组件到主面板
        mainPanel.add(buttonPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(statusPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void redirectConsoleOutput() {
        originalOut = System.out;
        originalErr = System.err;

        OutputStream out = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                appendToLogArea(String.valueOf((char) b));
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                appendToLogArea(new String(b, off, len));
            }

            @Override
            public void write(byte[] b) throws IOException {
                write(b, 0, b.length);
            }
        };

        System.setOut(new PrintStream(out, true));
        System.setErr(new PrintStream(out, true));
    }

    private void appendToLogArea(String text) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(text);
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    private void startServer(ActionEvent e) {
        if (serverRunning) {
            JOptionPane.showMessageDialog(this, "服务器已经在运行!", "警告", JOptionPane.WARNING_MESSAGE);
            return;
        }

        serverRunning = true;
        startButton.setEnabled(false);
        stopButton.setEnabled(true);
        if(server == null) {
            server = new Server();
        }
        server.start();
    }

    private void stopServer(ActionEvent e) {
        if (!serverRunning) {
            JOptionPane.showMessageDialog(this, "服务器未运行!", "警告", JOptionPane.WARNING_MESSAGE);
            return;
        }

        serverRunning = false;
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
        System.out.println("正在停止服务器...");
        // 中断服务器线程
        server.interrupt();


    }

    @Override
    public void dispose() {
        // 恢复原始输出流
        System.setOut(originalOut);
        System.setErr(originalErr);
        super.dispose();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ServerControlUI frame = new ServerControlUI();
            frame.setVisible(true);
        });
    }
}
