public class Util {
    public static double getVectorAngle(double[] vector1, double[] vector2) {
        double vectorProduct = vector1[0] * vector2[0] + vector1[1] * vector2[1];
        double vectorNorm = Math.sqrt(Math.pow(vector1[0], 2) + Math.pow(vector1[1], 2))
                * Math.sqrt(Math.pow(vector2[0], 2) + Math.pow(vector2[1], 2));

        double diffangel = Math.acos(vectorProduct / vectorNorm);
        return diffangel;
    }

    public static double getDistance(double[] pos1, double[] pos2) {
        double dis = Math.sqrt(Math.pow(pos1[0] - pos2[0], 2) + Math.pow(pos1[1] - pos2[1], 2));
        return dis;
    }
}
