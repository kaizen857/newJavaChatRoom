package com.yychat.control;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;

public class ImageLoaderUtil {

    private static final long MAX_SIZE_BYTES = 20 * 1024; // 1MB

    /**
     * 弹出文件选择器加载图像，进行格式转换和压缩处理。
     *
     * @param parentComponent 对话框的父组件，可以为null
     * @param title 对话框标题
     * @return 处理后的图像字节数组 (PNG格式, 小于1MB)，如果用户取消选择或发生错误则返回null
     */
    public static byte[] loadImageAndProcess(Component parentComponent,String title) {
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "图像文件 (jpg, jpeg, png, gif, bmp)", "jpg", "jpeg", "png", "gif", "bmp");
        fileChooser.setFileFilter(filter);
        fileChooser.setAcceptAllFileFilterUsed(false); // 不显示 "All Files" 选项
        fileChooser.setDialogTitle(title);
        int returnValue = fileChooser.showOpenDialog(parentComponent);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            // 1. 检查是否是图像文件
            BufferedImage image = null;
            String originalFormatName = null;
            try {
                // 尝试读取图像以验证
                image = ImageIO.read(selectedFile);
                if (image == null) {
                    JOptionPane.showMessageDialog(parentComponent,
                            "选择的文件不是有效的图像格式。",
                            "文件类型错误", JOptionPane.ERROR_MESSAGE);
                    return null;
                }
                // 获取原始格式
                originalFormatName = getFormatName(selectedFile);
                if (originalFormatName == null) { // 双重检查
                    JOptionPane.showMessageDialog(parentComponent,
                            "无法识别图像文件格式。",
                            "文件类型错误", JOptionPane.ERROR_MESSAGE);
                    return null;
                }

            } catch (IOException e) {
                JOptionPane.showMessageDialog(parentComponent,
                        "读取图像文件时发生错误: " + e.getMessage(),
                        "读取错误", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
                return null;
            }

            // 图像已成功加载到 BufferedImage image 中

            byte[] imageBytes;

            // 2. 转换为PNG格式 (如果需要)
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                // 无论原始格式如何，都统一尝试写入为PNG
                // 这也处理了原始就是PNG的情况，只是简单地重新编码
                if (!ImageIO.write(image, "png", baos)) {
                    JOptionPane.showMessageDialog(parentComponent,
                            "无法将图像转换为PNG格式。",
                            "格式转换错误", JOptionPane.ERROR_MESSAGE);
                    return null;
                }
                imageBytes = baos.toByteArray();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(parentComponent,
                        "转换为PNG时发生IO错误: " + e.getMessage(),
                        "转换错误", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
                return null;
            }
            // 3. 检查大小并压缩 (如果需要)
            if (imageBytes.length > MAX_SIZE_BYTES) {
                System.out.println("图像大小超过1MB (" + imageBytes.length + " bytes)，开始压缩...");
                try {
                    imageBytes = compressImage(image, imageBytes.length, "png", MAX_SIZE_BYTES);
                    System.out.println("压缩后大小: " + imageBytes.length + " bytes");
                    if (imageBytes.length > MAX_SIZE_BYTES) {
                        System.out.println("警告: 图像压缩后仍大于1MB。");
                        JOptionPane.showMessageDialog(parentComponent,
                                "图像压缩后仍大于1MB，可能无法进一步无损压缩PNG。",
                                "压缩提醒", JOptionPane.WARNING_MESSAGE);
                    }
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(parentComponent,
                            "压缩图像时发生错误: " + e.getMessage(),
                            "压缩错误", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                    return null; // 返回null表示压缩失败
                }
            }
            return imageBytes;
        }
        return null; // 用户取消选择
    }

    /**
     * 获取图像文件的实际格式名称。
     * @param file 图像文件
     * @return 格式名称 (例如 "png", "jpeg")，如果无法确定则返回null
     */
    private static String getFormatName(File file) {
        try (ImageInputStream iis = ImageIO.createImageInputStream(file)) {
            if (iis == null) return null;
            Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
            if (readers.hasNext()) {
                ImageReader reader = readers.next();
                return reader.getFormatName().toLowerCase(Locale.ROOT);
            }
        } catch (IOException e) {
            System.err.println("获取图像格式时出错: " + e.getMessage());
        }
        return null; // fallback or error
    }

