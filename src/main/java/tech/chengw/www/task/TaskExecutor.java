package tech.chengw.www.task;

/**
 * description 执行任务的实际类
 *
 * @author chengwj
 * @version 1.0
 * @date 2021/2/2
 **/
public abstract class TaskExecutor {

    private Boolean autoAck;
    private Acker acker;
    /**
     * 执行任务
     * @param tag 任务唯一标识符
     * @param taskBody 任务体
     */
    public void execute(Long tag, TaskBody taskBody) {
        if (autoAck) {
            ack(tag);
        }
        doExecute(tag,taskBody);
    }

    /**
     * 执行任务
     * @param tag 任务唯一标识符
     * @param taskBody 任务体
     */
    public abstract void doExecute(Long tag, TaskBody taskBody);

    /**
     * ack
     * @param tag 任务唯一标识符
     */
    protected final void ack(long tag) {
        acker.ack(tag);
    }

    /**
     * nack
     * @param tag 任务唯一标识符
     * @param resubmit 是否重新提交（重新提交将再次被执行）
     */
    protected final void nack(long tag, boolean resubmit) {
        acker.nack(tag, resubmit);
    }

    /**
     *
     * @param tag 任务唯一标识符
     * @param resubmit 是否重新提交（重新提交将再次被执行）
     * @param delayMillis 如果重新提交，再次执行的时延
     */
    protected final void nack(long tag, boolean resubmit,long delayMillis) {
        acker.nack(tag, resubmit, delayMillis);
    }


    /**
     * setAutoAck方法的优先级高于全局配置中的autoAck
     * @param autoAck
     * @return
     */
    TaskExecutor setAutoAck(boolean autoAck) {
        this.autoAck = autoAck;
        return this;
    }


    Boolean getAutoAck() {
        return autoAck;
    }

    public Acker getAcker() {
        return acker;
    }

    public TaskExecutor setAcker(Acker acker) {
        this.acker = acker;
        return this;
    }
}
