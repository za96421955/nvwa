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
    public List<Entity> queryByContent(String content, int topK) {
        EmbeddingContent titleEmbedding = embeddingService.getEmbeddingContent(content, 1024);
        List<BaseVector> queryDenseVectors = Collections.singletonList(new FloatVec(titleEmbedding.getDenseVectors()));
        return this.buildEntity(milvusTemplate.search().search(SearchReq.builder()
                .collectionName(COLLECTION_NAME)
                .data(queryDenseVectors)
                .annsField("content_dense")
                .metricType(IndexParam.MetricType.COSINE)
                .outputFields(List.of("title", "content"))
                .topK(topK)
                .searchParams(Collections.singletonMap("drop_ratio_search", 0.33))
                .build()));
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
                    .params("{\"nprobe\": 10}")     // 默认：8，<100万向量：16
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
                    .extraParams(Collections.singletonMap("inverted_index_algo", "DAAT_MAXSCORE"))
                    .build());
            // content index
            indexParams.add(IndexParam.builder()
                    .fieldName("content_dense")
                    .indexName("content_dense_index")
                    .indexType(IndexParam.IndexType.IVF_FLAT)
                    .metricType(IndexParam.MetricType.COSINE)
                    .extraParams(Collections.singletonMap("nlist", 128))
                    .build());
            return indexParams;
        }
    }

}


