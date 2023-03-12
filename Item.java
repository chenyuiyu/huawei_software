public class Item {
    //物品

    public Item(ItemType t) {
        //此构造函数不考虑时间与碰撞优化
        this.type = t;
        timeValueRatio = 0.0;
        corruptionValueRatio = 0.0;
    }


    public Item(double timeValueRatio, double corruptionValueRatio, ItemType t) {
        this.timeValueRatio = timeValueRatio;
        this.corruptionValueRatio = corruptionValueRatio;
        this.type = t;
    }

    public double getTimeValueRatio() {
        //获取时间价值系数
        return timeValueRatio;
    }

    public void setTimeValueRatio(double tvr) {
        //设置时间价值系数
        timeValueRatio = tvr;
    }

    public double getCorruptionValueRatio() {
        //获取碰撞价值系数
        return corruptionValueRatio;
    }

    public void setCorruptionValueRatio(double cvr) {
        //设置价值系数
        corruptionValueRatio = cvr;
    }

    public ItemType getItemType() {
        //获取该物品类型
        return type;
    }

    private double timeValueRatio;//时间价值系数
    private double corruptionValueRatio;//碰撞价值系数
    private ItemType type;//物品类型
}

