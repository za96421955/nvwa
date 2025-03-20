package ai.nvwa.model.chat.mode;

import ai.nvwa.tool.function.mode.Action;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 对话响应
 * <p> <功能详细描述> </p>
 *
 * @author 陈晨
 */
@Data
@Builder
@AllArgsConstructor
public class ChatResult implements Serializable {
    private static final long serialVersionUID = 3127620732164513308L;

    /** 对话请求内容 */
    private ChatRequest request;
    /** 错误信息 */
    private String errorMsg;
    /** 思考 */
    private StringBuilder reasoning;
    /** 回答 */
    private StringBuilder content;
    /** Token 使用情况 */
    private ChatResponse.Usage usage;
    /** 调用的 Action */
    private List<Action> actions;

    public ChatResult() {
        this.reasoning = new StringBuilder();
        this.content = new StringBuilder();
    }

}



