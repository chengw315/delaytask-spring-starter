package tech.chengw.www.task;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;

import java.util.ArrayList;
import java.util.List;

/**
 * description
 *
 * @author chengwj
 * @version 1.0
 * @date 2021/2/22
 **/
public class Acker {

    private RedisTemplate redisTemplate;
    private String zsetKey;
    private String hashBodyKey;
    private String hashCountKey;


    /**
     * ack
     * @param tag 任务唯一标识符
     */
    public void ack(long tag) {
        deleteTask(tag);
    }

    /**
     * nack
     * @param tag 任务唯一标识符
     * @param resubmit 是否重新提交（重新提交将再次被执行）
     */
    public void nack(long tag, boolean resubmit) {
        nack(tag,resubmit,0L);
    }

    /**
     *
     * @param tag 任务唯一标识符
     * @param resubmit 是否重新提交（重新提交将再次被执行）
     * @param delayMillis 如果重新提交，再次执行的时延
     */
    public void nack(long tag, boolean resubmit,long delayMillis) {
        //不重新提交，删除任务
        if (!resubmit) {
            deleteTask(tag);
            return;
        }

        //重新提交
        DefaultRedisScript script = new DefaultRedisScript();
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource("nack.lua")));


        List<String> keys = new ArrayList<>();
        keys.add(zsetKey);
        keys.add(hashBodyKey);

        redisTemplate.execute(script, keys, tag, delayMillis);
    }

    private void deleteTask(long tag) {
        DefaultRedisScript script = new DefaultRedisScript();
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource("del_task.lua")));


        List<String> keys = new ArrayList<>();
        keys.add(zsetKey);
        keys.add(hashBodyKey);
        keys.add(hashCountKey);

        redisTemplate.execute(script, keys, tag);
    }

    public RedisTemplate getRedisTemplate() {
        return redisTemplate;
    }

    public Acker setRedisTemplate(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        return this;
    }

    public String getZsetKey() {
        return zsetKey;
    }

    public Acker setZsetKey(String zsetKey) {
        this.zsetKey = zsetKey;
        return this;
    }

    public String getHashBodyKey() {
        return hashBodyKey;
    }

    public Acker setHashBodyKey(String hashBodyKey) {
        this.hashBodyKey = hashBodyKey;
        return this;
    }

    public String getHashCountKey() {
        return hashCountKey;
    }

    public Acker setHashCountKey(String hashCountKey) {
        this.hashCountKey = hashCountKey;
        return this;
    }
}
