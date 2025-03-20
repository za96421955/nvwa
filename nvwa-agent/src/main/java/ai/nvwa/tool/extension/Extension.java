package ai.nvwa.tool.extension;

import ai.nvwa.tool.function.Function;
import com.alibaba.fastjson.JSONObject;

/**
 * 扩展
 * <p> <功能详细描述> </p>
 *
 * @author 陈晨
 */
public interface Extension extends Function {

    /**
     * @description API调用
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    String call(JSONObject input);

}


