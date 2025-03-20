package ai.nvwa.agent.agents;

import ai.nvwa.agent.AbstractParallelAgent;
import ai.nvwa.model.chat.mode.ChatRequest;
import ai.nvwa.model.chat.mode.ChatResult;
import ai.nvwa.tool.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 架构师智能体
 * <p> <功能详细描述> </p>
 *
 * @author 陈晨
 */
@Component
@Slf4j
public class ArchitectAgent extends AbstractParallelAgent {

    @Override
    public String action() {
        return "architectAgent";
    }

    @Override
    public String name() {
        return "架构师智能体";
    }

    @Override
    public String desc() {
        return "专业的系统架构师，精通企业级应用架构";
    }

    @Override
    public List<Function> association() {
        List<Function> extensions = new ArrayList<>();
        extensions.add(functionMap.get("addressExtension"));
        extensions.add(functionMap.get("weatherExtension"));
        return extensions;
    }

    @Override
    protected void assistantBefore(int loop, ChatRequest request) {

    }

    @Override
    protected void assistantAfter(int loop, ChatResult result) {

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


