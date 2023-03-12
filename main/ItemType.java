public enum ItemType {
    /*
        ZERO为无携带物品状态，其他状态分别对应1-7号物品类型
    */
    ZERO(0, 0, 0), ONE(1, 3000, 6000), TWO(2, 4400, 7600), THREE(3, 5800, 9200), FOUR(4, 15400, 22500), FIVE(5, 17200, 25000), SIX(6, 19200, 27500), SEVEN(7, 76000, 105000);
    
    private int num;//物品编号
    private int buy;//买入价
    private int sell;//售出价
    private int earn;//单位物品获利

    private ItemType(int num, int buy, int sell) {
        this.num = num;
        this.buy = buy;
        this.sell = sell;
        this.earn = sell - buy;
    }

    public int getNum() {
        //返回物品编号
        return num;
    }

    public int getBuyPrice() {
        //返回买入价格
        return buy;
    }

    public int getSellPrice() {
        //返回卖出价格
        return sell;
    }

    public int getEarn() {
        //返回赚取差价
        return earn;
    }
}
