package ai.nvwa.agent.tool.datastore;

import ai.nvwa.agent.model.embedding.EmbeddingService;
import ai.nvwa.agent.model.embedding.mode.EmbeddingContent;
import ai.nvwa.agent.tool.datastore.extract.Extractor;
import ai.nvwa.agent.tool.datastore.extract.mode.Chunk;
import ai.nvwa.agent.tool.datastore.milvus.MilvusTemplate;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.milvus.common.clientenum.FunctionType;
import io.milvus.v2.common.ConsistencyLevel;
import io.milvus.v2.common.DataType;
import io.milvus.v2.common.IndexParam;
import io.milvus.v2.service.collection.request.AddFieldReq;
import io.milvus.v2.service.collection.request.CreateCollectionReq;
import io.milvus.v2.service.vector.request.AnnSearchReq;
import io.milvus.v2.service.vector.request.HybridSearchReq;
import io.milvus.v2.service.vector.request.QueryReq;
import io.milvus.v2.service.vector.request.SearchReq;
import io.milvus.v2.service.vector.request.data.BaseVector;
import io.milvus.v2.service.vector.request.data.FloatVec;
import io.milvus.v2.service.vector.request.ranker.RRFRanker;
import io.milvus.v2.service.vector.response.QueryResp;
import io.milvus.v2.service.vector.response.SearchResp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文档
 * <p> <功能详细描述> </p>
 *
 * @author 陈晨
 */
@Component
@Slf4j
public class Document {
    private static final String DB_NAME = "nvwa";
    private static final String COLLECTION_NAME = "nvwa_document";

    @Autowired
    private MilvusTemplate milvusTemplate;
    @Autowired
    private EmbeddingService embeddingService;
    @Autowired
    private Extractor extractor;

    @PostConstruct
    public void init() {
        new Structure(milvusTemplate).init();
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
            EmbeddingContent contentEmbedding = embeddingService.getEmbeddingContent(chunk.getText(), 1024);
            JsonObject row = new JsonObject();
            row.addProperty("title", title);
            row.addProperty("content", chunk.getText());
            row.add("content_dense", gson.toJsonTree(contentEmbedding.getDenseVectors()));
            rows.add(row);
        }
        return milvusTemplate.data().insert(COLLECTION_NAME, rows);
    }

    /**
     * @description title查询
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public List<Entity> queryByTitle(String title) {
        List<QueryResp.QueryResult> resultList = milvusTemplate.search().search(QueryReq.builder()
                .collectionName(COLLECTION_NAME)
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

    /**
     * @description content查询
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public List<Entity> queryByContent(String content, float minScore) {
        EmbeddingContent titleEmbedding = embeddingService.getEmbeddingContent(content, 1024);
        List<BaseVector> queryDenseVectors = Collections.singletonList(new FloatVec(titleEmbedding.getDenseVectors()));
        List<Entity> entityList = this.buildEntity(milvusTemplate.search().search(SearchReq.builder()
                .collectionName(COLLECTION_NAME)
                .data(queryDenseVectors)
                .annsField("content_dense")
                .metricType(IndexParam.MetricType.COSINE)
                .outputFields(List.of("title", "content"))
                .topK(10)
                .build()));
        return entityList.stream().filter(entity -> entity.getScore() > minScore).collect(Collectors.toList());
    }

    /**
     * @description contents查询
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public List<Entity> queryByContents(List<String> contents) {
        List<AnnSearchReq> searchRequests = new ArrayList<>();
        for (String content : contents) {
            EmbeddingContent titleEmbedding = embeddingService.getEmbeddingContent(content, 1024);
            List<BaseVector> queryDenseVectors = Collections.singletonList(new FloatVec(titleEmbedding.getDenseVectors()));
            searchRequests.add(AnnSearchReq.builder()
                    .vectorFieldName("content_dense")
                    .vectors(queryDenseVectors)
                    .metricType(IndexParam.MetricType.COSINE)
                    /**
                        要搜索的群集数量
                        搜索候选集群的集群数。数值越大，搜索的集群数越多，通过扩大搜索范围提高召回率，但代价是查询延迟增加。

                        值范围:
                        类型：整数
                        范围： [1, nlist[1，nlist］
                        默认值：8

                        调整建议:
                        增加该值可提高召回率，但可能会减慢搜索速度。设置nprobe 与nlist 成比例，以平衡速度和准确性。
                        在大多数情况下，我们建议您在此范围内设置一个值：[1，nlist]。
                     */
                    .params("{\"nprobe\": 8}")     // 默认：8，<100万向量：16
                    .topK(3)
                    .build());
        }
        return this.buildEntity(milvusTemplate.search().search(HybridSearchReq.builder()
                .collectionName(COLLECTION_NAME)
                .searchRequests(searchRequests)
                .ranker(new RRFRanker(100))
                .outFields(List.of("title", "content"))
                .topK(searchRequests.size())
                .consistencyLevel(ConsistencyLevel.BOUNDED)
                .build()));
    }

    /**
     * @description 构建entity
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    private List<Entity> buildEntity(List<List<SearchResp.SearchResult>> resultList) {
        if (CollectionUtils.isEmpty(resultList)) {
            return Collections.emptyList();
        }
        List<Entity> entityList = new ArrayList<>();
        for (List<SearchResp.SearchResult> list : resultList) {
            for (SearchResp.SearchResult result : list) {
                entityList.add(Entity.builder()
                        .title((String) result.getEntity().get("title"))
                        .content((String) result.getEntity().get("content"))
                        .score(result.getScore())
                        .build());
            }
        }
        return entityList;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Entity implements Serializable {
        private static final long serialVersionUID = 1545241606875897661L;
        private String title;
        private String content;
        private Float score;
    }

    /**
     * @description 数据结构
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    private static class Structure {
        private final MilvusTemplate milvusTemplate;

        public Structure(MilvusTemplate milvusTemplate) {
            this.milvusTemplate = milvusTemplate;
        }

        private void init() {
            // 创建数据库
            if (!milvusTemplate.database().check(DB_NAME)) {
                milvusTemplate.database().create(DB_NAME);
                log.info("[datastore-文档] 创建数据库: {}", DB_NAME);
            }
            // 创建集合
//        milvusTemplate.collection().drop(COLLECTION_NAME);
            if (!milvusTemplate.collection().check(COLLECTION_NAME)) {
                milvusTemplate.collection().create(COLLECTION_NAME, this.generateSchema(), this.generateIndex());
                log.info("[datastore-文档] 创建集合: {}", COLLECTION_NAME);
            }
            // 加载集合
            milvusTemplate.collection().load(COLLECTION_NAME);
            log.info("[datastore-文档] 加载集合: {}", COLLECTION_NAME);
        }

        /**
         * @description 生成Schema
         * <p> <功能详细描述> </p>
         *
         * @author 陈晨
         */
        private CreateCollectionReq.CollectionSchema generateSchema() {
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

        /**
         * @description 生成索引
         * <p> <功能详细描述> </p>
         *
         * @author 陈晨
         */
        private List<IndexParam> generateIndex() {
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
    }

}


