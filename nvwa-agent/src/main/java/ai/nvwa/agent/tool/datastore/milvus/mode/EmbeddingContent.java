package ai.nvwa.agent.tool.datastore.milvus.mode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 嵌入内容
 * <p> <功能详细描述> </p>
 *
 * @author 陈晨
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmbeddingContent implements Serializable {
    private static final long serialVersionUID = 7340987357656091577L;

    private String content;
    private List<Float> denseVectors;
    private Map<Integer, Float> sparseVectors;

}


