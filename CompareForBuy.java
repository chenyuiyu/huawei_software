import java.util.Comparator;

public class CompareForBuy implements Comparator<PlatForm> {
    public CompareForBuy(Robot r, double wDistance, double wAngle) {
        this.wDistance = wDistance;
        this.wAngle = wAngle;
        this.r = r;
        curVector = new double[]{Math.cos(r.getDirction()), Math.sin(r.getDirction())}; // 线速度向量
    }

    @Override
    public int compare(PlatForm p1, PlatForm p2) {
        double p1w = this.wDistance * Utils.getDistance(p1.getPosition(), r.getPosition()) +
                this.wAngle * Math.abs(Utils.getVectorAngle(Utils.getVectorBetweenPoints(r.getPosition(), p1.getPosition()), curVector));
        double p2w = this.wDistance * Utils.getDistance(p2.getPosition(), r.getPosition()) +
                this.wAngle * Math.abs(Utils.getVectorAngle(Utils.getVectorBetweenPoints(r.getPosition(), p1.getPosition()), curVector));
        if (p1w < p2w) return -1;
        return 1;
    }

    private double wDistance;
    private double wAngle;
    private double[] curVector;
    private Robot r;
}
