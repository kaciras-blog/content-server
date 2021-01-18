---
--- 更新 HASH 的值，如果值不存在则无动作，不会创建新值。
---
--- 调用方式：EVALSHA (sha1) 1 (key) hash-key value
---
if redis.call('HEXISTS', KEYS[1], ARGV[1]) == 0 then
	return 0;
else
	redis.call('HSET', KEYS[1], ARGV[1], ARGV[2])
	return 1
end
