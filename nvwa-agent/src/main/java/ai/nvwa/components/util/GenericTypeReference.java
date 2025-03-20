package ai.nvwa.components.util;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * 构造类型
 * <p> <功能详细描述> </p>
 *
 * @author 陈晨
 * @version 1.0
 */
@Slf4j
public class GenericTypeReference<T> {
    private final Class<T> type;

    @SuppressWarnings("unchecked")
    public GenericTypeReference() {
        Type superClass = getClass().getGenericSuperclass();
        if (superClass instanceof ParameterizedType) {
            this.type = (Class<T>) ((ParameterizedType) superClass).getActualTypeArguments()[0];
        } else {
            throw new RuntimeException("Cannot get parameterized type.");
        }
    }

    public T newInstance() {
        try {
            // 获取无参构造器
            Constructor<T> constructor = type.getDeclaredConstructor();
            // 如果构造器不是public的，则设置可访问
            if (!constructor.isAccessible()) {
                constructor.setAccessible(true);
            }
            // 创建实例
            return constructor.newInstance();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    public static<T> T instance() {
        return new GenericTypeReference<T>().newInstance();
    }
    
}


