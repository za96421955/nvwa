package ai.nvwa.agent.arrange.agents;

import ai.nvwa.agent.arrange.AbstractAgent;
import ai.nvwa.agent.model.chat.ChatService;
import ai.nvwa.agent.model.chat.mode.AlibabaChatRequest;
import ai.nvwa.agent.model.chat.mode.ChatRequest;
import ai.nvwa.agent.model.chat.mode.ChatResponse;
import ai.nvwa.agent.model.chat.mode.ChatResult;
import ai.nvwa.agent.tool.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 天气智能体
 * <p> <功能详细描述> </p>
 *
 * @author 陈晨
 */
@Component
@Slf4j
public class WeatherAgent extends AbstractAgent {

    @Override
    public String action() {
        return "weather";
    }

    @Override
    public String name() {
        return "天气智能体";
    }

    @Override
    public String desc() {
        return "通过IP获取用户当地实况天气";
    }

    @Override
    public int chatMax() {
        return 5;
    }

    /**
     * @description 关联函数/扩展
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    @Override
    public List<Function> association() {
        List<Function> extensions = new ArrayList<>();
//        extensions.add(extensionMap.get("baikeExtension"));
        extensions.add(functionMap.get("addressExtension"));
        extensions.add(functionMap.get("districtExtension"));
//        extensions.add(functionMap.get("weatherExtension"));
        extensions.add(functionMap.get("clientWeatherFunction"));
        return extensions;
    }

    @Override
    protected void process(int loop, String reasoning, String content, ChatResponse.Usage usage) {
        if (StringUtils.isNotBlank(reasoning)) {
            System.out.println("思考中：" + reasoning);
        }
        if (StringUtils.isNotBlank(content)) {
            System.out.println("回答中：" + content);
        }
    }

    @Override
    protected void termination(Map<Integer, ChatResult> chatResultMap) {
        for (Map.Entry<Integer, ChatResult> chatResult : chatResultMap.entrySet()) {
            System.out.println("\n\n>>> 第[" + chatResult.getKey() + "]轮");
            System.out.println("\n请求：\n" + chatResult.getValue().getRequest());
            System.out.println("\n错误信息：\n" + chatResult.getValue().getErrorMsg());
            System.out.println("\n思考：\n" + chatResult.getValue().getReasoning());
            System.out.println("\n回答：\n" + chatResult.getValue().getContent());
            System.out.println("\n使用：\n" + chatResult.getValue().getUsage());
        }
    }

    public static void main(String[] args) {
//        WeatherAgent agent = new WeatherAgent();
//        String content = "Thought: I have the user's location information from the IP query. They are not in Beijing.\n" +
//                "Answer: 根据IP地址查询结果，您位于江苏省南京市建邺区，而不是北京。";
//        System.out.println("isPause: " + agent.isPause(content));
//        System.out.println("getAction: " + agent.getAction(content));
//        System.out.println("getRequest: " + agent.getRequest(content));
//        System.out.println("isAnswer: " + agent.isAnswer(content));
//        System.out.println("getAnswer: " + agent.getAnswer(content));

        ChatRequest request = AlibabaChatRequest.llama()
//                .user(agent.getPrompt())
                .user("我的IP是218.94.151.22")
                .user("我在哪？现在天气怎么样")
//                .assistant("Thought: 首先，我需要通过用户的IP地址定位他们所在的位置，然后查询该地区的天气信息。\n" +
//                        "Action: address\n" +
//                        "Action Input:\n" +
//                        "{\n" +
//                        "  \"ip\": \"218.94.151.22\"\n" +
//                        "}\n" +
//                        "\n" +
//                        "PAUSE")
//                .user("{\"code\":\"Success\",\"data\":{\"continent\":\"亚洲\",\"country\":\"中国\",\"zipcode\":\"210019\",\"owner\":\"中国电信\",\"isp\":\"中国电信\",\"adcode\":\"320105\",\"prov\":\"江苏省\",\"city\":\"南京市\",\"district\":\"建邺区\"},\"ip\":\"218.94.151.22\"}")
//                .assistant("现在，我需要查询南京市的天气情况。\n" +
//                        "\n" +
//                        "Action: weather\n" +
//                        "Action Input:\n" +
//                        "{\n" +
//                        "  \"district\": \"南京市\"\n" +
//                        "}")
//                .user("{\n" +
//                        "    \"status\": 0,\n" +
//                        "    \"message\": \"Success\",\n" +
//                        "    \"request_id\": \"d3c4d2e7f1ba476096c5c2a222f0b077\",\n" +
//                        "    \"result\": {\n" +
//                        "        \"realtime\": [\n" +
//                        "            {\n" +
//                        "                \"province\": \"江苏省\",\n" +
//                        "                \"city\": \"南京市\",\n" +
//                        "                \"district\": \"\",\n" +
//                        "                \"adcode\": 320100,\n" +
//                        "                \"update_time\": \"2025-03-16 17:15\",\n" +
//                        "                \"infos\": {\n" +
//                        "                    \"weather\": \"晴天\",\n" +
//                        "                    \"temperature\": 9,\n" +
//                        "                    \"wind_direction\": \"西北风\",\n" +
//                        "                    \"wind_power\": \"5-6级\",\n" +
//                        "                    \"humidity\": 18\n" +
//                        "                }\n" +
//                        "            }\n" +
//                        "        ]\n" +
//                        "    }\n" +
//                        "}")
//                .assistant("Thought: 现在我已经得到了南京市的天气信息，我可以将这一信息告知用户。\n" +
//                        "Answer: 你目前位于中国江苏省南京市建邺区。现在南京市的天气情况是：晴天，气温为9摄氏度，西北风5-6级，湿度18%。天气非常宜人，适合外出活动。")
                .stream();
        System.out.println("\n\n请求: ");
        System.out.println(request.toString());

        ChatService chat = new ChatService();
        ChatResult result = new ChatResult();
        chat.chat(request, (reasoning, content, usage) -> {
            if (StringUtils.isNotBlank(reasoning)) {
                result.getReasoning().append(reasoning);
                System.out.println("思考中：" + result.getReasoning());
            }
            if (StringUtils.isNotBlank(content)) {
                result.getContent().append(content);
                System.out.println("回答：" + result.getContent());
            }
            if (null != usage) {
                result.setUsage(usage);
            }
        });
        System.out.println("\n\n响应: ");
        System.out.println("\n\n思考：\n" + result.getReasoning().toString());
        System.out.println("\n\n回答：\n" + result.getContent().toString());
        System.out.println("\n\n使用：\n" + result.getUsage());
    }

}


