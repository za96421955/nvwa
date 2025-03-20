package ai.nvwa.components.util.runtime;

import java.util.Optional;
import java.util.function.Function;

public interface Context {

    /**
     * 存储数据
     */
    void put(String name, Object obj);

    /**
     * 获取指定类型暂存数据
     */
    <T> Optional<T> get(String name, Class<T> type);

    /**
     * 当前值为空则从function接口中获取
     */
    <T> T get(String name, Class<T> type, Function<String,T> function);

    /**
     * @description get
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    <T> T get(String name);

    /**
     * 移除数据
     */
    void remove(String name);

    /**
     * @description 清除
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    void clear();

}


