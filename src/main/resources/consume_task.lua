local zset_key = KEYS[1]
local hash_body_key = KEYS[2]
local hash_count_key = KEYS[3]
local count_limit_key = KEYS[4]
-- 超时时间
local over_millis = ARGV[1]

-- 取出第一个将要执行的任务
local time = redis.call('time')
local curMillis = time[1] * 1000 + time[2] / 1000
local tag = tonumber(redis.call("ZRANGEBYSCORE", zset_key, 0, curMillis, "LIMIT", 0, 1)[1])
if tag == nil then
    return { -1, nil }
end

-- 任务重试次数
local maxCount = tonumber(redis.call("get", count_limit_key))
local currentCount = tonumber(redis.call("hget", hash_count_key, tag))
if currentCount == nil then
    currentCount = 0
end

if currentCount >= maxCount then
    -- 超出重试上限，删除任务
    local body = redis.call("hget", hash_body_key, tag)
    redis.call("zrem", zset_key, tag)
    redis.call("hdel", hash_body_key, tag)
    redis.call("hdel", hash_count_key, tag)
    return { tag, body }
else
    -- 未超出重试上限，备份任务
    redis.call("hset", hash_count_key, tag, currentCount + 1)
    local score = curMillis + over_millis
    redis.call("zadd", zset_key, score, tag)
    local body = redis.call("hget", hash_body_key, tag)
    return { tag, body }
end
