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
    public List<Order> Move(Robot r, List<PlatForm> p) {
        List<Order> res = new ArrayList<>();
        // 计算速度和角速度
        PlatForm target = p.get(r.getTargetPlatFormIndex());// 更新目标工作台，因为可能已经改变
        double[] rp = r.getPosition();// 机器人当前位置
        double[] tp = target.getPosition();// 目标工作台位置
        double dis = Math.sqrt(Math.pow(rp[0] - tp[0], 2) + Math.pow(rp[1] - tp[1], 2));
        double angleSpeed = r.getAngleSpeed();
        double dirction = r.getDirction();
        double[] vector1 = { tp[0] - rp[0], tp[1] - rp[1] };
        double[] vector2 = { Math.sin(dirction), Math.cos(dirction) };
        double vectorProduct = vector1[0] * vector2[0] + vector1[1] * vector2[1];
        double vectorNorm = Math.sqrt(Math.pow(vector1[0], 2) + Math.pow(vector1[1], 2))
                * Math.sqrt(Math.pow(vector2[0], 2) + Math.pow(vector2[1], 2));

        double diffangel = Math.acos(vectorProduct / vectorNorm);
        // 将两向量同时旋转，至机器人朝向的向量与x轴重合，此时即可判断是旋转方向
        double dirctionP2R;// 机器人相对工作台向量的角度
        if (rp[0] == tp[0])
            dirctionP2R = rp[1] > tp[1] ? -Math.PI : Math.PI;
        else
            dirctionP2R = Math.atan((vector2[1] - vector1[1]) / (vector2[0] - vector1[0]));
        // 角度为A的向量逆时针的旋转角度B的公式：y = |R|*sinA*cosB + |R|*cosA*sinB (-dirction为逆时针)
        // 旋转后的机器人相对工作台的向量的y值大于0 则逆时针否则顺时针
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
        double accelerateAngleSpeed = 50 / (Math.pow(ridus, 4) * 20);
        double newangleSpeed = 0;
        // 从当前角速度w 匀减速到0 平均速度为w/2 加速度为α 则减速时间为 w/α.所以旋转角度为 (w/2) * (w/α)
        // 所以根据当前角速度w 判断偏差角度接近 w^2/2a 就开始减速即可否则就保持匀加速到π即可
        if (diffangel > Math.pow(angleSpeed, 2) / (2 * accelerateAngleSpeed))
            newangleSpeed = Math.PI;
        else
            newangleSpeed = 0;
        res.add(new Order(OrderType.ROTATE, r.getNum(), anticlockwise * newangleSpeed));// 加入旋转指令
        return res;
    }
}
