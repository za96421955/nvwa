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
 * 天气终端智能体
 * <p> <功能详细描述> </p>
 *
 * @author 陈晨
 */
@Component
@Slf4j
public class WeatherClientAgent extends AbstractSerialAgent {

    @Override
    public String action() {
        return "weatherClientAgent";
    }

    @Override
    public String name() {
        return "天气终端智能体";
    }

    @Override
    public String desc() {
        return "通过IP获取可查询当地实况天气的kwargs";
    }

    @Override
    public int chatMax() {
        return 5;
    }

    @Override
    public List<Function> association() {
        List<Function> extensions = new ArrayList<>();
        extensions.add(functionMap.get("addressExtension"));
        extensions.add(functionMap.get("clientWeatherFunction"));
        return extensions;
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


