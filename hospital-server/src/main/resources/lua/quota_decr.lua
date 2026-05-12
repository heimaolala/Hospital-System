local quotaKey = KEYS[1]
local recordKey = KEYS[2]
local available = tonumber(redis.call('GET', quotaKey) or '0')
local alreadyRegistered = redis.call('EXISTS', recordKey)

if alreadyRegistered == 1 then
    return -1
end

if available > 0 then
    redis.call('DECR', quotaKey)
    redis.call('SETEX', recordKey, 300, '1')
    return 1
else
    return 0
end
