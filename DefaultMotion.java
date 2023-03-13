import java.util.ArrayList;
import java.util.List;

public class DefaultMotion implements MoveType {

    /**
     * 默认移动模式
     */
    public List<Order> Move(Robot r, PlatForm p) {
        List<Order> res = new ArrayList<>();
        // 判断机器人状态{0,1}
        // 0:去购买 与工作台距离<=0.4 buy
        // >=0.4 foward+rotate
        // 1:售卖
        // 约束: 与目标距离<=2 且朝向>=90 减速
        double[] rpos = r.getPosition();
        double[] ppos = p.getPosition();
        double dis = Math.sqrt(Math.pow(rpos[0] - ppos[0], 2) + Math.pow(rpos[1] - ppos[1], 2));
        if (!r.getStatus() && dis < 0.4) {
            // buy
            // res.add(new Order(OrderType.BUY, r.get))
            r.changeStatus(null);
        }
        return res;
    }
}
