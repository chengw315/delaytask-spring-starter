package tech.chengw.www.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * description
 *
 * @author chengwj
 * @version 1.0
 * @date 2021/2/2
 **/
@ConfigurationProperties(prefix = "delay-task")
public class DelayTaskProperties {

    private Boolean autoAck = true;
    private Long timeout = 60 * 1000L;
    private Integer retryLimit = 5;
    private Integer initConsumerThreadNum = 10;
    private Integer maxConsumerThreadNum = 64;
    private Long consumeInterval = 1000L;
    private Long adjustThreadNumInterval = 10000L;
    private String tagKey = "delay_task:task_tag";
    private String zsetKey = "delay_task:zset_task";
    private String hashTaskBodyKey = "delay_task:hash_task_body";
    private String hashRetryCountKey = "delay_task:hash_retry_count";
    private String retryLimitKey = "delay_task:task_retry_limit";

    public Boolean getAutoAck() {
        return autoAck;
    }

    public DelayTaskProperties setAutoAck(Boolean autoAck) {
        this.autoAck = autoAck;
        return this;
    }

    public Long getTimeout() {
        return timeout;
    }

    public DelayTaskProperties setTimeout(Long timeout) {
        this.timeout = timeout;
        return this;
    }

    public Integer getRetryLimit() {
        return retryLimit;
    }

    public DelayTaskProperties setRetryLimit(Integer retryLimit) {
        this.retryLimit = retryLimit;
        return this;
    }

    public String getTagKey() {
        return tagKey;
    }

    public DelayTaskProperties setTagKey(String tagKey) {
        this.tagKey = tagKey;
        return this;
    }

    public String getZsetKey() {
        return zsetKey;
    }

    public DelayTaskProperties setZsetKey(String zsetKey) {
        this.zsetKey = zsetKey;
        return this;
    }

    public String getHashTaskBodyKey() {
        return hashTaskBodyKey;
    }

    public DelayTaskProperties setHashTaskBodyKey(String hashTaskBodyKey) {
        this.hashTaskBodyKey = hashTaskBodyKey;
        return this;
    }

    public String getHashRetryCountKey() {
        return hashRetryCountKey;
    }

    public DelayTaskProperties setHashRetryCountKey(String hashRetryCountKey) {
        this.hashRetryCountKey = hashRetryCountKey;
        return this;
    }

    public Integer getInitConsumerThreadNum() {
        return initConsumerThreadNum;
    }

    public DelayTaskProperties setInitConsumerThreadNum(Integer initConsumerThreadNum) {
        this.initConsumerThreadNum = initConsumerThreadNum;
        return this;
    }

    public Long getConsumeInterval() {
        return consumeInterval;
    }

    public DelayTaskProperties setConsumeInterval(Long consumeInterval) {
        this.consumeInterval = consumeInterval;
        return this;
    }

    public String getRetryLimitKey() {
        return retryLimitKey;
    }

    public DelayTaskProperties setRetryLimitKey(String retryLimitKey) {
        this.retryLimitKey = retryLimitKey;
        return this;
    }

    public Long getAdjustThreadNumInterval() {
        return adjustThreadNumInterval;
    }

    public DelayTaskProperties setAdjustThreadNumInterval(Long adjustThreadNumInterval) {
        this.adjustThreadNumInterval = adjustThreadNumInterval;
        return this;
    }

    public Integer getMaxConsumerThreadNum() {
        return maxConsumerThreadNum;
    }

    public DelayTaskProperties setMaxConsumerThreadNum(Integer maxConsumerThreadNum) {
        this.maxConsumerThreadNum = maxConsumerThreadNum;
        return this;
    }
}
