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
import io.milvus.v2.service.vector.request.QueryReq;
import io.milvus.v2.service.vector.response.InsertResp;
import io.milvus.v2.service.vector.response.QueryResp;

import java.util.*;

/**
 * Milvus 文本匹配
 * <p>
 * </p>
 *
 * @author 陈晨
 */
public class MilvusTextMatch {

    private final MilvusClientV2 client;

    private MilvusTextMatch(MilvusClientV2 client) {
        this.client = client;
    }
    public static MilvusTextMatch init(MilvusClientV2 client) {
        return new MilvusTextMatch(client);
    }

    /**
     * @description 生成Schema
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public CreateCollectionReq.CollectionSchema generateSchema() {
        CreateCollectionReq.CollectionSchema schema = CreateCollectionReq.CollectionSchema.builder()
                .enableDynamicField(false)
                .build();
        // id：作为主键，自动生成 auto_id=True
        schema.addField(AddFieldReq.builder()
                .fieldName("id")
                .dataType(DataType.Int64)
                .isPrimaryKey(true)
                .autoID(true)
                .build());

        // 默认情况下，Milvus 使用standard分析器，该分析器根据空格和标点符号对文本进行标记，删除长度超过 40 个字符的标记，并将文本转换为小写
        // 无需其他参数即可应用此默认设置
        // 如果需要其他分析器，您可以使用 analyzer_params 参数配置一个分析器。例如，要应用english分析器来处理英语文本
        Map<String, Object> analyzerParams = new HashMap<>();
        analyzerParams.put("type", "chinese");
        // 要为特定 VARCHAR 字段启用文本匹配，请在定义字段架构时将 enable_analyzer 和 enable_match 参数都设置为 True
        // 这指示 Milvus 对文本进行分词并为指定字段创建倒排索引，从而实现快速高效的文本匹配。
        schema.addField(AddFieldReq.builder()
                .fieldName("text")
                .dataType(DataType.VarChar)
                .maxLength(1000)
                .enableAnalyzer(true)
                .analyzerParams(analyzerParams)
                .enableMatch(true)
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
                gson.fromJson("{\"text\": \"自然语言处理技术通过分析大量文本数据，显著提升了机器翻译的准确性和流畅度。\"}", JsonObject.class),
                gson.fromJson("{\"text\": \"中国古代诗词中的意象运用体现了独特的审美追求和文化传承价值。\"}", JsonObject.class),
                gson.fromJson("{\"text\": \"随着新能源汽车技术的突破，电池续航能力和充电效率成为行业竞争的核心指标。\"}", JsonObject.class),
                gson.fromJson("{\"text\": \"在数字孪生城市构建中，多源异构数据的实时融合与可视化呈现至关重要。\"}", JsonObject.class),
                gson.fromJson("{\"text\": \"元宇宙概念的兴起推动了虚拟现实与增强现实技术的深度融合与创新应用。\"}", JsonObject.class)
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
    public QueryReq generateSearch(String collectionName) {
        String filter = "TEXT_MATCH(text, '自然语言 新能源')";
        String filter2 = "TEXT_MATCH(text, '语言') or TEXT_MATCH(text, '新能源')";
        return QueryReq.builder()
                .collectionName(collectionName)
                .filter(filter2)
                .outputFields(Arrays.asList("id", "text"))
                .build();
//        return SearchReq.builder()
//                .collectionName(collectionName)
//                .annsField("sparse")
//                .data(Collections.singletonList(new FloatVec(new float[]{0.3580376395471989f, -0.6023495712049978f, 0.18414012509913835f})))
//                .filter(filter)
//                .topK(10)
//                .outputFields(Arrays.asList("id", "text"))
//                .build();
    }

    public static void main(String[] args) {
        MilvusClientV2 client = MilvusClient.connect(MilvusDemo.CLUSTER_ENDPOINT);
        MilvusCollection collection = MilvusCollection.init(client);
        MilvusTextMatch textMatch = new MilvusTextMatch(client);

        String collectionName = "text_match_search_collection";
        // 删除集合
        collection.drop(collectionName);
        // 生成Schema
        CreateCollectionReq.CollectionSchema schema = textMatch.generateSchema();
        // 生成索引
        List<IndexParam> indexParams = textMatch.generateIndex();
        // 创建集合
        collection.create(collectionName, schema, indexParams);
        // 插入数据
        InsertResp result = textMatch.insertData(collectionName);
        System.out.println("\n\n插入数据：");
        System.out.println(result);
        // 生成查询对象
        QueryReq query = textMatch.generateSearch(collectionName);

        // 查询
        QueryResp resp = client.query(query);
        System.out.println("\n\n查询数据：");
        System.out.println(resp);
    }

}


