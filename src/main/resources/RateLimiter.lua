---
--- EVALSHA [sha] 1 [IP] 所需令牌 当前时间(秒) 桶容量 速率(令牌/秒) 键超时(秒)
--- 所有参数都是必需的不能省略
--- 返回：0 - 通过，大于0表示需要等待的时间
---
--- 为啥需要传当前时间？因为Redis为了安全禁止访问os模块，也就无法在脚本内获取时间；并且由客户端控制时间
--- 的话就能对时间做Mock，测试更方便
---
--- 如果所需令牌数大于桶容量，则返回值无意义，这留给客户端自己检查
---
--- 该脚本每秒能执行4万次，详情见 RateLimiterBenchmark.txt
---
local required = tonumber(ARGV[1])
local now = tonumber(ARGV[2])
local maximum = tonumber(ARGV[3])
local rate = tonumber(ARGV[4])

local data = redis.pcall("HMGET", KEYS[1], "time", "permits")
local lastAcquire = data[1]
local currPermits = data[2]

--- 没有设置过，需要初始化。Lua中 false 和 nli 是falsy的
if not lastAcquire then
    currPermits = maximum
    lastAcquire = now
end

local timeToWait = 0
currPermits = math.min(currPermits, currPermits + (now - lastAcquire) * rate)

--- redis.call() 与 redis.pcall() 的区别是 call 抛异常，而 pcall 返回错误信息
if required <= currPermits then
    redis.call("HMSET", KEYS[1], "time", now, "permits", math.floor(currPermits - required))
else
    timeToWait = math.ceil((required - currPermits) / rate)
end

--- 运行到此处时，Redis里一定存在该IP的记录
redis.call("EXPIRE", KEYS[1], tonumber(ARGV[5]))
return timeToWait
