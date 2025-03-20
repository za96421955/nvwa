package ai.nvwa.components;

/**
 * 错误接口
 * <p> <功能详细描述> </p>
 *
 * @author 陈晨
 * @version 1.0
 */
public interface BaseError {

    /**
     * @description 获取错误编码
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    int getCode();

    /**
     * @description 获取错误信息
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    String getMessage();

    /**
     * @description 格式化错误信息
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    String format(Object... args);

    /**
     * @description 编码获取错误信息
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    BaseError getByCode(Integer code);

}


