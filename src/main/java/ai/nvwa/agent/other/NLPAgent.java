package ai.nvwa.agent.other;

import ai.nvwa.agent.NvwaAgent;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.checkpoint.BaseCheckpointSaver;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.deepseek.DeepSeekChatOptions;
import org.springframework.ai.deepseek.api.DeepSeekApi;

/**
 * @description 意图分析智能体
 * <p> <功能详细描述> </p>
 *
 * @author 陈晨
 * @date 2026/2/28 18:04
 */
public class NLPAgent implements NvwaAgent {

    private ReactAgent agent;

    @Override
    public String getName() {
        return "nlp_agent";
    }

    @Override
    public String getModel() {
        return System.getenv("AI_MODEL");
    }

    @Override
    public String getPrompt() {
        return "## 任务\n" +
                "根据提供的**输入内容**和**目标意图**，判断前者是否满足后者。\n" +
                "\n" +
                "## 输入格式\n" +
                "你的输入将包含两个明确部分：\n" +
                "1.  **输入内容**：一段需要被分析的文本。\n" +
                "2.  **目标意图**：一个希望被判断的意图描述。\n" +
                "\n" +
                "## 输出规则\n" +
                "-   请严格基于**输入内容**的语义进行分析，判断其是否匹配**目标意图**。\n" +
                "-   **如果匹配**，则输出：`是`\n" +
                "-   **如果不匹配**，则输出：`否`\n" +
                "-   你的响应**必须且只能**是单个词语“是”或“否”，**严禁**添加任何其他文字、标点、解释或上下文。\n" +
                "\n" +
                "## 示例\n" +
                "-   输入内容：我想预定明天的机票。目标意图：预定机票。\n" +
                "    输出：`是`\n" +
                "-   输入内容：今天天气很好。目标意图：查询天气。\n" +
                "    输出：`否`";
    }

    @Override
    public ChatModel getChatModel() {
        return DeepSeekChatModel.builder()
                .deepSeekApi(DeepSeekApi.builder()
                        .baseUrl(System.getenv("AI_BASE_URL"))
                        .apiKey(System.getenv("AI_API_KEY"))
                        .build())
                .defaultOptions(DeepSeekChatOptions.builder()
                        .model(this.getModel())
                        .build())
                .build();
    }

    @Override
    public BaseCheckpointSaver getSaver() {
        return null;
    }

    @Override
    public ReactAgent getAgent() {
        if (agent != null) {
            return agent;
        }
        agent = ReactAgent.builder()
                .name(this.getName())
                .model(this.getChatModel())
                .systemPrompt(this.getPrompt())
                .build();
        return agent;
    }

    @Override
    public void clear() {
        agent = null;
    }

    @Override
    public String chat(String input, RunnableConfig context) throws GraphRunnerException {
        AssistantMessage response = this.getAgent().call(input, null);
        return response.getText();
    }

    public boolean analysis(String content, String intention) {
        String input = "## 输入内容: " +
                "\n```" +
                "\n" + content +
                "\n```" +
                "\n## 目标意图: " +
                "\n```" +
                "\n" + intention +
                "\n```";
        try {
            String result = this.chat(input, null);
//            System.out.println("NLP Input: \n" + input);
//            System.out.println("NLP Result: " + result);
            return result.contains("是");
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        return false;
    }

}


