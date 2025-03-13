package ai.nvwa.agent.components.redis;

import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface RedisService {

    /** 超时时间, 1分钟 */
    long EXPIRE_1_MIN = 60;
    /** 默认超时时间, 5分钟 */
    long EXPIRE_DEFAULT = EXPIRE_1_MIN * 5;
    /** 超时时间, 3分钟 */
    long EXPIRE_3_MIN = EXPIRE_1_MIN * 3;
    /** 超时时间, 10分钟 */
    long EXPIRE_10_MIN = EXPIRE_1_MIN * 10;
    /** 超时时间, 30分钟 */
    long EXPIRE_30_MIN = EXPIRE_1_MIN * 30;
    /** 超时时间, 1小时 */
    long EXPIRE_1_HOUR = EXPIRE_1_MIN * 60;
    /** 超时时间, 1天 */
    long EXPIRE_1_DAY = EXPIRE_1_HOUR * 24;
    /** 超时时间, 1周 */
    long EXPIRE_1_WEEK = EXPIRE_1_DAY * 7;
    /** 超时时间, 1月 */
    long EXPIRE_1_MONTH = EXPIRE_1_DAY * 30;

    /** 波动时间5秒 */
    long FLUCTUATE_5_S = 5;
    /** 波动时间30秒 */
    long FLUCTUATE_30_S = 30;
    /** 波动时间60秒 */
    long FLUCTUATE_60_S = 60;

    RedisTemplate getRedisTemplate();

    /**
     * @description 超时时间波动, 固定值
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     * @date 2025/1/6 16:27
     */
    long fluctuate(long seconds, long fluctuateSeconds);

    /**
     * @description 超时时间波动, 10%
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     * @date 2025/1/6 16:27
     */
    long fluctuatePercent10(long seconds);

    /**
     * @description 超时时间波动, 30%
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     * @date 2025/1/6 16:27
     */
    long fluctuatePercent30(long seconds);

    /**
     * @Title: expire
     * @Description: 设置过期时间
     * @param key
     * @param seconds
     * @return Long
     * @throws
     */
    boolean expire(final String key, final long seconds);

    /**
     * @Title: set
     * @Description: 设置缓存值O
     * @param key
     * @param value
     * @return String
     * @throws
     */
    String set(final String key, final String value);

    /**
     * @Title: set
     * @Description: 设置string缓存值,设置过期时间
     * @param key
     * @param value
     * @param seconds
     * @return String
     * @throws
     */
    String set(final String key, final String value, final long seconds);

    String setFluctuatePercent10(final String key, final String value, final long seconds);

    /**
     * @Title: get
     * @Description: 根据健获取值
     * @param key
     * @return String
     * @throws
     */
    String get(final String key);

    /**
     * @Title: setMap
     * @Description: 设置map数据
     * @param key
     * @param value
     * @return String
     * @throws
     */
    void setMap(final String key, final Map<String, String> value);

    /**
     * @Title: setMap
     * @Description: 设置map缓存值,设置过期时间【非原子、建议优先SetKey】
     * @param key
     * @param value
     * @param seconds
     * @return String
     * @throws
     */
    void setMap(final String key, final Map<String, String> value, final long seconds);

    /**
     * 功能描述: <br>
     * 根据key,prop值获取map数据
     *
     * @param key
     * @param props
     * @return
     */
    List<String> getMap(String key, final String... props);

    /**
     * @Title: getMap
     * @Description: 根据key值获取map数据
     * @param key
     * @return Map<String,String>
     * @throws
     */
    Map<String, String> getMap(final String key);

    /**
     * 功能描述: <br>
     * 设置对象
     *
     * @param key
     * @param value
     * @return
     */
    <T extends Object> T setObject(final String key, final T value);

    /**
     * 功能描述: <br>
     * 设置对象 失效时间
     *
     * @param key
     * @param value
     * @param seconds
     * @return
     */
    <T extends Object> T setObject(final String key, final T value, final long seconds);

    /**
     * 功能描述: <br>
     * 获取对象
     *
     * @param key
     * @return
     */
    <T extends Object> T getObject(final String key, final Class<T> clazz);

    /**
     * @param <T>
     * @Title: setList
     * @Description: 将列表转化为json字符串存入数据库
     * @param value
     * @param key
     * @return String
     * @throws
     */
    <T> List<T> setList(final String key, final List<T> value);

    /**
     * @param <T>
     * @Title: setList
     * @Description: 设置list缓存值, 设置过期时间【非原子、建议优先SetKey】
     * @param key
     * @param value
     * @param seconds
     * @return String
     * @throws
     */
    <T> List<T> setList(final String key, final List<T> value, final long seconds);

    /**
     * @param <T>
     * @Title: getList
     * @Description: 根据key获取list数据
     * @param key
     * @return List<Map<String,Object>>
     * @throws
     */
    <T> List<T> getList(final String key);

    /**
     * @Title: del
     * @Description: 根据key删除值
     * @param key
     * @return Long
     * @throws
     */
    Boolean del(final String key);

    /**
     * 功能描述: <br>
     * 删除map中其中一个键值对
     *
     * @param key
     * @param props
     * @return
     */
    Long hdel(final String key, final String props);

    /**
     * 功能描述: <br>
     * 根据key获取列表长度
     *
     * @param key
     * @return
     */
    Long llen(final String key);

    /**
     * 功能描述: <br>
     * 头部压入列表
     *
     * @return
     */
    <T extends Object> Long lpush(final String key, final T value);

    /**
     * 功能描述: <br>
     * 尾部压入列表
     *
     * @param key
     * @param value
     * @return
     */
    <T extends Object> Long rpush(final String key, final T value);

    /**
     * 功能描述: <br>
     * 尾部取出列表
     *
     * @param key
     * @return
     */
    <T extends Object> T rpop(final String key, final Class<T> clazz);

    /**
     * 功能描述: <br>
     * 将一个元素放入集合
     *
     * @param key
     * @return
     * @see [相关类/方法](可选)
     * @since [产品/模块版本](可选)
     */
    Long sadd(final String key, final String value);

    /**
     * 功能描述: <br>
     * 批量返回缓存队列的值
     *
     * @param key
     * @param start
     * @param end
     * @return
     * @see [相关类/方法](可选)
     * @since [产品/模块版本](可选)
     */
    <T> List<T> lrange(final String key, final long start, final long end);

    /**
     * 功能描述: <br>
     * 批量删除缓存队列的值
     *
     * @param key
     * @param start
     * @param end
     * @return
     * @see [相关类/方法](可选)
     * @since [产品/模块版本](可选)
     */
    void ltrim(final String key, final long start, final long end);

    /**
     * 功能描述: <br>
     * 获取HashMap中指定字段的值
     *
     * @param key
     * @param field
     * @return
     * @see [相关类/方法](可选)
     * @since [产品/模块版本](可选)
     */
    String hget(final String key, final String field);

    /**
     * 功能描述: <br>
     * 设置HashMap中指定字段的值
     *
     * @param key
     * @param field
     * @param value
     * @return
     * @see [相关类/方法](可选)
     * @since [产品/模块版本](可选)
     */
    void hset(final String key, final String field, final String value);

    /**
     * 功能描述: <br>
     * 返回集合中的所有元素
     *
     * @param key
     * @return
     * @see [相关类/方法](可选)
     * @since [产品/模块版本](可选)
     */
    Set<String> smembers(final String key);

    /**
     * 检查给定 key 是否存在。
     * @param key
     * @return
     */
    boolean exists(final String key);

    /**
     * 查看哈希表 key 中，给定域 field 是否存在
     * @param key
     * @param field
     * @return
     */
    boolean hexists(final String key, final String field);

    /**
     * 返回哈希表 key 中域的数量
     * @param key
     * @return
     */
    Long hlen(final String key);

    /**
     * 返回列表 key 中，下标为 index 的元素
     * @param key
     * @param index
     * @return
     */
    String lindex(final String key, final Long index);

    /**
     * 锁过期时间与锁等待时间相等（ 秒）
     * redis 分布式锁
     * @param key
     * @param waitTimeOut 获取锁等待时间（毫秒）
     * @return
     */
    boolean lock(String key, long waitTimeOut);

    /**
     *
     * 功能描述: <br>
     * 〈功能详细描述〉
     *
     * @param key
     * @param waitTimeOut 获取锁等待时间（毫秒）
     * @param expire  锁过期时间（秒）
     * @return
     * @see [相关类/方法](可选)
     * @since [产品/模块版本](可选)
     */
    boolean lock(String key, long waitTimeOut, int expire);

    /**
     * 释放锁
     * @param key
     */
    boolean unLock(String key);

    /**
     *根据索引查询list
     * @param factory
     * @param start
     * @param end
     * @return
     */
    <T> List<T> getList(final String factory, int start, int end);


    /**
     *删除名称为key的list中的首元素 返回并
     * @param key
     * @return
     */
    String lpop(final String key);

    /**
     * 设置初始化值
     * @param key
     * @param value
     * @return
     */
    Long incrBy(String key, long value);

    /**
     * 获取流水号
     * @param key
     * @return
     */
    Long incr(String key);

    /**
     * @description 滑动窗口计数器限流
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     * @date 2025/2/11 19:38
     */
    boolean limit(String key, int limit);

}


