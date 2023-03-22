import java.util.Comparator;

/**
 * 该类用于比较平台p与其他平台的距离
 */
public class CompareBetweenPlatform implements Comparator<PlatForm> {
    public CompareBetweenPlatform(PlatForm p) {
        this.p = p;
    }

    @Override
    public int compare(PlatForm p1, PlatForm p2) {
        double cost1 = Utils.getDistance(p1.getPosition(), p.getPosition());
        double cost2 = Utils.getDistance(p2.getPosition(), p.getPosition());

        if (cost1 < cost2) return -1;
        return 1;
    }

    private PlatForm p; // 用于
}
