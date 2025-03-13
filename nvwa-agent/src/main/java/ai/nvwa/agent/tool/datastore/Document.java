package ai.nvwa.agent.tool.datastore;

import ai.nvwa.agent.model.embedding.EmbeddingService;
import ai.nvwa.agent.tool.datastore.milvus.MilvusTemplate;
import ai.nvwa.agent.tool.datastore.milvus.mode.EmbeddingContent;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.milvus.common.clientenum.FunctionType;
import io.milvus.v2.common.DataType;
import io.milvus.v2.common.IndexParam;
import io.milvus.v2.service.collection.request.AddFieldReq;
import io.milvus.v2.service.collection.request.CreateCollectionReq;
import io.milvus.v2.service.vector.request.SearchReq;
import io.milvus.v2.service.vector.request.data.BaseVector;
import io.milvus.v2.service.vector.request.data.FloatVec;
import io.milvus.v2.service.vector.response.SearchResp;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;

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

    @PostConstruct
    public void init() {
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
    public CreateCollectionReq.CollectionSchema generateSchema() {
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
                .maxLength(1000)
                .enableAnalyzer(true)
                .build());
        // 密集向量
        schema.addField(AddFieldReq.builder()
                .fieldName("title_dense")
                .dataType(DataType.FloatVector)
                .dimension(512)
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
        // 稀疏向量
        schema.addField(AddFieldReq.builder()
                .fieldName("content_sparse")
                .dataType(DataType.SparseFloatVector)
                .build());
        schema.addFunction(CreateCollectionReq.Function.builder()
                .functionType(FunctionType.BM25)
                .name("content_bm25_emb")
                .inputFieldNames(Collections.singletonList("content"))
                .outputFieldNames(Collections.singletonList("content_sparse"))
                .build());
        return schema;
    }

    /**
     * @description 生成索引
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public List<IndexParam> generateIndex() {
        List<IndexParam> indexParams = new ArrayList<>();
        // title index
        indexParams.add(IndexParam.builder()
                .fieldName("title_dense")
                .indexName("title_dense_index")
                .indexType(IndexParam.IndexType.IVF_FLAT)
                .metricType(IndexParam.MetricType.IP)
                .extraParams(Collections.singletonMap("nlist", 128))
                .build());
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
        indexParams.add(IndexParam.builder()
                .fieldName("content_sparse")
                .indexName("content_sparse_index")
                .indexType(IndexParam.IndexType.SPARSE_INVERTED_INDEX)
                .metricType(IndexParam.MetricType.BM25)
                .extraParams(Collections.singletonMap("inverted_index_algo", "DAAT_MAXSCORE"))
                .build());
        return indexParams;
    }

    /**
     * @description 获取嵌入内容
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    private EmbeddingContent getEmbeddingContent(String content, int dimension) {
        List<Float> denseVectors = embeddingService.getTextDenseVectors(content, dimension);
        Map<Integer, Float> sparseVectors = embeddingService.toSparseVector(denseVectors);
        return EmbeddingContent.builder()
                .content(content)
                .denseVectors(denseVectors)
                .sparseVectors(sparseVectors)
                .build();
    }

    /**
     * @description 插入数据
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public long insert(String title, String content) {
        String localTitle = this.splitContent(title, null).get(0);
        EmbeddingContent titleEmbedding = this.getEmbeddingContent(localTitle, 512);
        Gson gson = new Gson();
        List<JsonObject> rows = new ArrayList<>();
        // 512 分段, 25% 重叠
        for (String split : this.splitContent(content, "\\n\\n")) {
            EmbeddingContent contentEmbedding = this.getEmbeddingContent(split, 1024);
            JsonObject row = new JsonObject();
            row.addProperty("title", localTitle);
            row.add("title_dense", gson.toJsonTree(titleEmbedding.getDenseVectors()));
            row.addProperty("content", split);
            row.add("content_dense", gson.toJsonTree(contentEmbedding.getDenseVectors()));
            rows.add(row);
        }
        return milvusTemplate.data().insert(COLLECTION_NAME, rows);
    }

    /**
     * @description 内容分片, 512 分段, 25% 重叠
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    private List<String> splitContent(String content, String split) {
        List<String> splitContents;
        if (StringUtils.isNotBlank(split)) {
            splitContents = Arrays.asList(content.split(split));
        } else {
            splitContents = Collections.singletonList(content);
        }
        List<String> splits = new ArrayList<>();
        for (String splitContent : splitContents) {
            String localContent = splitContent;
            while (localContent.length() > 512) {
                splits.add(localContent.substring(0, 512));
                localContent = localContent.substring((int) (512 * 0.75));
            }
            if (StringUtils.isNotBlank(localContent)) {
                splits.add(localContent);
            }
        }
        return splits;
    }

    /**
     * @description title查询
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public List<List<SearchResp.SearchResult>> queryByTitle(String title) {
        EmbeddingContent titleEmbedding = this.getEmbeddingContent(title, 512);
        List<BaseVector> queryDenseVectors = Collections.singletonList(new FloatVec(titleEmbedding.getDenseVectors()));
        return milvusTemplate.search().search(SearchReq.builder()
                .collectionName(COLLECTION_NAME)
                .data(queryDenseVectors)
                .annsField("title_dense")
                .metricType(IndexParam.MetricType.IP)
                .outputFields(List.of("title", "content"))
                .topK(3)
                .searchParams(Collections.singletonMap("drop_ratio_search", 0.33))
                .build());
    }

    /**
     * @description content查询
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public List<List<SearchResp.SearchResult>> queryByContent(String content) {
        EmbeddingContent titleEmbedding = this.getEmbeddingContent(content, 1024);
        List<BaseVector> queryDenseVectors = Collections.singletonList(new FloatVec(titleEmbedding.getDenseVectors()));
        return milvusTemplate.search().search(SearchReq.builder()
                .collectionName(COLLECTION_NAME)
                .data(queryDenseVectors)
                .annsField("content_dense")
                .metricType(IndexParam.MetricType.COSINE)
                .outputFields(List.of("title", "content"))
                .topK(3)
                .searchParams(Collections.singletonMap("drop_ratio_search", 0.33))
                .build());
    }

}


