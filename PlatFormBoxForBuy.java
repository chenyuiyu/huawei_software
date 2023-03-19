/**
* 三个系数abc分别控制距离和角度差的权值以场上该类物品比例分数的权值
*/
public class PlatFormBoxForBuy implements Comparable<PlatFormBoxForBuy> {

    public PlatFormBoxForBuy(PlatForm p) {
        this.p = p;
    }

    public int compareTo(PlatFormBoxForBuy others) {
        PlatForm p2 = others.getPlatForm();
        int ind1 = p.getPlatFormType().getIndex();
        int ind2 = p2.getPlatFormType().getIndex();
        double p1w = a * Util.getDistance(p.getPosition(), curRobotposition)
            + 20.0 * b * Math.abs(Util.getVectorAngle(
                Util.getVectorBetweenPoints(curRobotposition, p.getPosition()), curVector))
                    + c * ipc[ind1] / (0.001 * (ipc[ind1] - cipc[ind1]) + 0.00000001);// 权值函数，越小越好

        double p2w = a * Util.getDistance(p2.getPosition(), curRobotposition)
            + 20.0 * b * Math.abs(Util.getVectorAngle(
                Util.getVectorBetweenPoints(curRobotposition, p2.getPosition()), curVector))
                    + c * ipc[ind2] / (0.001 * (ipc[ind2] - cipc[ind2]) + 0.00000001);// 权值函数，越小越好
        if (p1w < p2w) return -1;
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
        PlatFormBoxForBuy.ipc = ipc;
    }

    /**
     * 设置物品类型原料格当前占用数组
     * @param cipc 占用数组
     */
    public static void setCIPC(int[] cipc) {
        PlatFormBoxForBuy.cipc = cipc;
    }
    
    private PlatForm p;//工作台
    private static double[] curRobotposition;// 机器人位置
    private static double[] curVector;
    private static int[] ipc;// 1-6类物品原料格的总数
    private static int[] cipc;// 1-6类物品原料格当前数量
    private static final double a = 1.0, b = 1.0, c = 1.0;
}
