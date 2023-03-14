public enum ItemType {
    /*
     * ZERO为无携带物品状态，其他状态分别对应1-7号物品类型
     */
    ZERO(0, 0, 0), ONE(1, 3000, 6000), TWO(2, 4400, 7600), THREE(3, 5800, 9200), FOUR(4, 15400, 22500),
    FIVE(5, 17200, 25000), SIX(6, 19200, 27500), SEVEN(7, 76000, 105000);

    private final int num;// 物品编号
    private final int buy;// 买入价
    private final int sell;// 售出价
    private final int earn;// 单位物品获利

    private ItemType(int num, int buy, int sell) {
        this.num = num;
        this.buy = buy;
        this.sell = sell;
        this.earn = sell - buy;
    }

    /**
     * 返回物品编号
     *
     * @return
     */
    public int getNum() {
        //
        return num;
    }

    /**
     * 返回买入价格
     *
     * @return
     */
    public int getBuyPrice() {
        return buy;
    }

    /**
     * 返回卖出价格
     *
     * @return
     */
    public int getSellPrice() {
        return sell;
    }

    /**
     * 返回赚取差价
     *
     * @return
     */
    public int getEarn() {
        return earn;
    }
}
