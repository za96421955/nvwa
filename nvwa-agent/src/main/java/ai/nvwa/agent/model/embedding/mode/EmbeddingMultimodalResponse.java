package ai.nvwa.agent.model.embedding.mode;

import ai.nvwa.agent.model.ModelsEnum;
import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 多模态嵌入请求
 * <p> <功能详细描述> </p>
 *
 * @author 陈晨
 */
@Data
@AllArgsConstructor
public class EmbeddingMultimodalResponse implements Serializable {
    private static final long serialVersionUID = -4595731933050474981L;

    private Output output;
    private Usage usage;
    private String request_id;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Output implements Serializable {
        private static final long serialVersionUID = -6647908110078066116L;
        private List<Embedding> embeddings;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Embedding implements Serializable {
        private static final long serialVersionUID = -5388381698545267060L;
        private Integer index;
        private List<Float> embedding;
        private String type;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Usage implements Serializable {
        private static final long serialVersionUID = -7470957268999831531L;
        private Integer inputTokens;
        private Integer imageCount;
        private Double duration;
    }

}


