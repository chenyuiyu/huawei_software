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

    // 任务调度队列【根据此队列分发任务】
    public static PriorityQueue<Task> taskQueue = new PriorityQueue<>((a, b) -> {
        int diff = a.getPriority() - b.getPriority();
        return Integer.compare(diff, 0);

    });

    // 各类型工作台的集合
    public static List<List<PlatForm>> labelPlatforms = new ArrayList<>();

    private static final Scanner inStream = new Scanner(System.in);
    private static final PrintStream outStream = new PrintStream(new BufferedOutputStream(System.out));

    public static void main(String[] args) throws FileNotFoundException {
        PrintStream ps = new PrintStream(new FileOutputStream("./log/log.txt"));
        System.setErr(ps);
        schedule();
    }

    private static void schedule() throws FileNotFoundException {
        // 初始化
        Utils.readMapOK(inStream, robotsList, platformsList, taskQueue); // 读取地图信息 跳过
        Utils.initStructure(labelPlatforms, platformsList); // 分类工作台
        for (List<PlatForm> labelPlatform : labelPlatforms) {
            System.err.println("labelPlatform:" + labelPlatform.size());
        }
        outStream.println("OK");
        outStream.flush();

        // 每帧交互
        int frameID;
        while (inStream.hasNextLine()) {
            String line = inStream.nextLine();
            String[] parts = line.split(" ");
            frameID = Integer.parseInt(parts[0]); // 获得帧id

            Utils.readFrameOK(inStream, platformsList, robotsList, taskQueue); // 读取该帧信息 更新数据结构
            DefaultMotion dm = new DefaultMotion();
            List<Order> orderList = new ArrayList<>();
            for (Robot robot : robotsList) {
                orderList.addAll(dm.Move(robot, platformsList, labelPlatforms, taskQueue));
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
