package ai.nvwa.agent.agent.agents;

import ai.nvwa.agent.agent.AbstractSerialAgent;
import ai.nvwa.agent.model.chat.mode.ChatRequest;
import ai.nvwa.agent.model.chat.mode.ChatResult;
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
public class WeatherAgent extends AbstractSerialAgent {

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

    @Override
    public List<Function> association() {
        List<Function> extensions = new ArrayList<>();
        extensions.add(functionMap.get("addressExtension"));
//        extensions.add(functionMap.get("districtExtension"));
        extensions.add(functionMap.get("weatherExtension"));
        return extensions;
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
        System.out.println("\n工具：\n" + result.getActions());
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
            System.out.println("\n工具：\n" + chatResult.getValue().getActions());
            System.out.println("\n错误信息：\n" + chatResult.getValue().getErrorMsg());
        }
    }

}


