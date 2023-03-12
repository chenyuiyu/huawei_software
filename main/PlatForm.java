public class PlatForm {
    
    public PlatForm(int type, double positionX, double positionY) {
        if(type < 1 || type > 9) {
            System.err.println("Unwanted platform type !!!");
        }
        for(PlatFormType p : PlatFormType.values()) {
            if(p.getIndex() == type) {
                this.type = p;
                break;
            }
        }
        this.positionX = positionX;
        this.positionY = positionY;
        this.leftFrame = 0;
        this.resetMateriaStatus();//重置原料格状态
        this.hasProduct = false;
    }

    public PlatFormType getPlatFormType() {
        //返回工作台类型
        return type;
    }

    public double[] getPosition() {
        //返回工作台的位置坐标
        return new double[]{positionX, positionY};
    }

    public int getLeftFrame() {
        //获取剩余生产帧数
        return leftFrame;
    }

    public void setLeftFrame(int f) {
        //设置剩余生产帧数
        leftFrame = f;
    }

    public boolean getHasProduct() {
        //获取产品格状态
        return hasProduct;
    }

    public void setHasProduct(boolean p) {
        //设置产品格状态
        hasProduct = p;
    }

    /**
     * 查询物品类型t是否为工作台需要
     * @param t 查询的原料类型
     * @return true表示需要false表示不需要
     */
    public boolean isNeededMateria(ItemType t) {
        if(type == PlatFormType.NINE)return true;
        return !materiaStatus[t.getNum()];
    }

    /**
     * 添加原料
     * @param t 查询的原料类型
     */
    public void addMateria(ItemType t) {
        if(type == PlatFormType.NINE)return;
        materiaStatus[t.getNum()] = true;
    }

    /**
     * 此函数用于重置原料格状态
     */
    public void resetMateriaStatus() {
        if(type == PlatFormType.NINE)return;
        materiaStatus = type.getNeededMateria();
        for(int i = 0; i < 8; i++)materiaStatus[i] = !materiaStatus[i];
    }

    private PlatFormType type;//工作台类型，如果工作台为九号，则不使用materiaStatus
    private double positionX, positionY;//工作台的位置坐标
    private int leftFrame;//剩余生产时间（帧），若为0则表示当前不在生产状态
    private boolean[] materiaStatus;//原材料格状态，当数组全为true时满足生产条件
    private boolean hasProduct;//产品格状态，true为产品格满
}
