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
            while (!taskQueue.isEmpty() && !taskQueue.peek().isAtomic()) {
                Utils.splitTask(taskQueue);
            }
            if (taskQueue.isEmpty()) {
                break;
            }
            Task curTask = taskQueue.poll(); //队头任务
            System.err.printf("领取到的任务为：[%s]\n", curTask.toString());
            // 以下逻辑为：为该任务选择一个最恰当的机器人
            double[] position = platFormList.get(curTask.getPlatformIdForBuy()).getPosition(); //获取买材料目的地坐标
            int index = 0, len = needTask.size();
            double minDst = Double.MAX_VALUE;
            for (int i = 0; i < len; i++) {
                double curD = Utils.getDistance(position, needTask.get(i).getPosition()); //机器人与工作台的距离
                if (curD < minDst) {
                    minDst = curD;
                    index = i;
                }
            }
            Robot curR = needTask.get(index);
            if (curTask.getTaskNum() == 7) {
                // 如果当前任务是取7号材料
                CompareBetweenPlatform cmp = new CompareBetweenPlatform(platFormList.get(curTask.getPlatformIdForBuy()));
                PriorityQueue<PlatForm> pq = new PriorityQueue<>(cmp);
                for (PlatForm p : Main.labelPlatforms.get(8))
                    pq.add(p);
                for (PlatForm p : Main.labelPlatforms.get(9))
                    pq.add(p);
                curTask.setRootTaskPlatformId(pq.peek().getNum()); //设置卖目的工作台
            }
            curR.setTargetPlatFormIndex(curTask.getPlatformIdForBuy());
            curR.setNextTargetPlatformIndex(curTask.getPlatformIdForSell());

            // TODO 这里是设置预期运行帧
            if (curR.getTargetPlatFormIndex() >= 0) { // 用于碰撞
                double dis = Utils.getDistance(curR.getPosition(), platFormList.get(curR.getTargetPlatFormIndex()).getPosition());// 距离
                int frameNum = (int) dis * 15;// 预期所需帧数为 假定v=4/s t=dis/v 一秒50帧
                double adjustV = 1 + dis / 80;
                frameNum /= adjustV;
                curR.setExceptArriveFrame(frameNum);// 设置预期到达帧数
                curR.resetRealArriveFrame();// 重置运行帧数
            }
            needTask.remove(index); //分配了任务的机器人移除
        }
        // 上面是确定机器人的目标 以下是机器人靠近目标的动作
        for (Robot curR : robotList) {
            if (curR.getTargetPlatFormIndex() == -1) continue; // TODO 没有分配到任务的机器人 可以去生产队列搞事
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
                    // TODO 这里是设置预期运行帧
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
