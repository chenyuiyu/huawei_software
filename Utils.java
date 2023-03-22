import java.util.*;

public class Utils {

    /**
     * @param inStream      输入流
     * @param robotsList
     * @param platformsList
     * @param taskQueue
     * @description 地图初始化信息
     **/
    public static boolean readMapOK(Scanner inStream, List<Robot> robotsList, List<PlatForm> platformsList,
            PriorityQueue<Task> taskQueue) {
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
                if (c == 'A')
                    robotsList.add(new Robot(rnum++, col * 0.5 + 0.25, 50.0 - row * 0.50 + 0.25));
                else {
                    // 预处理平台
                    platformsList.add(
                            new PlatForm(PLAYFORM_NUMBER, (int) (c - '0'), col * 0.5 + 0.25, 50.0 - row * 0.50 + 0.25));
                    int kind = c - '0';
                    PlatForm p = platformsList.get(PLAYFORM_NUMBER);
                    // 初始化任务队列
                    if (kind >= 4 && kind <= 7) {
                        taskQueue.add(new Task(false, PLAYFORM_NUMBER, -1, p.getPlatFormType().getProductTaskPriority(),
                                kind));
                        PlatForm ele = platformsList.get(PLAYFORM_NUMBER);
                        p.setAssignProductTask(true); // 设置标志位状态为已发布生产任务 防止重复发布任务
                    }
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
    public static boolean readFrameOK(Scanner inStream, List<PlatForm> platformsList, List<Robot> robotsList,
            PriorityQueue<Task> taskQueue) {
        String line;
        int ridx = 0, pidx = 0;
        while (inStream.hasNextLine()) {
            line = inStream.nextLine();
            if ("OK".equals(line)) {
                return true;
            }
            String[] msg = line.split(" ");
            if (msg.length == PLATFORM_MSG_LENGTH) {
                PlatForm p = platformsList.get(pidx++);
                p.setLeftFrame(Integer.parseInt(msg[3])); // 更新剩余生产时间
                p.updateMateriaStatus(Integer.parseInt(msg[4])); // 更新原材料状态
                p.updateProductStatus(Integer.parseInt(msg[5])); // 更新产品格状态

                int need = p.getPlatFormType().getNeededMateria(); // 所需材料
                int materiaStatus = p.getMateriaStatus(); // 物料状态
                int assgin = p.getAssignStatus();
                int pType = p.getPlatFormType().getIndex(); // 获得平台类型

                // 只有平台类型4-7的才会发布生产任务或者fetch任务
                if (pType >= 4 && pType <= 7) {
                    // 检查是否能发布fetch任务
                    // 如果产品格有内容 并且 平台尚未发布fetch任务 并且 平台类型[4,7]
                    if ((materiaStatus & 1) == 1 && !p.isAssignFetchTask()) {
                        int rootPlatformId = -1;
                        Queue<Integer> queue = p.getPlatformsWhichNeedProductionQueue();
                        if (!queue.isEmpty())
                            rootPlatformId = queue.poll();
                        // 生成fetch任务
                        Task task = new Task(true, p.getNum(),
                                rootPlatformId, p.getPlatFormType().getFetchTaskPriority(), pType);
                        if (rootPlatformId == -1 && task.getTaskNum() != 7) {
                            // 如果当前fetch任务不知道卖材料的平台Id 则加入到相应的生产链表之中
                            Main.productionsList.get(pType).add(task);
                        } else {
                            taskQueue.add(task);
                        }
                        p.setAssignFetchTask(true); // 修改发布fetch任务标记位 防止重复发布任务

                    }
                    // 检查是否能发布生产任务
                    // 仅当能发布任务 ｜ 且材料格没有阻塞
                    if (!p.isAssignProductTask() && (p.getMateriaStatus() >> 1) == 0) {
                        Task task = new Task(false, p.getNum(),
                                -1, p.getPlatFormType().getProductTaskPriority(), pType);
                        taskQueue.add(task);
                        p.setAssignProductTask(true);
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
        PlatForm target = null;
        CompareForBuy cmp = new CompareForBuy(r, 1.0, 1.0);// a:距离权重 b：角度权重
        PriorityQueue<PlatForm> records123 = new PriorityQueue<>(cmp);// 1/2/3类工作台存一个优先队列
        PriorityQueue<PlatForm> records4567 = new PriorityQueue<>(cmp);// 4/5/6/7类工作台存一个优先队列
        for (PlatForm cur : platformsList) {
            if (cur.HasProduct() && !cur.isAssigned(0)) {
                // 该平台有产出并且未派遣机器人
                if (cur.getPlatFormType().getIndex() <= 3)
                    records123.add(cur);
                else
                    records4567.add(cur);// 加入到对应类型的优先队列
            }
        }
        if (r.getNum() <= 1) {
            target = records123.peek();
            if (target == null)
                target = records4567.peek();
        } else {
            target = records4567.peek();
            if (target == null)
                target = records123.peek();
        }
        // 需要处理没有下一个目标的情况
        if (target == null) {
            // 以下逻辑为处理第一轮未找到目标工作台,寻找1-3类型的工作台,派机器人到那里等待
            for (PlatForm cur : platformsList) {
                if (cur.getPlatFormType().getIndex() <= 3) {
                    target = cur;
                    break;
                }
            }
        }
        target.setAssignStatus(0, true);// 翻转派遣位
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
            if (((p.getPlatFormType().getNeededMateria() ^ p.getMateriaStatus() ^ p.getAssignStatus())
                    & (1 << id)) > 0) {
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
            if (target == null)
                target = queue789.peek();
        } else {
            target = queue789.peek();
            if (target == null)
                target = queue456.peek();
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
        return new double[] { pos2[0] - pos1[0], pos2[1] - pos1[1] };
    }

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
     * 为当前任务寻找适合的平台
     *
     * @param taskNum             任务类型 或者说 平台类型
     * @param rRootTaskPlatformId 父任务对应平台id 若-1，则没有父任务
     * @param labelPlatforms      各类型平台
     * @return 返回适合的平台id
     *         //TODO 可优化
     */
    public static int findProperPlatform(int taskNum, int rRootTaskPlatformId, List<List<PlatForm>> labelPlatforms) {
        List<PlatForm> specPlatforms = labelPlatforms.get(taskNum);
        PlatForm ans = null;
        PriorityQueue<PlatForm> queue;
        if (rRootTaskPlatformId == -1) {
            // 没有父平台 单纯考虑选择类型为taskNum的平台即可
            for (PlatForm p : specPlatforms) {
                if (!p.isChoosedForProduct()) {
                    p.setChoosedForProduct(true); // 将该平台标记为已使用
                    ans = p;
                    break;
                }
            }
        } else {
            // 若有父平台，则利用优先队列，寻找与父平台距离最近的taskNum类型平台
            PlatForm p = Main.platformsList.get(rRootTaskPlatformId); // 获取对应的父平台
            queue = new PriorityQueue<>(new CompareBetweenPlatform(p));
            for (PlatForm platform : specPlatforms) {
                if (!platform.isChoosedForProduct())
                    queue.add(platform);
            }
            PlatForm head = queue.peek(); // 选择队头
            if (head != null) {
                // head.setChoosedForProduct(true); //将该平台标记为已使用
                ans = head;
            }

        }
        // TODO 此处有可能为空！

        return ans == null ? -1 : ans.getNum();
    }

    /**
     * 拆分复合任务:7 或者 4 5 6
     * 当分解一个curTaskPlatformId为-1的任务时，一定要确定具体的平台
     */
    public static void splitTask(PriorityQueue<Task> taskQueue) {
        if (taskQueue.peek().isAtomic())
            return; // 队头元素如果是原子任务 则不用处理
        Task task = taskQueue.poll();
        // 队列中的任务可能需要撤销
        if (task.getCurTaskPlatformId() != -1) {
            PlatForm p = Main.platformsList.get(task.getCurTaskPlatformId());
            if (p.isChoosedForProduct()) { // 如果平台此前已经被选择为生产平台 则撤销任务
                return;
            }
        }

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
        int rCurTaskPlatformId = root.getCurTaskPlatformId(); // 父任务对应的平台
        int rRootTaskPlatformId = root.getRootTaskPlatformId(); // 父任务的父任务对应的平台
        int taskNum = root.getTaskNum();
        LinkedList<Task> tasks = Main.productionsList.get(taskNum);
        Task tmp = null;
        // 若生产4任务的平台还没确定 则先去生产链表看看有无成品4
        if (rCurTaskPlatformId == -1 && !tasks.isEmpty()) {
            double minDistance = Double.MAX_VALUE;
            for (Task task : tasks) {
                double distance = Utils.getDistance(Main.platformsList.get(rRootTaskPlatformId).getPosition(),
                        Main.platformsList.get(task.getCurTaskPlatformId()).getPosition());
                if (distance < minDistance) {
                    minDistance = distance;
                    tmp = task;
                }
            }
            tasks.remove(tmp); // 生产链表中的某个任务被领取，需要从链表中删除
            // 将任务加入任务队列中
            Task task = new Task(true, tmp.getCurTaskPlatformId(), rRootTaskPlatformId,
                    PlatFormType.FOUR.getFetchTaskPriority(), 4);
            taskQueue.add(task);
        } else {
            // 产品4无成品 那么依照原有的分解任务逻辑即可
            if (rCurTaskPlatformId == -1)
                rCurTaskPlatformId = findProperPlatform(root.getTaskNum(), rRootTaskPlatformId, Main.labelPlatforms);
            if (rCurTaskPlatformId == -1)
                return; // 撤销任务
            PlatForm p = Main.platformsList.get(rCurTaskPlatformId);
            // 确定平台的同时就将所有相关标记位设置为true
            p.setAssignProductTask(true); // 升至该平台已经发布了生产任务
            p.setChoosedForProduct(true); // 设置该平台已经用于生产
            if (rRootTaskPlatformId != -1)
                p.getPlatformsWhichNeedProductionQueue().add(rRootTaskPlatformId); // 设置父任务对应的平台id 后续生产好的东西需要往哪里送
            Task t1 = new Task(true, -1, rCurTaskPlatformId,
                    root.getPriority() - 1, 1);
            Task t2 = new Task(true, -1, rCurTaskPlatformId,
                    root.getPriority() - 1, 2);
            taskQueue.add(t1);
            taskQueue.add(t2);
        }

    }

    private static void split5Task(Task root, PriorityQueue<Task> taskQueue) {
        int rCurTaskPlatformId = root.getCurTaskPlatformId();
        int rRootTaskPlatformId = root.getRootTaskPlatformId();
        int taskNum = root.getTaskNum();
        LinkedList<Task> tasks = Main.productionsList.get(taskNum);
        Task tmp = null;
        if (rCurTaskPlatformId == -1 && !tasks.isEmpty()) {
            double minDistance = Double.MAX_VALUE;
            for (Task task : tasks) {
                double distance = Utils.getDistance(Main.platformsList.get(rRootTaskPlatformId).getPosition(),
                        Main.platformsList.get(task.getCurTaskPlatformId()).getPosition());
                if (distance < minDistance) {
                    minDistance = distance;
                    tmp = task;
                }
            }
            tasks.remove(tmp); // 生产链表中的某个任务被领取，需要从链表中删除
            // 将任务加入任务队列中
            Task task = new Task(true, tmp.getCurTaskPlatformId(), rRootTaskPlatformId,
                    PlatFormType.FIVE.getFetchTaskPriority(), 5);
            taskQueue.add(task);
        } else {
            // 产品5无成品 那么依照原有的分解任务逻辑即可
            if (rCurTaskPlatformId == -1)
                rCurTaskPlatformId = findProperPlatform(root.getTaskNum(), rRootTaskPlatformId, Main.labelPlatforms);
            if (rCurTaskPlatformId == -1)
                return; // 撤销任务
            PlatForm p = Main.platformsList.get(rCurTaskPlatformId);
            p.setAssignProductTask(true); // 升至该平台已经发布了生产任务
            p.setChoosedForProduct(true); // 设置该平台已经用于生产
            if (rRootTaskPlatformId != -1)
                p.getPlatformsWhichNeedProductionQueue().add(rRootTaskPlatformId); // 设置父任务对应的平台id 后续生产好的东西需要往哪里送
            Task t1 = new Task(true, -1, rCurTaskPlatformId,
                    root.getPriority() - 1, 1);
            Task t3 = new Task(true, -1, rCurTaskPlatformId,
                    root.getPriority() - 1, 3);
            taskQueue.add(t1);
            taskQueue.add(t3);
        }

    }

    private static void split6Task(Task root, PriorityQueue<Task> taskQueue) {
        int rCurTaskPlatformId = root.getCurTaskPlatformId();
        int rRootTaskPlatformId = root.getRootTaskPlatformId();
        int taskNum = root.getTaskNum();
        LinkedList<Task> tasks = Main.productionsList.get(taskNum);
        Task tmp = null;
        if (rCurTaskPlatformId == -1 && !tasks.isEmpty()) {
            double minDistance = Double.MAX_VALUE;
            for (Task task : tasks) {
                double distance = Utils.getDistance(Main.platformsList.get(rRootTaskPlatformId).getPosition(),
                        Main.platformsList.get(task.getCurTaskPlatformId()).getPosition());
                if (distance < minDistance) {
                    minDistance = distance;
                    tmp = task;
                }
            }
            tasks.remove(tmp); // 生产链表中的某个任务被领取，需要从链表中删除
            // 将任务加入任务队列中
            Task task = new Task(true, tmp.getCurTaskPlatformId(), rRootTaskPlatformId,
                    PlatFormType.SIX.getFetchTaskPriority(), 6);
            taskQueue.add(task);
        } else {
            // 产品6无成品 那么依照原有的分解任务逻辑即可
            if (rCurTaskPlatformId == -1)
                rCurTaskPlatformId = findProperPlatform(root.getTaskNum(), rRootTaskPlatformId, Main.labelPlatforms);
            if (rCurTaskPlatformId == -1)
                return; // 撤销任务
            PlatForm p = Main.platformsList.get(rCurTaskPlatformId);
            p.setAssignProductTask(true); // 升至该平台已经发布了生产任务
            p.setChoosedForProduct(true); // 设置该平台已经用于生产
            if (rRootTaskPlatformId != -1)
                p.getPlatformsWhichNeedProductionQueue().add(rRootTaskPlatformId); // 设置父任务对应的平台id 后续生产好的东西需要往哪里送
            Task t2 = new Task(true, -1, rCurTaskPlatformId,
                    root.getPriority() - 1, 2);
            Task t3 = new Task(true, -1, rCurTaskPlatformId,
                    root.getPriority() - 1, 3);
            taskQueue.add(t2);
            taskQueue.add(t3);
        }

    }

    private static void split7Task(Task root, PriorityQueue<Task> taskQueue) {
        int rCurTaskPlatformId = root.getCurTaskPlatformId();
        PlatForm p = Main.platformsList.get(rCurTaskPlatformId);
        // p.setChoosedForProduct(true);
        Task t4 = new Task(false, -1, rCurTaskPlatformId,
                root.getPriority() - 1, 4);
        Task t5 = new Task(false, -1, rCurTaskPlatformId,
                root.getPriority() - 1, 5);
        Task t6 = new Task(false, -1, rCurTaskPlatformId,
                root.getPriority() - 1, 6);
        taskQueue.add(t4);
        taskQueue.add(t5);
        taskQueue.add(t6);
    }

    public static final int PLATFORM_TYPE_NUMER = 9;
    public static final int ROBOT_TYPE_NUMER = 4;
    public static final int PLATFORM_MSG_LENGTH = 6;
    public static final int ROBOT_MSG_LENGTH = 10;
    public static int PLAYFORM_NUMBER = 0;
    public static int curFrameID;
}
