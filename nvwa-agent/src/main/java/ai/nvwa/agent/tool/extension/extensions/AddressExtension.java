package ai.nvwa.agent.tool.extension.extensions;

import ai.nvwa.agent.components.util.HttpClient;
import ai.nvwa.agent.tool.extension.Extension;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Component;

/**
 * 扩展: IP定位
 * <p>
 *     百度定位（免费）: https://qifu.baidu.com/
 *     腾讯定位（经纬度）: https://lbs.qq.com/dev/console/home
 * </p>
 *
 * @author 陈晨
 */
// name = action + Extension
@Component("addressExtension")
public class AddressExtension implements Extension {

    @Override
    public String action() {
        return "address";
    }

    @Override
    public String name() {
        return "IP定位";
    }

    @Override
    public String desc() {
        return "通过IP地址定位所在位置";
        // 通过IP地址定位所在位置及经纬度
    }

    @Override
    public JSONObject inputFormat() {
        JSONObject input = new JSONObject();
        input.put("ip", "XXX.XXX.XXX.XXX");
        return input;
    }

    @Override
    public String call(JSONObject input) {
        String url = "https://qifu.baidu.com/ip/geo/v1/district?ip={ip}"
                .replaceFirst("\\{ip\\}", (String) input.get("ip"));
        return HttpClient.get(url).asString();

//        String url = "https://apis.map.qq.com/ws/location/v1/ip?key={key}&ip={ip}"
//                .replaceFirst("\\{key\\}", TENCENT_KEY)
//                .replaceFirst("\\{ip\\}", (String) request.get("ip"));
//        return HttpClient.get(url).asString();
    }

}


