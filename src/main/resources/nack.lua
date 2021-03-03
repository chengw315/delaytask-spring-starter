local zset_key = KEYS[1]
local tag = ARGV[1]
local delayMillis = ARGV[2]

-- 任务是否已被删除
local row = tonumber(redis.call("zrank", zset_key, tag))
if row ~= nil then
    --更新任务时间
    local time = redis.call('time')
    local newScore = time[1] * 1000 + time[2] / 1000 + delayMillis
    redis.call("zadd", zset_key, newScore, tag)
end

