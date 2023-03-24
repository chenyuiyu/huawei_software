import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class DefaultMotion {

    /**
     * 此函数用于计算买卖指令与移动指令
     *
     * @param robotList      机器人列表
     * @param platFormList   机器人对应的目标工作台
     * @param labelPlatforms 分类各种工作台
     * @param taskQueue      当前任务队列
     * @return 当前机器人的指令序列
     */
    public static List<Order> Move(List<Robot> robotList, List<PlatForm> platFormList, List<List<PlatForm>> labelPlatforms, PriorityQueue<Task> taskQueue) {
        List<Order> res = new ArrayList<>();
        List<Robot> needTask = new ArrayList<>(); // 空闲机器人队列
        for (Robot r : robotList) {
            if (r.getTargetPlatFormIndex() == -1) needTask.add(r);
        }
        while (!needTask.isEmpty()) {
            // 为每个机器人寻找一个任务
            System.err.printf("FrameID:%d, 存在%d个机器人空闲\n", Utils.curFrameID, needTask.size());

            // printfr5800
            PriorityQueue<Task> del = new PriorityQueue<>(taskQueue);
            System.err.printf("FrameID: %d 任务队列情况", Utils.curFrameID);
            for (Task task : del) {
                System.err.println(task.toString());
            }
            // print_end

            int MAX_SPLIT_TIME = 10;
            while (!taskQueue.isEmpty() && !taskQueue.peek().isAtomic() && MAX_SPLIT_TIME-- > 0) {
                Utils.splitTask(taskQueue);
            }
            if (taskQueue.isEmpty() || !taskQueue.peek().isAtomic()) {
                break;
            }
            Task t = taskQueue.poll(); //队头任务
            System.err.printf("队头任务[%s]\n", t.toString());
            int taskNum = t.getTaskNum(); // 任务类型
            int platformIdForBuy = t.getPlatformIdForBuy();// 机器人买材料目的地
            int platformIdForSell = t.getPlatformIdForSell();// 机器人卖材料目的地
            /**
             * 任务队列的原子任务，只有可能是(-1,x) 或者 (x,xx) 【其中二元祖表示买目的地，卖目的地】
             * 其中对于(-1, x)， 如何确定买的目的地，并找到最适合接受该任务的机器人？【需要确定两个东西】
             * 例如tasuNum=1, 我们要遍历4个机器人，根据机器人【去拿1并且卖给x走过的距离】选择走的路程最少的机器人
             * 从而确定1，确定机器人，
             *
             * 对于fetch型任务同理
             */
            if (platformIdForSell == -1) {
                // 类型为7的fetch任务 特殊处理一下
                CompareBetweenPlatform cmp = new CompareBetweenPlatform(Main.platformsList.get(t.getCurTaskPlatformId()));
                PriorityQueue<PlatForm> queue = new PriorityQueue<>(cmp);
                if (!labelPlatforms.get(8).isEmpty()) {
                    queue.addAll(labelPlatforms.get(8));
                }
                if (!labelPlatforms.get(9).isEmpty()) {
                    queue.addAll(labelPlatforms.get(9));
                }
                platformIdForSell = queue.poll().getNum();
            }
            double[] sellPosition = Main.platformsList.get(platformIdForSell).getPosition();
            Robot targetR = null; // 需要确定机器人
            double minDis = Double.MAX_VALUE;
            if (platformIdForBuy == -1) { // 若不知道买的目的地
                int platformId = -1;
                minDis = Double.MAX_VALUE;
                for (Robot r : needTask) {
                    List<PlatForm> platForms = Main.labelPlatforms.get(taskNum);
                    double curDis = Double.MAX_VALUE;
                    int idx = -1;
                    for (PlatForm p : platForms) {
                        double curRToBuyDis = Utils.getDistance(r.getPosition(), p.getPosition());
                        double BuyToSellDis = Utils.getDistance(p.getPosition(), sellPosition);
                        if (curRToBuyDis + BuyToSellDis < curDis) {
                            curDis = curRToBuyDis + BuyToSellDis; // 更新距离当前机器人的最短距离
                            idx = p.getNum(); // 更新距离当前机器人最近的1
                        }
                    }
                    if (curDis < minDis) {
                        minDis = curDis;
                        targetR = r; //确定机器人
                        platformIdForBuy = idx;//确定目的地
                    }
                }
            } else {
                // 确定机器人
                double[] buyPosition = Main.platformsList.get(platformIdForBuy).getPosition();
                minDis = Double.MAX_VALUE;
                for (Robot r : needTask) {
                    double curRToBuyDis = Utils.getDistance(r.getPosition(), buyPosition);
                    double buyToSellDis = Utils.getDistance(buyPosition, sellPosition);
                    if (curRToBuyDis + buyToSellDis < minDis) {
                        minDis = curRToBuyDis + buyToSellDis;
                        targetR = r;
                    }
                }
            }
            // 尾优化 上面得到的minDis为机器人接受该任务 一买一卖的最短距离
            int needFrame = (int) (minDis / 2.9) * 50;
            if (needFrame >= 9000 - Utils.curFrameID) {
                // 如果需要的帧数大于当前剩余帧数
                // 则不做这个任务 不指派给任何机器人
                System.err.printf("FrameId:%d 尾优化\n", Utils.curFrameID);
                continue;
            }

            needTask.remove(targetR); // 机器人从空闲链表中移除
            targetR.setTargetPlatFormIndex(platformIdForBuy);
            targetR.setNextTargetPlatformIndex(platformIdForSell);

            if (targetR.getTargetPlatFormIndex() >= 0) { // 用于碰撞
                double dis = Utils.getDistance(targetR.getPosition(), platFormList.get(targetR.getTargetPlatFormIndex()).getPosition());// 距离
                int frameNum = (int) dis * 15;// 预期所需帧数为 假定v=4/s t=dis/v 一秒50帧
                double adjustV = 1 + dis / 80;
                frameNum /= adjustV;
                targetR.setExceptArriveFrame(frameNum);// 设置预期到达帧数
                targetR.resetRealArriveFrame();// 重置运行帧数
            }
        }
        // 上面是确定机器人的目标 以下是机器人靠近目标的动作
        for (Robot curR : robotList) {
            if (curR.getTargetPlatFormIndex() == -1) continue;
            PlatForm target = platFormList.get(curR.getTargetPlatFormIndex()); // 获得对应的平台
            if (curR.getNearByPlatFormId() == curR.getTargetPlatFormIndex()) { // 靠近目标平台
                //机器人为买途，并且产品格有产出
                if (!curR.getStatus() && target.HasProduct()) {
                    res.add(new Order(OrderType.BUY, curR.getNum()));// 加入买指令【buy, 当前机器人编号】
                    target.changeProductStatus();// 产品格设置为空
                    target.setAssignStatus(0, false);// 把产品格委派状态复位
                    // 机器人状态转换为卖途
                    curR.changeStatus();
                    curR.setItem(new Item(target.getPlatFormType().getProductItemType()));
                    int type = target.getPlatFormType().getIndex();
                    if (type >= 4 && type <= 7) {
                        target.setAssignFetchTask(false); //重新发布fetch任务
                    }

                    ItemType carryItemType = curR.getItem().getItemType();
                    int nextTargetPlatForm = curR.getNextTargetPlatformIndex(); // 获得卖材料目的地
                    if (nextTargetPlatForm == -1) { // 不知道卖给谁
                        CompareForBuy c = new CompareForBuy(curR, 1.0, 20.0);
                        PriorityQueue<PlatForm> queue = new PriorityQueue<>(c);
                        for (PlatForm p : platFormList) {
                            // 平台需要该物品 并且 该平台是否已经选择用于生产成品【如果是，则说明已经有机器人帮他收集材料，所以不能放】
                            if (p.isNeededMateria(carryItemType) && !p.isChoosedForProduct())
                                queue.add(p);
                        }
                        PlatForm p = queue.poll();
                        p.setAssignStatus(carryItemType.getNum(), true); // 修改平台分配该物品的标记位
                        curR.setTargetPlatFormIndex(p.getNum());
                    } else {
                        platFormList.get(nextTargetPlatForm).setAssignStatus(carryItemType.getNum(), true); // 修改平台分配该物品的标记位
                        curR.setTargetPlatFormIndex(nextTargetPlatForm);
                    }
                    if (curR.getTargetPlatFormIndex() >= 0) {
                        double dis = Utils.getDistance(curR.getPosition(),
                                platFormList.get(nextTargetPlatForm).getPosition());// 距离
                        int frameNum = (int) dis * 15;// 预期所需帧数为 假定v=4/s t=dis/v 一秒50帧
                        double adjustV = 1 + dis / 80;
                        frameNum /= adjustV;
                        curR.setExceptArriveFrame(frameNum);// 设置预期到达帧数
                        curR.resetRealArriveFrame();// 重置运行帧数
                    }
                    curR.setNextTargetPlatformIndex(-1);
                } else if (curR.getStatus() && !target.getMateriaStatusByIndex(curR.getItem().getItemType().getNum())) {
                    // 机器人为卖途并且原料格未被占用
                    res.add(new Order(OrderType.SELL, curR.getNum()));// 加入卖指令
                    int index = curR.getItem().getItemType().getNum();// 机器人携带的产品类型编号
                    target.setAssignStatus(index, false);// 把原料格委派位复位
                    target.changeMateriaStatusByIndex(index);// 把原料位置位
                    curR.changeStatus();// 机器人状态转换为买途
                    curR.setItem(new Item(ItemType.ZERO)); // 清空货物

                    // 如果卖了东西之后，平台的材料格全满了 把发布任务的标志位重新置为false，代表完成了任务
                    if (((target.getMateriaStatus() | (1 << index)) >> 1) == (target.getPlatFormType().getNeededMateria() >> 1)) {
                        target.setAssignProductTask(false);
                        target.setChoosedForProduct(false);
                    }
                    // 机器人完成了一个任务，将所有平台相关的信息置空 等待下一帧分配任务
                    curR.setTargetPlatFormIndex(-1);
                    curR.setNextTargetPlatformIndex(-1);
                }
            }
            res.addAll(new Motion().Move(curR, platFormList, labelPlatforms, taskQueue));//针对当前机器人加入移动指令
        }
        return res;
    }
}
