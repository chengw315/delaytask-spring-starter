package tech.chengw.www.task;

import com.alibaba.fastjson.JSON;
import tech.chengw.www.configuration.DelayTaskProperties;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * description 任务消费器，负责取到期的任务并转交给{@link TaskExecutor}执行
 *
 * @author chengwj
 * @version 1.0
 * @date 2021/2/20
 **/
public class TaskConsumer {

    private final Long adjustThreadNumInterval;
    private Logger logger = LoggerFactory.getLogger(TaskConsumer.class);

    private ScheduledThreadPoolExecutor consumeTaskSchedulerPool;

    private ScheduledThreadPoolExecutor dynamicAdjustThreadNumSchedulerPool = new ScheduledThreadPoolExecutor(1, new PrefixNamedThreadFactory("dynamicAdjustThread"));
    private TaskExecutorRegistry taskExecutorRegistry;
    private RedisTemplate redisTemplate;

    private DelayTaskProperties delayTaskProperties;
    private AtomicLong statisticsConsumeMillis = new AtomicLong();
    private AtomicLong statisticsConsumeCount = new AtomicLong();
    private int consumeThreadNum;
    private final int MAX_THREAD_NUM;


    public TaskConsumer(TaskExecutorRegistry taskExecutorRegistry, RedisTemplate redisTemplate, DelayTaskProperties delayTaskProperties) {
        this.taskExecutorRegistry = taskExecutorRegistry;
        this.redisTemplate = redisTemplate;
        this.delayTaskProperties = delayTaskProperties;
        this.MAX_THREAD_NUM = delayTaskProperties.getMaxConsumerThreadNum();
        this.consumeThreadNum = Math.min(MAX_THREAD_NUM, delayTaskProperties.getInitConsumerThreadNum());

        //消费任务前设置最大重试次数
        redisTemplate.opsForValue().set(delayTaskProperties.getRetryLimitKey(), delayTaskProperties.getRetryLimit());

        consumeTaskSchedulerPool = new ScheduledThreadPoolExecutor(consumeThreadNum, new PrefixNamedThreadFactory("consumeThread"));
        Long consumeInterval = delayTaskProperties.getConsumeInterval();
        //消费任务的启动间隔
        long startInterval = consumeInterval / consumeThreadNum;
        for (int i = 0; i < consumeThreadNum; i++) {
            consumeTaskSchedulerPool.scheduleAtFixedRate(new ConsumeTask(taskExecutorRegistry), startInterval * i, consumeInterval, TimeUnit.MILLISECONDS);
        }
        //周期调整消费任务的线程数
        adjustThreadNumInterval = delayTaskProperties.getAdjustThreadNumInterval();
        dynamicAdjustThreadNumSchedulerPool.scheduleAtFixedRate(this::dynamicAdjustThreadNum,
                adjustThreadNumInterval,
                adjustThreadNumInterval,
                TimeUnit.MILLISECONDS);
    }

    /**
     * 取一个到期的任务
     *
     * @return {任务tag，任务body}，如果无到期的任务，则会返回 {-1,null}
     */
    public Pair<Long, TaskBody> consumeTask() {
        DefaultRedisScript<List> script = new DefaultRedisScript();
        script.setResultType(List.class);
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource("consume_task.lua")));


        List<String> keys = new ArrayList<>();
        keys.add(delayTaskProperties.getZsetKey());
        keys.add(delayTaskProperties.getHashTaskBodyKey());
        keys.add(delayTaskProperties.getHashRetryCountKey());
        keys.add(delayTaskProperties.getRetryLimitKey());

        List<Object> list = (List<Object>) redisTemplate.execute(script, keys, delayTaskProperties.getTimeout());
        if (list == null || list.size() < 2) {
            return null;
        } else {
            return new Pair(list.get(0), JSON.parseObject((String) list.get(1), TaskBody.class));
        }
    }

    /**
     * 更新统计
     *
     * @param cost
     */
    public void updateConsumeStatistics(long cost) {
        statisticsConsumeCount.incrementAndGet();
        long cur;
        do {
            cur = statisticsConsumeMillis.get();
        } while (!statisticsConsumeMillis.compareAndSet(cur, cur + cost));

    }

    /**
     * 动态调整consumeTaskSchedulerPool的实际线程数
     * 实际线程数 = ConsumeTask平均执行时间 * ConsumeTask数 / Consume间隔 + 1
     */
    public void dynamicAdjustThreadNum() {
        long sumCost = statisticsConsumeMillis.get();
        long averageCost = sumCost / statisticsConsumeCount.get();
        statisticsConsumeMillis.set(0);
        statisticsConsumeCount.set(0);
        int newThreadNum = (int) (averageCost * delayTaskProperties.getInitConsumerThreadNum() / delayTaskProperties.getConsumeInterval() + 1);
        newThreadNum = Math.min(MAX_THREAD_NUM, newThreadNum);
        logger.info("Statistics in last {}ms: consume count-{}, average consume cost-{}ms", adjustThreadNumInterval, sumCost, averageCost);

        if (consumeThreadNum != newThreadNum) {
            logger.info("Consume Thread Num Adjust: {} -> {}", consumeThreadNum, newThreadNum);
            consumeThreadNum = newThreadNum;
            consumeTaskSchedulerPool.setCorePoolSize(newThreadNum);
        }
    }

    public class ConsumeTask implements Runnable {

        private TaskExecutorRegistry taskExecutorRegistry;

        public ConsumeTask(TaskExecutorRegistry taskExecutorRegistry) {
            this.taskExecutorRegistry = taskExecutorRegistry;
        }

        @Override
        public void run() {
            try {
                long currentTimeMillis = System.currentTimeMillis();
                TaskExecutor taskExecutor = taskExecutorRegistry.nextExecutor();
                //无执行器
                if (taskExecutor == null) {
                    return;
                }
                Pair<Long, TaskBody> tagTaskPair = consumeTask();
                //无到期任务
                if (tagTaskPair == null || tagTaskPair.getKey() == -1) {
                    return;
                }
                taskExecutor.execute(tagTaskPair.getKey(), tagTaskPair.getValue());
                long cost = System.currentTimeMillis() - currentTimeMillis;
                updateConsumeStatistics(cost);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public class PrefixNamedThreadFactory implements ThreadFactory {

        private String prefixName;
        private AtomicInteger threadNum;
        private ThreadGroup threadGroup;

        public PrefixNamedThreadFactory(String prefixName) {
            this.prefixName = prefixName + "-";
            threadNum = new AtomicInteger();
            SecurityManager s = System.getSecurityManager();
            threadGroup = (s != null) ? s.getThreadGroup() :
                    Thread.currentThread().getThreadGroup();
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(threadGroup, r,
                    prefixName + threadNum.getAndIncrement(),
                    0);
            if (t.isDaemon()) {
                t.setDaemon(false);
            }
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            return t;
        }
    }
}
