//package com.huawei.codecraft;
/**
 * 工具类
 */
public class Util {

    /**
     * 求两个向量的夹角
     * @param vector1 向量1
     * @param vector2 向量2
     * @return 向量夹角(rad)
     */
    public static double getVectorAngle(double[] vector1, double[] vector2) {
        double vectorProduct = vector1[0] * vector2[0] + vector1[1] * vector2[1];
        double vectorNorm = Math.sqrt(Math.pow(vector1[0], 2) + Math.pow(vector1[1], 2))
                * Math.sqrt(Math.pow(vector2[0], 2) + Math.pow(vector2[1], 2));

        double diffangel = Math.acos(vectorProduct / vectorNorm);
        return diffangel;
    }

    /**
     * 求两点距离
     * @param pos1 坐标点1
     * @param pos2 坐标点2
     * @return 两坐标点距离
     */
    public static double getDistance(double[] pos1, double[] pos2) {
        double dis = Math.sqrt(Math.pow(pos1[0] - pos2[0], 2) + Math.pow(pos1[1] - pos2[1], 2));
        return dis;
    }

    /**
     * 求点pos1到点pos2的向量
     * @param pos1
     * @param pos2
     * @return
     */
    public static double[] getVectorBetweenPoints(double[] pos1, double[] pos2) {
        return new double[] {pos2[0] - pos1[0], pos2[1] - pos1[1]};
    }


    /**
     * 判断两条线段是否相交
     * 
     * @param pos1
     * @param pos2
     * @param pos3
     * @param pos4
     * @return
     */
    public static boolean intersectCheck(double[] pos1, double[] pos2, double[] pos3, double[] pos4) {
        double temp = 1e-10;
        double m = (pos2[0] - pos1[0]) * (pos3[1] - pos1[1]) - (pos3[0] - pos1[0]) * (pos2[1] - pos1[1]);
        double n = (pos2[0] - pos1[0]) * (pos4[1] - pos1[1]) - (pos4[0] - pos1[0]) * (pos2[1] - pos1[1]);
        double p = (pos4[0] - pos3[0]) * (pos1[1] - pos3[1]) - (pos1[0] - pos3[0]) * (pos4[1] - pos3[1]);
        double q = (pos4[0] - pos3[0]) * (pos2[1] - pos3[1]) - (pos2[0] - pos3[0]) * (pos4[1] - pos3[1]);
        if (m * n <= temp && p * q <= temp)
            return true;
        else
            return false;
    }
}
