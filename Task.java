import java.util.Queue;

public class Task {


    private boolean isAtomic = false; // 任务是否是原子任务 原子任务不需要再分
    private int curTaskPlatformId = -1;  //当前任务对应的平台 【-1表示尚未指定】
    private Queue<Task> childTasks; // 子任务，例如对于任务7，可以分解成4,5,6
    private PlatForm platForm; // 该任务关联的平台 因为对于生产类型任务，我完成了子任务之后 需要将材料送到该平台
    private Task rootTask; // 关联父任务， 用于更新父任务状态
    private int priority; // 优先级
}
