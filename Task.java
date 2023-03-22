/**
 * 任务数据结构 平台发布任务 机器人领取任务
 */
public class Task {
    public Task() {

    }

    public Task(boolean isAtomic, int curTaskPlatformId, int rootTaskPlatformId, int priority, int taskNum) {
        this.isAtomic = isAtomic;
        this.curTaskPlatformId = curTaskPlatformId;
        this.rootTaskPlatformId = rootTaskPlatformId;
        this.platformIdForBuy = this.curTaskPlatformId;
        this.platformIdForSell = this.rootTaskPlatformId;
        this.priority = priority;
        this.taskNum = taskNum;
    }

    public boolean isAtomic() {
        return isAtomic;
    }

    public void setAtomic(boolean atomic) {
        isAtomic = atomic;
    }

    public int getPlatformIdForBuy() {
        return curTaskPlatformId;
    }

    public void setPlatformIdForBuy(int platformIdForBuy) {
        setCurTaskPlatformId(platformIdForBuy);
    }

    public int getPlatformIdForSell() {
        return rootTaskPlatformId;
    }

    public void setPlatformIdForSell(int platformIdForSell) {
        this.rootTaskPlatformId = platformIdForSell;
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

    @Override
    public String toString() {
        return "Task{" +
                "isAtomic=" + isAtomic +
                ", curTaskPlatformId=" + curTaskPlatformId +
                ", rootTaskPlatformId=" + rootTaskPlatformId +
                ", platformIdForBuy=" + platformIdForBuy +
                ", platformIdForSell=" + platformIdForSell +
                ", priority=" + priority +
                ", taskNum=" + taskNum +
                '}';
    }

    // 成员变量
    // curTaskPlatformId 完全等同于 platformIdForBuy
    // rootTaskPlatformId 完全等同于 platformIdForSell
    // 这样定义这是为了语义性更好
    private boolean isAtomic; // 任务是否是原子任务【1，2，3】or 【Sell】
    private int curTaskPlatformId; // 平台角度： 当前任务对应的平台id 【-1表示未指定】
    private int rootTaskPlatformId; // 平台角度： 父任务对应的平台id 【-1表示未指定】
    private int platformIdForBuy;  // 机器人角度： 机器人领取任务后，需要去哪里买材料【-1表示未指定】
    private int platformIdForSell; // 机器人角度： 机器人领取任务后，需要去哪里卖材料 【-1 表示未指定】
    private int priority; // 任务优先级
    private int taskNum; // 任务编号[1, 7]

}
