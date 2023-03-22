import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

// TODO 撤销任务之后没有重置任务
public class DefaultMotion implements MoveType {

    /**
     * 此函数用于计算买卖指令与移动指令
     *
     * @param curR           当前机器人
     * @param platFormList   机器人对应的目标工作台
     * @param labelPlatforms 分类各种工作台
     * @param taskQueue      当前任务队列
     * @return 当前机器人的指令序列
     */
    public List<Order> Move(Robot curR, List<PlatForm> platFormList, List<List<PlatForm>> labelPlatforms, PriorityQueue<Task> taskQueue) {
        List<Order> res = new ArrayList<>();
        // 如果机器人空闲  则领取任务
        if (curR.getTargetPlatFormIndex() == -1) {
            System.err.printf("FrameId:%d, robot%d为空闲状态\n ", Utils.curFrameID, curR.getNum());
            // 处理任务队列
            while (!taskQueue.isEmpty() && !taskQueue.peek().isAtomic()) {
                // 懒加载策 略 分解队头复合任务 至多分解两次
                Utils.splitTask(taskQueue);
            }

            // 获取任务
            if (taskQueue.isEmpty()) {
                // 没有任务领取 TODO 若任务队列没有任务 也许可以从生产队列中入手
                return new ArrayList<>();
            }
            Task t = taskQueue.poll(); // 队头领取任务
            System.err.printf("领取到的任务为：[%s]\n", t.toString());
            int taskNum = t.getTaskNum(); // 任务类型
            int platformIdForBuy = t.getPlatformIdForBuy();// 机器人买材料目的地
            int platformIdForSell = t.getPlatformIdForSell();// 机器人卖材料目的地
            CompareForBuy c = new CompareForBuy(curR, 1.0, 20.0);
            PriorityQueue<PlatForm> priorityQueue = new PriorityQueue<>(c);
            if (platformIdForBuy == -1) {  // 不知道去哪里买 则寻找适合平台
                List<PlatForm> platForms = labelPlatforms.get(taskNum);
                for (PlatForm p : platForms) { // 第一轮查找
                    // 若任务类型为<=3 则直接加进去就好 否则判断是否有产品,并且产品格是否委派了机器人
                    if ((taskNum <= 3 || p.HasProduct()) && !p.isAssigned(0))
                        priorityQueue.add(p);
                }
                if (!priorityQueue.isEmpty()) {
                    PlatForm p = priorityQueue.poll();
                    p.setAssignStatus(0, true); // 表明该产品格已经分配机器人
                    platformIdForBuy = p.getNum();
                } else {
                    //TODO 二轮查找
                }
            }
            curR.setTargetPlatFormIndex(platformIdForBuy); // 设置买材料的目的地
            curR.setNextTargetPlatformIndex(platformIdForSell); // 设置卖材料的目的地
            if (platformIdForBuy >= 0) { // 用于碰撞
                double dis = Utils.getDistance(curR.getPosition(), platFormList.get(platformIdForBuy).getPosition());// 距离
                int frameNum = (int) dis * 15;// 预期所需帧数为 假定v=4/s t=dis/v 一秒50帧
                double adjustV = 1 + dis / 80;
                frameNum /= adjustV;
                curR.setExceptArriveFrame(frameNum);// 设置预期到达帧数
                curR.resetRealArriveFrame();// 重置运行帧数
            }
        }


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
                if (nextTargetPlatForm >= 0) {
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
        // 根据目的地 发出最新控制指令
        res.addAll(new Motion().Move(curR, platFormList, labelPlatforms, taskQueue));//加入移动指令
        return res;
    }
}
