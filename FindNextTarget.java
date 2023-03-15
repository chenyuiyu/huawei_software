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
        double[] curVector = r.getLineSpeed();//机器人的线速度向量
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
            for(PlatForm cur : p) {
                int origin = cur.getPlatFormType().getNeededMateria();//平台类型需要的原材料状态
                if(((origin ^ cur.getAssignStatus() ^ cur.getMateriaStatus()) & (1 << id)) > 0) {
                    //该平台需要改材料并且该材料格空闲并且未派遣机器人则加入备选队列
                    q.offer(cur);
                }
            }
            return q.peek().getNum();
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
                if(cur.HasProduct())q.offer(cur);
            }
            return q.peek().getNum();
        }
    }

    private double a, b, c;//三个系数
}
