package ai.nvwa.agent.impl;

import ai.nvwa.agent.NvwaAgent;
import ai.nvwa.tools.CLITool;
import ai.nvwa.tools.SearchTool;
import ai.nvwa.tools.SubAgentTool;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.checkpoint.BaseCheckpointSaver;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.deepseek.DeepSeekChatOptions;
import org.springframework.ai.deepseek.api.DeepSeekApi;

/**
 * @description 执行智能体
 * <p> <功能详细描述> </p>
 *
 * @author 陈晨
 * @date 2026/2/28 10:18
 */
public class MainAgent implements NvwaAgent {

    private ChatModel chatModel;
    private BaseCheckpointSaver saver;
    private ReactAgent agent;

    @Override
    public String getName() {
        return "main_agent";
    }

    @Override
    public String getModel() {
        return System.getenv("AI_MODEL");
    }

    @Override
    public String getPrompt() {
        return "1. 你直接使用 CLI 工具完成任务" +
                "\n2. 如果有必要，你可以编写 shell 脚本通过 CLI 执行" +
                "\n3. 如果有必要，你可以创建 subAgent 分配子任务" +
                "\n4. 再实际操作前先不要急着写代码，和用户需求讨论充分后再行动";
    }

    @Override
    public ChatModel getChatModel() {
        if (chatModel != null) {
            return chatModel;
        }
        DeepSeekApi scopeApi = DeepSeekApi.builder()
                .baseUrl(System.getenv("AI_BASE_URL"))
                .apiKey(System.getenv("AI_API_KEY"))
                .build();
        chatModel = DeepSeekChatModel.builder()
                .deepSeekApi(scopeApi)
                .defaultOptions(DeepSeekChatOptions.builder()
                        .model(this.getModel())
                        .build())
                .build();
        return this.chatModel;
    }

    @Override
    public BaseCheckpointSaver getSaver() {
        if (saver != null) {
            return saver;
        }
        saver = MemorySaver.builder().build();
        return saver;
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
                .saver(this.getSaver())
                .tools(CLITool.build(), SubAgentTool.build(), SearchTool.build())
//                .enableLogging(true)
                .build();
        return agent;
    }

    @Override
    public void clear() {
        chatModel = null;
        saver = null;
        agent = null;
    }

    /**
     * @description 对话
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     * @date 2026/2/26 19:56
     */
    public String chat(String input, RunnableConfig context) throws GraphRunnerException {
        if (StringUtils.isBlank(input)) {
            return "";
        }
        System.out.println("nvwa 思考中...");
        AssistantMessage response = this.getAgent().call(input, context);
        System.out.println("\nnvwa 回答[" + context.threadId().get() + "]:");
        System.out.println(response.getText());
        return response.getText();
    }

}


