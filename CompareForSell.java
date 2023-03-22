import java.util.Comparator;

public class CompareForSell implements Comparator<PlatForm> {

    public CompareForSell(Robot r, double a, double b) {
        this.wDistance = a;
        this.wAngle = b;
        this.r = r;
        this.curVector = new double[]{Math.cos(r.getDirction()), Math.sin(r.getDirction())};

    }

    @Override
    public int compare(PlatForm p1, PlatForm p2) {
        double p1w = this.wDistance * Utils.getDistance(p1.getPosition(), r.getPosition()) +
                this.wAngle * Math.abs(Utils.getVectorAngle(Utils.getVectorBetweenPoints(r.getPosition(), p1.getPosition()),
                        curVector));

        double p2w = this.wDistance * Utils.getDistance(p2.getPosition(), r.getPosition()) +
                this.wAngle * Math.abs(Utils.getVectorAngle(Utils.getVectorBetweenPoints(r.getPosition(), p2.getPosition()),
                        curVector));
        if (p1w < p2w) return -1;
        return 1;
    }

    private double wDistance; // 距离权重
    private double wAngle; // 角度权重
    private double[] curVector; // 线速度向量
    private Robot r;
}
