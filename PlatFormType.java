public enum PlatFormType {
    // 工作台类型

    ONE(1, ItemType.ONE, (int)0b00000000, 50),
    TWO(2, ItemType.TWO, (int)0b00000000, 50),
    THREE(3, ItemType.THREE, (int)0b00000000, 50),
    FOUR(4, ItemType.FOUR, (int)0b00000110, 500),
    FIVE(5, ItemType.FIVE, (int)0b00001010, 500),
    SIX(6, ItemType.SIX, (int)0b00001100, 500),
    SEVEN(7, ItemType.SEVEN, (int)0b01110000, 1000),
    EIGHT(8, ItemType.ZERO, (int)0b10000000, 1),
    NINE(9, ItemType.ZERO, (int)0b11111110, 1);

    private PlatFormType(int ind, ItemType num, int neededMateria, int workFrame) {
        this.ind = ind;
        this.num = num;
        this.neededMateria = neededMateria;
        this.workFrame = workFrame;
    }

    /**
     * 获取生产物品的类型
     * @return 生产的产品类型
     */
    public ItemType getProductItemType() {
        return num;
    }

    /**
     * 获取需要的原料(二进制表示)
     * @return 原料需求的二进制表示
     */
    public int getNeededMateria() {
        return neededMateria;
    }

    /**
     * 获取生产需要的帧数
     * @return 生产周期(帧)
     */
    public int getWorkFrame() {
        return workFrame;
    }

    /**
     * 检查该工作台类型是否收购原料t
     * @param t 原料类型
     * @return true 表示工作台需要该原料
     */
    public boolean checkNeeded(ItemType t) {
        return (neededMateria & (1 << t.getNum())) == 1;
    }

    /**
     * 返回类型编号 
     * @return 类型编号[1-9]
     */
    public int getIndex() {
        return ind;
    }

    private int ind;// 类型编号[1-9]
    private ItemType num;// 生产的物品类型
    private int neededMateria;// 需要的原料，二进制位表示
    private int workFrame;// 生产所需要的帧数
}
