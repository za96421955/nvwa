package ai.nvwa.model.chat.mode;

import ai.nvwa.model.ModelsEnum;
import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 对话请求
 * <p> <功能详细描述> </p>
 *
 * @author 陈晨
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest implements Serializable {
    private static final long serialVersionUID = -5971976031256584323L;

    private String model;
    private List<ChatMessage> messages;
    private Boolean stream;
    private StreamOptions stream_options;

    public ChatRequest(String model) {
        this.model = model;
        this.messages = new ArrayList<>();
    }

    public ChatRequest(ModelsEnum model) {
        this(model.getModel());
    }

    public ChatRequest message(ChatMessage chatMessage) {
        this.messages.add(chatMessage);
        return this;
    }

    public ChatRequest user(String content) {
        return this.message(ChatMessage.user(content));
    }

    public ChatRequest assistant(String content) {
        return this.message(ChatMessage.assistant(content));
    }

    public ChatRequest stream() {
        this.stream = true;
        this.stream_options = new StreamOptions(true);
        return this;
    }

    @Override
    public String toString() {
        return JSONObject.toJSONString(this);
    }

    /**
     * 消息
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatMessage implements Serializable  {
        private static final long serialVersionUID = -8169372345888213071L;

        public static final String ROLE_USER = "user";
        public static final String ROLE_ASSISTANT = "assistant";

        private String role;
        private String content;

        public static ChatMessage user(String content) {
            return new ChatMessage(ROLE_USER, content);
        }

        public static ChatMessage assistant(String content) {
            return new ChatMessage(ROLE_ASSISTANT, content);
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StreamOptions implements Serializable  {
        private static final long serialVersionUID = -8766007957829886981L;
        private Boolean include_usage;
    }

}


