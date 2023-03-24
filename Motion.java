
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class Motion implements MoveType {
    /**
     * 此类用于计算机器人的角速度和线速度
     *
     * @param r 当前机器人
     * @param p 工作台数组
     * @return 运动指令列表（forward and/or rotate）
     */
    public List<Order> Move(Robot r, List<PlatForm> p, List<List<PlatForm>> labelPlatforms,
            PriorityQueue<Task> taskQueue) {
        if (r.getTargetPlatFormIndex() == -1)
            return new ArrayList<>();
        List<Order> res = new ArrayList<>();
        // 计算速度和角速度
        PlatForm target = p.get(r.getTargetPlatFormIndex());// 更新目标工作台，因为可能已经改变
        double[] rp = r.getPosition();// 机器人当前位置
        double[] tp = target.getPosition();// 目标工作台位置
        double[] ep = r.getExceptPosition(2);// 按照当前方向可能到达的位置
        double dis = Utils.getDistance(rp, tp);
        double angleSpeed = r.getAngleSpeed();
        double dirction = r.getDirction();
        double[] vector1 = { tp[0] - rp[0], tp[1] - rp[1] };
        double[] vector2 = { Math.cos(dirction), Math.sin(dirction) };
        int excepteFrame = r.getExceptArriveFrame();// 预期所需要的帧数
        double diffangel = Utils.getVectorAngle(vector1, vector2);// 偏差角度
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

        // double ridus = r.getRadius();
        // 加速度计算: 力矩=转动惯量*加速度 转动惯量为△mr^2 ;
        // 转动惯量需要积分求得:积分下为 2*π*r^3 * ρ,积分上限为半径;求得积分为 ρ*π*r^4 /2
        // ρ=20 力矩=50
        // double accelerateAngleSpeed = 5 / (Math.pow(ridus, 4) * 180);// 因为需要弧度制
        double accelerateAngleSpeed = 0.4;// 角加速一帧最多增加0.39
        // 计算角速度减速到0的时间
        int temp = 0;
        double newangleSpeed = angleSpeed;
        if (diffangel < Math.PI / 40) {
            newangleSpeed = 0;
        } else if (diffangel < Math.PI / 10) {
            newangleSpeed = 0.5 * anticlockwise * diffangel;
            temp = 1;
        } else if (diffangel < Math.PI / 2) {
            newangleSpeed += anticlockwise * diffangel / Math.PI;
            if (Math.abs(angleSpeed) > Math.PI) {// 偏差角较小但是角速度太大
                newangleSpeed = 0;
            }
            temp = 2;
        } else {
            newangleSpeed += anticlockwise * diffangel / Math.PI;
            temp = 3;
        }
        double newlineSpeed = 0;
        if (dis < 2) {
            newlineSpeed = 4;
            if (diffangel <= Math.PI / 20)// 无偏差则不用减速
                newlineSpeed = 6;
            if ((tp[0] < 1 || tp[0] > 49 || tp[1] < 1 || tp[1] > 49)) {// 如果工作台在墙边, 则减速防止碰撞
                newlineSpeed = 3;
            }
            if (diffangel > Math.PI / 3) {// 如果在目标附近但是偏差角度较大 则减速使其对准方向
                newlineSpeed = 1;
            } else if (diffangel > Math.PI / 2) {
                newlineSpeed = 0;
            }
        } else {// 离目标较远
            newlineSpeed = 6;
            if (ep[0] < 0 || ep[0] > 50 || ep[1] < 0 || ep[1] > 50) {// 预判位置在墙外
                if (r.getStatus() && (r.getRealArriveFrame() < 200 || excepteFrame < 200)) {// 且携带物品 则减速
                                                                                            // 但如果运行帧数超过200帧则没必要减速
                    newlineSpeed = 2;
                }
                double[] vectortemp = { 25 - rp[0], 25 - rp[1] };// 在墙边 尽可能让其对着中心位置
                double diffangel2 = Utils.getVectorAngle(vectortemp, vector2);
                temp = 2;
                if (diffangel2 < Math.PI / 10) { // 同样的方法，暂时让机器人对准(25,25)
                    newangleSpeed = 0;
                } else {
                    newangleSpeed += 2 * anticlockwise * diffangel2 / Math.PI;
                }
            }
        }

        // 因此预期帧数多的时候 预留的调整时间应该减少方便更快的脱离卡机状态 比如超过400帧 只需要超过1.1或1.2倍就随机运动
        int resFrmae = 10 + excepteFrame / 20;
        int[] collsionDet = r.collsionDetection();// 碰撞相关变量
        if (dis < 3) {// 机器人在工作台附近徘徊
            if (excepteFrame + resFrmae < r.getRealArriveFrame()) {
                temp = 4;
                newlineSpeed = 0;// 线速度减到0 然后角速度调整直到对准角度
                newangleSpeed = 0;
                // 不预设多少帧来脱离圆周运动，直接减到0，再保留5帧角速度为0(但这样会出现碰撞后 角速度和线速度都为0，导致卡死)
                if (excepteFrame + resFrmae + 5 < r.getRealArriveFrame()) {// 保证能从当前速度减到0并多保留5帧用来脱离圆周运动
                    r.resetRealArriveFrame();
                    r.addRealArriveFrame(excepteFrame);
                    if (angleSpeed != 0)
                        r.addRealArriveFrame(resFrmae);
                }
            }
            if (collsionDet[3] > 0 && collsionDet[0] > 0) { // 在离目标工作台很近范围内(此时大概率是同一个平台会出现对撞问题)，此时谁离得远谁减速避让
                newlineSpeed -= 2;
                temp = 5;
            }
        } // 如果在目标工作台附近则不进行碰撞检测
        else if (collsionDet[0] >= 100) {// 机器人之间相向而行 两者都携带物品都减速
            temp = 6;
            newlineSpeed = 3;
            if (collsionDet[0] >= 150) // 坐标大的进行避让
                newangleSpeed = Math.PI;
        } else if (collsionDet[0] >= 10) {// 自身携带物品 不改变方向但减速
            newlineSpeed = 4;
            temp = 7;
        } else if (collsionDet[0] >= 1) {// 不携带物品则不需要减速直接扭开即可 撞到也无妨
            newangleSpeed = Math.PI * collsionDet[4];
            temp = 8;
        } else if (collsionDet[1] >= 100) {// 同向而行且前方就加速 前如果已经被推离出工作台则重新寻找而不是回头
            newlineSpeed = 6;
            temp = 9;
        } else if (collsionDet[1] >= 10) {// 在后方稍微减速
            newlineSpeed = 4;
            temp = 10;
        } else if (collsionDet[1] >= 1) {
            temp = 11;
            newangleSpeed = Math.PI * collsionDet[4];
        }
        // 已经碰撞后的处理
        if (Utils.curFrameID > 50 && collsionDet[2] > 100) {// 多个机器人互相卡位(可能刚出来的时候会在附近，因此先运行几帧)只对其中一个机器人进行退让
            temp = 12;
            newangleSpeed = Math.PI * collsionDet[4];
            newlineSpeed = 0;
            if (collsionDet[2] >= 110)
                newlineSpeed = -2;
            temp = 12;
        }

        res.add(new Order(OrderType.FORWARD, r.getNum(), newlineSpeed));// 加入前进指令 默认以最大速度前进
        res.add(new Order(OrderType.ROTATE, r.getNum(), newangleSpeed));// 加入旋转指令

        System.err.println("frameID:" + Utils.curFrameID + " R:" + r.getNum() +
                " diffangel: " + diffangel + " angle:" + angleSpeed + " dis:" + dis +
                " line:" + r.getlineSpeed() + "   dir:" + dirction + "   temp:" + temp + "  except:" + excepteFrame
                + "  real:" + r.getRealArriveFrame());
        return res;
    }

}