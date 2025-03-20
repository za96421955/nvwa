package ai.nvwa.model.chat.mode;

import ai.nvwa.model.ModelsEnum;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 阿里对话请求
 * <p> <功能详细描述> </p>
 *
 * @author 陈晨
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AlibabaChatRequest extends ChatRequest {
    private static final long serialVersionUID = 5982760983764079350L;

    public AlibabaChatRequest(ModelsEnum model) {
        super(model.getModel());
    }

    public static AlibabaChatRequest deepSeek() {
        return new AlibabaChatRequest(ModelsEnum.DEEPSEEK_R1);
    }

    public static AlibabaChatRequest llama() {
        return new AlibabaChatRequest(ModelsEnum.DEEPSEEK_R1_DISTILL_LLAMA_70B);
    }

    public AlibabaChatRequest message(ChatMessage chatMessage) {
        this.getMessages().add(chatMessage);
        return this;
    }

    @Override
    public String toString() {
        return JSONObject.toJSONString(this);
    }

}


