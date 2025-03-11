package ai.nvwa.agent.model.chat.mode;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 对话响应
 * <p> <功能详细描述> </p>
 *
 * @author 陈晨
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse implements Serializable {
    private static final long serialVersionUID = 454214917503614103L;
    public static final String DATA = "data:";
    public static final String DONE = "[DONE]";

    private List<Choice> choices;
    private String object;
    private Usage usage;
    private Long created;
    private String systemFingerprint;
    private String model;
    private String id;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Choice implements Serializable {
        private static final long serialVersionUID = -3074179652661808940L;

        private Delta delta;
        private String finishReason;
        private Integer index;
        private Object logprobs;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Delta implements Serializable {
        private static final long serialVersionUID = -4156149164932026363L;

        private String content;
        private String reasoningContent;
        private String role;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Usage implements Serializable {
        private static final long serialVersionUID = 6472177721419359387L;

        private Integer promptTokens;
        private Integer completionTokens;
        private Integer totalTokens;
    }

}



