package ai.nvwa.tool.datastore;

import ai.nvwa.model.embedding.mode.EmbeddingContent;
import ai.nvwa.tool.datastore.extract.mode.Chunk;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.milvus.common.clientenum.FunctionType;
import io.milvus.v2.common.DataType;
import io.milvus.v2.common.IndexParam;
import io.milvus.v2.service.collection.request.AddFieldReq;
import io.milvus.v2.service.collection.request.CreateCollectionReq;
import io.milvus.v2.service.vector.request.QueryReq;
import io.milvus.v2.service.vector.response.QueryResp;
import io.milvus.v2.service.vector.response.SearchResp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 文档
 * <p> <功能详细描述> </p>
 *
 * @author 陈晨
 */
@Component
@Slf4j
public class Document extends AbstractMilvus {

    @Override
    protected String getDbName() {
        return "nvwa";
    }

    @Override
    protected String getCollectionName() {
        return "nvwa_document";
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
        // title
        schema.addField(AddFieldReq.builder()
                .fieldName("title")
                .dataType(DataType.VarChar)
                .maxLength(2000)
                .enableAnalyzer(true)
                .analyzerParams(Collections.singletonMap("type", "chinese"))
                .enableMatch(true)
                .build());
        // 稀疏向量
        schema.addField(AddFieldReq.builder()
                .fieldName("title_sparse")
                .dataType(DataType.SparseFloatVector)
                .build());
        schema.addFunction(CreateCollectionReq.Function.builder()
                .functionType(FunctionType.BM25)
                .name("title_bm25_emb")
                .inputFieldNames(Collections.singletonList("title"))
                .outputFieldNames(Collections.singletonList("title_sparse"))
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
        // title index
        indexParams.add(IndexParam.builder()
                .fieldName("title_sparse")
                .indexName("title_sparse_index")
                .indexType(IndexParam.IndexType.SPARSE_INVERTED_INDEX)
                .metricType(IndexParam.MetricType.BM25)
                /**
                 params.inverted_index_algo:用于建立和查询索引的算法。有效值：

                 "DAAT_MAXSCORE" (默认）：使用 MaxScore 算法进行优化的 Document-at-a-Time (DAAT) 查询处理。MaxScore 通过跳过可能影响最小的术语和文档，为高 k 值或包含大量术语的查询提供更好的性能。为此，它根据最大影响分值将术语划分为基本组和非基本组，并将重点放在对前 k 结果有贡献的术语上。

                 "DAAT_WAND":使用 WAND 算法优化 DAAT 查询处理。WAND 算法利用最大影响分数跳过非竞争性文档，从而评估较少的命中文档，但每次命中的开销较高。这使得 WAND 对于 k 值较小的查询或较短的查询更有效，因为在这些情况下跳过更可行。

                 "TAAT_NAIVE":基本术语一次查询处理（TAAT）。虽然与DAAT_MAXSCORE 和DAAT_WAND 相比速度较慢，但TAAT_NAIVE 具有独特的优势。DAAT 算法使用的是缓存的最大影响分数，无论全局 Collections 参数（avgdl）如何变化，这些分数都保持静态，而TAAT_NAIVE 不同，它能动态地适应这种变化。
                 */
                .extraParams(Collections.singletonMap("inverted_index_algo", "DAAT_MAXSCORE"))
                .build());
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
        return List.of("title", "content");
    }

    @Override
    protected Entity buildEntity(SearchResp.SearchResult result) {
        return Entity.builder()
                .title((String) result.getEntity().get("title"))
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
    public long insert(String title, String content) {
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
            row.addProperty("title", title);
            row.addProperty("content", chunk.getText());
            row.add("content_dense", gson.toJsonTree(contentEmbedding.getDenseVectors()));
            rows.add(row);
        }
        if (CollectionUtils.isEmpty(rows)) {
            return 0;
        }
        return milvusTemplate.data().insert(this.getCollectionName(), rows);
    }

    /**
     * @description title查询
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public List<Entity> queryByTitle(String title) {
        List<QueryResp.QueryResult> resultList = milvusTemplate.search().search(QueryReq.builder()
                .collectionName(this.getCollectionName())
                .filter("TEXT_MATCH(title, '" + title + "') ")
                .outputFields(List.of("title", "content"))
//                .searchParams(Collections.singletonMap("drop_ratio_search", 0.67))
                .build());
        if (CollectionUtils.isEmpty(resultList)) {
            return Collections.emptyList();
        }
        List<Entity> entityList = new ArrayList<>();
        for (QueryResp.QueryResult result : resultList) {
            entityList.add(Entity.builder()
                    .title((String) result.getEntity().get("title"))
                    .content((String) result.getEntity().get("content"))
                    .build());
        }
        return entityList;
    }

}


