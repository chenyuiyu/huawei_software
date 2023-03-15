import java.util.ArrayList;
import java.util.List;


public class Motion implements MoveType {
    /**
     * 此类用于计算机器人的角速度和线速度
     *
     * @param r 当前机器人
     * @param p 工作台数组
     * @return 运动指令列表（forward and/or rotate）
     */
    public List<Order> Move(Robot r, PlatForm[] p) {
        List<Order> res = new ArrayList<>();
        // 计算速度和角速度
        PlatForm target = p[r.getTargetPlatFormIndex()];// 更新目标工作台，因为可能已经改变
        double[] rp = r.getPosition();// 机器人当前位置
        double[] tp = target.getPosition();// 目标工作台位置
        double dis = Util.getDistance(rp, tp);
        double angleSpeed = r.getAngleSpeed();
        double dirction = r.getDirction();
        double[] vector1 = {tp[0] - rp[0], tp[1] - rp[1]};
        double[] vector2 = {Math.cos(dirction), Math.sin(dirction)};

        double diffangel = Util.getVectorAngle(vector1, vector2);
        // 将两向量同时旋转，至机器人朝向的向量与x轴重合，此时即可判断是旋转方向
        double dirctionP2R;// 机器人相对工作台向量的角度
        if (rp[0] == tp[0])
            dirctionP2R = vector1[1] < 0 ? -Math.PI / 2 : Math.PI / 2;
        else {
            dirctionP2R = Math.atan(vector1[1] / vector1[0]);
            if (vector1[0] < 0) {
                if (vector1[1] > 0)
                    dirctionP2R += Math.PI;
                else
                    dirctionP2R -= Math.PI;
            }
        }
        // 角度为A的向量逆时针的旋转角度B的公式：y = |R|*sinA*cosB + |R|*cosA*sinB (-dirction为逆时针)
        // 旋转后的机器人相对工作台的向量的y值大于0 则顺时针否则逆时针
        int anticlockwise = (Math.sin(dirctionP2R) * Math.cos(-dirction)
                + Math.cos(dirctionP2R) * Math.sin(-dirction)) > 0 ? 1 : -1;
        // 若旋转后工作台相对于机器人在上方 说明需要向上旋转，即逆时针
        // 系统中正数表示逆时针 负数表示顺时针

        double newlineSpeed = 0;
        if (dis < 2 && diffangel > Math.PI / 2)
            newlineSpeed = 0;
        else
            newlineSpeed = 6;
        res.add(new Order(OrderType.FORWARD, r.getNum(), newlineSpeed));// 加入前进指令 默认以最大速度前进
        double ridus = r.getRadius();
        // 加速度计算: 力矩=转动惯量*加速度 转动惯量为△mr^2 ;
        // 转动惯量需要积分求得:积分下为 2*π*r^3 * ρ,积分上限为半径;求得积分为 ρ*π*r^4 /2
        // ρ=20 力矩=50
        // double accelerateAngleSpeed = 50 / (Math.pow(ridus, 4) * 20/2);
        double accelerateAngleSpeed = 5 / (Math.pow(ridus, 4) * Math.PI);// 因为需要弧度制
        double newangleSpeed = 0;
        // 从当前角速度w 匀减速到0 平均速度为w/2 加速度为α 则减速时间为 w/α.所以旋转角度为 (w/2) * (w/α)
        // 所以根据当前角速度w 判断偏差角度接近 w^2/2a 就开始减速即可否则就保持匀加速到π即可
        if (diffangel > Math.pow(angleSpeed, 2) / (2 * accelerateAngleSpeed))
            newangleSpeed = Math.PI * anticlockwise;
        else
            newangleSpeed = 0;
        res.add(new Order(OrderType.ROTATE, r.getNum(), newangleSpeed));// 加入旋转指令

        // diffangel newangleSpeed
        System.err.println("diffangel: " + diffangel + " newangleSpeed" + newangleSpeed);
        return res;
    }


}
