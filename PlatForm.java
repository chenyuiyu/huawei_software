public class PlatForm {

    public PlatForm(int num, int type, double positionX, double positionY) {
        /*
        if (type < 1 || type > 9) {
            System.err.println("Unwanted platform type !!!");
        }
        */
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
    }

    /**
     * 返回工作台编号
     * @return 工作台编号
     */
    public int getNum() {
        return num;
    }

    /**
     * 返回工作台类型
     * @return 工作台类型
     */
    public PlatFormType getPlatFormType() {
        return type;
    }

    /**
     * 返回工作台位置坐标
     * @return 位置坐标(x, y)
     */
    public double[] getPosition() {
        return new double[] { positionX, positionY };
    }

    /**
     * 获取剩余生产帧数
     * @return 帧数
     */
    public int getLeftFrame() {
        return leftFrame;
    }

    /**
     * 设置剩余生产帧数
     * @param f 帧数
     */
    public void setLeftFrame(int f) {
        leftFrame = f;
    }

    /**
     * 获取产品状态
     * @return true 表示产品格满
     */
    public boolean HasProduct() {
        return (materiaStatus & 1) == 1;
    }

    /**
     * 获取原料格状态
     * @return 原料格状态的二进制表示
     */
    public int getMateriaStatus() {
        return materiaStatus;
    }

    /**
     * 此函数用于返回原料格状态index位置的状态，调用此函数前请确保index >= 1 && index <= 7(逻辑约束) 
     * @param index 位置索引
     * @return true 如果该位为1 false 该位为0
     */
    public boolean getMateriaStatusByIndex(int index) {
        return (materiaStatus & (1 << index)) > 0;
    }

    /**
     * 此函数改变原料格状态index位置的状态，调用此函数前请确保index >= 1 && index <= 7(逻辑约束)
     * @param index 目标位置
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
     * @param ps 产品格状态 1 表示有 0 表示无
     */
    public void updateProductStatus(int ps) {
        materiaStatus |= ps;
    }

    /**
     * 查询物品类型t是否为工作台需要
     * @param t 物品类型
     * @return true 表示此工作台可接受此物品
     */
    public boolean isNeededMateria(ItemType t) {
        return (type.getNeededMateria() & (1 << t.getNum())) > 0;
    }

    /**
     * 此函数用于更新原料格状态
     * @param status 新的原料格状态
     */
    public void updateMateriaStatus(int status) {
        materiaStatus = status | (materiaStatus & 1);
    }

    /**
     * 返回当前工作台的机器人委派情况
     * @return 委派情况二进制表示
     */
    public int getAssignStatus() {
        return assignStatus;
    }

    /**
     * 此函数用于将委派情况某位状态进行翻转
     * @param flag true 表示置位 false 表示复位
     * @param index 需要翻转的位的索引（第0位对应产品格委派状态， 1-7表示原料格委派状态），调用此函数前请确保翻转该位为合法操作
     */
    public void setAssignStatus(int index, boolean flag) {
        if (flag)
            assignStatus |= (1 << index);// 置位index位
        else
            assignStatus &= ((((1 << (8 - index)) - 1) << index) - 1);// 复位index位
    }

    /**
     * 此函数用于判断给定index位置是否已经派遣机器人
     * @param index 查询位置 0 表示产品格委派情况 1-7 表示原料格委派情况
     * @return true 表示已经委派机器人 false 表示未委派
     */
    public boolean isAssigned(int index) {
        return (assignStatus & (1 << index)) > 0;
    }

    /**
     * 返回工作台的材料格计算的分数
     * score = 1 / (原料格占满位置数 + 1)
     * @return 分数
     */
    public double getScore() {
        int cur = 1, count = 0, status = materiaStatus;
        for (int i = 1; i <= 6; i++) {
            cur <<= 1;
            if ((cur & status) > 0)
                count++;
        }
        return 1.0 / (count + 1);
    }

    /**
     * 计算还需要多少个材料才能进行下一次生产
     * @return 次数
     */
    public int getNeededCount() {
        int count = 0, need = type.getNeededMateria();
        for(int i = 1; i <= 6; i++) {
            if(!getMateriaStatusByIndex(i) && (need & (1 << i)) == 1)count++;
        }
        return count;
    }

    private int num;// 工作台的编号
    private PlatFormType type;// 工作台类型，如果工作台为九号，则不使用materiaStatus
    private double positionX, positionY;// 工作台的位置坐标
    private int leftFrame;// 剩余生产时间（帧），若为-1则表示当前不在生产状态, 0表示生产格满被阻塞
    private int materiaStatus;// 原材料格状态，最低位二进制位（第0位）为产品产出格（1表示产品格有东西），第1-7位为产品原料格（1表示原料格已经被占用）
    private int assignStatus;// 分配机器人状态（二进制表示，1表示已经分配机器人）
}