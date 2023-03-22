
public class Robot {
    // 机器人

    public Robot(int num, double positionX, double positionY) {
        this.num = num;
        this.positionX = positionX;
        this.positionY = positionY;
        this.prePositionX = 0;
        this.prePositionY = 0;
        radius = 0.45;
        materia = new Item(ItemType.ZERO);
        status = false;
        targetPlatformIndex = -1;
        lineSpeedX = 0.0;
        lineSpeedY = 0.0;
        dirction = 0.0;
        angleSpeed = 0.0;
        nearByPlatFormId = -1;
        exceptArriveFrame = 0;
        realArriveFrame = 0;
        robotGroup = new Robot[3];
    }

    /**
     * 获取机器人的编号[0,3]
     * 
     * @return 机器人编号
     */
    public int getNum() {
        return num;
    }

    /**
     * 设置机器人位置
     * 
     * @param x 横坐标
     * @param y 纵坐标
     */
    public void setPosition(double x, double y) {
        this.positionX = x;
        this.positionY = y;
    }

    /**
     * 获取机器人位置
     * 
     * @return 机器人位置坐标
     */
    public double[] getPosition() {
        return new double[] { positionX, positionY };
    }

    /**
     * 记录机器人上一帧的位置
     * 
     * @param x
     * @param y
     */
    public void setprePosition(double x, double y) {
        this.prePositionX = x;
        this.prePositionY = y;
    }

    /**
     * 获取机器人上一帧的位置
     * 
     * @return
     */
    public double[] getPrePosition() {
        return new double[] { prePositionX, prePositionY };
    }

    /**
     * 返回机器人半径
     * 
     * @return 半径(m)
     */
    public double getRadius() {
        return radius;
    }

    /**
     * 设置机器人半径
     * 
     * @param r 半径(m)
     */
    public void setRadius(double r) {
        radius = r;
    }

    /**
     * 获取携带物品
     * 
     * @return 物品
     */
    public Item getItem() {
        return materia;
    }

    /**
     * 设置携带物品
     * 
     * @param m 物品
     */
    public void setItem(Item m) {
        materia = m;
    }

    /**
     * 获取机器人的状态
     * 
     * @return 机器人当前状态
     */
    public boolean getStatus() {
        return status;
    }

    /**
     * 返回目标工作台的数组下标
     * 
     * @return 数组下标
     */
    public int getTargetPlatFormIndex() {
        return targetPlatformIndex;
    }

    /**
     * 设置目标工作台的数组下标
     * 
     * @param ind 数组下标
     */
    public void setTargetPlatFormIndex(int ind) {
        targetPlatformIndex = ind;
    }

    /**
     * 获取机器人的线速度
     * 
     * @return 线速度向量
     */
    public double[] getLineSpeed() {
        return new double[] { lineSpeedX, lineSpeedY };
    }

    /**
     * 设置机器人的线速度
     * 
     * @param lpx 线速度向量横坐标
     * @param lpy 线速度向量纵坐标
     */
    public void setLineSpeed(double lpx, double lpy) {
        lineSpeedX = lpx;
        lineSpeedY = lpy;
    }

    /**
     * 获取机器人线速度(标量)
     * 
     * @return
     */
    public double getlineSpeed() {
        return Math.sqrt(Math.pow(lineSpeedX, 2) + Math.pow(lineSpeedY, 2));
    }

    /**
     * 获取机器人朝向
     * 
     * @return 朝向[-pi, pi]
     */
    public double getDirction() {
        return dirction;
    }

    /**
     * 设置机器人朝向
     * 
     * @param d 朝向[-pi, pi]
     */
    public void setDirction(double d) {
        dirction = d;
    }

    /**
     * 获取机器人的角速度，正表示逆时针，负表示顺时针
     * 
     * @return 角速度(rad/s)
     */
    public double getAngleSpeed() {
        return angleSpeed;
    }

    /**
     * 设置机器人角速度，正表示逆时针，负表示顺时针
     * 
     * @param as 角速度(rad/s)
     */
    public void setAngleSpeed(double as) {
        angleSpeed = as;
    }

    /**
     * 此函数用于改变机器人状态
     */
    public void changeStatus() {
        status = !status;
    }

    /**
     * 获取附近工作台ID
     * 
     * @return -1 表示附近无工作台， [0, 工作台数-1]表示工作台编号
     */
    public int getNearByPlatFormId() {
        return nearByPlatFormId;
    }

