import java.io.BufferedOutputStream;
import java.io.PrintStream;
import java.util.*;

public class Main {

    // 机器人列表
    public static List<Robot> robotsList = new ArrayList<>();

    // 记录每种工作台的状态
    public static List<PlatForm> platformsList = new ArrayList<>();

    private static final Scanner inStream = new Scanner(System.in);
    private static final PrintStream outStream = new PrintStream(new BufferedOutputStream(System.out));

    public static void main(String[] args) {
        schedule();
    }

    private static void schedule() {
        // 初始化
        Utils.readMapOK(inStream, robotsList, platformsList); // 读取地图信息 跳过
        outStream.println("OK");
        outStream.flush();

        // 每帧交互
        int frameID;
        while (inStream.hasNextLine()) {
            String line = inStream.nextLine();
            String[] parts = line.split(" ");
            frameID = Integer.parseInt(parts[0]);
            Utils.readFrameOK(inStream, platformsList, robotsList); // 读取该帧信息 更新数据结构
            Utils.findTargetForRobot(platformsList, robotsList);    // 处理机器人运动问题

            outStream.printf("%d\n", frameID);
//            outStream.printf("forward %d %d\n", robotId, lineSpeed);
//            outStream.printf("rotate %d %f\n", robotId, angleSpeed);
            outStream.print("OK\n");
            outStream.flush();
        }
    }


}
