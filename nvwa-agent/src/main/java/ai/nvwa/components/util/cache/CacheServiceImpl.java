package ai.nvwa.components.util.cache;

import ai.nvwa.components.Result;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

/**
 * 缓存
 * <p> <功能详细描述> </p>
 *
 * @author 陈晨
 */
@Service
@Slf4j
public class CacheServiceImpl<T> implements CacheService<T> {

    /**
     * 缓存超时时间
     * 单位: 分钟
     * 默认: 10
     */
    @Value("${nvwa.cache.over-time:10}")
    private int overTime;
    /**
     * 最大缓存数量
     * 默认: 1000
     */
    @Value("${nvwa.cache.max-count:1000}")
    private int macCount = 1000;

    private LoadingCache<String, Result<T>> localCache;
    @PostConstruct
    public void init() {
        localCache = CacheBuilder.newBuilder()
            .refreshAfterWrite(overTime, TimeUnit.MINUTES)
            .maximumSize(macCount)
            .build(new CacheLoader<>() {
                @Override
                public Result<T> load(String key) {
                    return null;
                }
            });
    }

    @Override
    public void put(String key, T obj) {
        localCache.put(key, Result.success(obj));
    }

    @Override
    public Result<T> get(String key) {
        try {
            return localCache.get(key);
        } catch (Exception e) {
            log.error("[缓存查询] key={}, 缓存信息获取异常, {}", key, e.getMessage(), e);
            return Result.fail("缓存信息获取异常: " + e.getMessage());
        }
    }

}


