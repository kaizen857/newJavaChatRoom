package com.yychat.view;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

class RoundedAvatar extends JPanel {
    private Image image;
    private Color placeholderColor;
    private int diameter;
    private static Image defaultPlaceholderImage;

    static {
        try {
            defaultPlaceholderImage = new BufferedImage(80, 80, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = (Graphics2D) defaultPlaceholderImage.getGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.fillOval(0, 0, 79, 79);
            g2d.setColor(Color.GRAY);
            g2d.setFont(new Font("SansSerif", Font.BOLD, 30));
            FontMetrics fm = g2d.getFontMetrics();
            String initials = "U"; // Default initial
            g2d.drawString(initials, (80 - fm.stringWidth(initials)) / 2, (80 - fm.getHeight()) / 2 + fm.getAscent());
            g2d.dispose();
        } catch (Exception e) {
            System.err.println("Failed to create default placeholder image: " + e.getMessage());
            defaultPlaceholderImage = null;
        }
    }

    public RoundedAvatar(int diameter, String imagePath) {
        this.diameter = Math.min(diameter, 80);
        setPreferredSize(new Dimension(this.diameter, this.diameter));
        setOpaque(false);

        if (imagePath != null && !imagePath.trim().isEmpty()) {
            try {
                // 1. 先尝试从类路径加载
                URL imgUrl = getClass().getResource("/" + imagePath); // 注意开头的 "/"
                if (imgUrl != null) {
                    this.image = new ImageIcon(imgUrl).getImage();
                } else {
                    // 2. 类路径失败后，尝试文件系统路径
                    Path fsPath = Paths.get(imagePath);
                    if (Files.exists(fsPath)) {
                        this.image = new ImageIcon(fsPath.toString()).getImage();
                    } else {
                        throw new FileNotFoundException("Image not found in classpath or filesystem: " + imagePath);
                    }
                }
            } catch (Exception e) {
                System.err.println("Error loading avatar: " + e.getMessage());
                this.image = defaultPlaceholderImage;
            }
        }

        if (this.image == null) { // Ultimate fallback if static placeholder also somehow failed
            this.placeholderColor = new Color((int) (Math.random() * 0xFFFFFF));
        }
    }

    public RoundedAvatar(int diameter, Color placeholderColor) {
        this.diameter = Math.min(diameter, 80);
        this.placeholderColor = placeholderColor;
        this.image = null;
        setPreferredSize(new Dimension(this.diameter, this.diameter));
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        Ellipse2D.Float ellipse = new Ellipse2D.Float(0, 0, diameter - 1, diameter - 1);
        g2d.setClip(ellipse);

        if (image != null) {
            g2d.drawImage(image, 0, 0, diameter, diameter, this);
        } else if (placeholderColor != null) {
            g2d.setColor(placeholderColor);
            g2d.fillRect(0, 0, diameter, diameter);
        } else { // Should not be reached if defaultPlaceholderImage is properly set
            g2d.setColor(Color.DARK_GRAY);
            g2d.fillRect(0, 0, diameter, diameter);
        }
        g2d.dispose();
    }
}