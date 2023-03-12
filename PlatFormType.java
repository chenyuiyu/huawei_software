import java.util.Arrays;

public enum PlatFormType {
    // 工作台类型

    ONE(1, ItemType.ONE, new boolean[] { false, false, false, false, false, false, false, false }, 50),
    TWO(2, ItemType.TWO, new boolean[] { false, false, false, false, false, false, false, false }, 50),
    THREE(3, ItemType.THREE, new boolean[] { false, false, false, false, false, false, false, false }, 50),
    FOUR(4, ItemType.FOUR, new boolean[] { false, true, true, false, false, false, false, false }, 500),
    FIVE(5, ItemType.FIVE, new boolean[] { false, true, false, true, false, false, false, false }, 500),
    SIX(6, ItemType.SIX, new boolean[] { false, false, true, true, false, false, false, false }, 500),
    SEVEN(7, ItemType.SEVEN, new boolean[] { false, false, false, false, true, true, true, false }, 1000),
    EIGHT(8, ItemType.ZERO, new boolean[] { false, false, false, false, false, false, false, true }, 1),
    NINE(9, ItemType.ZERO, new boolean[] { false, true, true, true, true, true, true, true }, 1);

    private PlatFormType(int ind, ItemType num, boolean[] needed, int workFrame) {
        this.ind = ind;
        this.num = num;
        this.neededMateria = needed;
        this.workFrame = workFrame;
    }

    /**
     * 获取生产物品的类型
     * 
     * @return
     */
    public ItemType getProductItemType() {
        return num;
    }

    /**
     * 获取需要的原料数组
     * 
     * @return
     */
    public boolean[] getNeededMateria() {
        boolean[] res = (boolean[]) Arrays.copyOf(neededMateria, 8);
        return res;
    }

    /**
     * 获取生产需要的帧数
     * 
     * @return
     */
    public int getWorkFrame() {
        return workFrame;
    }

    /**
     * 检查该工作台类型是否收购原料t
     * 
     * @param t
     * @return
     */
    public boolean checkNeeded(ItemType t) {
        return neededMateria[t.getNum()];
    }

    /**
     * 返回类型编号
     * 
     * @return
     */
    public int getIndex() {
        return ind;
    }

    private int ind;// 类型编号[1-9]
    private ItemType num;// 生产的物品类型
    private boolean[] neededMateria;// 需要的原料
    private int workFrame;// 生产所需要的帧数
}
