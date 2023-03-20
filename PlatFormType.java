public enum PlatFormType {
    // 工作台类型

    ONE(1, ItemType.ONE, (int) 0b00000000, 50, -1, -1),
    TWO(2, ItemType.TWO, (int) 0b00000000, 50, -1, -1),
    THREE(3, ItemType.THREE, (int) 0b00000000, 50, -1, -1),
    FOUR(4, ItemType.FOUR, (int) 0b00000110, 500, 13, 4),
    FIVE(5, ItemType.FIVE, (int) 0b00001010, 500, 12, 3),
    SIX(6, ItemType.SIX, (int) 0b00001100, 500, 11, 2),
    SEVEN(7, ItemType.SEVEN, (int) 0b01110000, 1000, 10, 1),
    EIGHT(8, ItemType.ZERO, (int) 0b10000000, 1, -1, -1),
    NINE(9, ItemType.ZERO, (int) 0b11111110, 1, -1, -1);

    private PlatFormType(int ind, ItemType num, int neededMateria, int workFrame, int p1, int p2) {
        this.ind = ind;
        this.num = num;
        this.neededMateria = neededMateria;
        this.workFrame = workFrame;
        this.productTaskPriority = p1; // 生产任务的优先级
        this.fetchTaskPriority = p2; // fetch任务的优先级
    }

    /**
     * 获取生产物品的类型
     *
     * @return 生产的产品类型
     */
    public ItemType getProductItemType() {
        return num;
    }

    /**
     * 获取需要的原料(二进制表示)
     *
     * @return 原料需求的二进制表示
     */
    public int getNeededMateria() {
        return neededMateria;
    }

    /**
     * 获取生产需要的帧数
     *
     * @return 生产周期(帧)
     */
    public int getWorkFrame() {
        return workFrame;
    }

    /**
     * 检查该工作台类型是否收购原料t
     *
     * @param t 原料类型
     * @return true 表示工作台需要该原料
     */
    public boolean checkNeeded(ItemType t) {
        return (neededMateria & (1 << t.getNum())) == 1;
    }

    /**
     * 返回类型编号
     *
     * @return 类型编号[1-9]
     */
    public int getIndex() {
        return ind;
    }

    /**
     * 返回平台对应生产任务的优先级
     */
    public int getProductTaskPriority() {
        return productTaskPriority;
    }

    /**
     * 返回平台对应fetch任务的优先级
     */
    public int getFetchTaskPriority() {
        return fetchTaskPriority;
    }

    private int ind;// 类型编号[1-9]
    private ItemType num;// 生产的物品类型
    private int neededMateria;// 需要的原料，二进制位表示
    private int workFrame;// 生产所需要的帧数
    private int productTaskPriority; // 生产任务的优先级
    private int fetchTaskPriority; // fetch任务的优先级
}
