import java.util.ArrayList;
import java.util.List;

public class DefaultMotion implements MoveType {

    /**
     * 默认移动模式
     * @param r 当前机器人
     * @param p 机器人对应的目标工作台
     * @return  当前机器人的指令序列
     */
    public List<Order> Move(Robot r, PlatForm p) {
        List<Order> res = new ArrayList<>();
        double[] rp = r.getPosition();//机器人当前位置
        double[] pp = r.getPosition();//目标工作台位置
        double dis = Math.sqrt(Math.pow(rp[0] - pp[0], 2) + Math.pow(rp[1] - pp[1], 2));
        FindNextTarget f = new FindNextTarget();
        if(!r.getStatus() && dis < 0.4 && p.HasProduct()) {
            /*
                机器人为买途，并且当前位置距离目标工作台的距离小于0.4且产品格有产出
            
            */
            res.add(new Order(OrderType.BUY, r.getNum()));//加入买指令
            p.changeProductStatus();//产品格设置为空
            p.changeAssignStatus(0);//把产品格委派状态复位
            r.changeStatus(p.getPlatFormType().getProductItemType());//机器人状态转换为卖途
            r.setTargetPlatFormIndex(f.findTarget(r));
        } else if(r.getStatus() && dis < 0.4 && !p.getMateriaStatusByIndex(r.getItemType().getNum())) {
            /*
                机器人为卖途并且当前位置距离目标工作台的距离小于0.4且原料格未被占用
            */
            res.add(new Order(OrderType.SELL, r.getNum()));//加入卖指令
            int index = r.getItemType().getNum();//机器人携带的产品类型编号
            p.changeAssignStatus(index);//把原料格委派位复位
            p.changeMateriaStatusByIndex(index);//把原料位置位
            r.changeStatus(ItemType.ZERO);//机器人状态转换为买途
            if(p.HasProduct() && !p.isAssigned(0)) {
                //当前工作台有产品可买且未派遣机器人
                res.add(new Order(OrderType.BUY, p.getPlatFormType().getIndex()));
                p.changeProductStatus();//产品格设置为空
                r.changeStatus(p.getPlatFormType().getProductItemType());//机器人状态转换为卖途
            }
            r.setTargetPlatFormIndex(f.findTarget(r));//为机器人寻找下一个目标工作台 
        } 

        //计算速度和角速度

        
        return res;
    }
}
