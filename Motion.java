
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
        double[] vector1 = { tp[0] - rp[0], tp[1] - rp[1] };
        double[] vector2 = { Math.cos(dirction), Math.sin(dirction) };

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
        if (dis < 2) {
            newlineSpeed = 4;
            if (diffangel > Math.PI / 2)
                newlineSpeed = 0;
        } else
            newlineSpeed = 6;

        double ridus = r.getRadius();
        // 加速度计算: 力矩=转动惯量*加速度 转动惯量为△mr^2 ;
        // 转动惯量需要积分求得:积分下为 2*π*r^3 * ρ,积分上限为半径;求得积分为 ρ*π*r^4 /2
        // ρ=20 力矩=50
        double accelerateAngleSpeed = 5 / (Math.pow(ridus, 4) * Math.PI);// 因为需要弧度制
        accelerateAngleSpeed *= (Math.PI / 180);
        double newangleSpeed = angleSpeed;
        // 从当前角速度w 匀减速到0 平均速度为w/2 加速度为α 则减速时间为 w/α.所以旋转角度为 (w/2) * (w/α)
        // 所以根据当前角速度w 判断偏差角度接近 w^2/2a 就开始减速即可否则就保持匀加速到π即可
        if (diffangel < Math.PI / 10) {
            newangleSpeed = 0;
        } else {
            newangleSpeed += accelerateAngleSpeed * anticlockwise * diffangel / Math.PI;
        }
        // // 特殊情况 超过预期一定帧数还没到达目标 此时给旋转角度加个随机数
        int excepteFrame = r.getExceptArriveFrame();// 根据预期所需要的帧数，帧数越多 更不容易陷入死转状态
        // 因此预期帧数多的时候 预留的调整时间应该减少方便更快的脱离卡机状态 比如超过400帧 只需要超过1.1或1.2倍就随机运动
        int resFrmae = 10 + excepteFrame / 20;
        if (rp[0] < 0.5 || rp[0] > 49.5 || rp[1] < 0.5 || rp[1] > 49.5) {// 靠着墙 且角速度较小时会发生卡死 此时加大加速度
            newangleSpeed += (accelerateAngleSpeed * anticlockwise);
        }
        if (excepteFrame + resFrmae < r.getRealArriveFrame()) {// 机器人在工作台附近徘徊
            newlineSpeed = 0;// 适当减速
            newangleSpeed = 0;
            // 不预设多少帧来脱离圆周运动，直接一直减到0，再保留5帧角速度为0(但这样会出现碰撞后 角速度和线速度都为0，导致卡死)
            if (excepteFrame + resFrmae + 5 < r.getRealArriveFrame()) {// 保证能从当前速度减到0并多保留5帧用来脱离圆周运动
                r.resetRealArriveFrame();
                r.addRealArriveFrame(excepteFrame);
                if (angleSpeed != 0)
                    r.addRealArriveFrame(resFrmae);
            }
        } else if (Util.getDistance(r.getPrePosition(), rp) < newlineSpeed * 3 / 400
                && Math.abs(angleSpeed) < Math.PI / 4) {// 移动的距离小于预期的一半,且角速度较小
            // (正常移动的距离为0.1左右)
            newlineSpeed = 4;
            newangleSpeed = Math.PI * anticlockwise;
            r.addRealArriveFrame(-3);// 因为碰撞了 所以预期时间应增加，这里将实际时间减小
        }
        // 碰撞的单独检测
        if (r.collsionDetection()[0] >= 1) {// 机器人之间相向而行
            newangleSpeed = Math.PI * anticlockwise;
            newlineSpeed = 4;
        }
        if (r.collsionDetection()[1] > 1) {// 多个机器人互相卡位
            newlineSpeed = -2;
        }

        res.add(new Order(OrderType.FORWARD, r.getNum(), newlineSpeed));// 加入前进指令 默认以最大速度前进
        res.add(new Order(OrderType.ROTATE, r.getNum(), newangleSpeed));// 加入旋转指令

        System.out.println(Robot.frameID + "   R:" + r.getNum() +
                "   diffangel: " + diffangel + "   angleSpeed:" + angleSpeed + "   except:"
                + excepteFrame + "   real:" + r.getRealArriveFrame() + "   dis:" + dis +
                "  linespeed:"
                + r.getlineSpeed() + "   collison:" + r.collsionDetection()[0] + "  dentent:"
                + r.collsionDetection()[1]);
        return res;
    }

}