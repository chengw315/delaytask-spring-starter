package tech.chengw.www.configuration;

import tech.chengw.www.annocation.DelayTaskBeanPostProcessorRegistry;
import tech.chengw.www.task.Acker;
import tech.chengw.www.task.TaskConsumer;
import tech.chengw.www.task.TaskExecutorRegistry;
import tech.chengw.www.task.TaskSubmitter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * description
 *
 * @author chengwj
 * @version 1.0
 * @date 2021/2/1
 **/
@Configuration
@Import(DelayTaskBeanPostProcessorRegistry.class)
@EnableConfigurationProperties(DelayTaskProperties.class)
public class DelayTaskConfiguration {

    @Autowired
    private DelayTaskProperties delayTaskProperties;
    @Autowired
    private RedisTemplate redisTemplate;

    @Bean
    public TaskSubmitter taskSubmitter() {
        return new TaskSubmitter(redisTemplate,
                delayTaskProperties.getTagKey(),
                delayTaskProperties.getZsetKey(),
                delayTaskProperties.getHashTaskBodyKey(),
                delayTaskProperties.getHashRetryCountKey());
    }

    @Bean
    public Acker acker() {
        return new Acker()
                .setRedisTemplate(redisTemplate)
                .setHashBodyKey(delayTaskProperties.getHashTaskBodyKey())
                .setHashCountKey(delayTaskProperties.getHashRetryCountKey())
                .setZsetKey(delayTaskProperties.getZsetKey());
    }

    @Bean
    public TaskExecutorRegistry taskExecutorRegistry(Acker acker) {
        return new TaskExecutorRegistry(delayTaskProperties, acker);
    }

    @Bean
    public TaskConsumer taskConsumer(TaskExecutorRegistry taskExecutorRegistry) {
        return new TaskConsumer(taskExecutorRegistry, redisTemplate, delayTaskProperties);
    }
}
