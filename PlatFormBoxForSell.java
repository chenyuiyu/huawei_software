//package com.huawei.codecraft;
/**
 * 该类用于封装工作台，并且内置了工作台的比较策略
 */
public class PlatFormBoxForSell implements Comparable<PlatFormBoxForSell> {

    public PlatFormBoxForSell(PlatForm p) {
        this.p = p;
    }

    public int compareTo(PlatFormBoxForSell others) {
        int ind1 = p.getPlatFormType().getIndex();
        int ind2 = others.getPlatForm().getPlatFormType().getIndex();
        PlatForm p2 = others.getPlatForm();
        double p1w = a * Util.getDistance(p.getPosition(), curRobotposition)
            + 20.0 * b * Math.abs(Util.getVectorAngle(
                Util.getVectorBetweenPoints(curRobotposition, p.getPosition()), curVector))
                    + c * 60.0 * p.getScore();
                        //+ d * ipc[ind1] / (0.0001 * (ipc[ind1] - cipc[ind1]) + 0.00000001);// 权值函数，越小越好

        double p2w = a * Util.getDistance(p2.getPosition(), curRobotposition)
            + 20.0 * b * Math.abs(Util.getVectorAngle(
                Util.getVectorBetweenPoints(curRobotposition, p2.getPosition()), curVector))
                    + c * 60.0 * p2.getScore();
                        //+ d * ipc[ind2] / (0.0001 * (ipc[ind2] - cipc[ind2]) + 0.00000001);// 权值函数，越小越好
        if (p1w < p2w)
            return -1;
        return 1;
    }

    /**
     * 获取封装的工作台
     * @return 工作台
     */
    public PlatForm getPlatForm() {
        return p;
    }

    /**
     * 设置当前机器人的位置，此参数所有实例共享
     * @param pos 当前机器人的位置
     */
    public static void setCurRobotposition(double[] pos) {
        curRobotposition = pos;
    }

    /**
     * 设置机器人朝向向量
     * @param vector 机器人朝向
     */
    public static void setCurVector(double[] vector) {
        curVector = vector;
    }

    /**
     * 设置物品类型原料格总数数组
     * @param ipc 总数数组
     */
    public static void setIPC(int[] ipc) {
        PlatFormBoxForSell.ipc = ipc;
    }

    /**
     * 设置物品类型原料格当前占用数组
     * @param cipc 占用数组
     */
    public static void setCIPC(int[] cipc) {
        PlatFormBoxForSell.cipc = cipc;
    }

    private PlatForm p;//工作台
    private static double[] curRobotposition;// 机器人位置
    private static double[] curVector;
    private static int[] ipc;// 1-6类物品原料格的总数
    private static int[] cipc;// 1-6类物品原料格当前数量
    private static final double a = 100.0, b = 1.0, c = 100.0, d = 1.0;
}