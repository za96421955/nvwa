package ai.nvwa.agent.components.redis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
@Slf4j
public class RedisServiceImpl implements RedisService {
    // 加锁状态标记
    public final static String LOCKED = "TRUE";
    // nan秒基础值
    public static final long ONE_MILLI_NANOS = 1000000L;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public RedisTemplate getRedisTemplate() {
        return redisTemplate;
    }

    @Override
    public long fluctuate(long seconds, long fluctuateSeconds) {
        long abs = Math.abs(fluctuateSeconds);
        if (abs > seconds) {
            return seconds;
        }
        long fluctuate = (long) (Math.random() * (abs * 2 + 1)) - abs;
        return seconds + fluctuate;
    }

    @Override
    public long fluctuatePercent10(long seconds) {
        return this.fluctuate(seconds, (long) (seconds * 0.1));
    }

    @Override
    public long fluctuatePercent30(long seconds) {
        return this.fluctuate(seconds, (long) (seconds * 0.3));
    }

    @Override
    public boolean expire(String key, long seconds) {
        return redisTemplate.expire(key, seconds, TimeUnit.SECONDS);
    }

    @Override
    public String set(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
        return value;
    }

    @Override
    public String set(String key, String value, long seconds) {
        redisTemplate.opsForValue().set(key, value, seconds, TimeUnit.SECONDS);
        return value;
    }

    @Override
    public String setFluctuatePercent10(String key, String value, long seconds) {
        return this.set(key, value, this.fluctuatePercent10(seconds));
    }

    @Override
    public String get(String key) {
        return (String) redisTemplate.opsForValue().get(key);
    }

    @Override
    public void setMap(String key, Map<String, String> value) {
        redisTemplate.boundHashOps(key).putAll(value);
    }

    @Override
    public void setMap(String key, Map<String, String> value, long seconds) {
        redisTemplate.boundHashOps(key).putAll(value);
        redisTemplate.expire(key, seconds, TimeUnit.SECONDS);
    }

    @Override
    public List<String> getMap(String key, String... props) {
        return redisTemplate.boundHashOps(key).multiGet(Arrays.stream(props).collect(Collectors.toList()));
    }

    @Override
    public Map<String, String> getMap(String key) {
        return redisTemplate.boundHashOps(key).entries();
    }

    @Override
    public <T> T setObject(String key, T value) {
        redisTemplate.opsForValue().set(key, value);
        return value;
    }

    @Override
    public <T> T setObject(String key, T value, long seconds) {
        redisTemplate.opsForValue().set(key, value, seconds, TimeUnit.SECONDS);
        return value;
    }

    @Override
    public <T> T getObject(String key, Class<T> clazz) {
        return (T) redisTemplate.opsForValue().get(key);
    }

    @Override
    public <T> List<T> setList(String key, List<T> value) {
        //清空
        if (redisTemplate.opsForList().size(key)>0) {
            redisTemplate.opsForList().leftPop(key);
        }
        redisTemplate.opsForList().rightPushAll(key,value);
        return value;
    }

    @Override
    public <T> List<T> setList(String key, List<T> value, long seconds) {
        //清空
        if(redisTemplate.opsForList().size(key)>0) {
            redisTemplate.opsForList().leftPop(key);
        }
        redisTemplate.opsForList().rightPushAll(key, value);
        redisTemplate.expire(key,seconds,TimeUnit.SECONDS);
        return value;
    }

    @Override
    public <T> List<T> getList(String key) {
        return redisTemplate.opsForList().range(key,0,-1);
    }

    @Override
    public Boolean del(String key) {
        return redisTemplate.delete(key);
    }

    @Override
    public Long hdel(String key, String props) {
        return redisTemplate.opsForHash().delete(key, props);
    }

    @Override
    public Long llen(String key) {
        return redisTemplate.opsForList().size(key);
    }

    @Override
    public <T> Long lpush(String key, T value) {
        return redisTemplate.opsForList().leftPush(key,value);
    }

