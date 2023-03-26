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
                    + c * p.getScore()
                        + d * p.getNumberPriority();// 权值函数，越小越好

        double p2w = a * Util.getDistance(p2.getPosition(), curRobotposition)
            + 20.0 * b * Math.abs(Util.getVectorAngle(
                Util.getVectorBetweenPoints(curRobotposition, p2.getPosition()), curVector))
                    + c  * p2.getScore()
                        + d * p2.getNumberPriority();// 权值函数，越小越好
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
    
    /**
     * 设置各个权值系数
     * @param a 距离权值系数
     * @param b 角度权值系数
     * @param c 材料格权值系数(已满材料格占该平台需要材料格的比例)
     * @param d 工作台类型数量权值系数(该类型工作台数量越多越大)
     */
    public static void setCoffients(double a, double b, double c, double d) {
        PlatFormBoxForSell.a = a;
        PlatFormBoxForSell.b = b;
        PlatFormBoxForSell.c = c;
        PlatFormBoxForSell.d = d;
    }

    private PlatForm p;//工作台
    private static double[] curRobotposition;// 机器人位置
    private static double[] curVector;
    private static int[] ipc;// 1-6类物品原料格的总数
    private static int[] cipc;// 1-6类物品原料格当前数量
    private static double a = 1000.0, b = 10.0, c = 100.0, d = 10000.0;//图4d要大（至少10000）
}