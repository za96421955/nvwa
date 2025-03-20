package ai.nvwa.tool.datastore;

import ai.nvwa.model.embedding.mode.EmbeddingContent;
import ai.nvwa.tool.datastore.extract.mode.Chunk;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.milvus.v2.common.DataType;
import io.milvus.v2.common.IndexParam;
import io.milvus.v2.service.collection.request.AddFieldReq;
import io.milvus.v2.service.collection.request.CreateCollectionReq;
import io.milvus.v2.service.vector.response.SearchResp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 数据集
 * <p> <功能详细描述> </p>
 *
 * @author 陈晨
 */
@Component
@Slf4j
public class DataSet extends AbstractMilvus {

    @Override
    protected String getDbName() {
        return "nvwa";
    }

    @Override
    protected String getCollectionName() {
        return "nvwa_data_set";
    }

    @Override
    protected CreateCollectionReq.CollectionSchema generateSchema() {
        CreateCollectionReq.CollectionSchema schema = milvusTemplate.client().createSchema();
        // id
        schema.addField(AddFieldReq.builder()
                .fieldName("id")
                .dataType(DataType.Int64)
                .isPrimaryKey(true)
                .autoID(true)
                .build());
        // content
        schema.addField(AddFieldReq.builder()
                .fieldName("content")
                .dataType(DataType.VarChar)
                .maxLength(2000)
                .enableAnalyzer(true)
                .build());
        // 密集向量
        schema.addField(AddFieldReq.builder()
                .fieldName("content_dense")
                .dataType(DataType.FloatVector)
                .dimension(1024)
                .build());
        return schema;
    }

    @Override
    protected List<IndexParam> generateIndex() {
        List<IndexParam> indexParams = new ArrayList<>();
        // content index
        indexParams.add(IndexParam.builder()
                .fieldName("content_dense")
                .indexName("content_dense_index")
                .indexType(IndexParam.IndexType.IVF_FLAT)
                .metricType(IndexParam.MetricType.COSINE)
                /**
                 划分数据集的簇数
                 在索引构建过程中使用 K-means 算法创建的簇数。每个簇由一个中心点表示，存储一个向量列表。增加该参数可减少每个簇中的向量数量，从而创建更小、更集中的分区。

                 值范围:
                 类型： 整数整数
                 范围： [1, 65536[1, 65536]
                 默认值：128

                 调整建议:
                 nlist 值越大，创建的聚类越精细，召回率越高，但会增加索引构建时间。根据数据集大小和可用资源进行优化。在大多数情况下，我们建议在此范围内设置值：[32, 4096].
                 */
                .extraParams(Collections.singletonMap("nlist", 128))
                .build());
        return indexParams;
    }

    @Override
    protected String vectorField() {
        return "content_dense";
    }

    @Override
    protected List<String> outputFields() {
        return List.of("content");
    }

    @Override
    protected Entity buildEntity(SearchResp.SearchResult result) {
        return Entity.builder()
                .content((String) result.getEntity().get("content"))
                .score(result.getScore())
                .build();
    }

    /**
     * @description 插入数据
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public long insert(String content) {
        Gson gson = new Gson();
        List<JsonObject> rows = new ArrayList<>();
        // 默认512token, 25%重叠
        for (Chunk chunk : extractor.splitChunks(content)) {
            log.info("chunk: metas={}, size={}, text={}", chunk.getMetas(), chunk.getText().length(), chunk.getText());
            // 重复内容检查
            if (this.isRepeat(chunk.getText())) {
                continue;
            }
            // 数据入库
            EmbeddingContent contentEmbedding = embeddingService.getEmbeddingContent(chunk.getText(), 1024);
            JsonObject row = new JsonObject();
            row.addProperty("content", chunk.getText());
            row.add("content_dense", gson.toJsonTree(contentEmbedding.getDenseVectors()));
            rows.add(row);
        }
        if (CollectionUtils.isEmpty(rows)) {
            return 0;
        }
        return milvusTemplate.data().insert(this.getCollectionName(), rows);
    }

}


