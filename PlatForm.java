import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

public class PlatForm {

    public PlatForm(int num, int type, double positionX, double positionY) {
        if (type < 1 || type > 9) {
            System.err.println("Unwanted platform type !!!");
        }
        for (PlatFormType p : PlatFormType.values()) {
            if (p.getIndex() == type) {
                this.type = p;
                break;
            }
        }
        this.num = num;
        this.positionX = positionX;
        this.positionY = positionY;
        this.leftFrame = -1;
        this.materiaStatus = 0;
        this.assignStatus = 0;

        // new add
        this.isAssignProductTask = false; // 无发布生产任务
        this.isAssignFetchTask = false;   // 无发布fetch任务
        this.isChoosedForProduct = false; //没有被选择作为父平台
        this.platformsWhichNeedProductionQueue = new ArrayDeque<>();
    }

    /**
     * 返回工作台编号
     *
     * @return 工作台编号
     */
    public int getNum() {
        return num;
    }

    /**
     * 返回工作台类型
     *
     * @return 工作台类型
     */
    public PlatFormType getPlatFormType() {
        return type;
    }

    /**
     * 返回工作台位置坐标
     *
     * @return 位置坐标(x, y)
     */
    public double[] getPosition() {
        return new double[]{positionX, positionY};
    }

    /**
     * 获取剩余生产帧数
     *
     * @return 帧数
     */
    public int getLeftFrame() {
        return leftFrame;
    }

    /**
     * 设置剩余生产帧数
     *
     * @param f 帧数
     */
    public void setLeftFrame(int f) {
        leftFrame = f;
    }

    /**
     * 获取产品状态
     *
     * @return true 表示产品格满
     */
    public boolean HasProduct() {
        return (materiaStatus & 1) == 1;
    }

    /**
     * 获取原料格状态
     *
     * @return 原料格状态的二进制表示
     */
    public int getMateriaStatus() {
        return materiaStatus;
    }

    /**
     * 此函数用于返回原料格状态index位置的状态，调用此函数前请确保index >= 1 && index <= 7(逻辑约束)
     *
     * @param index 位置索引
     * @return true 如果该位为1 false 该位为0
     */
    public boolean getMateriaStatusByIndex(int index) {
        return (materiaStatus & (1 << index)) > 0;
    }

    /**
     * 此函数改变原料格状态index位置的状态，调用此函数前请确保index >= 1 && index <= 7(逻辑约束)
     *
     * @param index
     */
    public void changeMateriaStatusByIndex(int index) {
        materiaStatus ^= (1 << index);
    }

    /**
     * 改变产品格状态,最低位取反
     */
    public void changeProductStatus() {
        materiaStatus ^= 1;
    }

    /**
     * 此函数用于更新产品格状态
     *
     * @param ps 产品格状态 1 表示有 0 表示无
     */
    public void updateProductStatus(int ps) {
        materiaStatus |= ps;
    }

    /**
     * 查询物品类型t是否为工作台需要
     *
     * @param t 物品类型
     * @return true 表示此工作台可接受此物品
     */
    public boolean isNeededMateria(ItemType t) {
        return (type.getNeededMateria() & (1 << t.getNum())) > 0;
    }

    /**
     * 此函数用于更新原料格状态
     *
     * @param status 新的原料格状态
     */
    public void updateMateriaStatus(int status) {
        materiaStatus = status | (materiaStatus & 1);
    }

    /**
     * 返回当前工作台的机器人委派情况
     *
     * @return 委派情况二进制表示
     */
    public int getAssignStatus() {
        return assignStatus;
    }

    /**
     * 此函数用于将委派情况某位状态进行翻转
     *
     * @param index 需要翻转的位的索引（第0位对应产品格委派状态， 1-7表示原料格委派状态），调用此函数前请确保翻转该位为合法操作
     */
    public void setAssignStatus(int index, boolean flag) {
        if (flag) assignStatus |= (1 << index);//置位index位
        else assignStatus &= ((((1 << (8 - index + 1)) - 1) << index) - 1);//复位index位
    }

    /**
     * 设置该平台标记位 若flag=true 则全部派遣位全部设置为1（除了产品格）
     * 若flag=false 则恢复全部派遣位（除了产品格）
     *
     * @param flag
     */
    public void setAllAssignStatus(boolean flag) {
        int need = this.getPlatFormType().getNeededMateria();
        if (flag)
            this.assignStatus = need | (this.assignStatus & 1);
        else
            this.assignStatus = this.assignStatus & 1;
    }

    /**
     * 此函数用于判断给定index位置是否已经派遣机器人
     *
     * @param index 查询位置 0 表示产品格委派情况 1-7 表示原料格委派情况
     * @return true 表示已经委派机器人 false 表示未委派
     */
    public boolean isAssigned(int index) {
        return (assignStatus & (1 << index)) > 0;
    }

    //new add

    /**
     * 判断平台是否发布了生产型任务
     */
    public boolean isAssignProductTask() {
        return isAssignProductTask;
    }

    public void setAssignProductTask(boolean assignProductTask) {
        isAssignProductTask = assignProductTask;
    }

    /**
     * 判断平台是否发布了取任务
     */
    public boolean isAssignFetchTask() {
        return isAssignFetchTask;
    }

    public void setAssignFetchTask(boolean assignFetchTask) {
        isAssignFetchTask = assignFetchTask;
    }

    public Queue<Integer> getPlatformsWhichNeedProductionQueue() {
        return platformsWhichNeedProductionQueue;
    }

