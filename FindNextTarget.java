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
        PlatFormBoxForBuy.curRobotposition = curRobotposition;// 机器人位置
        PlatFormBoxForBuy.curVector = curVector;
        PlatFormBoxForBuy.ipc = ipc;
        PlatFormBoxForBuy.cipc = cipc;
        PriorityQueue<PlatFormBoxForBuy> records123 = new PriorityQueue<>();// 1/2/3类工作台存一个优先队列
        PriorityQueue<PlatFormBoxForBuy> records456 = new PriorityQueue<>();// 4/5/6类工作台存一个优先队列
        PriorityQueue<PlatFormBoxForBuy> records7 = new PriorityQueue<>();// 7类工作台存一个优先队列
        for (PlatForm cur : p) {
            int type = cur.getPlatFormType().getProductItemType().getNum();//生产的材料的类型
            if (cur.HasProduct() && !cur.isAssigned(0) && (ipc[type] == 0 || ipc[type] > cipc[type])) {
                // 该平台有产出并且未派遣机器人并且有剩余空位则加入对应优先队列
                int index = cur.getPlatFormType().getIndex();
                PlatFormBoxForBuy box = new PlatFormBoxForBuy(cur);
                if (index <= 3)
                    records123.add(box);
                else if (index <= 6)
                    records456.add(box);// 加入到对应类型的优先队列
                else
                    records7.add(box);
            }
        }
        PlatFormBoxForBuy box = null;
        if (r.getNum() <= 2) {
            box = records123.peek();
            if (box == null)
                box = records456.peek();
            if (box == null)
                box = records7.peek();
        } else {
            box = records7.peek();
            if (box == null)
                box = records456.peek();
            if (box == null)
                box = records123.peek();
        }

        // 需要处理没有下一个目标的情况
        if (box == null) {
            // 以下逻辑为处理第一轮未找到目标工作台,寻找1-3类型的未分配机器人的并且产品有剩余材料位的工作台,派机器人到那里等待
            for (PlatForm cur : p) {
                int type = cur.getPlatFormType().getProductItemType().getNum();//产品类型
                if (cur.getPlatFormType().getIndex() <= 3 && !cur.isAssigned(0) && ipc[type] > cipc[type]) {
                    target = cur;
                    break;
                }
            }
        } else target = box.getPlatForm();
        target.setAssignStatus(0, true);// 翻转派遣位
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
        PlatFormBoxForSell.curRobotposition = curRobotposition;// 机器人位置
        PlatFormBoxForSell.curVector = curVector;
        PlatFormBoxForSell.ipc = ipc;
        PlatFormBoxForSell.cipc = cipc;
        PriorityQueue<PlatFormBoxForSell> records456 = new PriorityQueue<>();// 每类工作台存一个优先队列
        PriorityQueue<PlatFormBoxForSell> records789 = new PriorityQueue<>();
        int id = r.getItem().getItemType().getNum();// 机器人携带材料的编号
        int status = 1 << id;
        for (PlatForm cur : p) {
            PlatFormType type = cur.getPlatFormType();//工作台类型
            int index = type.getProductItemType().getNum();//产品类型
            if (((type.getNeededMateria() & status) > 0) && ((cur.getAssignStatus() & status) == 0)
                    && ((cur.getMateriaStatus() & status) == 0) && (ipc[index] == 0 || ipc[index] > cipc[index])) {
                // 该平台需要该材料并且该材料格空闲并且未派遣机器人则加入备选队列
                PlatFormBoxForSell box = new PlatFormBoxForSell(cur);
                if (type.getIndex() <= 6)
                    records456.add(box);// 加入到对应类型的优先队列
                else
                    records789.add(box);
            }
        }
        // 需要处理没有下一个目标的情况
        PlatFormBoxForSell box = null;
        box = records456.peek();
        if (box == null)
            box = records789.peek();
        if (box == null) {
            // 以下逻辑为处理第一轮未找到目标工作台,寻找最少剩余生产时间并且需要该原料的工作台,派机器人到那里等待
            int leftTime = 1000;// 剩余生产帧数
            for (PlatForm cur : p) {
                if ((cur.getPlatFormType().getNeededMateria() & status) > 0 && cur.getLeftFrame() <= leftTime) {
                    target = cur;
                    leftTime = cur.getLeftFrame();
                }
            }
        } else target = box.getPlatForm();
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

    /**
     * 三个系数abc分别控制距离和角度差的权值以占位格分数的权值
     */
    private static class PlatFormBoxForSell implements Comparable<PlatFormBoxForSell> {

        public PlatFormBoxForSell(PlatForm p) {
            this.p = p;
        }

        public int compareTo(PlatFormBoxForSell others) {
            int ind1 = p.getPlatFormType().getIndex();
            int ind2 = others.getPlatForm().getPlatFormType().getIndex();
            PlatForm p2 = others.getPlatForm();
            double p1w = a * Util.getDistance(p.getPosition(), curRobotposition)
                + 20.0 * b * Math.abs(Util.getVectorAngle(
                        Util.getVectorBetweenPoints(curRobotposition, p.getPosition()), curVector))
                            + c * 60.0 * p.getScore()
                                + d * ipc[ind1] / (0.01 * (ipc[ind1] - cipc[ind1]) + 0.00000001);// 权值函数，越小越好

            double p2w = a * Util.getDistance(p2.getPosition(), curRobotposition)
                + 20.0 * b * Math.abs(Util.getVectorAngle(
                    Util.getVectorBetweenPoints(curRobotposition, p2.getPosition()), curVector))
                        + c * 60.0 * p2.getScore()
                            + d * ipc[ind2] / (0.01 * (ipc[ind2] - cipc[ind2]) + 0.00000001);// 权值函数，越小越好
            if (p1w < p2w)
                return -1;
            return 1;
        }

        public PlatForm getPlatForm() {
            return p;
        }

        private PlatForm p;//工作台
        private static double[] curRobotposition;// 机器人位置
        private static double[] curVector;
        private static int[] ipc;// 1-6类物品原料格的总数
        private static int[] cipc;// 1-6类物品原料格当前数量
        private static final double a = 1.0, b = 1.0, c = 1.0, d = 1.0;
    }

    /**
     * 三个系数abc分别控制距离和角度差的权值以场上该类物品比例分数的权值
     */
    private static class PlatFormBoxForBuy implements Comparable<PlatFormBoxForBuy> {

        public PlatFormBoxForBuy(PlatForm p) {
           this.p = p;
        }

        public int compareTo(PlatFormBoxForBuy others) {
            PlatForm p2 = others.getPlatForm();
            int ind1 = p.getPlatFormType().getIndex();
            int ind2 = p2.getPlatFormType().getIndex();
            double p1w = a * Util.getDistance(p.getPosition(), curRobotposition)
                + 20.0 * b * Math.abs(Util.getVectorAngle(
                    Util.getVectorBetweenPoints(curRobotposition, p.getPosition()), curVector))
                        + c * ipc[ind1] / (0.01 * (ipc[ind1] - cipc[ind1]) + 0.00000001);// 权值函数，越小越好

            double p2w = a * Util.getDistance(p2.getPosition(), curRobotposition)
                + 20.0 * b * Math.abs(Util.getVectorAngle(
                    Util.getVectorBetweenPoints(curRobotposition, p2.getPosition()), curVector))
                        + c * ipc[ind2] / (0.01 * (ipc[ind2] - cipc[ind2]) + 0.00000001);// 权值函数，越小越好
            if (p1w < p2w)
                return -1;
            return 1;
        }

        public PlatForm getPlatForm() {
            return p;
        }

        private PlatForm p;//工作台
        private static double[] curRobotposition;// 机器人位置
        private static double[] curVector;
        private static int[] ipc;// 1-6类物品原料格的总数
        private static int[] cipc;// 1-6类物品原料格当前数量
        private static final double a = 1.0, b = 1.0, c = 1.0;
    }

}