import java.io.FileNotFoundException;
import java.io.BufferedOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {

    private static final Scanner inStream = new Scanner(System.in);

    private static final PrintStream outStream = new PrintStream(new BufferedOutputStream(System.out));

    public static void main(String[] args) {
        try {
            PrintStream print = new PrintStream("E:\\test.txt"); // 写好输出位置文件；
            System.setOut(print);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        schedule();
    }

    private static void schedule() {
        Robot[] robotsList = new Robot[4];// 机器人列表
        ArrayList<PlatForm> platformsList = new ArrayList<>();// 工作台列表
        readMap(robotsList, platformsList);// 读取地图数据
        int n = platformsList.size();
        PlatForm[] platformsArray = new PlatForm[n];
        for (int i = 0; i < n; i++)
            platformsArray[i] = platformsList.get(i);
        InitFunction(robotsList, platformsArray);// 执行算法初始化函数
        while (inStream.hasNextLine()) {
            readFlame(robotsList, platformsArray);
        }
    }

    /**
     * 初始化地图数据
     * 
     * @param rl 机器人列表
     * @param pl 工作台列表
     * @return 读取状态
     */
    private static boolean readMap(Robot[] rl, ArrayList<PlatForm> pl) {
        String line;
        int rnum = 0, pnum = 0, row = 0, col = 0;
        while (inStream.hasNextLine()) {
            line = inStream.nextLine();
            if ("OK".equals(line)) {
                return true;
            }
            for (char c : line.toCharArray()) {
                if (c == '.') {
                    col++;
                    continue;
                }
                if (c == 'A')
                    rl[rnum] = new Robot(rnum++, col * 0.5 + 0.25, 50.0 - row * 0.50 + 0.25);
                else
                    pl.add(new PlatForm(pnum++, (int) (c - '0'), col * 0.5 + 0.25, 50.0 - row * 0.50 + 0.25));
                col++;
            }
            row++;
            col = 0;
        }
        return false;
    }

    /**
     * 读取每一帧的数据
     * 
     * @param rl 机器人列表
     * @param pl 工作台列表
     * @return 读取状态 true 表示输入读取到OK， false表示异常退出循环
     */
    private static boolean readFlame(Robot[] rl, PlatForm[] pl) {
        boolean status = false;
        String line = inStream.nextLine();
        String[] parts = line.split(" ");
        int frameID = Integer.parseInt(parts[0]);// 帧序号
        for (int i = 0; i < 4; i++) {
            rl[i].addRealArriveFrame(1); // 递增实际运行帧数
        }
        System.out.println("frameID:" + frameID + "  target:" + "0:" + rl[0].getTargetPlatFormIndex() + "   1:"
                + rl[1].getTargetPlatFormIndex() + "   2:" + rl[2].getTargetPlatFormIndex() + "   3:"
                + rl[3].getTargetPlatFormIndex());

        // int money = Integer.parseInt(parts[1]);// 当前金钱数
        line = inStream.nextLine();
        int k = Integer.parseInt(line), rcount = 0, pcount = 0;
        while (inStream.hasNextLine()) {
            line = inStream.nextLine();
            String[] data = line.split(" ");
            if ("OK".equals(line)) {
                status = true;
                break;
            }
            if (pcount < k) {
                // 更新工作台数据
                PlatForm p = pl[pcount++];// 当前工作台
                p.setLeftFrame(Integer.parseInt(data[3]));// 更新剩余生产时间
                p.updateMateriaStatus(Integer.parseInt(data[4]));// 更新原材料格状态
                p.updateProductStatus(Integer.parseInt(data[5]));// 更新产品格状态
            } else {
                // 更新机器人状态
                Robot r = rl[rcount++];
                r.setNearByPlatFormId(Integer.parseInt(data[0]));// 更新附近工作台ID
                int tid = Integer.parseInt(data[1]);// 物品类型编号
                for (ItemType t : ItemType.values()) {
                    // 设置携带物品类型
                    if (t.getNum() == tid) {
                        r.setItem(new Item(Double.parseDouble(data[2]), Double.parseDouble(data[3]), t));

                        /*
                         * //下面的违法状态检测实际上可能导致异常行为
                         * if((tid == 0 && r.getStatus()) || (tid != 0 && !r.getStatus())) {
                         * //未携带物品的卖途状态（违法）或 携带物品的卖途状态（违法）
                         * r.changeStatus();
                         * }
                         */
                        break;
                    }
                }
                r.setAngleSpeed(Double.parseDouble(data[4]));// 更新角速度
                r.setLineSpeed(Double.parseDouble(data[5]), Double.parseDouble(data[6]));// 更新线速度
                r.setDirction(Double.parseDouble(data[7]));// 更新朝向
                r.setPosition(Double.parseDouble(data[8]), Double.parseDouble(data[9]));// 更新位置坐标
            }
        }
        outStream.printf("%d\n", frameID);
        ArrayList<Order> res = new ArrayList<>();
        DefaultMotion dm = new DefaultMotion();
        for (Robot r : rl) {
            res.addAll(dm.Move(r, pl));// 获取所有机器人的指令
        }
        for (Order order : res)
            order.printOrder(outStream);// 输出所有指令
        // Test
        for (Robot r : rl)
            System.err.printf("Robot %d target: %d status: %d\n", r.getNum(), r.getTargetPlatFormIndex(),
                    r.getStatus() ? 1 : 0);
        // for(Order order : res)System.err.println(order);
        System.err.printf("Frameid: %d\n", frameID);
        outStream.print("OK\n");
        outStream.flush();
        return status;
    }

    /**
     * 算法初始化函数
     * 
     * @param rl 机器人列表
     * @param pl 工作台列表
     */
    private static void InitFunction(Robot[] rl, PlatForm[] pl) {
        FindNextTarget f = new FindNextTarget();
        for (Robot r : rl)
            r.setTargetPlatFormIndex(f.findTarget(r, pl));
        outStream.println("OK");
        outStream.flush();
    }

}