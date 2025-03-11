package ai.nvwa.agent.model;

import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

/**
 * @description 模型
 * <p> <功能详细描述> </p>
 *
 * @author 陈晨
 */
@Getter
@AllArgsConstructor
public enum ModelsEnum {

    /**
     * 对话模型
     */
    DEEPSEEK_R1("deepseek-r1", "671B 满血版模型"),
    DEEPSEEK_V3("deepseek-v3", "参数量为 671B"),
    DEEPSEEK_R1_DISTILL_QWEN_1_5B("deepseek-r1-distill-qwen-1.5b", "基于 Qwen2.5-Math-1.5B"),
    DEEPSEEK_R1_DISTILL_QWEN_7B("deepseek-r1-distill-qwen-7b", "基于 Qwen2.5-Math-7B"),
    DEEPSEEK_R1_DISTILL_QWEN_14B("deepseek-r1-distill-qwen-14b", "基于 Qwen2.5-14B"),
    DEEPSEEK_R1_DISTILL_QWEN_32B("deepseek-r1-distill-qwen-32b", "基于 Qwen2.5-32B"),
    DEEPSEEK_R1_DISTILL_LLAMA_8B("deepseek-r1-distill-llama-8b", "基于 Llama-3.1-8B"),
    DEEPSEEK_R1_DISTILL_LLAMA_70B("deepseek-r1-distill-llama-70b", "基于 Llama-3.3-70B"),

    /**
     * 嵌入模型
     */
    MULTIMODAL_EMBEDDING_V1("multimodal-embedding-v1", "XXXXXX"),
    TEXT_EMBEDDING_V3("text-embedding-v3", "XXXXXX"),

    XXXXXX("XXXXXX", "XXXXXX"),
    ;

    private final String model;
    private final String desc;

    public static ModelsEnum getByModel(String model) {
        if (StringUtils.isBlank(model)) {
            return null;
        }
        return Arrays.stream(ModelsEnum.values())
                .filter(e -> e.model.equals(model)).findFirst().orElse(null);
    }

    @Override
    public String toString() {
        return JSONObject.toJSONString(this);
    }

}
