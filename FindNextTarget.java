import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * 该类为主算法类
 */
public class FindNextTarget {

    /**
     * 此函数用于计算机器人完成买或卖之后下一个目标工作台的确定
     * weight = value / max(0.2, dis + 20.0 * abs(angle))
     * @param r 当前机器人
     * @return 目标工作台编号
     */
    public int findTarget(Robot r, PlatForm[] p) {
        if(!r.getStatus()) return findTargetForBuy(r, p);//为0、1号机器人寻找下一个目标
        return findTargetForSell(r, p);//为2、3号机器人寻找下一个目标
    }

    /**
     * 函数用于为机器人分配买任务
     * @param r 需要分配任务的机器人
     * @param p 工作台队列
     * @return 下一个目标工作台
     */
    public int findTargetForBuy(Robot r, PlatForm[] p) {
        PlatForm target = null;
        CompareForSell cmp = new CompareForSell(r, 1.0, 1.0);//b = 1.0 c = 1.0
        PriorityQueue<PlatForm> records123 = new PriorityQueue<>(cmp);//1/2/3类工作台存一个优先队列
        PriorityQueue<PlatForm> records4567 = new PriorityQueue<>(cmp);//4/5/6/7类工作台存一个优先队列
        for(PlatForm cur : p) {
            if(cur.HasProduct() && !cur.isAssigned(0)) {
                //该平台有产出并且未派遣机器人
                if(cur.getPlatFormType().getIndex() <= 3)records123.add(cur);
                else records4567.add(cur);//加入到对应类型的优先队列
            }
        }
        if(r.getNum() <= 1) {
            target = records123.peek();
            if(target == null)target = records4567.peek();
        } else {
            target = records4567.peek();
            if(target == null)target = records123.peek();
        }
        //需要处理没有下一个目标的情况
        if(target == null) {
            //以下逻辑为处理第一轮未找到目标工作台,寻找1-3类型的工作台,派机器人到那里等待
            for(PlatForm cur : p) {
                if(cur.getPlatFormType().getIndex() <= 3) {
                    target = cur;
                    break;
                }
            }
        }
        target.setAssignStatus(0, true);//翻转派遣位
        return target.getNum();
    }

    /**
     * 函数用于为机器人分配卖任务
     * @param r 需要分配任务的机器人
     * @param p 工作台队列
     * @return 下一个目标工作台
     */
    public int findTargetForSell(Robot r, PlatForm[] p) {
        //如果机器人为卖途
        PlatForm target = null;
        CompareForSell cmp = new CompareForSell(r, 1.0, 1.0);//b = 1.0 c = 1.0
        PriorityQueue<PlatForm> records456 = new PriorityQueue<>(cmp);//每类工作台存一个优先队列
        PriorityQueue<PlatForm> records789 = new PriorityQueue<>(cmp);
        int id = r.getItem().getItemType().getNum();//机器人携带材料的编号
        int status = 1 << id;
        for(PlatForm cur : p) {
            PlatFormType type = cur.getPlatFormType();
            if(((type.getNeededMateria() & status) > 0) && ((cur.getAssignStatus() & status) == 0) && ((cur.getMateriaStatus() & status) == 0)) {
                //该平台需要该材料并且该材料格空闲并且未派遣机器人则加入备选队列
                if(type.getIndex() <= 6)records456.add(cur);//加入到对应类型的优先队列
                else records789.add(cur);
            }
        }
        //需要处理没有下一个目标的情况
        if(r.getNum() <= 1) {
            target = records456.peek();
            if(target == null)target = records789.peek();
        } else {
            target = records789.peek();
            if(target == null)target = records456.peek();
        }
        if(target == null) {
            //以下逻辑为处理第一轮未找到目标工作台,寻找最少剩余生产时间并且需要该原料的工作台,派机器人到那里等待
            int leftTime = 1000;//剩余生产帧数
            for(PlatForm cur : p) {
                if((cur.getPlatFormType().getNeededMateria() & status) > 0 && cur.getLeftFrame() <= leftTime) {
                    target = cur;
                    leftTime = cur.getLeftFrame();
                }
            }
        } 
        target.setAssignStatus(id, true);//置位派遣位
        return target.getNum();
    }

    /**
     * 两个系数ab分别控制距离和角度差的权值
     */
    private class CompareForSell implements Comparator<PlatForm> {

        public CompareForSell(Robot r, double a, double b) {
            this.a = a;
            this.b = b;
            curRobotposition = r.getPosition();//机器人位置
            double dirction = r.getDirction();
            curVector = new double[]{ Math.cos(dirction), Math.sin(dirction) };//机器人的线速度向量
        }

        public int compare(PlatForm p1, PlatForm p2) {
            double p1w = this.a * Util.getDistance(p1.getPosition(), curRobotposition) 
                + 20.0 * this.b * Math.abs(Util.getVectorAngle(Util.getVectorBetweenPoints(curRobotposition, p1.getPosition()), curVector));//权值函数，越小越好

            double p2w = this.a * Util.getDistance(p2.getPosition(), curRobotposition) 
                + 20.0 * this.b * Math.abs(Util.getVectorAngle(Util.getVectorBetweenPoints(curRobotposition, p2.getPosition()), curVector));//权值函数，越小越好
            if(p1w < p2w)return -1;
            return 1;
        }

        private double[] curRobotposition;//机器人位置
        private double[] curVector;
        private double a, b;//两个系数
    }

}
