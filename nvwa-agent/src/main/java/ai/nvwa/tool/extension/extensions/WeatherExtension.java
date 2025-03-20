package ai.nvwa.tool.extension.extensions;

import ai.nvwa.components.util.HttpClient;
import ai.nvwa.tool.extension.Extension;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Component;

/**
 * 扩展: 获取天气实况
 * <p> <功能详细描述> </p>
 *
 * @author 陈晨
 */
@Component("weatherExtension")
public class WeatherExtension implements Extension {

    public static final String TENCENT_KEY = "6R4BZ-MGICU-V6JVV-GN6KJ-QHVU2-BHF3J";

    @Override
    public String action() {
        return "weatherExtension";
    }

    @Override
    public String name() {
        return "天气实况";
    }

    @Override
    public String desc() {
        return "通过行政区划编码查询当地实况天气";
    }

    @Override
    public JSONObject inputFormat() {
        JSONObject input = new JSONObject();
        input.put("adcode", "XXXXXX");
        return input;
    }

    @Override
    public String call(JSONObject input) {
        String url = "https://apis.map.qq.com/ws/weather/v1/?key={key}&adcode={adcode}"
                .replaceFirst("\\{key\\}", TENCENT_KEY)
                .replaceFirst("\\{adcode\\}", (String) input.get("adcode"));
        return HttpClient.get(url).asString();
    }

}


