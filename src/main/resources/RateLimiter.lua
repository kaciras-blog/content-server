---
--- 调用方式：EVALSHA [sha] 1 [key] 所需令牌 当前时间(时间) 桶容量 速率(令牌/时间) 键超时(秒)
--- 时间的单位由客户端决定，不同的单位将导致精度不同。所有参数都是必需的不能省略。
--- 返回：0 表示通过，大于 0 表示需要等待的时间。如果速率为0，可能返回负数(Long.MIN_VALUE)
---
--- 为啥需要传当前时间？因为Redis为了安全禁止访问os模块，也就无法在脚本内获取时间；并且由
--- 客户端控制时间的话就能对时间做Mock，测试更方便
---
--- 如果所需令牌数大于桶容量，则返回值无意义，这留给客户端去检查
---
--- Redis 对 Lua 脚本的说明见：https://redis.io/commands/eval
--- 经测试，该脚本执行速度非常快，详情见 test/java/resources/RateLimiterBenchmark.txt
---
local requirement = tonumber(ARGV[1])
local now = tonumber(ARGV[2])
local bucketSize = tonumber(ARGV[3])
local rate = tonumber(ARGV[4])

--- redis.call() 与 redis.pcall() 的区别是 call 抛异常，而 pcall 返回错误信息
local data = redis.call("HMGET", KEYS[1], "time", "permits")
local lastAcquire = data[1]
local currPermits = data[2]

--- Lua 中 nli 是 falsy 的，可以直接用 if 判断
if not lastAcquire then

	--- Redis里没有记录过，直接使用参数作为当前值。
	lastAcquire = now
	currPermits = bucketSize
else

	--- 当前令牌（令牌）= 上次剩余（令牌）+（当前时间(时间)- 上次获取时间(时间)）* 添加速率（令牌/时间）
	--- 注意不能超出桶的容量
	currPermits = math.min(bucketSize, currPermits + (now - lastAcquire) * rate)
end

local timeToWait = 0

--- 如果所需令牌小于当前令牌则成功，保存剩余令牌和这次获取的时间，否则失败返回需要等待的时间
if requirement <= currPermits then
	redis.call("HMSET", KEYS[1], "time", now, "permits", currPermits - requirement)
else
	--- 需要等待的时间（时间）= （所需令牌（令牌）- 当前令牌（令牌））/ 速率（令牌/时间）
	--- 注意：Lua 的浮点数直接返回会被 Redis 截断成整数，所以这里要向上取整
	timeToWait = math.ceil((requirement - currPermits) / rate)
end

--- 刷新纪录的过期时间，此时Redis里一定存在该KEY的记录
redis.call("EXPIRE", KEYS[1], tonumber(ARGV[5]))
return timeToWait
