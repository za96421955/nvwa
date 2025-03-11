package ai.nvwa.agent.model.chat;

import ai.nvwa.agent.components.util.HttpClient;
import ai.nvwa.agent.model.CloudConfig;
import ai.nvwa.agent.model.chat.mode.AlibabaChatRequest;
import ai.nvwa.agent.model.chat.mode.ChatRequest;
import ai.nvwa.agent.model.chat.mode.ChatResponse;
import ai.nvwa.agent.prompt.Prompt;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicReference;

/**
 * 对话服务
 * <p> <功能详细描述> </p>
 *
 * @author 陈晨
 */
@Service
public class ChatService {

    /**
     * @description 对话
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public ChatResponse chat(ChatRequest request) {
        String result = HttpClient.post(CloudConfig.Alibaba.Url.CHAT)
                .authorization(CloudConfig.Alibaba.AUTH)
                .body(request.toString())
                .asString();
        return JSONObject.parseObject(result, ChatResponse.class);
    }

    /**
     * @description 对话, 流式响应
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public void chat(ChatRequest request, HttpClient.StreamHandle handle) {
        HttpClient.post(CloudConfig.Alibaba.Url.CHAT)
                .authorization(CloudConfig.Alibaba.AUTH)
                .body(request.toString())
                .asStreamHandle(handle);
    }

    /**
     * @description 对话处理
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public interface ChatHandle {
        void handle(String reasoning, String content, ChatResponse.Usage usage);
    }

    /**
     * @description 对话, 流式响应
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public void chat(ChatRequest request, ChatHandle handle) {
        this.chat(request, input -> {
            if (StringUtils.isBlank(input)
                    || input.indexOf(ChatResponse.DATA) != 0
                    || input.contains(ChatResponse.DONE)) {
                return;
            }
            ChatResponse response = JSONObject.parseObject(input.substring(6), ChatResponse.class);
            // usage
            if (null != response.getUsage()) {
                handle.handle(null, null, response.getUsage());
                return;
            }
            // delta
            ChatResponse.Delta delta = response.getChoices().get(0).getDelta();
            String reasoning = delta.getReasoningContent();
            String content = delta.getContent();
            if (StringUtils.isNotBlank(reasoning)) {
                handle.handle(reasoning, null, null);
            }
            if (StringUtils.isNotBlank(content)) {
                handle.handle(null, content, null);
            }
        });
    }

    public static void main(String[] args) {
        ChatRequest request = AlibabaChatRequest.llama()
                .user(Prompt.CRISPE)
                .user("你是谁？")
                .stream();
        System.out.println("\n请求: ");
        System.out.println(request.toString());

        ChatService service = new ChatService();
        StringBuilder reasoningBuilder = new StringBuilder();
        StringBuilder contentBuilder = new StringBuilder();
        AtomicReference<ChatResponse.Usage> usage = new AtomicReference<>();
        service.chat(request, (reasoning, content, usage1) -> {
            if (StringUtils.isNotBlank(reasoning)) {
                reasoningBuilder.append(reasoning);
                System.out.println("思考中：" + reasoningBuilder);
            }
            if (StringUtils.isNotBlank(content)) {
                contentBuilder.append(content);
                System.out.println("回答：" + contentBuilder);
            }
            if (null != usage1) {
                usage.set(usage1);
            }
        });
        System.out.println("\n\n响应: ");
        System.out.println("思考：" + reasoningBuilder);
        System.out.println("回答：" + contentBuilder);
        System.out.println("使用：" + usage.get());
    }

}


