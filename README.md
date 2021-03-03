# 分布式延时任务

- **轻量**，基于redis实现，无其他依赖
- **任务可靠性**，ack机制和超时后延机制，保证任务不会丢失
- **均衡**，任务分发均匀



## 快速开始

### 引入依赖 pom.xml

```xml
<dependency>
    <groupId>tech.chengw.cloud</groupId>
    <artifactId>delaytask-springboot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```



### 提交延时任务

通过TaskSubmitter工具类

```java
@Autowired
private TaskSubmitter taskSubmitter;

public void main() {
    //提交任务，返回任务唯一标识——tag
    long tag = taskSubmitter.submit(new TaskBody(), 60 * 1000L);
    //通过tag，删除任务
    taskSubmitter.deleteTask(tag);
}
```



### 添加延时任务执行器

以下三种方式均可实现：

1. 方法注解 @TaskExecutor**【推荐】**

    ```java
    @TaskExecutor(autoAck = true)
    public void execute(long tag, TaskBody taskBody, Acker acker) {
    	//...执行任务
        acker.ack(tag);
    }
    //参考了spring-integration-amqp的实现，对方法名、参数名、参数个数无要求
    ```

2. 类注解 ，使用@Component等注册继承了TaskExecutor的子类 

    ```java
    @Component
    public class TaskExecutorImpl extends TaskExecutor  {
        @Override
        public void doExecute(long tag, TaskBody taskBody) {
            //执行任务
            //ack(tag);
            nack(tag,true,1000);
        }
    }
    ```

3. 手动注册，通过TaskExecutorRegistry

    ```java
    @Autowired
    private TaskExecutorRegistry taskExecutorRegistry;
    
    public void main() {
        //手动注册
        taskExecutorRegistry.registry(new TaskExecutor() {
            @Override
            public void doExecute(long tag, TaskBody taskBody) {
                //执行任务
                ack(tag);
            }
        });
    }
    ```

    

### 配置文件

```yaml
delay-task:
  autoAck: true 				  #是否自动ack
  timeout: 60000 	    		  #任务执行超时时间
  retryLimit: 5					  #任务最大重试次数
  initConsumerThreadNum: 10   	  #初始消费任务的线程数。消费任务负责取延时任务。消费任务基于调度线程池实现。实际线程数会根据取延时任务的平均时耗以及consumeInterval、consumerTaskNum的配置动态变化。实际线程数公式为： 实际线程数 = 平均时耗 * consumerTaskNum / consumeInterval
  maxConsumerThreadNum: 64        #消费任务的调度线程池的最大线程数
  consumeInterval: 1000 		  #每个消费任务取延时任务的时间间隔（ms）
  adjustThreadNumInterval: 10000  #消费任务的调度线程池动态调整线程数的时间间隔（ms）
  tagKey: delay_task:task_tag 					  #Redis Key ——任务tag
  zsetKey: delay_task:zset_task				      #Redis Key ——任务执行时间排序器
  hashTaskBodyKey: delay_task:hash_task_body      #Redis Key ——任务body存储器
  hashRetryCountKey: delay_task:hash_retry_count  #Redis Key ——任务重试次数计数器
  retryLimitKey: delay_task:task_retry_limit      #Redis Key ——任务重试次数上限
```



## 原理简析

https://www.processon.com/view/link/60179cdde401fd1b8dacf385



## 目前问题

未做检测延时任务大量堆积的报警器 
