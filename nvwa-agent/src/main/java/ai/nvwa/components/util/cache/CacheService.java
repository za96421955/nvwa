package ai.nvwa.components.util.cache;

import ai.nvwa.components.Result;

/**
 * 缓存
 * <p> <功能详细描述> </p>
 *
 * @author 陈晨
 */
public interface CacheService<T> {

    /**
     * @description 添加缓存
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    void put(String key, T obj);

    /**
     * @description 获取缓存
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    Result<T> get(String key);

}