    public void setPlatformsWhichNeedProductionQueue(Queue<Integer> platformsWhichNeedProductionQueue) {
        this.platformsWhichNeedProductionQueue = platformsWhichNeedProductionQueue;
    }

    public boolean isChoosedForProduct() {
        return isChoosedForProduct;
    }

    public void setChoosedForProduct(boolean choosedForProduct) {
        isChoosedForProduct = choosedForProduct;
    }

    
   /**
     * 此函数用于初始化工作台后备子节点数组
     * @param pl 工作台分类列表
     * @param distanceBetweenPlatforms 工作台间距离数组
     */
    public void InitBackUpChildren(List<List<PlatForm>> pl, double[][] distanceBetweenPlatforms) {
        int index = type.getIndex();//工作台类型编号
        CompareBetweenPlatform cmp = new CompareBetweenPlatform(this);//比较器
        backUpChildren = new HashMap<>();
        if(index == 7) {
            backUpChildren.put(4, new PriorityQueue<PlatForm>(cmp));
            backUpChildren.put(5, new PriorityQueue<PlatForm>(cmp));
            backUpChildren.put(6, new PriorityQueue<PlatForm>(cmp));
            for(PlatForm curP : pl.get(4)) {
                backUpChildren.get(4).add(curP);
            }
            for(PlatForm curP : pl.get(5)) {
                backUpChildren.get(5).add(curP);
            }
            for(PlatForm curP : pl.get(6)) {
                backUpChildren.get(6).add(curP);
            }
        } else if(index == 4) {
            backUpChildren.put(1, new PriorityQueue<PlatForm>(cmp));
            backUpChildren.put(2, new PriorityQueue<PlatForm>(cmp));
            for(PlatForm curP : pl.get(1)) {
                backUpChildren.get(1).add(curP);
            }
            for(PlatForm curP : pl.get(2)) {
                backUpChildren.get(2).add(curP);
            }
        } else if(index == 5) {
            backUpChildren.put(1, new PriorityQueue<PlatForm>(cmp));
            backUpChildren.put(3, new PriorityQueue<PlatForm>(cmp));
            for(PlatForm curP : pl.get(1)) {
                backUpChildren.get(1).add(curP);
            }
            for(PlatForm curP : pl.get(3)) {
                backUpChildren.get(3).add(curP);
            }
        } else if(index == 6) {
            backUpChildren.put(2, new PriorityQueue<PlatForm>(cmp));
            backUpChildren.put(3, new PriorityQueue<PlatForm>(cmp));
            for(PlatForm curP : pl.get(2)) {
                backUpChildren.get(2).add(curP);
            }
            for(PlatForm curP : pl.get(3)) {
                backUpChildren.get(3).add(curP);
            }
        }
    }

    /**
     * 此函数用于获取可用的特定子节点
     * @param index 子节点工作台类型 7:4/5/6 4:1/2 5:1/3 6:2/3
     * @return 对应的工作台的数组索引 对于4-6号工作台，如果未找到将会进行二次查找， 对于7号工作台，如果没找到返回-1
     */
    public int getChildren(int index) {
        PlatForm res = null;
        PriorityQueue<PlatForm> pq = new PriorityQueue<>(backUpChildren.get(index));
        while(!pq.isEmpty()) {
            PlatForm cur = pq.poll();
            if(!cur.isChoosedForProduct() && (cur.getMateriaStatus() >> 1) == 0) {
                res = cur;
                cur.setAssignStatus(0, true);//产品格委派位置一
                if(index > 3)res.setChoosedForProduct(true);//修改选择位
                break;
            }
        }
        if(res == null && index <= 3) {
            //二次查找
            res = backUpChildren.get(index).peek();//去最近的1-3号台等待
            res.setAssignStatus(0, true);
        }
        if(res != null) {
            res.getPlatformsWhichNeedProductionQueue().add(this.num);//设置好下游的目的结点为本节点
        }
        return res == null ? -1 : res.getNum();
    }

    /**
     * 此函数判断当前工作台是否存在至少一个空闲格（未派遣并且为空）
     * @return true 表示存在 false 表示不存在
     */
    public boolean isAnyPlace() {
        for(ItemType t : ItemType.values()) {
            if(isNeededMateria(t) && !isAssigned(t.getNum()) && !getMateriaStatusByIndex(t.getNum()))return true;
        }
        return false;
    }

    private Map<Integer, PriorityQueue<PlatForm>> backUpChildren;//后备子节点
    private int num;//工作台的编号
    private PlatFormType type;// 工作台类型，如果工作台为九号，则不使用materiaStatus
    private double positionX, positionY;// 工作台的位置坐标
    private int leftFrame;// 剩余生产时间（帧），若为-1则表示当前不在生产状态, 0表示生产格满被阻塞
    private int materiaStatus;// 原材料格状态，最低位二进制位（第0位）为产品产出格（1表示产品格有东西），第1-7位为产品原料格（1表示原料格已经被占用）
    private int assignStatus;// 分配机器人状态（二进制表示，1表示已经分配机器人）

    // new add
    private boolean isAssignProductTask; //是否发布生产任务
    private boolean isAssignFetchTask; //是否发布取的任务
    private boolean isChoosedForProduct; //是否被选择作为某些任务的父平台
    private Queue<Integer> platformsWhichNeedProductionQueue; //需要本平台产品的平台，表现为一个队列，按照请求该产品的顺序排队
}
