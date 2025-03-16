package ai.nvwa.agent.tool.function;

import com.alibaba.fastjson.JSONObject;

/**
 * 函数
 * <p> <功能详细描述> </p>
 *
 * @author 陈晨
 */
public interface Function {

    /**
     * @description 执行名 (方法名)
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    String action();

    /**
     * @description 名称
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    String name();

    /**
     * @description 简介
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    String desc();

    /**
     * @description 输入参数格式
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    JSONObject inputFormat();

}


