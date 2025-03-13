package ai.nvwa.agent.tool.datastore.milvus.mode;

import io.milvus.v2.service.vector.request.data.BaseVector;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Milvus查询对象
 * <p> <功能详细描述> </p>
 *
 * @author 陈晨
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MilvusQuery implements Serializable {
    private static final long serialVersionUID = 5218238383452109521L;

    private String collectionName;
    private List<String> partitionNames;
    private List<BaseVector> vectors;
    private int topK;
    private String filter;
    private List<String> outputFields;
    private Map<String, Object> searchParams;
    private String groupField;
    private int groupSize;
    private int page;
    private long pageSize;

}


