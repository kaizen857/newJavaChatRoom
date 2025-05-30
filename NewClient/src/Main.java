import java.sql.Timestamp;
import java.util.Date;

//TIP 要<b>运行</b>代码，请按 <shortcut actionId="Run"/> 或
// 点击装订区域中的 <icon src="AllIcons.Actions.Execute"/> 图标。
public class Main {
    public static void main(String[] args) {
        Date nowTime = new Date();
        Timestamp timestamp = new java.sql.Timestamp(nowTime.getTime());
        System.out.println(nowTime.getTime());
        System.out.println(timestamp);
    }
}