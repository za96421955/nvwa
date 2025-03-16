package ai.nvwa.agent.model.chat.mode;

import lombok.AllArgsConstructor;
import lombok.Builder;
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
@Builder
@AllArgsConstructor
public class ChatResult implements Serializable {
    private static final long serialVersionUID = 3127620732164513308L;

    private String request;
    private String errorMsg;
    private StringBuilder reasoning;
    private StringBuilder content;
    private ChatResponse.Usage usage;

    public ChatResult() {
        this.reasoning = new StringBuilder();
        this.content = new StringBuilder();
    }

}



