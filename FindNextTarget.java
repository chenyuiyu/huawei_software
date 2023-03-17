import java.util.Comparator;
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
        CompareForBuy cmp = new CompareForBuy(r, 1.0, 1.0, 1.0, ipc, cipc);// a = 1.0 b = 1.0 c = 1.0
        PriorityQueue<PlatForm> records123 = new PriorityQueue<>(cmp);// 1/2/3类工作台存一个优先队列
        PriorityQueue<PlatForm> records456 = new PriorityQueue<>(cmp);// 4/5/6类工作台存一个优先队列
        PriorityQueue<PlatForm> records7 = new PriorityQueue<>(cmp);// 7类工作台存一个优先队列
        for (PlatForm cur : p) {
            if (cur.HasProduct() && !cur.isAssigned(0)) {
                // 该平台有产出并且未派遣机器人
                int index = cur.getPlatFormType().getIndex();
                if (index <= 3)
                    records123.add(cur);
                else if (index <= 6)
                    records456.add(cur);// 加入到对应类型的优先队列
                else
                    records7.add(cur);
            }
        }
        if (r.getNum() <= 1) {
            target = records123.peek();
            if (target == null)
                target = records456.peek();
            if (target == null)
                target = records7.peek();
        } else {
            target = records7.peek();
            if (target == null)
                target = records456.peek();
            if (target == null)
                target = records123.peek();
        }

        // 需要处理没有下一个目标的情况
        if (target == null) {
            // 以下逻辑为处理第一轮未找到目标工作台,寻找1-3类型的工作台,派机器人到那里等待
            for (PlatForm cur : p) {
                if (cur.getPlatFormType().getIndex() <= 3 && !cur.isAssigned(0)) {
                    target = cur;
                    break;
                }
            }
        }

        target.setAssignStatus(0, true);// 翻转派遣位
        double dis = Util.getDistance(r.getPosition(), target.getPosition());// 距离
        int frameNum = (int) dis * 50 / 3;// 预期所需帧数为 假定v=6m/s t=dis/v 一秒50帧
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
        CompareForSell cmp = new CompareForSell(r, 1.0, 1.0, 1.0, 1.0, ipc, cipc);// a = 1.0, b = 1.0 c = 1.0
        PriorityQueue<PlatForm> records456 = new PriorityQueue<>(cmp);// 每类工作台存一个优先队列
        PriorityQueue<PlatForm> records789 = new PriorityQueue<>(cmp);
        int id = r.getItem().getItemType().getNum();// 机器人携带材料的编号
        int status = 1 << id;
        for (PlatForm cur : p) {
            PlatFormType type = cur.getPlatFormType();
            if (((type.getNeededMateria() & status) > 0) && ((cur.getAssignStatus() & status) == 0)
                    && ((cur.getMateriaStatus() & status) == 0)) {
                // 该平台需要该材料并且该材料格空闲并且未派遣机器人则加入备选队列
                if (type.getIndex() <= 6)
                    records456.add(cur);// 加入到对应类型的优先队列
                else
                    records789.add(cur);
            }
        }
        // 需要处理没有下一个目标的情况
        target = records456.peek();
        if (target == null)
            target = records789.peek();
        if (target == null) {
            // 以下逻辑为处理第一轮未找到目标工作台,寻找最少剩余生产时间并且需要该原料的工作台,派机器人到那里等待
            int leftTime = 1000;// 剩余生产帧数
            for (PlatForm cur : p) {
                if ((cur.getPlatFormType().getNeededMateria() & status) > 0 && cur.getLeftFrame() <= leftTime) {
                    target = cur;
                    leftTime = cur.getLeftFrame();
                }
            }
        }
        target.setAssignStatus(id, true);// 置位派遣位
        double dis = Util.getDistance(r.getPosition(), target.getPosition());// 距离
        int frameNum = (int) dis * 50 / 3;// 预期所需帧数为 假定v=4m/s t=dis/v 所以帧数为t*1000/15
        r.setExceptArriveFrame(frameNum);// 设置预期到达帧数
        r.resetRealArriveFrame();// 重置运行帧数
        return target.getNum();
    }

    /**
     * 三个系数abc分别控制距离和角度差的权值以占位格分数的权值
     */
    private static class CompareForSell implements Comparator<PlatForm> {

        public CompareForSell(Robot r, double a, double b, double c, double d, int[] ipc, int[] cipc) {
            this.a = a;
            this.b = b;
            this.c = c;
            this.d = d;
            curRobotposition = r.getPosition();// 机器人位置
            double dirction = r.getDirction();
            curVector = new double[] { Math.cos(dirction), Math.sin(dirction) };// 机器人的线速度向量
            this.ipc = ipc;
            this.cipc = cipc;
        }

        public int compare(PlatForm p1, PlatForm p2) {
            int ind1 = p1.getPlatFormType().getIndex();
            int ind2 = p2.getPlatFormType().getIndex();
            double p1w = this.a * Util.getDistance(p1.getPosition(), curRobotposition)
                    + 20.0 * this.b * Math.abs(Util.getVectorAngle(
                            Util.getVectorBetweenPoints(curRobotposition, p1.getPosition()), curVector))
                    + this.c * 60.0 * p1.getScore()
                    + this.d * 1.0 / (0.01 * (ipc[ind1] - cipc[ind1]) + 0.0000001);// 权值函数，越小越好

            double p2w = this.a * Util.getDistance(p2.getPosition(), curRobotposition)
                    + 20.0 * this.b * Math.abs(Util.getVectorAngle(
                            Util.getVectorBetweenPoints(curRobotposition, p2.getPosition()), curVector))
                    + this.c * 60.0 * p2.getScore()
                    + this.d * 1.0 / (0.01 * (ipc[ind2] - cipc[ind2]) + 0.0000001);// 权值函数，越小越好
            if (p1w < p2w)
                return -1;
            return 1;
        }

        private double[] curRobotposition;// 机器人位置
        private double[] curVector;
        private double a, b, c, d;// 四个系数
        private int[] ipc;// 1-6类物品原料格的总数
        private int[] cipc;// 1-6类物品原料格当前数量
    }

    /**
     * 三个系数abc分别控制距离和角度差的权值以场上该类物品比例分数的权值
     */
    private static class CompareForBuy implements Comparator<PlatForm> {

        public CompareForBuy(Robot r, double a, double b, double c, int[] ipc, int[] cipc) {
            this.a = a;
            this.b = b;
            this.c = c;
            curRobotposition = r.getPosition();// 机器人位置
            double dirction = r.getDirction();
            curVector = new double[] { Math.cos(dirction), Math.sin(dirction) };// 机器人的线速度向量
            this.ipc = ipc;
            this.cipc = cipc;
        }

        public int compare(PlatForm p1, PlatForm p2) {
            int ind1 = p1.getPlatFormType().getIndex();
            int ind2 = p2.getPlatFormType().getIndex();
            double p1w = this.a * Util.getDistance(p1.getPosition(), curRobotposition)
                    + 20.0 * this.b * Math.abs(Util.getVectorAngle(
                            Util.getVectorBetweenPoints(curRobotposition, p1.getPosition()), curVector))
                    + this.c * 1.0 / (0.01 * (ipc[ind1] - cipc[ind1]) + 0.0000001);// 权值函数，越小越好

            double p2w = this.a * Util.getDistance(p2.getPosition(), curRobotposition)
                    + 20.0 * this.b * Math.abs(Util.getVectorAngle(
                            Util.getVectorBetweenPoints(curRobotposition, p2.getPosition()), curVector))
                    + this.c * 1.0 / (0.01 * (ipc[ind2] - cipc[ind2]) + 0.0000001);// 权值函数，越小越好
            if (p1w < p2w)
                return -1;
            return 1;
        }

        private double[] curRobotposition;// 机器人位置
        private double[] curVector;
        private double a, b, c;// 三个系数
        private int[] ipc;// 1-6类物品原料格的总数
        private int[] cipc;// 1-6类物品原料格当前数量
    }

}