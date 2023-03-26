//package com.huawei.codecraft;
import java.util.PriorityQueue;

/**
 * 该类为主算法类
 */
public class FindNextTarget {

    /**
     * 此函数用于计算机器人完成买或卖之后下一个目标工作台的确定
     * 
     * @param r 当前机器人
     * @return 目标工作台编号
     */
    public static int findTarget(Robot r, PlatForm[] p, int[] ipc, int[] cipc) {
        if (!r.getStatus())
            return findTargetForBuy(r, p, ipc, cipc);// 为机器人寻找下一个目标买取目标
        return findTargetForSell(r, p, ipc, cipc);// 为2、3号机器人寻找下一个卖出目标
    }

    /**
     * 函数用于为机器人分配买任务
     * 
     * @param r 需要分配任务的机器人
     * @param p 工作台队列
     * @return 下一个目标工作台
     */
    public static int findTargetForBuy(Robot r, PlatForm[] p, int[] ipc, int[] cipc) {
        PlatForm target = null;
        double[] curRobotposition = r.getPosition();// 机器人位置
        double dirction = r.getDirction();
        double[] curVector = { Math.cos(dirction), Math.sin(dirction) };// 机器人的线速度向量
        PlatFormBoxForBuy.setCurRobotposition(curRobotposition);// 机器人位置
        PlatFormBoxForBuy.setCurVector(curVector);
        PlatFormBoxForBuy.setIPC(ipc);
        PlatFormBoxForBuy.setCIPC(cipc);
        PriorityQueue<PlatFormBoxForBuy> records123456 = new PriorityQueue<>();// 1-6类工作台存一个优先队列
        PriorityQueue<PlatFormBoxForBuy> records7 = new PriorityQueue<>();// 7类工作台存一个优先队列
        for (PlatForm cur : p) {
            int type = cur.getPlatFormType().getProductItemType().getNum();//生产的材料的类型
            if (cur.HasProduct() && !cur.isAssigned(0) && (ipc[type] == 0 || ipc[type] > cipc[type])) {
                // 该平台有产出并且未派遣机器人并且有剩余空位则加入对应优先队列
                int index = cur.getPlatFormType().getIndex();
                PlatFormBoxForBuy box = new PlatFormBoxForBuy(cur);
                if (index <= 6)
                    records123456.add(box);
                else
                    records7.add(box);
            }
        }
        PlatFormBoxForBuy box = null;
        box = records7.peek();
        if (box == null)
            box = records123456.peek();
        // 需要处理没有下一个目标的情况
        if (box == null) {
            // 第二轮查找
            for (PlatForm cur : p) {
                int type = cur.getPlatFormType().getProductItemType().getNum();//产品类型
                int lf = cur.getLeftFrame();//剩余生产帧数
                if ((lf >= 0 && cur.computeFrameToPoint(curRobotposition) >= lf) && ipc[type] > cipc[type] && !cur.isAssigned(0)) {
                    records7.add(new PlatFormBoxForBuy(cur));
                }
            }
            box = records7.peek();
        } 
        if(box == null) {
            //第三轮查找
            for (PlatForm cur : p) {
                int type = cur.getPlatFormType().getIndex();//工作台类型
                if (type <= 3 && ipc[type] > cipc[type] ) {
                    records7.add(new PlatFormBoxForBuy(cur));
                }
            }
            box = records7.peek();
        }
        if(box == null) {
            //第四轮查找
            for (PlatForm cur : p) {
                int type = cur.getPlatFormType().getIndex();//工作台类型
                if (type <= 3) {
                    records7.add(new PlatFormBoxForBuy(cur));
                }
            }
            box = records7.peek();
        }
        //if(box == null)return -1;//未找到
        target = box.getPlatForm();
        target.setAssignStatus(0, true);// 翻转派遣位
        cipc[target.getPlatFormType().getProductItemType().getNum()]++;//该按材料格占用数加一
        double dis = Util.getDistance(r.getPosition(), target.getPosition());// 距离
        int frameNum = (int) dis * 15;// 预期所需帧数为 假定v=6m/s t=dis/v 一秒50帧
        if (dis > 60)
            frameNum /= 1.8;
        else if (dis > 30)
            frameNum /= 1.5;
        r.setExceptArriveFrame(frameNum);// 设置预期到达帧数
        r.resetRealArriveFrame();// 重置运行帧数
        return target.getNum();
    }

