public class Robot {
    // 机器人

    public Robot(double positionX, double positionY) {
        this.positionX = positionX;
        this.positionY = positionY;
        radius = 0.45;
        num = ItemType.ZERO;
        status = false;
        targetPlatformIndex = -1;
        lineSpeedX = 0.0;
        lineSpeedY = 0.0;
        dirction = 0.0;
        angleSpeed = 0.0;
    }

    /*
     * getter/settter methods
     */
    /**
     * 设置机器人位置
     * 
     * @param x
     * @param y
     */
    public void setPosition(double x, double y) {
        this.positionX = x;
        this.positionY = y;
    }

    /**
     * 获取机器人位置
     * 
     * @return
     */
    public double[] getPosition() {
        return new double[] { positionX, positionY };
    }

    /**
     * 返回机器人半径
     * 
     * @return
     */
    public double getRadius() {
        return radius;
    }

    /**
     * 设置机器人半径
     * 
     * @param r
     */
    public void setRadius(double r) {
        radius = r;
    }

    /**
     * 获取携带物品类型
     * 
     * @return
     */
    public ItemType getItemType() {
        return num;
    }

    /**
     * 设置携带物品种类
     * 
     * @param t
     */
    public void setItemType(ItemType t) {
        num = t;
    }

    /**
     * 返回目标工作台的数组下标
     * 
     * @return
     */
    public int getTargetPlatFormIndex() {
        return targetPlatformIndex;
    }

    /**
     * 设置目标工作台的数组下标
     * 
     * @param ind
     */
    public void setTargetPlatFormIndex(int ind) {
        targetPlatformIndex = ind;
    }

    /**
     * 获取机器人的线速度
     * 
     * @return
     */
    public double[] getLineSpeed() {
        return new double[] { lineSpeedX, lineSpeedY };
    }

    /**
     * 设置机器人的线速度
     * 
     * @param lpx
     * @param lpy
     */
    public void setLineSpeed(double lpx, double lpy) {
        lineSpeedX = lpx;
        lineSpeedY = lpy;
    }

    /**
     * 获取机器人朝向
     * 
     * @return
     */
    public double getDirction() {
        return dirction;
    }

    /**
     * 设置机器人朝向
     * 
     * @param d
     */
    public void setDirction(double d) {
        dirction = d;
    }

    /**
     * 获取机器人的角速度
     * 
     * @return
     */
    public double getAngleSpeed() {
        return angleSpeed;
    }

    /**
     * 设置机器人角速度
     * 
     * @param as
     */
    public void setAngleSpeed(double as) {
        angleSpeed = as;
    }

    /**
     * 此函数用于改变机器人状态
     * 
     * @param t 当由买途转为卖途时，买入的物品的类型
     */
    public void changeStatus(ItemType t) {

        if (status) {
            // 机器人卖掉物品，半径减小，携带物品类型变无
            this.setRadius(0.53);
            this.setItemType(ItemType.ZERO);
        } else {
            // 机器人买入物品，半径增大，携带物品类型变为t
            this.setRadius(0.45);
            this.setItemType(t);
        }
        status = !status;
    }

    private double positionX, positionY;// 位置坐标(positionX, positionY)
    private double radius;// 机器人半径(m)
    private ItemType num;// 携带材料编号
    private boolean status;// 机器人状态，买途为false，卖途为true
    private int targetPlatformIndex;// 目标工作台所在的数组的下标
    private double lineSpeedX, lineSpeedY;// 线速度二维向量(m/s)
    private double dirction;// 朝向
    private double angleSpeed;// 角速度向量，正表示逆时针，负表示顺时针

}