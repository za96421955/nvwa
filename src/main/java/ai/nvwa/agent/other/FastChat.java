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
 * @description 快速对话
 * <p> <功能详细描述> </p>
 *
 * @author 陈晨
 * @date 2026/2/28 11:59
 */
public class FastChat implements NvwaAgent {

    private ReactAgent agent;

    @Override
    public String getName() {
        return "fast_chat";
    }

    @Override
    public String getModel() {
        return System.getenv("AI_MODEL");
    }

    @Override
    public String getPrompt() {
        return null;
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
                .build();
        return agent;
    }

    @Override
    public void clear() {
        agent = null;
    }

    @Override
    public String chat(String input, RunnableConfig context) throws GraphRunnerException {
        try {
            AssistantMessage response = this.getAgent().call(input, null);
            return response.getText();
        } catch (GraphRunnerException e) {
            System.err.println(e.getMessage());
        }
        return "";
    }

}


