import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

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
        List<Order> res = new ArrayList<>(); // 计算当前帧的指令集合
        // 如果机器人空闲  则领取任务
        if (curR.getTargetPlatFormIndex() == -1) {
            System.err.printf("FrameId:%d, robot%d为空闲状态\n ", Utils.curFrameID, curR.getNum());
            // 处理任务队列
            if (!taskQueue.peek().isAtomic()) {
                // 懒加载策略 如果队头不是原子任务
                Utils.splitTask(taskQueue);
                Utils.splitTask(taskQueue);
            }

            // print
            PriorityQueue<Task> del = new PriorityQueue<>(taskQueue);
            while (!del.isEmpty()) {
                System.err.println(del.poll().toString());
            }
            // print end

            // 获取任务
            Task curTask = taskQueue.poll(); // 队头领取任务
            int taskNum = curTask.getTaskNum(); // 任务类型
            int rootTaskPlatformId = curTask.getRootTaskPlatformId(); // 父平台id
            int curTaskPlatformId = curTask.getCurTaskPlatformId(); // 当前平台
            CompareForBuy cfb = new CompareForBuy(curR, 1.0, 20.0);
            PriorityQueue<PlatForm> priorityQueue = new PriorityQueue<>(cfb);
            if (curTaskPlatformId == -1) { // 寻找出售该产品的平台
                List<PlatForm> platForms = labelPlatforms.get(taskNum);
                for (PlatForm p : platForms) { // 第一轮查找
                    // 若任务类型为<=3 则直接加进去就好 否则判断是否有产品,并且产品格是否委派了机器人
                    if ((taskNum <= 3 || p.HasProduct()) && !p.isAssigned(0))
                        priorityQueue.add(p);
                }
                if (!priorityQueue.isEmpty()) {
                    PlatForm p = priorityQueue.poll();
                    p.setAssignStatus(0, true); // 表明该产品格已经分配机器人
                    curTaskPlatformId = p.getNum();
                } else {
                    //TODO 二轮查找
//                    int leftTime = 1000;
//                    for (PlatForm p : platForms) {
//                        p.
//                    }
                }
            }
            curR.setTargetPlatFormIndex(curTaskPlatformId); // 当前需要去往的平台
            curR.setNextTargetPlatformIndex(rootTaskPlatformId); //下一个需要去往的平台
        }


        PlatForm target = platFormList.get(curR.getTargetPlatFormIndex()); // 获得对应的平台
        if (curR.getNearByPlatFormId() == curR.getTargetPlatFormIndex()) { // 靠近目标平台
            //目标工作台id与附近工作台id相同
            if (!curR.getStatus() && target.HasProduct()) {
                /*
                 * 机器人为买途，并且产品格有产出
                 */
                res.add(new Order(OrderType.BUY, curR.getNum()));// 加入买指令【buy, 当前机器人编号】
                target.changeProductStatus();// 产品格设置为空
                target.setAssignStatus(0, false);// 把产品格委派状态复位
                curR.changeStatus();// 机器人状态转换为卖途
                curR.setItem(new Item(target.getPlatFormType().getProductItemType()));
                int type = target.getPlatFormType().getIndex();
                if (type >= 4 && type <= 7) {
                    target.setAssignFetchTask(false); //平台产品卖出 可以重新发布fetch任务
                }
                //下面可能需要修改
                // 拿到物品，变成卖东西
                ItemType carryItemType = curR.getItem().getItemType();

                int nextTargetPlatForm = curR.getNextTargetPlatformIndex();

                if (nextTargetPlatForm == -1) { // 即拿到了物品，不知道卖给谁
                    CompareForBuy cfb = new CompareForBuy(curR, 1.0, 20.0);
                    PriorityQueue<PlatForm> pQ = new PriorityQueue<>(cfb);
                    // TODO edit
                    for (PlatForm p : platFormList) {
                        if (p.isNeededMateria(carryItemType) && !p.getMateriaStatusByIndex(0))
                            // 平台需要该物品 并且 该平台是否已经选择用于生产成品【如果是，则说明已经有机器人帮他收集材料，所以不能放】
                            pQ.add(p);
                    }
                    PlatForm p = pQ.poll();
                    p.setAssignStatus(carryItemType.getNum(), true); // 修改平台分配该物品的标记位
                    curR.setTargetPlatFormIndex(p.getNum());
                } else {
                    platFormList.get(nextTargetPlatForm).setAssignStatus(carryItemType.getNum(), true); // 修改平台分配该物品的标记位
                    curR.setTargetPlatFormIndex(nextTargetPlatForm);
                }
                curR.setNextTargetPlatformIndex(-1);
            } else if (curR.getStatus() && !target.getMateriaStatusByIndex(curR.getItem().getItemType().getNum())) {
                /*
                 * 机器人为卖途并且原料格未被占用
                 */
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
                curR.setTargetPlatFormIndex(-1);
                curR.setNextTargetPlatformIndex(-1);
            }
        }
        // 根据目的地 发出最新控制指令
        res.addAll(new Motion().Move(curR, platFormList, labelPlatforms, taskQueue));//加入移动指令
        return res;
    }
}
