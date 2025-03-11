package ai.nvwa.agent.tool.datastore.milvus;

import ai.nvwa.agent.tool.datastore.MilvusDemo;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.milvus.common.clientenum.FunctionType;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.common.DataType;
import io.milvus.v2.common.IndexParam;
import io.milvus.v2.service.collection.request.AddFieldReq;
import io.milvus.v2.service.collection.request.CreateCollectionReq;
import io.milvus.v2.service.vector.request.InsertReq;
import io.milvus.v2.service.vector.request.SearchReq;
import io.milvus.v2.service.vector.request.data.EmbeddedText;
import io.milvus.v2.service.vector.response.InsertResp;
import io.milvus.v2.service.vector.response.SearchResp;

import java.util.*;

/**
 * Milvus 全文检索
 * <p>
 * </p>
 *
 * @author 陈晨
 */
public class MilvusFullText {

    private final MilvusClientV2 client;

    private MilvusFullText(MilvusClientV2 client) {
        this.client = client;
    }
    public static MilvusFullText init(MilvusClientV2 client) {
        return new MilvusFullText(client);
    }

    /**
     * @description 生成Schema
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public CreateCollectionReq.CollectionSchema generateSchema() {
        CreateCollectionReq.CollectionSchema schema = client.createSchema();
        // id：作为主键，自动生成 auto_id=True
        schema.addField(AddFieldReq.builder()
                .fieldName("id")
                .dataType(DataType.Int64)
                .isPrimaryKey(true)
                .autoID(true)
                .build());
        // text：存储用于全文搜索作的原始文本数据
        // 数据类型必须是 VARCHAR，因为 VARCHAR 是 Milvus 的文本存储字符串数据类型
        // 设置 enable_analyzer=True 以允许 Milvus 对文本进行分词
        // 默认情况下，Milvus 使用标准分析器进行文本分析
        schema.addField(AddFieldReq.builder()
                .fieldName("text")
                .dataType(DataType.VarChar)
                .maxLength(1000)
                .enableAnalyzer(true)
                .build());
        // sparse：一个 vector 字段，用于存储内部生成的 sparse embeddings，用于全文搜索作
        // 数据类型必须为 SPARSE_FLOAT_VECTOR
        schema.addField(AddFieldReq.builder()
                .fieldName("sparse")
                .dataType(DataType.SparseFloatVector)
                .build());

        // 对于具有多个 VARCHAR 字段需要文本到稀疏向量转换的集合
        // 请向集合架构添加单独的函数，确保每个函数都有唯一的名称和output_field_names值
        schema.addFunction(CreateCollectionReq.Function.builder()
                .functionType(FunctionType.BM25)
                .name("text_bm25_emb")
                .inputFieldNames(Collections.singletonList("text"))
                .outputFieldNames(Collections.singletonList("sparse"))
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
        indexParams.add(IndexParam.builder()
                .fieldName("sparse")
                .indexType(IndexParam.IndexType.SPARSE_INVERTED_INDEX)
                .metricType(IndexParam.MetricType.BM25)
                .build());
        return indexParams;
    }

    /**
     * @description 插入数据
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public InsertResp insertData(String collectionName) {
        Gson gson = new Gson();
        List<JsonObject> rows = Arrays.asList(
                gson.fromJson("{\"text\": \"information retrieval is a field of study.\"}", JsonObject.class),
                gson.fromJson("{\"text\": \"information retrieval focuses on finding relevant information in large datasets.\"}", JsonObject.class),
                gson.fromJson("{\"text\": \"data mining and information retrieval overlap in research.\"}", JsonObject.class)
        );
        return client.insert(InsertReq.builder()
                .collectionName(collectionName)
                .data(rows)
                .build());
    }

    /**
     * @description 生成查询对象
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public SearchReq generateSearch(String collectionName) {
        Map<String,Object> searchParams = new HashMap<>();
        searchParams.put("drop_ratio_search", 0.2);
        return SearchReq.builder()
                .collectionName(collectionName)
                .data(Collections.singletonList(new EmbeddedText("whats the focus of information retrieval?")))
                .annsField("sparse")
                .topK(3)
                .searchParams(searchParams)
                .outputFields(Collections.singletonList("text"))
                .build();
    }

    public static void main(String[] args) {
        MilvusClientV2 client = MilvusClient.connect(MilvusDemo.CLUSTER_ENDPOINT);
        MilvusCollection collection = MilvusCollection.init(client);
        MilvusFullText fullText = new MilvusFullText(client);

        String collectionName = "full_text_search_collection";
        // 删除集合
        collection.drop(collectionName);
        // 生成Schema
        CreateCollectionReq.CollectionSchema schema = fullText.generateSchema();
        // 生成索引
        List<IndexParam> indexParams = fullText.generateIndex();
        // 创建集合
        collection.create(collectionName, schema, indexParams);
        // 插入数据
        InsertResp result = fullText.insertData(collectionName);
        System.out.println("\n\n插入数据：");
        System.out.println(result);
        // 生成查询对象
        SearchReq search = fullText.generateSearch(collectionName);

        // 查询
        SearchResp resp = client.search(search);
        System.out.println("\n\n查询数据：");
        System.out.println(resp);
    }

}


