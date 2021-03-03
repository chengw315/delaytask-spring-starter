local zset_key = KEYS[1]
local hash_body_key = KEYS[2]
local hash_count_key = KEYS[3]
local tag = ARGV[1]

-- 任务执行时间
redis.call("zrem", zset_key, tag)

-- 任务body
redis.call("hdel", hash_body_key, tag)

-- 任务重试次数
redis.call("hdel", hash_count_key, tag)