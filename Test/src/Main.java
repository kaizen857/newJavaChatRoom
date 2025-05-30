import javax.swing.*;
import java.awt.*;
import java.util.Date;

//TIP 要<b>运行</b>代码，请按 <shortcut actionId="Run"/> 或
// 点击装订区域中的 <icon src="AllIcons.Actions.Execute"/> 图标。
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            NewFriendChat chatUI = new NewFriendChat();
            chatUI.setVisible(true);

            // 加载头像图片
            ImageIcon selfAvatar = new ImageIcon("./assert/1.jpg"); // 替换为实际路径
            ImageIcon friendAvatar = new ImageIcon("./assert/2.jpg"); // 替换为实际路径

            // 调整头像大小
            selfAvatar = new ImageIcon(selfAvatar.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH));
            friendAvatar = new ImageIcon(friendAvatar.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH));

            // 添加测试消息
            chatUI.addMessage(friendAvatar, "你好，最近怎么样？", false,new Date(System.currentTimeMillis()));
            chatUI.addMessage(selfAvatar, "我很好，谢谢关心！", true,new Date(System.currentTimeMillis() + 10));
            chatUI.addMessage(friendAvatar, "周末有空一起吃饭吗？", false,new Date(System.currentTimeMillis() + 20));
            chatUI.addMessage(selfAvatar, "好啊，时间地点你定吧。", true,new Date(System.currentTimeMillis() + 30));
        });
    }
}