    @Override
    public <T> Long rpush(String key, T value) {
        return redisTemplate.opsForList().rightPush(key,value);
    }

    @Override
    public <T> T rpop(String key, Class<T> clazz) {
        return (T) redisTemplate.opsForList().rightPop(key);
    }

    @Override
    public Long sadd(String key, String value) {
        return redisTemplate.opsForList().rightPush(key,value);
    }

    @Override
    public <T> List<T> lrange(String key, long start, long end) {
        return redisTemplate.opsForList().range(key,start,end);
    }

    @Override
    public void ltrim(String key, long start, long end) {
        redisTemplate.opsForList().trim(key,start,end);
    }

    @Override
    public String hget(String key, String field) {
        return (String) redisTemplate.opsForHash().get(key, field);
    }

    @Override
    public void hset(String key, String field, String value) {
        redisTemplate.opsForHash().put(key,field,value);
    }

    @Override
    public Set<String> smembers(String key) {
        return (Set<String>) redisTemplate.opsForSet().pop(key);
    }

    @Override
    public boolean exists(String key) {
        return redisTemplate.hasKey(key);
    }

    @Override
    public boolean hexists(String key, String field) {
        return redisTemplate.opsForHash().hasKey(key,field);
    }

    @Override
    public Long hlen(String key) {
        return redisTemplate.opsForHash().size(key);
    }

    @Override
    public String lindex(String key, Long index) {
        return (String) redisTemplate.opsForList().index(key,index);
    }

    @Override
    public boolean lock(String key, long waitTimeOut) {
        int expire = (int)(waitTimeOut + 1) / 1000;
        return lock(key, waitTimeOut, expire);
    }

    @Override
    public boolean lock(String key, long waitTimeOut, int seconds) {
        long nano = System.nanoTime();
        long tmOut = ONE_MILLI_NANOS * waitTimeOut;
        try{
            while ((System.nanoTime() - nano) < tmOut) {
                //存在则插入redis  不存在则插入
                if (seconds > 0) {
                    if (redisTemplate.opsForValue().setIfAbsent(key, LOCKED, seconds, TimeUnit.SECONDS)) {
                        return true;
                    }
                } else {
                    boolean result = redisTemplate.opsForValue().setIfAbsent(key, LOCKED);
                    if (result) {
                        return true;
                    }
                }
                //如果不等待，则直接返回
                if (waitTimeOut <= 0) {
                    return false;
                }
                // 短暂休眠，nano避免出现活锁
                Thread.sleep(new Random().nextInt(20));
            }
        } catch (InterruptedException e) {
            log.error("exception : " ,e);
            Thread.currentThread().interrupt();
        }
        return false;
    }

    @Override
    public boolean unLock(String key) {
        return redisTemplate.delete(key);
    }

    @Override
    public <T> List<T> getList(String factory, int start, int end) {
        return redisTemplate.opsForList().range(factory,start,end);
    }

    @Override
    public String lpop(String key) {
        return (String) redisTemplate.opsForList().leftPop(key);
    }

    @Override
    public Long incr(String key) {
        return redisTemplate.opsForValue().increment(key,1);
    }

    @Override
    public Long incrBy(String key, long value) {
        return redisTemplate.opsForValue().increment(key,value);
    }

    @Override
    public boolean limit(String key, int limit) {
        // 滑动窗口计数器限流
        long currentTime = System.currentTimeMillis();
        long windowStart = currentTime - 60 * 1000; // 一分钟窗口期
        // 移除窗口期之前的数据
        redisTemplate.opsForZSet().removeRangeByScore(key, 0, windowStart);
        // 统计当前窗口的请求数
        Long count = redisTemplate.opsForZSet().count(key, windowStart, currentTime);
        if (count != null && count >= limit) { // 限制每分钟100次
            return true; // 超过限流阈值
        }
        // 记录当前请求
        redisTemplate.opsForZSet().add(key, "1", currentTime);
        redisTemplate.expire(key, 120, TimeUnit.SECONDS); // 设置过期时间为2分钟
        return false;
    }

}


