
import java.util.Comparator;

public class CompareBetween456 implements Comparator<Integer> {

    public CompareBetween456(int[] cipc) {
        this.cipc = cipc;
    }

    public int compare(Integer a, Integer b) {
        return cipc[a] - cipc[b];
    }

    private int[] cipc;
}
