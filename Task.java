import java.util.Comparator;

/**
 * 任务数据结构 任务有两种类型：
 */
public class Task {
    public Task() {

    }

    public Task(boolean isAtomic, boolean isProductTypeTask, int curTaskPlatformId, int rootTaskPlatformId, int priority, int taskNum) {
        this.isAtomic = isAtomic;
        this.isProductTypeTask = isProductTypeTask;
        this.curTaskPlatformId = curTaskPlatformId;
        this.rootTaskPlatformId = rootTaskPlatformId;
        this.priority = priority;
        this.taskNum = taskNum;
    }

    public boolean isAtomic() {
        return isAtomic;
    }

    public void setAtomic(boolean atomic) {
        isAtomic = atomic;
    }

    public boolean isProductTypeTask() {
        return isProductTypeTask;
    }

    public void setProductTypeTask(boolean productTypeTask) {
        isProductTypeTask = productTypeTask;
    }

    public int getCurTaskPlatformId() {
        return curTaskPlatformId;
    }

    public void setCurTaskPlatformId(int curTaskPlatformId) {
        this.curTaskPlatformId = curTaskPlatformId;
    }

    public int getRootTaskPlatformId() {
        return rootTaskPlatformId;
    }

    public void setRootTaskPlatformId(int rootTaskPlatformId) {
        this.rootTaskPlatformId = rootTaskPlatformId;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getTaskNum() {
        return taskNum;
    }

    public void setTaskNum(int taskNum) {
        this.taskNum = taskNum;
    }

    // 成员变量
    private boolean isAtomic = false; // 任务是否是原子任务【1，2，3】
    private boolean isProductTypeTask; // true为生产类型任务 false为fetch类型任务
    private int curTaskPlatformId = -1;  //当前任务对应的平台 【-1表示尚未指定】
    private int rootTaskPlatformId = -1; // 父任务对应的平台 用于指定生产出来的产品该送去哪个平台进行进一步加工【-1 表示没有】
    private int priority; // 优先级任务优先级类型
    private int taskNum; // 任务编号[1, 7]

}
