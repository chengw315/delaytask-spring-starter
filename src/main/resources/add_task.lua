local tag_key = KEYS[1]
local zset_key = KEYS[2]
local hash_body_key = KEYS[3]
local body = ARGV[1]
local delay_millis = ARGV[2]

-- 任务tag
local tag = tonumber(redis.call("get", tag_key))
if tag == nil then
    tag = 0
end
redis.call("set", tag_key, tag + 1)

-- 任务执行时间
local time = redis.call('time')
local score = time[1] * 1000 + time[2] / 1000 + delay_millis
redis.call("zadd", zset_key, score, tag)

-- 任务body
redis.call("hset", hash_body_key, tag, body)

return tag