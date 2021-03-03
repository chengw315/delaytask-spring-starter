package tech.chengw.www.task;

import com.alibaba.fastjson.JSON;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;

import java.util.ArrayList;
import java.util.List;

/**
 * description 此对象提供：提交任务、删除任务接口
 *
 * @author chengwj
 * @version 1.0
 * @date 2021/2/2
 **/
public class TaskSubmitter {

    private RedisTemplate redisTemplate;
    private String tagKey;
    private String zsetKey;
    private String hashBodyKey;
    private String hashCountKey;

    public TaskSubmitter(RedisTemplate redisTemplate, String tagKey, String zsetKey, String hashBodyKey, String hashCountKey) {
        this.redisTemplate = redisTemplate;
        this.tagKey = tagKey;
        this.zsetKey = zsetKey;
        this.hashBodyKey = hashBodyKey;
        this.hashCountKey = hashCountKey;
    }

    /**
     * 提交任务
     * @param taskBody
     * @param delayMillis
     * @return -1提交失败
     */
    public long submit(TaskBody taskBody,long delayMillis) {
        DefaultRedisScript<Long> script = new DefaultRedisScript();
        script.setResultType(Long.class);
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource("add_task.lua")));


        List<String> keys = new ArrayList<>();
        keys.add(tagKey);
        keys.add(zsetKey);
        keys.add(hashBodyKey);

        return (Long) redisTemplate.execute(script, keys, JSON.toJSONString(taskBody),delayMillis);
    }

    /**
     * 删除任务/取消任务
     * @param tag
     */
    public void deleteTask(long tag) {
        DefaultRedisScript script = new DefaultRedisScript();
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource("del_task.lua")));


        List<String> keys = new ArrayList<>();
        keys.add(zsetKey);
        keys.add(hashBodyKey);
        keys.add(hashCountKey);

        redisTemplate.execute(script, keys, tag);
    }
}
