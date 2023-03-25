import java.util.Comparator;

public class CompareBetweenPlatformAndRobot implements Comparator<PlatForm> {


    public CompareBetweenPlatformAndRobot(Robot r) {
        this.r = r;
    }

    @Override
    public int compare(PlatForm p1, PlatForm p2) {
        double cost1 = Utils.getDistance(r.getPosition(), p1.getPosition());
        double cost2 = Utils.getDistance(r.getPosition(), p2.getPosition());
        if (cost1 < cost2) return -1;
        return 1;
    }

    private Robot r;
}
