import java.util.List;

public class Task {
    // 将工作台生产的商品抽象成一个个任务
    public Task(boolean isProductTask) {
        this.isProductTask = isProductTask;

    }


    private boolean isProductTask = true; // 是否是生产类型任务 true为生产类型 false为出售类型
    private boolean finish = false; // 任务完成情况
    private List<Task> childTasks; // 子任务，例如对于任务7，可以分解成4,5,6
    private PlatForm platForm; // 该任务关联的平台 因为对于生产类型任务，我完成了子任务之后 需要将材料送到该平台
}
