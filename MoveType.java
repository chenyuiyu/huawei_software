import java.util.List;
import java.util.PriorityQueue;

public interface MoveType {
    List<Order> Move(Robot curR, List<PlatForm> platFormList, List<List<PlatForm>> labelPlatforms, PriorityQueue<Task> taskQueue);
}
