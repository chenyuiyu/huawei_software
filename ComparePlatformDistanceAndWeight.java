import java.util.Comparator;

/**
 * 注意此类与CompareBetweenPlatform的用法区别
 * 本类仅用于初始化阶段为每个平台生成一颗搜索树所使用，
 * 例如：如果是7号类型的工作台 对于4号类型的时候，应该权重应该是7到4，加上4自身的权重【4自身的权重含义为4到1，4到2】
 */
public class ComparePlatformDistanceAndWeight implements Comparator<PlatForm> {
    public ComparePlatformDistanceAndWeight(PlatForm p) {
        this.p = p;
    }

    @Override
    public int compare(PlatForm p1, PlatForm p2) {
        double cost1 = Utils.getDistance(p1.getPosition(), p.getPosition());
        double cost2 = Utils.getDistance(p2.getPosition(), p.getPosition());
        if (p.getPlatFormType().getIndex() == 7) {
            cost1 += p1.getWeight();
            cost2 += p2.getWeight();
        }

        if (cost1 < cost2) return -1;
        return 1;
    }

    private PlatForm p;
}
