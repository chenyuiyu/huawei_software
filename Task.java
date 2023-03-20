import java.util.Comparator;

/**
 * 任务数据结构 任务有两种类型：
 */
public class Task implements Comparator<Task> {
    public Task() {

    }

    public Task(boolean isAtomic, boolean isProductTypeTask, int curTaskPlatformId, int rootTaskPlatformId, int priority) {
        this.isAtomic = isAtomic;
        this.isProductTypeTask = isProductTypeTask;
        this.curTaskPlatformId = curTaskPlatformId;
        this.rootTaskPlatformId = rootTaskPlatformId;
        this.priority = priority;
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


    // 成员变量
    private boolean isAtomic = false; // 任务是否是原子任务【1，2，3】
    private boolean isProductTypeTask; // true为生产类型任务 false为fetch类型任务
    private int curTaskPlatformId = -1;  //当前任务对应的平台 【-1表示尚未指定】
    private int rootTaskPlatformId = -1; // 父任务对应的平台 用于指定生产出来的产品该送去哪个平台进行进一步加工【-1 表示没有】
    private int priority; // 优先级任务优先级类型

    // 类变量
    //
    public static int PRIO_PRODUCT_7 = 10;
    public static int PRIO_PRODUCT_6 = 11;
    public static int PRIO_PRODUCT_5 = 12;
    public static int PRIO_PRODUCT_4 = 13;

    public static int PRIO_FETCH_7 = 1;
    public static int PRIO_FETCH_6 = 2;
    public static int PRIO_FETCH_5 = 3;
    public static int PRIO_FETCH_4 = 4;

    @Override
    public int compare(Task t1, Task t2) {
        int diff = t1.getPriority() - t2.getPriority();
        return Integer.compare(diff, 0);
    }
}
