package ai.nvwa.agent.tool.function.functions;

import ai.nvwa.agent.tool.function.Function;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Component;

/**
 * 扩展: 获取天气实况
 * <p> <功能详细描述> </p>
 *
 * @author 陈晨
 */
@Component
public class ClientWeatherFunction implements Function {

    @Override
    public String action() {
        return "clientWeatherFunction";
    }

    @Override
    public String name() {
        return "天气实况";
    }

    @Override
    public String desc() {
//        return "查询实况天气,提供客户端调用API使用";
        return "通过行政区划编码查询当地实况天气";
    }

    @Override
    public JSONObject inputFormat() {
        JSONObject input = new JSONObject();
        input.put("adcode", "XXXXXX");
        return input;
    }

}


