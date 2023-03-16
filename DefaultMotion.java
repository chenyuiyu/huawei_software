import java.util.ArrayList;
import java.util.List;

public class DefaultMotion implements MoveType {

    /**
     * 算法问题主要为死锁问题材料格死锁问题和类型优先级问题
     * 此函数用于计算买卖指令与移动指令
     * 
     * @param r 当前机器人
     * @param p 机器人对应的目标工作台
     * @return 当前机器人的指令序列
     */
    public List<Order> Move(Robot r, PlatForm[] p) {
        List<Order> res = new ArrayList<>();
        PlatForm target = p[r.getTargetPlatFormIndex()];
        FindNextTarget f = new FindNextTarget();
        if (r.getNearByPlatFormId() == target.getNum()) {
            // 目标工作台id与附近工作台id相同
            if (!r.getStatus() && target.HasProduct()) {
                /*
                 * 机器人为买途，并且产品格有产出
                 */
                res.add(new Order(OrderType.BUY, r.getNum()));// 加入买指令
                target.changeProductStatus();// 产品格设置为空
                target.setAssignStatus(0, false);// 把产品格委派状态复位
                r.changeStatus();// 机器人状态转换为卖途
                r.setItem(new Item(target.getPlatFormType().getProductItemType()));// 设置机器人的携带物品，方便查找下一个目标工作台
                // 下面可能需要修改
                r.setTargetPlatFormIndex(f.findTarget(r, p));// 为机器人寻找下一个目标工作台
            } else if (r.getStatus() && !target.getMateriaStatusByIndex(r.getItem().getItemType().getNum())) {
                /*
                 * 机器人为卖途并且原料格未被占用
                 */
                res.add(new Order(OrderType.SELL, r.getNum()));// 加入卖指令
                int index = r.getItem().getItemType().getNum();// 机器人携带的产品类型编号
                target.setAssignStatus(index, false);// 把原料格委派位复位
                target.changeMateriaStatusByIndex(index);// 把原料位置位
                r.changeStatus();// 机器人状态转换为买途
                r.setItem(new Item(ItemType.ZERO));// 清空机器人携带物
                if (target.HasProduct() && !target.isAssigned(0)) {
                    // 当前工作台有产品可买且未派遣机器人
                    res.add(new Order(OrderType.BUY, r.getNum()));// 加入买指令
                    target.changeProductStatus();// 产品格设置为空
                    r.changeStatus();// 机器人状态转换为卖途
                    r.setItem(new Item(target.getPlatFormType().getProductItemType()));// 设置机器人的携带物品，方便查找下一个目标工作台
                }
                // 下面可能需要修改
                r.setTargetPlatFormIndex(f.findTarget(r, p));// 为机器人寻找下一个目标工作台
            }
        }
        res.addAll(new Motion().Move(r, p));// 加入移动指令
        return res;
    }
}