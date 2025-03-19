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
 * 百科智能体
 * <p> <功能详细描述> </p>
 *
 * @author 陈晨
 */
@Component
@Slf4j
public class BaikeAgent extends AbstractAgent {

    @Override
    public String action() {
        return "baikeAgent";
    }

    @Override
    public String name() {
        return "百度百科智能体";
    }

    @Override
    public String desc() {
        return "中国最大在线百科平台，专业审核与用户共创确保权威性，涵盖多领域知识，支持多媒体及20余种语言，日均访问量超亿次。";
    }

    @Override
    public int chatMax() {
        return 3;
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
        extensions.add(functionMap.get("baikeExtension"));
        return extensions;
    }

    @Override
    public String prompt() {
        return ExtensionPrompt.ACTION_ONLY;
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


