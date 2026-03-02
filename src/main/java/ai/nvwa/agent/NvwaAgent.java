package ai.nvwa.agent;

import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.checkpoint.BaseCheckpointSaver;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import org.springframework.ai.chat.model.ChatModel;

/**
 * @description NVWA 智能体
 * <p> <功能详细描述> </p>
 *
 * @author 陈晨
 * @date 2026/2/28 11:13
 */
public interface NvwaAgent {

    String getName();

    // 普通模型，可以按自定义的 ReAct 循环执行
    // 深度思考模型，会一次性把事情做完，ReAct 智能体慎用
    String getModel();

    String getPrompt();

    ChatModel getChatModel();

    BaseCheckpointSaver getSaver();

    ReactAgent getAgent();

    void clear();

    String chat(String input, RunnableConfig context) throws GraphRunnerException;

}


