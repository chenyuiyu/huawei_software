import com.sun.org.apache.xpath.internal.operations.Or;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Scanner;

public class Utils {
    /**
     * @param platformsList 存储平台信息
     * @param robotsList    存储机器人信息
     * @description 初始化数据结构
     **/
    public static void initStructure(List<PlatForm> platformsList, List<Robot> robotsList) {
        platformsList.add(new PlatForm(-1, -1, -1, -1));
        for (int i = 1; i <= PLAYFORM_NUMBER; i++) {
            platformsList.add(new PlatForm(0, 0, 0, 0));
        }
        // -1 0 0 0 1.5 1.593541145 -2.54177618 -0.9808044434 22.92929459 39.6531105
        for (int i = 0; i < ROBOT_TYPE_NUMER; i++) {
            robotsList.add(new Robot(0, 0, 0));
        }
    }

    /**
     * @param inStream      输入流
     * @param robotsList
     * @param platformsList
     * @description 地图初始化信息 不作处理
     **/
    public static boolean readMapOK(Scanner inStream, List<Robot> robotsList, List<PlatForm> platformsList) {
        String line;
        int rnum = 0, PLAYFORM_NUMBER = 0, row = 0, col = 0;
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
                if (c == 'A') robotsList.add(new Robot(rnum++, col * 0.5 + 0.25, 50.0 - row * 0.50 + 0.25));
                else
                    platformsList.add(new PlatForm(PLAYFORM_NUMBER++, (int) (c - '0'), col * 0.5 + 0.25, 50.0 - row * 0.50 + 0.25));
                col++;
            }
            row++;
            col = 0;
        }
        return false;
    }

    /**
     * @param inStream 输入流
     * @description 读取每一帧的信息 更新数据结构
     **/
    public static boolean readFrameOK(Scanner inStream, List<PlatForm> platformsList, List<Robot> robotsList) {
        String line;
        int ridx = 0, pidx = 0;
        while (inStream.hasNextLine()) {
            line = inStream.nextLine();
            if ("OK".equals(line)) {
                return true;
            }
            System.err.println(line);
            String[] msg = line.split(" ");
            if (msg.length == PLATFORM_MSG_LENGTH) {
                // 更新工作台数据
                // 工作台：  1       1.25 48.75      0      0           1
                //       工作台类型     坐标      剩余时间  原材料状态    产品格状态
                PlatForm curP = platformsList.get(pidx++);
                curP.setLeftFrame(Integer.parseInt(msg[3])); // 更新剩余生产时间
                curP.updateMateriaStatus(Integer.parseInt(msg[4])); // 更新原材料状态
                curP.updateProductStatus(Integer.parseInt(msg[5])); // 更新产品格状态
            } else if (msg.length == ROBOT_MSG_LENGTH) {
                // 更新机器人状态
                // 机器人： -1        0       0     0      1.5    -2.669386148 -1.369079232
                //       附近工作台  携带物品  系数1 系数2    角速度    线速度x      线速度y
                // -2.637692928 23.66864967 42.50642776
                //  朝向          坐标x       坐标y
                Robot curR = robotsList.get(ridx++);
                curR.setNearByPlatFormId(Integer.parseInt(msg[0])); // 附近工作台id
                int itemIdx = Integer.parseInt(msg[1]); // 携带物品类型
                for (ItemType value : ItemType.values()) {
                    if (value.getNum() == itemIdx) {
                        curR.setItem(new Item(Double.parseDouble(msg[2]), Double.parseDouble(msg[3]), value));
                    }
                }
                curR.setAngleSpeed(Double.parseDouble(msg[4])); // 设置角速度
                curR.setLineSpeed(Double.parseDouble(msg[5]), Double.parseDouble(msg[6])); // 设置线速度
                curR.setDirction(Double.parseDouble(msg[7])); // 更新朝向
                curR.setPosition(Double.parseDouble(msg[8]), Double.parseDouble(msg[9])); // 更新位置坐标
            }

        }
        return false;
    }


    /**
     * @return 目标平台的编号
     * @description 用于机器人寻找下一个目标平台
     **/
    public static int findTargetForRobot(List<PlatForm> platformsList, Robot curR) {
        // 4个机器人
        // 用于卖物品
        PlatForm target;
        if (curR.getStatus()) {
            PriorityQueue<PlatForm> queue = new PriorityQueue<>((a, b) -> {
                double costa = coefficientDistance * Utils.getDistance(a.getPosition(), curR.getPosition());
                costa += (costa > a.getLeftFrame() ? 0 : a.getLeftFrame() - costa);
                double costb = coefficientDistance * Utils.getDistance(b.getPosition(), curR.getPosition());
                costb += (costa > b.getLeftFrame() ? 0 : b.getLeftFrame() - costb);
                if (costa < costb) return -1;
                return 1;
            });
            // 选择一个可以去的地方
            int id = curR.getItem().getItemType().getNum(); // 携带的编号
            for (PlatForm p : platformsList) {
                // 查看当前平台是否需要该材料 且 材料的收集情况 且 是否已经分派了机器人来放置这个材料
                if (((p.getPlatFormType().getNeededMateria() ^ p.getMateriaStatus() ^ p.getAssignStatus()) & (1 << id)) > 0) {
                    // 只有1 0 0的情况需要派机器人
                    queue.offer(p);
                }
            }
            target = queue.peek();
            target.changeAssignStatus(id);
            return target.getNum();
        } else {
            // 买物品
            PriorityQueue<PlatForm> queue = new PriorityQueue<>((a, b) -> {
                double costa = coefficientDistance * Utils.getDistance(a.getPosition(), curR.getPosition()) +
                        +coefficientEarn * a.getPlatFormType().getProductItemType().getEarn();
                double costb = coefficientDistance * Utils.getDistance(b.getPosition(), curR.getPosition()) +
                        +coefficientEarn * b.getPlatFormType().getProductItemType().getEarn();
                if (costa < costb) return -1;
                return 1;
            });
            for (PlatForm p : platformsList) {
                if (p.HasProduct() && !p.isAssigned(0)) {
                    queue.add(p);
                }
            }
            target = queue.peek();
            target.changeAssignStatus(0); // 翻转派遣位位置 即产品位置已经派遣人去拿了
            return target.getNum();
        }
    }

    /**
     * @description 计算二维空间两坐标点的欧氏距离
     **/
    private static double getDistance(double[] pos1, double[] pos2) {
        return Math.sqrt(Math.pow(pos1[0] - pos2[0], 2) + Math.pow(pos1[1] - pos2[1], 2));

    }

    public static final int PLATFORM_TYPE_NUMER = 9;
    public static final int ROBOT_TYPE_NUMER = 4;
    public static final int PLATFORM_MSG_LENGTH = 6;
    public static final int ROBOT_MSG_LENGTH = 10;
    public static int PLAYFORM_NUMBER = 0;
    public static double coefficientDistance = 1.0;
    public static double coefficientEarn = 1.2;
}