    /**
     * 压缩图像字节直到小于目标大小。
     * 对于PNG，主要是通过降低分辨率来实现。对于JPEG可以调整质量。
     * 此实现主要通过逐步降低图像分辨率来减小PNG文件大小。
     *
     * @param originalImage 原始BufferedImage
     * @param currentSize 当前图像字节大小
     * @param formatName 图像格式 (期望是 "png")
     * @param targetMaxBytes 目标最大字节数
     * @return 压缩后的图像字节数组
     * @throws IOException 如果读写图像时发生错误
     */
    private static byte[] compressImage(BufferedImage originalImage, long currentSize, String formatName, long targetMaxBytes) throws IOException {
        if (!"png".equalsIgnoreCase(formatName)) {
            // 此方法目前主要针对PNG的尺寸缩减
            System.err.println("警告: compressImage 设计用于PNG格式的尺寸缩减。");
        }

        byte[] currentBytes;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(originalImage, formatName, baos);
            currentBytes = baos.toByteArray();
        }

        if (currentBytes.length <= targetMaxBytes) {
            return currentBytes; // 初始大小已符合要求
        }

        BufferedImage imageToCompress = originalImage;
        double scale = 0.9; // 初始缩放比例
        int iterations = 0; // 防止无限循环

        // 当图像大于目标大小时，并且缩放比例仍然有效，并且迭代次数没有过多时
        while (currentBytes.length > targetMaxBytes && scale > 0.1 && iterations < 10) {
            System.out.println("当前大小: " + currentBytes.length + " bytes, 目标: " + targetMaxBytes + " bytes. 缩放比例: " + scale);

            int newWidth = (int) (imageToCompress.getWidth() * scale);
            int newHeight = (int) (imageToCompress.getHeight() * scale);

            if (newWidth <= 0 || newHeight <= 0) { // 防止尺寸过小
                System.err.println("图像尺寸过小，停止压缩。");
                break;
            }

            BufferedImage scaledImage = new BufferedImage(newWidth, newHeight,
                    (formatName.equalsIgnoreCase("png") && originalImage.getType() != 0) ? originalImage.getType() : BufferedImage.TYPE_INT_ARGB); // 保持类型或使用ARGB

            Graphics2D g2d = scaledImage.createGraphics();
            // 使用高质量的缩放算法
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.drawImage(imageToCompress, 0, 0, newWidth, newHeight, null);
            g2d.dispose();

            imageToCompress = scaledImage; // 下一轮压缩使用缩放后的图像

            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                if (!ImageIO.write(imageToCompress, formatName, baos)) {
                    System.err.println("在压缩循环中写入图像失败。");
                    break; // 如果写入失败，则中断
                }
                currentBytes = baos.toByteArray();
            }

            // 调整缩放因子，可以根据需要更精细地调整
            if (currentBytes.length > targetMaxBytes * 1.5) { // 如果还是很大，则缩放幅度大一点
                scale -= 0.15;
            } else {
                scale -= 0.05; // 否则小幅缩放
            }
            scale = Math.max(0.1, scale); //确保scale不会太小
            iterations++;
        }
        System.out.println("最终压缩尝试后大小: " + currentBytes.length + " bytes");
        return currentBytes;
    }

    // --- 示例如何调用 ---
    public static void main(String[] args) {
        // 确保在EDT中运行Swing组件
        SwingUtilities.invokeLater(() -> {
            // 创建一个简单的JFrame作为父组件示例
            JFrame frame = new JFrame("图像加载器测试");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(300, 200);
            frame.setLocationRelativeTo(null);
            // frame.setVisible(true); // 可以不显示，仅作为父组件

            System.out.println("尝试加载图像...");
            byte[] imageData = loadImageAndProcess(frame,"title");

            if (imageData != null) {
                System.out.println("成功加载并处理图像。最终大小: " + imageData.length + " bytes.");
                // 在这里可以使用imageData，例如上传或显示
                // 为了演示，可以尝试将字节数组转换回图像并显示
                try {
                    ByteArrayInputStream bais = new ByteArrayInputStream(imageData);
                    BufferedImage loadedImage = ImageIO.read(bais);
                    if (loadedImage != null) {
                        JLabel picLabel = new JLabel(new ImageIcon(loadedImage));
                        JOptionPane.showMessageDialog(frame, picLabel, "加载的图像", JOptionPane.PLAIN_MESSAGE);
                    } else {
                        System.err.println("无法从处理后的字节数组中重新加载图像。");
                    }
                } catch (IOException e) {
                    System.err.println("从字节数组创建图像时出错: " + e.getMessage());
                    e.printStackTrace();
                }

            } else {
                System.out.println("未能加载或处理图像，或者用户取消了操作。");
            }
            System.exit(0); // 退出测试
        });
    }
}
