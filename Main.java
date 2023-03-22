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

    /**
     * 生产队列 对于非7分解的4，5，6，我们应该将任务加到生产链表
     * 生产链表中元素本质上是一个没有指定SellPlatformID的fetch型任务
     * productionsList.get(i)表示获取产品i的生产链表
     * 而非任务队列
     */
    public static LinkedList<LinkedList<Task>> productionsList = new LinkedList<>();

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
        for (int i = 0; i < 4; i++) {
            int ind = 0;
            for (int j = 0; j < 4; j++) {
                if (i == j)
                    continue;
                robotsList.get(i).setrobotGroup(ind, robotsList.get(j));
                ind++;
            }
        }
        // 初始化生产链表
        for (int i = 0; i < 8; i++) {
            productionsList.add(new LinkedList<>());
        }
        outStream.println("OK");
        outStream.flush();

        // 每帧交互
        int frameID;
        while (inStream.hasNextLine()) {
            String line = inStream.nextLine();
            String[] parts = line.split(" ");
            frameID = Integer.parseInt(parts[0]); // 获得帧id
            for (int i = 0; i < 4; i++) {
                robotsList.get(i).addRealArriveFrame(1);
            }
            Utils.curFrameID = frameID;
            Utils.readFrameOK(inStream, platformsList, robotsList, taskQueue); // 读取该帧信息 更新数据结构
            DefaultMotion dm = new DefaultMotion();
            List<Order> orderList = new ArrayList<>();
            for (Robot robot : robotsList) {
                orderList.addAll(dm.Move(robot, platformsList, labelPlatforms, taskQueue));
            }

            // print
            System.err.printf("FrameId: %d\n", Utils.curFrameID);
            for (Robot robot : robotsList) {
                System.err.printf("robot%d: 当前目前地: %d, 下一目的地： %d ", robot.getNum(), robot.getTargetPlatFormIndex(),
                        robot.getNextTargetPlatformIndex());
                if (robot.getStatus()) System.err.printf("当前机器人在卖东西\n");
                else System.err.printf("当前机器人在买东西\n");
            }
            // print

            outStream.printf("%d\n", frameID);
            for (Order order : orderList) {
                order.printOrder(outStream);
            }
            outStream.print("OK\n");
            outStream.flush();
        }
    }


}