    /**
     * 函数用于为机器人分配卖任务
     * 
     * @param r 需要分配任务的机器人
     * @param p 工作台队列
     * @return 下一个目标工作台
     */
    public static int findTargetForSell(Robot r, PlatForm[] p, int[] ipc, int[] cipc) {
        // 如果机器人为卖途
        PlatForm target = null;
        double[] curRobotposition = r.getPosition();// 机器人位置
        double dirction = r.getDirction();
        double[] curVector = { Math.cos(dirction), Math.sin(dirction) };// 机器人的线速度向量
        PlatFormBoxForSell.setCurRobotposition(curRobotposition);// 机器人位置
        PlatFormBoxForSell.setCurVector(curVector);
        PlatFormBoxForSell.setIPC(ipc);
        PlatFormBoxForSell.setCIPC(cipc);
        PriorityQueue<PlatFormBoxForSell> records4567 = new PriorityQueue<>();// 每类工作台存一个优先队列
        PriorityQueue<PlatFormBoxForSell> records89 = new PriorityQueue<>();// 每类工作台存一个优先队列
        PriorityQueue<PlatFormBoxForSell> records4 = new PriorityQueue<>();//图四专用队列
        int id = r.getItem().getItemType().getNum();// 机器人携带材料的编号
        int status = 1 << id;
        for (PlatForm cur : p) {
            PlatFormType type = cur.getPlatFormType();//工作台类型
            if ((type.getNeededMateria() & status) > 0 && !cur.isAssigned(id) && !cur.getMateriaStatusByIndex(id)) {
                // 该平台需要该材料并且该材料格空闲并且未派遣机器人则加入备选队列
                PlatFormBoxForSell box = new PlatFormBoxForSell(cur);
                if(type.getIndex() == 4)records4.add(box);
                if (type.getIndex() <= 7)
                    records4567.add(box);// 加入到对应类型的优先队列
                else records89.add(box);
            }
        }
        // 需要处理没有下一个目标的情况
        PlatFormBoxForSell box = null;
        if(Main.mapNumber == 4)box = records4.peek();
        if(box == null)
            box = records4567.peek();
        if (box == null)
            box = records89.peek();
        if (box == null) {
            // 以下逻辑为处理第一轮未找到目标工作台,寻找最少剩余生产时间并且需要该原料的工作台,派机器人到那里等待
            for (PlatForm cur : p) {
                if ((cur.getPlatFormType().getNeededMateria() & status) > 0 && cur.getLeftFrame() != 0) {
                    records4567.add(new PlatFormBoxForSell(cur));
                }
            }
            box = records4567.peek();
        }
        if(box == null) {
            //第三轮查找
            for (PlatForm cur : p) {
                if ((cur.getPlatFormType().getNeededMateria() & status) > 0) {
                    records4567.add(new PlatFormBoxForSell(cur));
                }
            }
            box = records4567.peek();
        }
        target = box.getPlatForm();
        target.setAssignStatus(id, true);// 置位派遣位
        double dis = Util.getDistance(r.getPosition(), target.getPosition());// 距离
        // 距离越长 保持6m/s的时间越长 且花在旋转的时间占比更下 此时可以根据距离调整速度大小
        int frameNum = (int) dis * 15;// 预期所需帧数为 假定v=4m/s t=dis/v 所以帧数为t*1000/15 简化为dis*15
        if (dis > 60)
            frameNum /= 1.8;
        else if (dis > 30)
            frameNum /= 1.5;
        r.setExceptArriveFrame(frameNum);// 设置预期到达帧数
        r.resetRealArriveFrame();// 重置运行帧数
        return target.getNum();
    }

}