import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.*;

public class Main {

    // 机器人列表
    public static List<Robot> robotsList = new ArrayList<>();

    // 记录每种工作台的状态
    public static List<PlatForm> platformsList = new ArrayList<>();

    private static final Scanner inStream = new Scanner(System.in);
    private static final PrintStream outStream = new PrintStream(new BufferedOutputStream(System.out));

    public static void main(String[] args) throws FileNotFoundException {
        PrintStream ps = new PrintStream(new FileOutputStream("./log/log.txt"));
        System.setErr(ps);
        schedule();
    }

    private static void schedule() throws FileNotFoundException {
        // 初始化
        Utils.readMapOK(inStream, robotsList, platformsList); // 读取地图信息 跳过
        System.err.printf("robotsList的长度为%d\n", robotsList.size());
        outStream.println("OK");
        outStream.flush();

        // 每帧交互
        int frameID;
        while (inStream.hasNextLine()) {
            String line = inStream.nextLine();
            String[] parts = line.split(" ");
            frameID = Integer.parseInt(parts[0]); // 获得帧id

            Utils.readFrameOK(inStream, platformsList, robotsList); // 读取该帧信息 更新数据结构
            DefaultMotion dm = new DefaultMotion();
            List<Order> orderList = new ArrayList<>();
            for (Robot robot : robotsList) {
                orderList.addAll(dm.Move(robot, platformsList));
            }
            outStream.printf("%d\n", frameID);
            for (Order order : orderList) {
                order.printOrder(outStream);
            }
            outStream.print("OK\n");
            outStream.flush();
        }
    }


}
