package ai.nvwa.agent.tool.extension;

import com.alibaba.fastjson.JSONObject;

/**
 * 扩展
 * <p> <功能详细描述> </p>
 *
 * @author 陈晨
 */
public interface Extension {

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

    /**
     * @description API调用
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    String call(JSONObject input);

//    /**
//     * @description API调用
//     * <p> <功能详细描述> </p>
//     *
//     * @author 陈晨
//     */
//    interface API {
//        String call(JSONObject request);
//    }

}


