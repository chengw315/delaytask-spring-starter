package tech.chengw.www.task;

import tech.chengw.www.configuration.DelayTaskProperties;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * description
 *
 * @author chengwj
 * @version 1.0
 * @date 2021/2/2
 **/
public class TaskExecutorRegistry {

    public TaskExecutorRegistry(DelayTaskProperties delayTaskProperties, Acker acker) {
        this.delayTaskProperties = delayTaskProperties;
        this.acker = acker;
    }

    private DelayTaskProperties delayTaskProperties;
    private Acker acker;
    private Set<TaskExecutor> taskExecutorSet = new HashSet<>();
    private List<TaskExecutor> taskExecutors = new ArrayList<>();
    private int curExecutorIndex = 0;

    /**
     * 获取下一个Executor
     * @return
     */
    TaskExecutor nextExecutor() {
        if (taskExecutors.isEmpty()) {
            return null;
        }
        curExecutorIndex = (curExecutorIndex + 1) % taskExecutors.size();
        return taskExecutors.get(curExecutorIndex);
    }

    /**
     * 添加TaskExecutor
     * 如果注册了多个TaskExecutor，任务到来时每个executor轮流消费，假设有10个任务，有5个Executor，则理论上每个Executor会执行两个任务
     * @param taskExecutor
     * @return
     */
    public TaskExecutorRegistry registry(TaskExecutor taskExecutor) {
        if (taskExecutor.getAutoAck() == null) {
            taskExecutor.setAutoAck(delayTaskProperties.getAutoAck());
        }
        taskExecutor.setAcker(acker);
        //去重
        if(taskExecutorSet.add(taskExecutor)) {
            taskExecutors.add(taskExecutor);
        }
        return this;
    }

    /**
     * 移除指定的TaskExecutor
     * @param taskExecutor
     * @return
     */
    public TaskExecutorRegistry removeTaskExecutor(TaskExecutor taskExecutor) {
        taskExecutorSet.remove(taskExecutor);
        taskExecutors.remove(taskExecutor);
        return this;
    }

    /**
     * 移除指定类型的TaskExecutor
     * @param taskExecutorClass
     * @return
     */
    public TaskExecutorRegistry removeTaskExecutor(Class taskExecutorClass) {
        taskExecutorSet.removeIf(taskExecutorClass::isInstance);
        taskExecutors.removeIf(taskExecutorClass::isInstance);
        return this;
    }

    /**
     * 移除所有TaskExecutor
     * @return
     */
    public TaskExecutorRegistry removeAllExecutor() {
        taskExecutorSet.clear();
        taskExecutors.clear();
        return this;
    }
}
