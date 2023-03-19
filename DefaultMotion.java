import java.util.ArrayList;
import java.util.List;

public class DefaultMotion implements MoveType {

    /**
     * 此函数用于计算买卖指令与移动指令
     *
     * @param curR         当前机器人
     * @param platFormList 机器人对应的目标工作台
     * @return 当前机器人的指令序列
     */
    public List<Order> Move(Robot curR, List<PlatForm> platFormList) {
        List<Order> res = new ArrayList<>(); // 计算当前帧的指令集合
        if (curR.getTargetPlatFormIndex() == -1) { // 分配机器人任务

//            int targetPlatform = Utils.findTargetForRobot(platFormList, curR); // 找到目的平台
//            curR.setTargetPlatFormIndex(targetPlatform);
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
                curR.setTargetPlatFormIndex(Utils.findTargetForRobot(platFormList, curR));// 为机器人寻找下一个目标工作台
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
                if (target.HasProduct() && !target.isAssigned(0)) {
                    // 当前工作台有产品可买且未派遣机器人
                    res.add(new Order(OrderType.BUY, curR.getNum()));
                    target.changeProductStatus();// 产品格设置为空
                    curR.changeStatus();// 机器人状态转换为卖途
                    curR.setItem(new Item(target.getPlatFormType().getProductItemType()));
                }
                //下面可能需要修改
                curR.setTargetPlatFormIndex(Utils.findTargetForRobot(platFormList, curR));// 为机器人寻找下一个目标工作台
            }
        }
        // 根据目的地 发出最新控制指令
        res.addAll(new Motion().Move(curR, platFormList));//加入移动指令
        return res;
    }
}
