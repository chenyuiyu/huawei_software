import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Scanner;

public class Utils {

    /**
     * @param inStream      输入流
     * @param robotsList
     * @param platformsList
     * @param taskQueue
     * @description 地图初始化信息
     **/
    public static boolean readMapOK(Scanner inStream, List<Robot> robotsList, List<PlatForm> platformsList, PriorityQueue<Task> taskQueue) {
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
                else {
                    // 预处理平台
                    platformsList.add(new PlatForm(PLAYFORM_NUMBER, (int) (c - '0'), col * 0.5 + 0.25, 50.0 - row * 0.50 + 0.25));
                    int kind = c - '0';
                    PlatForm p = platformsList.get(PLAYFORM_NUMBER);
                    // 初始化任务队列
                    switch (kind) {
                        case 7:
                            taskQueue.add(new Task(false, true, PLAYFORM_NUMBER, -1, p.getPlatFormType().getProductTaskPriority(), kind));
                            break;
                        case 6:
                            taskQueue.add(new Task(false, true, PLAYFORM_NUMBER, -1, p.getPlatFormType().getProductTaskPriority(), kind));
                            break;
                        case 5:
                            taskQueue.add(new Task(false, true, PLAYFORM_NUMBER, -1, p.getPlatFormType().getProductTaskPriority(), kind));
                            break;
                        case 4:
                            taskQueue.add(new Task(false, true, PLAYFORM_NUMBER, -1, p.getPlatFormType().getProductTaskPriority(), kind));
                            break;
                    }
                    // 将【是否发布任务】标志位置为true 防止重复发布任务
                    if (kind >= 4 && kind <= 7) platformsList.get(PLAYFORM_NUMBER).setAssignProductTask(true);
                    PLAYFORM_NUMBER++;
                }
                col++;
            }
            row++;
            col = 0;
        }

        return false;
    }

    /**
     * @param inStream  输入流
     * @param taskQueue
     * @description 读取每一帧的信息 更新数据结构
     **/
    public static boolean readFrameOK(Scanner inStream, List<PlatForm> platformsList, List<Robot> robotsList, PriorityQueue<Task> taskQueue) {
        String line;
        int ridx = 0, pidx = 0;
        while (inStream.hasNextLine()) {
            line = inStream.nextLine();
            if ("OK".equals(line)) {
                return true;
            }
            String[] msg = line.split(" ");
            if (msg.length == PLATFORM_MSG_LENGTH) {
                PlatForm curP = platformsList.get(pidx++);
                curP.setLeftFrame(Integer.parseInt(msg[3])); // 更新剩余生产时间
                curP.updateMateriaStatus(Integer.parseInt(msg[4])); // 更新原材料状态
                curP.updateProductStatus(Integer.parseInt(msg[5])); // 更新产品格状态

                // 更新平台状态
                int need = curP.getPlatFormType().getNeededMateria(); // 所需材料
                int materiaStatus = curP.getMateriaStatus(); // 物料状态
                int pType = curP.getPlatFormType().getIndex(); // 获得平台类型
                // 只有平台类型4-7的才会发布生产任务或者fetch任务
                if (pType >= 4 && pType <= 7) {
                    // 检查是否能发布fetch任务
                    if ((materiaStatus & 1) == 1 && !curP.isAssignFetchTask()) {
                        // 如果产品格有内容 并且 平台尚未发布fetch任务 并且 平台类型[4,7]
                        // 则发布任务 fetch任务都是不可再分的
                        taskQueue.add(new Task(true, false, curP.getNum(),
                                curP.getRootPlatformId(), curP.getPlatFormType().getFetchTaskPriority(), pType));
                        curP.setAssignFetchTask(true);
                    }
                    // 检查是否能发布生产任务
                    if (!curP.isAssignProductTask()) {
                        taskQueue.add(new Task(false, true, curP.getNum(),
                                curP.getRootPlatformId(), curP.getPlatFormType().getProductTaskPriority(), pType));
                        curP.setAssignProductTask(true);
                    }
                }

            } else if (msg.length == ROBOT_MSG_LENGTH) {
                Robot curR = robotsList.get(ridx++);
                curR.setNearByPlatFormId(Integer.parseInt(msg[0])); // 附近工作台id
                int itemIdx = Integer.parseInt(msg[1]); // 携带物品类型
                for (ItemType t : ItemType.values()) {
                    if (t.getNum() == itemIdx) {
                        curR.setItem(new Item(Double.parseDouble(msg[2]), Double.parseDouble(msg[3]), t));
                        break;
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
     * @return 目标平台的编号Z
     * @description 用于机器人寻找下一个目标平台[卖东西 or 买东西]
     **/
    public static int findTargetForRobot(List<PlatForm> platformsList, Robot r) {
        if (r.getStatus()) {
            // 机器人卖东西
            return findTargetForSell(platformsList, r);
        } else {
            // 机器人买东西
            return findTargetForBuy(platformsList, r);
        }
    }

    /**
     * @param platformsList
     * @param r
     * @return
     * @description 为买操作寻找平台
     */
    private static int findTargetForBuy(List<PlatForm> platformsList, Robot r) {
        System.err.printf("robot%d正在寻找买平台\n", r.getNum());
        PlatForm target = null;
        CompareForBuy cmp = new CompareForBuy(r, 1.0, 1.0);//a:距离权重  b：角度权重
        PriorityQueue<PlatForm> records123 = new PriorityQueue<>(cmp);//1/2/3类工作台存一个优先队列
        PriorityQueue<PlatForm> records4567 = new PriorityQueue<>(cmp);//4/5/6/7类工作台存一个优先队列
        for (PlatForm cur : platformsList) {
            if (cur.HasProduct() && !cur.isAssigned(0)) {
                //该平台有产出并且未派遣机器人
                if (cur.getPlatFormType().getIndex() <= 3) records123.add(cur);
                else records4567.add(cur);//加入到对应类型的优先队列
            }
        }
        if (r.getNum() <= 1) {
            target = records123.peek();
            if (target == null) target = records4567.peek();
        } else {
            target = records4567.peek();
            if (target == null) target = records123.peek();
        }
        //需要处理没有下一个目标的情况
        if (target == null) {
            //以下逻辑为处理第一轮未找到目标工作台,寻找1-3类型的工作台,派机器人到那里等待
            for (PlatForm cur : platformsList) {
                if (cur.getPlatFormType().getIndex() <= 3) {
                    target = cur;
                    break;
                }
            }
        }
        target.setAssignStatus(0, true);//翻转派遣位
        return target.getNum();
    }

    /**
     * @param platformsList
     * @param r
     * @return
     * @description 为卖操作寻找平台
     */
    public static int findTargetForSell(List<PlatForm> platformsList, Robot r) {
        PlatForm target;
        CompareForSell cfs = new CompareForSell(r, 1, 20);
        PriorityQueue<PlatForm> queue456 = new PriorityQueue<>(cfs); // 优先卖给456
        PriorityQueue<PlatForm> queue789 = new PriorityQueue<>(cfs); // 其次是789
        int id = r.getItem().getItemType().getNum(); // 携带的编号
        for (PlatForm p : platformsList) {
            PlatFormType type = p.getPlatFormType();
            // 查看当前平台是否需要该材料 且 材料的收集情况 且 是否已经分派了机器人来放置这个材料
            if (((p.getPlatFormType().getNeededMateria() ^ p.getMateriaStatus() ^ p.getAssignStatus()) & (1 << id)) > 0) {
                // 只有1 0 0的情况需要派机器人
                if (type.getIndex() <= 6) {
                    queue456.add(p);
                } else
                    queue789.add(p);
            }
        }
        // 机器人分成两批
        if (r.getNum() <= 1) {
            target = queue456.peek();
            if (target == null) target = queue789.peek();
        } else {
            target = queue789.peek();
            if (target == null) target = queue456.peek();
        }
        if (target == null) {
            // 若以下逻辑为处理第一轮中未找到工作台的情况
            int leftTime = 1000;
            for (PlatForm cur : platformsList) {
                if ((cur.getPlatFormType().getNeededMateria() & (1 << id)) > 0 && cur.getLeftFrame() <= leftTime) {
                    target = cur;
                    leftTime = cur.getLeftFrame();
                }
            }
        }
        target.setAssignStatus(id, true);
        return target.getNum();
    }

    /**
     * @description 计算二维空间两坐标点的欧氏距离
     **/
    public static double getDistance(double[] pos1, double[] pos2) {
        return Math.sqrt(Math.pow(pos1[0] - pos2[0], 2) + Math.pow(pos1[1] - pos2[1], 2));

    }


    /**
     * 求两个向量的夹角
     *
     * @param vector1
     * @param vector2
     * @return 两向量夹角
     */
    public static double getVectorAngle(double[] vector1, double[] vector2) {
        double vectorProduct = vector1[0] * vector2[0] + vector1[1] * vector2[1];
        double vectorNorm = Math.sqrt(Math.pow(vector1[0], 2) + Math.pow(vector1[1], 2))
                * Math.sqrt(Math.pow(vector2[0], 2) + Math.pow(vector2[1], 2));

        return Math.acos(vectorProduct / vectorNorm);
    }


    /**
     * 求点pos1到点pos2的向量
     *
     * @param pos1
     * @param pos2
     * @return
     */
    public static double[] getVectorBetweenPoints(double[] pos1, double[] pos2) {
        return new double[]{pos2[0] - pos1[0], pos2[1] - pos1[1]};
    }

    public static final int PLATFORM_TYPE_NUMER = 9;
    public static final int ROBOT_TYPE_NUMER = 4;
    public static final int PLATFORM_MSG_LENGTH = 6;
    public static final int ROBOT_MSG_LENGTH = 10;
    public static int PLAYFORM_NUMBER = 0;
    public static double coefficientDistance = 1.0;
    public static double coefficientEarn = 1.2;

    /**
     * 初始化数据结构
     *
     * @param labelPlatforms
     * @param platformsList
     */
    public static void initStructure(List<List<PlatForm>> labelPlatforms, List<PlatForm> platformsList) {
        for (int i = 0; i < 10; i++) {
            labelPlatforms.add(i, new ArrayList<>());
        }
        for (PlatForm p : platformsList) {
            int kind = p.getPlatFormType().getIndex(); // 平台类型
            labelPlatforms.get(kind).add(p);
        }
    }

    /**
     * 拆分复合任务:7 或者 4 5 6
     */
    public static void splitTask(PriorityQueue<Task> taskQueue) {
        if (taskQueue.peek().isAtomic()) return; // 队头元素如果是原子任务 则不用处理
        Task task = taskQueue.poll();
        int taskNum = task.getTaskNum();
        switch (taskNum) {
            case 7:
                split7Task(task, taskQueue);
                break;
            case 6:
                split6Task(task, taskQueue);
                break;
            case 5:
                split5Task(task, taskQueue);
                break;
            case 4:
                split4Task(task, taskQueue);
                break;
        }
    }

    private static void split4Task(Task root, PriorityQueue<Task> taskQueue) {
        Task t1 = new Task(true, true, -1, root.getCurTaskPlatformId(),
                root.getPriority() - 1, 1);
        Task t2 = new Task(true, true, -1, root.getCurTaskPlatformId(),
                root.getPriority() - 1, 2);
        taskQueue.add(t1);
        taskQueue.add(t2);
    }

    private static void split5Task(Task root, PriorityQueue<Task> taskQueue) {
        Task t1 = new Task(true, true, -1, root.getCurTaskPlatformId(),
                root.getPriority() - 1, 1);
        Task t3 = new Task(true, true, -1, root.getCurTaskPlatformId(),
                root.getPriority() - 1, 3);
        taskQueue.add(t1);
        taskQueue.add(t3);
    }

    private static void split6Task(Task root, PriorityQueue<Task> taskQueue) {
        Task t2 = new Task(true, true, -1, root.getCurTaskPlatformId(),
                root.getPriority() - 1, 2);
        Task t3 = new Task(true, true, -1, root.getCurTaskPlatformId(),
                root.getPriority() - 1, 3);
        taskQueue.add(t2);
        taskQueue.add(t3);
    }

    private static void split7Task(Task root, PriorityQueue<Task> taskQueue) {
        Task t4 = new Task(false, true, -1, root.getCurTaskPlatformId(),
                root.getPriority() - 1, 4);
        Task t5 = new Task(false, true, -1, root.getCurTaskPlatformId(),
                root.getPriority() - 1, 5);
        Task t6 = new Task(false, true, -1, root.getCurTaskPlatformId(),
                root.getPriority() - 1, 6);
        taskQueue.add(t4);
        taskQueue.add(t5);
        taskQueue.add(t6);
    }
}
