import java.util.ArrayList;
import java.util.List;

public class DefaultMotion implements MoveType {

    /**
     * 默认移动模式
     * 
     * @param r 当前机器人
     * @param p 机器人对应的目标工作台
     * @return 当前机器人的指令序列
     */
    public List<Order> Move(Robot r, PlatForm p) {
        List<Order> res = new ArrayList<>();
        double[] rp = r.getPosition();// 机器人当前位置
        double[] pp = r.getPosition();// 目标工作台位置
        double dis = Math.sqrt(Math.pow(rp[0] - pp[0], 2) + Math.pow(rp[1] - pp[1], 2));
        FindNextTarget f = new FindNextTarget();
        if (!r.getStatus() && dis < 0.4 && p.HasProduct()) {
            /*
             * 机器人为买途，并且当前位置距离目标工作台的距离小于0.4且产品格有产出
             * 
             */
            res.add(new Order(OrderType.BUY, r.getNum()));// 加入买指令
            p.changeProductStatus();// 产品格设置为空
            p.changeAssignStatus(0);// 把产品格委派状态复位
            r.changeStatus(p.getPlatFormType().getProductItemType());// 机器人状态转换为卖途
            r.setTargetPlatFormIndex(f.findTarget(r));
        } else if (r.getStatus() && dis < 0.4 && !p.getMateriaStatusByIndex(r.getItemType().getNum())) {
            /*
             * 机器人为卖途并且当前位置距离目标工作台的距离小于0.4且原料格未被占用
             */
            res.add(new Order(OrderType.SELL, r.getNum()));// 加入卖指令
            int index = r.getItemType().getNum();// 机器人携带的产品类型编号
            p.changeAssignStatus(index);// 把原料格委派位复位
            p.changeMateriaStatusByIndex(index);// 把原料位置位
            r.changeStatus(ItemType.ZERO);// 机器人状态转换为买途
            if (p.HasProduct() && !p.isAssigned(0)) {
                // 当前工作台有产品可买且未派遣机器人
                res.add(new Order(OrderType.BUY, p.getPlatFormType().getIndex()));
                p.changeProductStatus();// 产品格设置为空
                r.changeStatus(p.getPlatFormType().getProductItemType());// 机器人状态转换为卖途
            }
            r.setTargetPlatFormIndex(f.findTarget(r));// 为机器人寻找下一个目标工作台
        }

        // 计算速度和角速度
        double[] lineSpeed = r.getLineSpeed();
        double angleSpeed = r.getAngleSpeed();
        double dirction = r.getDirction();
        double[] vector1 = { pp[0] - rp[0], pp[1] - rp[1] };
        double[] vector2 = { Math.sin(dirction), Math.cos(dirction) };
        double vectorProduct = vector1[0] * vector2[0] + vector1[1] * vector2[1];
        double vectorNorm = Math.sqrt(Math.pow(vector1[0], 2) + Math.pow(vector1[1], 2))
                * Math.sqrt(Math.pow(vector2[0], 2) + Math.pow(vector2[1], 2));

        double diffangel = Math.acos(vectorProduct / vectorNorm);
        // 将两向量同时旋转，至机器人朝向的向量与x轴重合，此时即可判断是旋转方向
        double dirctionP2R;// 机器人相对工作台向量的角度
        if (rp[0] == pp[0])
            dirctionP2R = rp[1] > pp[1] ? -Math.PI : Math.PI;
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

        double newangleSpeed = 0;
        // 从当前角速度w 匀减速到0 至少要选择w/2
        // 所以根据当前角速度w 判断偏差角度接近w/2 就开始减速即可否则就保持匀加速到π即可
        if (diffangel > angleSpeed / 2)
            newangleSpeed = Math.PI;
        else
            newangleSpeed = 0;
        res.add(new Order(OrderType.ROTATE, r.getNum(), anticlockwise * newangleSpeed));// 加入旋转指令
        return res;
    }
}
