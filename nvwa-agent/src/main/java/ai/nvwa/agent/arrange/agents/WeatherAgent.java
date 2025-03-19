package ai.nvwa.agent.arrange.agents;

import ai.nvwa.agent.arrange.AbstractAgent;
import ai.nvwa.agent.model.chat.mode.ChatRequest;
import ai.nvwa.agent.model.chat.mode.ChatResult;
import ai.nvwa.agent.prompt.ExtensionPrompt;
import ai.nvwa.agent.tool.function.Function;
import lombok.extern.slf4j.Slf4j;
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
        return "weatherAgent";
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
        extensions.add(functionMap.get("addressExtension"));
//        extensions.add(functionMap.get("districtExtension"));
        extensions.add(functionMap.get("weatherExtension"));
//        extensions.add(functionMap.get("clientWeatherFunction"));
        return extensions;
    }

    @Override
    public String prompt() {
        return ExtensionPrompt.DEFAULT;
    }

    @Override
    public String datastore(String question) {
        // 江苏省南京市建邺区。当地天气晴朗，气温12°C，北风5-6级，湿度21%。
        return super.datastore(question);
    }

    @Override
    protected void assistantBefore(int loop, ChatRequest request) {

    }

    @Override
    protected void assistantAfter(int loop, ChatResult result) {
        System.out.println("\n\n>>> 第[" +loop + "]轮");
        System.out.println("\n请求：");
        System.out.println("模型：" + result.getRequest().getModel());
        for (ChatRequest.ChatMessage message : result.getRequest().getMessages()) {
            System.out.println("【" + message.getRole() + "】：" + message.getContent());
        }
        System.out.println("\n思考：\n" + result.getReasoning());
        System.out.println("\n回答：\n" + result.getContent());
        System.out.println("\nToken：\n" + result.getUsage());
        System.out.println("\n工具：\n" + result.getAction());
        System.out.println("\n错误信息：\n" + result.getErrorMsg());
        System.out.println("\n\n");
    }

    @Override
    protected void termination(Map<Integer, ChatResult> chatResultMap) {
        for (Map.Entry<Integer, ChatResult> chatResult : chatResultMap.entrySet()) {
            System.out.println("\n\n>>> 第[" + chatResult.getKey() + "]轮");
            System.out.println("\n请求：");
            System.out.println("模型：" + chatResult.getValue().getRequest().getModel());
            for (ChatRequest.ChatMessage message : chatResult.getValue().getRequest().getMessages()) {
                System.out.println("【" + message.getRole() + "】：" + message.getContent());
            }
            System.out.println("\n思考：\n" + chatResult.getValue().getReasoning());
            System.out.println("\n回答：\n" + chatResult.getValue().getContent());
            System.out.println("\nToken：\n" + chatResult.getValue().getUsage());
            System.out.println("\n工具：\n" + chatResult.getValue().getAction());
            System.out.println("\n错误信息：\n" + chatResult.getValue().getErrorMsg());
        }
    }

}


