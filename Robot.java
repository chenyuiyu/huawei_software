import javax.swing.text.Position;

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
        exceptPosition = new double[2];
        this.nextTargetPlatformIndex = -1; // 下一个目的地
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
    public void setprePosition(double[] pos) {
        this.prePositionX = pos[0];
        this.prePositionY = pos[1];
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
     * @return 角速度(rad / s)
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
     * @param frameNum
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
        int[] temp = { 0, 0, 0, 0, 1 };// 分别对应：相向 同向 碰撞 近目标时标志谁离得远谁避让 避让时旋转方向
        for (int i = 0; i < 3; i++) {
            Robot oRobot = robotGroup[i]; // 其他机器人
            double dirction1 = getDirction();// 自身朝向
            double dirction2 = oRobot.getDirction();// 其他机器人朝向
            double diffangel = Math.abs(dirction1 - dirction2);
            double[] vector1 = { Math.cos(dirction1), Math.sin(dirction1) };// 自身朝向向量
            double[] op = oRobot.getPosition();// 其他机器人位置
            double[] rp = getPosition();
            double[] vector3 = { positionX - op[0], positionY - op[1] };// 自身相对其他机器人的方向向量
            double diffangel2 = Utils.getVectorAngle(vector1, vector3);
            double dis = Utils.getDistance(rp, op);// 机器人之间距离

            // 根据机器人位置和期望位置 以及其他机器人位置和期望位置的两个线段 线段相交则可能碰撞
            double[] erpos = getExceptPosition(3);// 获取自身预期到达位置
            double[] eopos = oRobot.getExceptPosition(3);// 获取其他机器人预期到达位置

            if (targetPlatformIndex != -1) {
                double[] r2ppos = Main.platformsList.get(targetPlatformIndex).getPosition();// 平台位置
                double disr2p = Utils.getDistance(rp, r2ppos);// 离目标距离
                double diso2p = Utils.getDistance(op, r2ppos);// 其他机器人离目标距离
                if (disr2p > diso2p) { // 在离目标工作台很近范围内(此时大概率是同一个平台会出现对撞问题)，此时谁离得远谁避让
                    temp[3]++;
                }
            }
            if ((Math.abs(Math.PI - diffangel) < Math.PI / 40 && Math.abs(Math.PI - diffangel2) < Math.PI / 40
                    && Math.abs(angleSpeed) < Math.PI / 180)
                    || ((Math.abs(Math.PI - diffangel) < Math.PI / 5) && (Math.abs(Math.PI - diffangel2) < Math.PI / 5)
                            && (dis < 5))
                    || Utils.intersectCheck(rp, erpos, op, eopos)) {// 相向而行
                // 都携带则按照正方向的进行避让，反向保持 不携带则直接进行避让即可
                if (status && oRobot.getStatus()) {// 都携带物品
                    temp[0] += 100;
                    if (op[0] + op[1] < rp[0] + rp[1]) {// 坐标大的进行避让
                        temp[0] += 50;
                    }
                } else if (status) {// 自身携带
                    temp[0] += 10;
                } else {// 不携带
                    if ((op[0] + op[1] < rp[0] + rp[1]) || oRobot.getStatus()) {// 不携带者进行避让 都不携带则判断机器人在右上方的进行避让(因为同向碰撞的话
                                                                                // 都避让会一直绕圈),
                        temp[0]++;
                    }
                }

            } else if ((diffangel < Math.PI / 5 && dis < 3)) {// 非严格同向而行
                if (Math.abs(Math.PI - diffangel2) < Math.PI / 5) {// 后方
                    temp[1] += 10;
                } else if (diffangel2 < Math.PI / 5) {// 前方
                    temp[1] += 100;
                } else if (Math.abs(Math.PI / 2 - diffangel2) < Math.PI / 5) {// 不是严格的前后方 而是并排在一条与朝向垂直的
                    if (op[0] + op[1] < rp[0] + rp[1])
                        temp[1]++;
                }
            }

            if (dis < 0.92 + (status ? 0 : 0.08) + (oRobot.getStatus() ? 0 : 0.08)) {// 机器人互相卡位的情况
                temp[2] += 100;
                // 只有两个机器人互相卡的话 坐标大的避让(且不能在前方) 或者两个互卡但是有一个朝向墙使得另一个被卡死了出不去
                if (((op[0] + op[1] < rp[0] + rp[1]) && !(diffangel2 < Math.PI / 5))) {// 不在墙边则两个都可以变化
                    temp[2]++;
                }

                if (erpos[0] < 0 || erpos[0] > 50
                        || erpos[1] < 0 || erpos[1] > 50 && !(eopos[0] < 0 || eopos[0] > 50
                                || eopos[1] < 0 || eopos[1] > 50)) {// 朝向墙避让 另一个不朝向墙继续前进
                    temp[2] += 10;
                } else if (erpos[0] < 0 || erpos[0] > 50
                        || erpos[1] < 0 || erpos[1] > 50 && (eopos[0] < 0 || eopos[0] > 50
                                || eopos[1] < 0 || eopos[1] > 50)) {// 都朝向墙
                    double[] centerPos = { 25, 25 };// 离地图中心近的后退
                    if (Utils.getDistance(rp, centerPos) < Utils.getDistance(op, centerPos)) {
                        temp[2] += 10;
                    }
                }
                if (rp[1] > op[1]) { // 自身在其他机器人上方
                    temp[4] = -1;
                }
            }

        }
        return temp;

    }

    /**
     * 获取下个平台的索引
     * 
     * @return
     */
    public int getNextTargetPlatformIndex() {
        return nextTargetPlatformIndex;
    }

    /**
     * 设置下一个平台
     * 
     * @param nextTargetPlatformIndex
     */
    public void setNextTargetPlatformIndex(int nextTargetPlatformIndex) {
        this.nextTargetPlatformIndex = nextTargetPlatformIndex;
    }

    /**
     * 获取状态
     * 
     * @return
     */
    public boolean isStatus() {
        return status;
    }

    /**
     * 设置状态
     * 
     * @param status
     */
    public void setStatus(boolean status) {
        this.status = status;
    }

    /**
     * 计算预期前进2米会到达的位置
     * 
     * @param forward
     * @return
     */
    public double[] getExceptPosition(int forward) {
        exceptPosition[0] = positionX + forward * Math.cos(dirction);
        exceptPosition[1] = positionY + forward * Math.sin(dirction);
        return exceptPosition;
    }

    private int num;// 机器人的编号[0,3]
    private double positionX, positionY;// 位置坐标(positionX, positionY)
    private double prePositionX, prePositionY;// 上一帧的坐标位置(prePositionX,prePositionY)
    private double radius;// 机器人半径(m)
    private Item materia;// 携带材料
    private boolean status;// 机器人状态，买途为false，卖途为true
    private double lineSpeedX, lineSpeedY;// 线速度二维向量(m/s)
    private double dirction;// 朝向
    private double angleSpeed;// 角速度向量，正表示逆时针，负表示顺时针
    private int nearByPlatFormId;// 所处工作台ID，-1：表示当前没有处于任何工作台附近，[0,工作台总数-1] ：表示某工作台的下标
    private int exceptArriveFrame;// 预估到达目标所需帧数
    private int realArriveFrame;// 实际到达目标所需帧数
    public static int frameID;// 当前帧数
    public static int ENDFRAMEID = 9000;
    private Robot[] robotGroup;
    private double[] exceptPosition; // 预判按照该方向前进2米会到达的位置

    // 机器人接到一个购买类型任务，需要确定购买目的地
    private int nextTargetPlatformIndex; // 下一个目的地
    private int targetPlatformIndex;// 目标工作台所在的数组的下标

}