    /**
     * 设置附近工作台id
     * 
     * @param id 附近工作台的id
     */
    public void setNearByPlatFormId(int id) {
        nearByPlatFormId = id;
    }

    /**
     * 设置预期到达目标的帧数
     * 
     * @param frameid
     */
    public void setExceptArriveFrame(int frameNum) {
        exceptArriveFrame = frameNum;
    }

    /**
     * 返回预期到达的帧数
     * 
     * @return
     */
    public int getExceptArriveFrame() {
        return exceptArriveFrame;
    }

    /**
     * 实际运行帧数增加
     * 
     * @param frameNum
     */
    public void addRealArriveFrame(int frameNum) {
        realArriveFrame += frameNum;
    }

    /**
     * 返回实际运行帧数
     * 
     * @return
     */
    public int getRealArriveFrame() {
        return realArriveFrame;
    }

    /**
     * 增加实际运行帧数
     */
    public void resetRealArriveFrame() {
        realArriveFrame = 0;
    }

    /**
     * 存储其他机器人
     * 
     * @param ind
     * @param cooperateRobot
     */
    public void setrobotGroup(int ind, Robot cooperateRobot) {
        robotGroup[ind] = cooperateRobot;
    }

    /**
     * 碰撞检测
     * 
     * @return
     */
    public int[] collsionDetection() {// 碰撞检测只能调整自身的速度设置
        int[] temp = { 0, 0, 0 };
        for (int i = 0; i < 3; i++) {
            Robot oRobot = robotGroup[i]; // 其他机器人
            double dirction1 = getDirction();// 自身朝向
            double dirction2 = oRobot.getDirction();// 其他机器人朝向
            double diffangel = Math.abs(dirction1 - dirction2);
            double[] vector1 = { Math.cos(dirction1), Math.sin(dirction1) };// 自身朝向向量
            double[] op = oRobot.getPosition();// 其他机器人位置
            double[] vector3 = { positionX - op[0], positionY - op[1] };// 自身相对其他机器人的方向向量
            double diffangel2 = Util.getVectorAngle(vector1, vector3);
            double dis = Util.getDistance(getPrePosition(), oRobot.getPosition());

            if ((Math.PI - diffangel < Math.PI / 40 && Math.PI - diffangel2 < Math.PI / 40
                    && Math.abs(angleSpeed) < Math.PI / 180)
                    || (Math.PI - diffangel < Math.PI / 5 && Math.PI - diffangel2 < Math.PI / 5 && dis < 5)) {// 相向而行
                // 都携带则按照正方向的进行避让，反向保持 不携带则直接进行避让即可
                if (status && oRobot.getStatus()) {// 都携带物品
                    temp[0] += 100;
                } else if (status) {// 自身携带
                    temp[0] += 10;
                } else {// 不携带
                    temp[0]++;
                }
            } else if (diffangel < Math.PI / 5 && dis < 3) {// 非严格同向而行

                if (diffangel2 < Math.PI / 5) {// 前方
                    temp[1] += 10;
                } else if (Math.PI - diffangel2 < Math.PI / 5) {// 后方
                    temp[1]++;
                }
            }

            if (dis < 0.92 + (status ? 0 : 0.08) + (oRobot.getStatus() ? 0 : 0.08))// 机器人互相卡位的情况
                temp[2]++;
        }
        return temp;
    }

    private int num;// 机器人的编号[0,3]
    private double positionX, positionY;// 位置坐标(positionX, positionY)
    private double prePositionX, prePositionY;// 上一帧的坐标位置(prePositionX,prePositionY)
    private double radius;// 机器人半径(m)
    private Item materia;// 携带材料
    private boolean status;// 机器人状态，买途为false，卖途为true
    private int targetPlatformIndex;// 目标工作台所在的数组的下标
    private double lineSpeedX, lineSpeedY;// 线速度二维向量(m/s)
    private double dirction;// 朝向
    private double angleSpeed;// 角速度向量，正表示逆时针，负表示顺时针
    private int nearByPlatFormId;// 所处工作台ID，-1：表示当前没有处于任何工作台附近，[0,工作台总数-1] ：表示某工作台的下标
    private int exceptArriveFrame;// 预估到达目标所需帧数
    private int realArriveFrame;// 实际到达目标所需帧数
    public static int frameID;// 当前帧数
    public static int ENDFRAMEID = 9000;
    private Robot[] robotGroup;

}
