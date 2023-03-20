
//package com.huawei.codecraft;
import java.util.ArrayList;
import java.util.List;

public class DefaultMotion implements MoveType {

    public DefaultMotion(int[] itemPlaceCount, int[] curItemPlaceCount) {
        this.itemPlaceCount = itemPlaceCount;
        this.curItemPlaceCount = curItemPlaceCount;
    }

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
        if (r.getNearByPlatFormId() == target.getNum()) {
            // 目标工作台id与附近工作台id相同
            int beside = target.getPlatFormType().getProductItemType().getNum();// 工作台产出材料的类型
            if (!r.getStatus()) {
                if (target.HasProduct()) {
                    /*
                     * 机器人为买途，并且产品格有产出
                     */
                    if (r.getExceptArriveFrame() + Robot.frameID + 200 > Robot.ENDFRAMEID)
                        return res;// 最后5s不买任何东西
                    res.add(new Order(OrderType.BUY, r.getNum()));// 加入买指令
                    curItemPlaceCount[beside]++;// 把原料格委派位复位
                    target.changeProductStatus();// 产品格设置为空
                    target.setAssignStatus(0, false);// 把产品格委派状态复位
                    r.changeStatus();// 机器人状态转换为卖途
                    r.setItem(new Item(target.getPlatFormType().getProductItemType()));// 设置机器人的携带物品，方便查找下一个目标工作台
                }
                // 下面可能需要修改
                r.setTargetPlatFormIndex(FindNextTarget.findTarget(r, p, itemPlaceCount, curItemPlaceCount));// 为机器人寻找下一个目标工作台
            } else if (r.getStatus()) {
                /*
                 * 机器人为卖途并且原料格未被占用
                 */
                int index = r.getItem().getItemType().getNum();// 机器人携带的产品类型编号
                if (!target.getMateriaStatusByIndex(index)) {
                    // 原料格空则售出
                    res.add(new Order(OrderType.SELL, r.getNum()));// 加入卖指令
                    target.setAssignStatus(index, false);// 把原料格委派位复位
                    target.changeMateriaStatusByIndex(index);// 把原料位置位
                    r.changeStatus();// 机器人状态转换为买途
                    r.setItem(new Item(ItemType.ZERO));// 清空机器人携带物
                    if (target.HasProduct() && !target.isAssigned(0)
                            && (itemPlaceCount[beside] == 0 || itemPlaceCount[beside] > curItemPlaceCount[beside])) {
                        // 当前工作台有合法产品可买且未派遣机器人
                        if (r.getExceptArriveFrame() + Robot.frameID + 200 > Robot.ENDFRAMEID)
                            return res;// 最后5s不买任何东西
                        res.add(new Order(OrderType.BUY, r.getNum()));// 加入买指令
                        curItemPlaceCount[beside]++;// 材料格占用加1
                        target.changeProductStatus();// 产品格设置为空
                        r.changeStatus();// 机器人状态转换为卖途
                        r.setItem(new Item(target.getPlatFormType().getProductItemType()));// 设置机器人的携带物品，方便查找下一个目标工作台
                    }
                    // r.setTargetPlatFormIndex(FindNextTarget.findTarget(r, p, itemPlaceCount,
                    // curItemPlaceCount));// 为机器人寻找下一个目标工作台
                }
                r.setTargetPlatFormIndex(FindNextTarget.findTarget(r, p, itemPlaceCount, curItemPlaceCount));
                /*
                 * else if(index <= 3) {
                 * //原料格未空并且携带材料为1-3号则销毁
                 * res.add(new Order(OrderType.DESTROY, r.getNum()));//加入销毁指令
                 * target.setAssignStatus(index, false);// 把原料格委派位复位
                 * r.changeStatus();// 机器人状态转换为买途
                 * r.setItem(new Item(ItemType.ZERO));// 清空机器人携带物
                 * r.setTargetPlatFormIndex(FindNextTarget.findTarget(r, p, itemPlaceCount,
                 * curItemPlaceCount));// 为机器人寻找下一个目标工作台
                 * }
                 */
            }
        }
        res.addAll(new Motion().Move(r, p));// 加入移动指令
        return res;
    }

    private int[] itemPlaceCount;// 各类物品原料格的总数
    private int[] curItemPlaceCount;// 当前各类物品已满原料格的计数
}