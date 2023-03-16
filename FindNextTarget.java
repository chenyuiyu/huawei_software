import java.util.PriorityQueue;

/**
 * 该类为主算法类
 */
public class FindNextTarget {

    public FindNextTarget(double a, double b, double c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }

    /**
     * 此函数用于计算机器人完成买或卖之后下一个目标工作台的确定
     * weight = value / max(0.2, dis + 20.0 * abs(angle))
     * @param r 当前机器人
     * @return 目标工作台编号
     */
    public int findTarget(Robot r, PlatForm[] p) {
        double[] curRobotposition = r.getPosition();//j机器人位置
        double dirction = r.getDirction();
        double[] curVector = { Math.cos(dirction), Math.sin(dirction) };;//机器人的线速度向量
        PlatForm target;
        if(r.getStatus()) {
            //如果机器人为卖途
            PriorityQueue<PlatForm> q = new PriorityQueue<>((PlatForm p1, PlatForm p2) -> {
                double p1w = this.b * Util.getDistance(p1.getPosition(), curRobotposition) 
                + 20.0 * this.c * Math.abs(Util.getVectorAngle(Util.getVectorBetweenPoints(curRobotposition, p1.getPosition()), curVector));//权值函数，越小越好

                double p2w = this.b * Util.getDistance(p2.getPosition(), curRobotposition) 
                + 20.0 * this.c * Math.abs(Util.getVectorAngle(Util.getVectorBetweenPoints(curRobotposition, p2.getPosition()), curVector));//权值函数，越小越好
                if(p1w < p2w)return -1;
                return 1;
            });//工作台优先队列
            int id = r.getItem().getItemType().getNum();//机器人携带材料的编号
            int status = 1 << id;
            for(PlatForm cur : p) {
                if(((cur.getPlatFormType().getNeededMateria() & status) > 0) && ((cur.getAssignStatus() & status) == 0) && ((cur.getMateriaStatus() & status) == 0)) {
                    //该平台需要该材料并且该材料格空闲并且未派遣机器人则加入备选队列
                    q.offer(cur);
                }
            }
            //需要处理没有下一个目标的情况
            target = q.peek();//目标工作台
            if(target == null) {
                //以下逻辑为处理第一轮未找到目标工作台,寻找最少剩余生产时间并且需要该原料的工作台
                int leftTime = 1000;//剩余生产帧数
                for(PlatForm cur : p) {
                    if((cur.getPlatFormType().getNeededMateria() & status) > 0 && cur.getLeftFrame() <= leftTime) {
                        target = cur;
                        leftTime = cur.getLeftFrame();
                    }
                }
            }
            target.setAssignStatus(id, true);//翻转派遣位
            return target.getNum();
        } else {
            //机器人为买途
            PriorityQueue<PlatForm> q = new PriorityQueue<>((PlatForm p1, PlatForm p2) -> {
                double p1w = this.a * p1.getPlatFormType().getProductItemType().getEarn()
                / Math.max(0.2, this.b * Util.getDistance(p1.getPosition(), curRobotposition) 
                + 20.0 * this.c * Math.abs(Util.getVectorAngle(Util.getVectorBetweenPoints(curRobotposition, p1.getPosition()), curVector)));//权值函数，越大越好
                double p2w = this.a * p2.getPlatFormType().getProductItemType().getEarn()
                / Math.max(0.2, this.b * Util.getDistance(p2.getPosition(), curRobotposition) 
                + 20.0 * this.c * Math.abs(Util.getVectorAngle(Util.getVectorBetweenPoints(curRobotposition, p2.getPosition()), curVector)));//权值函数，越大越好
                if(p1w > p2w)return -1;
                return 1;
            });
            for(PlatForm cur : p) {
                int curid = cur.getPlatFormType().getIndex();
                if((curid <= 3 || cur.HasProduct()) && !cur.isAssigned(0))q.offer(cur);
            }
            target = q.peek();//目标工作台
            if(target == null) {
                //以下逻辑为处理第一轮未找到目标工作台
                int leftTime = 1000;//剩余生产帧数
                for(PlatForm cur : p) {
                    if(cur.getLeftFrame() <= leftTime) {
                        target = cur;
                        leftTime = cur.getLeftFrame();
                    }
                }
            }
            target.setAssignStatus(0, true);//翻转派遣位
            return target.getNum();
        }
    }

    private double a, b, c;//三个系数
}
