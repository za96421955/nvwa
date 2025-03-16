package ai.nvwa.agent.tool.extension.extensions;

import ai.nvwa.agent.components.util.HttpClient;
import ai.nvwa.agent.tool.extension.Extension;
import ai.nvwa.agent.tool.extension.mode.District;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 扩展: 获取行政区划编码
 * <p> <功能详细描述> </p>
 *
 * @author 陈晨
 */
@Component("districtExtension")
public class DistrictExtension implements Extension {
    public static final String TENCENT_KEY = "6R4BZ-MGICU-V6JVV-GN6KJ-QHVU2-BHF3J";

    @Override
    public String action() {
        return "districtExtension";
    }

    @Override
    public String name() {
        return "行政区划";
    }

    @Override
    public String desc() {
        return "输入地址信息通过行政区划列表获取行政区划编码";
    }

    @Override
    public JSONObject inputFormat() {
        JSONObject input = new JSONObject();
        input.put("district", "XXXXXX");
        return input;
    }

    @Override
    public String call(JSONObject input) {
        String url = "https://apis.map.qq.com/ws/district/v1/list?key={key}"
                .replaceFirst("\\{key\\}", TENCENT_KEY);
        JSONObject result = JSONObject.parseObject(HttpClient.get(url).asString());
        List<District> districtList = new ArrayList<>();
        for (int i = 0; i < result.getJSONArray("result").size(); i++) {
            JSONArray areas = result.getJSONArray("result").getJSONArray(i);
            for (int j = 0; j < areas.size(); j++) {
                JSONObject area = areas.getJSONObject(j);
                JSONObject location = area.getJSONObject("location");
                districtList.add(District.builder()
                        .id((String) area.get("id"))
                        .name((String) area.get("name"))
                        .fullName((String) area.get("fullname"))
                        .lat(location.get("lat") + "")
                        .lng(location.get("lng") + "")
                        .build());
            }
        }
        String districtReq = (String) input.get("district");
        // 全匹配
        for (District district : districtList) {
            if (district.getFullName().equals(districtReq)) {
                return district.getId();
            }
        }
        // 模糊匹配
        for (District district : districtList) {
            if (district.getFullName().indexOf(districtReq) > 0) {
                return district.getId();
            }
        }
        // 反向模糊匹配
        for (int i = districtList.size() - 1; i >= 0; i--) {
            if (districtReq.indexOf(districtList.get(i).getFullName()) > 0) {
                return districtList.get(i).getId();
            }
        }
        return null;
    }

}


