import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

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
        // 如果机器人空闲
        if (curR.getTargetPlatFormIndex() == -1) {
            System.err.printf("FrameId:%d, robot%d为空闲状态\n ", Utils.curFrameID, curR.getNum());
            // 领取任务
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
            System.out.println();
            // print

            Task curTask = taskQueue.poll(); // 队头领取任务
            int taskNum = curTask.getTaskNum(); // 任务类型
            int rootTaskPlatformId = curTask.getRootTaskPlatformId(); // 父平台id
            int curTaskPlatformId = curTask.getCurTaskPlatformId(); // 当前平台
            CompareForBuy cfb = new CompareForBuy(curR, 1.0, 20.0);
            PriorityQueue<PlatForm> priorityQueue = new PriorityQueue<>(cfb);
            if (curTaskPlatformId == -1) { // 寻找出售该产品的平台
                List<PlatForm> platForms = labelPlatforms.get(taskNum);
                for (PlatForm p : platForms) {
                    // TODO 待优化
                    priorityQueue.add(p);
                }
                curTaskPlatformId = priorityQueue.poll().getNum();
            }

            // print
//            System.err.printf("领到的原子任务为:%d, 需要到平台%d领取\n", taskNum, p.getNum());
            // print END
            curR.setpS(curTaskPlatformId);
            curR.setpE(rootTaskPlatformId); // 有可能为-1
            curR.setTargetPlatFormIndex(curR.getpS());
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
                //下面可能需要修改
                // 拿到物品，变成卖东西
                int sellId = curR.getpE();
                if (sellId == -1) {
                    CompareForBuy cfb = new CompareForBuy(curR, 1.0, 20.0);
                    PriorityQueue<PlatForm> priorityQueue = new PriorityQueue<>(cfb);
                    List<PlatForm> platForms = labelPlatforms.get(target.getPlatFormType().getIndex());
                    for (PlatForm p : platForms) {
                        // TODO 待优化
                        priorityQueue.add(p);
                    }
                    PlatForm p = priorityQueue.poll();
                    curR.setTargetPlatFormIndex(p.getNum());
                    curR.setpE(p.getNum());
                } else {
                    curR.setTargetPlatFormIndex(sellId);
                    curR.setpS(-1);
                    curR.setpE(-1);
                }

//                curR.setTargetPlatFormIndex(Utils.findTargetForRobot(platFormList, curR));// 为机器人寻找下一个目标工作台

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

                curR.setpE(-1);
                curR.setpS(-1);
                curR.setTargetPlatFormIndex(-1);

                // 当前平台是否能发布生产请求
                int need = target.getPlatFormType().getNeededMateria();
                int materiaStatus = target.getMateriaStatus() | (1 << index);
                if (((need >> 1) ^ (materiaStatus >> 1)) == 0 && target.getLeftFrame() == -1) {
                    // 发布请求
                    target.setAssignProductTask(false);
                    target.setChoosedForProduct(false);
                }
            }
        }
        // 根据目的地 发出最新控制指令
        res.addAll(new Motion().Move(curR, platFormList, labelPlatforms, taskQueue));//加入移动指令
        return res;
    }
}
