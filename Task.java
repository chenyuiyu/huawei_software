import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class Task {
    // 将工作台生产的商品抽象成一个个任务
    public Task(boolean isAtomic) {
        this.isAtomic = isAtomic;
        this.finish = false;
        if (!isAtomic) childTasks = new ArrayDeque<>();


    }


    private boolean isAtomic = false; // 任务是否是原子任务，即1，2，3
    private boolean finish = false; // 任务完成情况
    private boolean isAssign = false; // 任务是否派遣
    private Queue<Task> childTasks; // 子任务，例如对于任务7，可以分解成4,5,6
    private PlatForm platForm; // 该任务关联的平台 因为对于生产类型任务，我完成了子任务之后 需要将材料送到该平台
    private Task rootTask; // 关联父任务， 用于更新父任务状态
    private int priority; // 优先级
}
