package ai.nvwa.model.embedding.mode;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 嵌入请求
 * <p> <功能详细描述> </p>
 *
 * @author 陈晨
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmbeddingResponse implements Serializable {
    private static final long serialVersionUID = 4974708730792994258L;

    private List<RequestData> data;
    private String model;
    private String object;
    private Usage usage;
    private String id;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RequestData implements Serializable {
        private static final long serialVersionUID = 6500848927214131430L;
        private List<Float> embedding;
        private Integer index;
        private String object;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Usage implements Serializable {
        private static final long serialVersionUID = 8692179569960447047L;
        private Integer prompt_tokens;
        private Integer total_tokens;
    }

}


