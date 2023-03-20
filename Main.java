
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
            PrintStream print = new PrintStream("C:\\Users\\ASUS\\Desktop\\华为软设资料\\WindowsRelease\\WindowsRelease\\SDK\\java\\src\\com\\huawei\\codecraft\\output.txt"); // 写好输出位置文件；
            System.setOut(print);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        
        schedule();
    }

    private static void schedule() {
        Robot[] robotsList = new Robot[4];// 机器人列表
        ArrayList<PlatForm> platformsList = new ArrayList<>();// 工作台列表
        int[] itemPlaceCount = new int[8];// 记录1-6类型物品原料格的总数量,两个为预留的空位（必须有！！！）
        readMap(robotsList, platformsList, itemPlaceCount);// 读取地图数据
        int n = platformsList.size();
        PlatForm[] platformsArray = new PlatForm[n];
        for (int i = 0; i < n; i++)
            platformsArray[i] = platformsList.get(i);
        InitFunction(robotsList, platformsArray, itemPlaceCount, new int[7]);// 执行算法初始化函数
        while (inStream.hasNextLine()) {
            readFlame(robotsList, platformsArray, itemPlaceCount);
        }
    }

    /**
     * 初始化地图数据
     * 
     * @param rl  机器人列表
     * @param pl  工作台列表
     * @param ipc 1-6物品类型材料位计数
     * @return 读取状态
     */
    private static boolean readMap(Robot[] rl, ArrayList<PlatForm> pl, int[] ipc) {
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
                else {
                    PlatForm cur = new PlatForm(pnum++, (int) (c - '0'), col * 0.5 + 0.25, 50.0 - row * 0.50 + 0.25);// 当前工作台
                    pl.add(cur);
                    PlatFormType type = cur.getPlatFormType();// 工作台类型
                    // 根据类型增加原料位计数
                    if (type == PlatFormType.FOUR) {
                        ipc[1]++;
                        ipc[2]++;
                    } else if (type == PlatFormType.FIVE) {
                        ipc[1]++;
                        ipc[3]++;
                    } else if (type == PlatFormType.SIX) {
                        ipc[2]++;
                        ipc[3]++;
                    } else if (type == PlatFormType.SEVEN) {
                        ipc[4]++;
                        ipc[5]++;
                        ipc[6]++;
                    } else if (type == PlatFormType.NINE) {
                        for(int i = 1; i <= 6; i++) ipc[i]++;
                    }
                }
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
     * @param rl  机器人列表
     * @param pl  工作台列表
     * @param ipc 记录1-6类型物品原料格的总数量的数组
     * @return 读取状态 true 表示输入读取到OK， false表示异常退出循环
     */
    private static boolean readFlame(Robot[] rl, PlatForm[] pl, int[] ipc) {
        boolean status = false;
        String line = inStream.nextLine();
        String[] parts = line.split(" ");
        int[] curItemPlaceCount = new int[8];// 当前各类物品的原料格的数量
        int frameID = Integer.parseInt(parts[0]);// 帧序号
        Robot.frameID = frameID;
        for (int i = 0; i < 4; i++) {
            rl[i].addRealArriveFrame(1); // 递增实际运行帧数
        }

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
                PlatFormType type = p.getPlatFormType();// 当前工作台类型
                p.setLeftFrame(Integer.parseInt(data[3]));// 更新剩余生产时间
                p.updateMateriaStatus(Integer.parseInt(data[4]));// 更新原材料格状态
                p.updateProductStatus(Integer.parseInt(data[5]));// 更新产品格状态
                // 根据原材料格状态更新当前原料格占用数数组
                if (type == PlatFormType.FOUR) {
                    if (p.getMateriaStatusByIndex(1) || p.isAssigned(1))
                        curItemPlaceCount[1]++;
                    if (p.getMateriaStatusByIndex(2) || p.isAssigned(2))
                        curItemPlaceCount[2]++;
                } else if (type == PlatFormType.FIVE) {
                    if (p.getMateriaStatusByIndex(1) || p.isAssigned(1))
                        curItemPlaceCount[1]++;
                    if (p.getMateriaStatusByIndex(3) || p.isAssigned(3))
                        curItemPlaceCount[3]++;
                } else if (type == PlatFormType.SIX) {
                    if (p.getMateriaStatusByIndex(2) || p.isAssigned(2))
                        curItemPlaceCount[2]++;
                    if (p.getMateriaStatusByIndex(3) || p.isAssigned(3))
                        curItemPlaceCount[3]++;
                } else if (type == PlatFormType.SEVEN) {
                    if (p.getMateriaStatusByIndex(4) || p.isAssigned(4))
                        curItemPlaceCount[4]++;
                    if (p.getMateriaStatusByIndex(5) || p.isAssigned(5))
                        curItemPlaceCount[5]++;
                    if (p.getMateriaStatusByIndex(6) || p.isAssigned(6))
                        curItemPlaceCount[6]++;
                }
            } else {
                // 更新机器人状态
                Robot r = rl[rcount++];
                r.setNearByPlatFormId(Integer.parseInt(data[0]));// 更新附近工作台ID
                int tid = Integer.parseInt(data[1]);// 物品类型编号
                for (ItemType t : ItemType.values()) {
                    // 设置携带物品类型
                    if (t.getNum() == tid) {
                        r.setItem(new Item(Double.parseDouble(data[2]), Double.parseDouble(data[3]), t));
                        break;
                    }
                }
                //当前机器人目标产品类型占用一个空位
                /*
                if(!r.getStatus())curItemPlaceCount[pl[r.getTargetPlatFormIndex()].getPlatFormType().getProductItemType().getNum()]++;
                else curItemPlaceCount[r.getItem().getItemType().getNum()]++;//当前机器人携带的材料也算一个占用位
                */
                r.setAngleSpeed(Double.parseDouble(data[4]));// 更新角速度
                r.setLineSpeed(Double.parseDouble(data[5]), Double.parseDouble(data[6]));// 更新线速度
                r.setDirction(Double.parseDouble(data[7]));// 更新朝向
                r.setprePosition(r.getPosition()[0], r.getPosition()[1]);
                r.setPosition(Double.parseDouble(data[8]), Double.parseDouble(data[9]));// 更新位置坐标
            }
        }
        outStream.printf("%d\n", frameID);
        ArrayList<Order> res = new ArrayList<>();
        DefaultMotion dm = new DefaultMotion(ipc, curItemPlaceCount);
        for (Robot r : rl) {
            res.addAll(dm.Move(r, pl));// 获取所有机器人的指令
        }
        for (Order order : res)
            order.printOrder(outStream);// 输出所有指令
        // Test
        
        System.out.println("frameID:" + frameID + "  target:" + "0:" + rl[0].getTargetPlatFormIndex() + "   1:"
                + rl[1].getTargetPlatFormIndex() + "   2:" + rl[2].getTargetPlatFormIndex() + "   3:"
                + rl[3].getTargetPlatFormIndex());
        System.out.print("ipc: [ ");
        for (int i = 1; i <= 6; i++)
            System.out.printf("%d ", ipc[i]);
        System.out.println("]");
        System.out.print("cipc: [ ");
        for (int i = 1; i <= 6; i++)
            System.out.printf("%d ", curItemPlaceCount[i]);
        System.out.println("]");

        // for(Order order : res)System.out.println(order);
        //System.err.printf("Frameid: %d\n", frameID);
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
    private static void InitFunction(Robot[] rl, PlatForm[] pl, int[] ipc, int[] cipc) {
        for (Robot r : rl)
            r.setTargetPlatFormIndex(FindNextTarget.findTarget(r, pl, ipc, cipc));
        outStream.println("OK");
        outStream.flush();
    }

}