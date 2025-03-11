package ai.nvwa.agent.tool.datastore;

import ai.nvwa.agent.model.embedding.EmbeddingService;
import ai.nvwa.agent.tool.datastore.milvus.MilvusClient;
import ai.nvwa.agent.tool.datastore.milvus.MilvusCollection;
import ai.nvwa.agent.tool.datastore.milvus.MilvusSearch;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.vector.request.data.BaseVector;
import io.milvus.v2.service.vector.request.data.FloatVec;
import io.milvus.v2.service.vector.response.SearchResp;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * TODO
 * <p> <功能详细描述> </p>
 *
 * @author 陈晨
 */
@Slf4j
public class MilvusDemo {
    public static String CLUSTER_ENDPOINT = "http://localhost:19530";
    public static String TOKEN = "root:Milvus";

    private static Gson gson = new Gson();
    private static List<JsonObject> insertData = Arrays.asList(
            gson.fromJson("{\"id\": 0, \"vector\": [0.3580376395471989f, -0.6023495712049978f, 0.18414012509913835f, -0.26286205330961354f, 0.9029438446296592f], \"color\": \"pink_8682\"}", JsonObject.class),
            gson.fromJson("{\"id\": 1, \"vector\": [0.19886812562848388f, 0.06023560599112088f, 0.6976963061752597f, 0.2614474506242501f, 0.838729485096104f], \"color\": \"red_7025\"}", JsonObject.class),
            gson.fromJson("{\"id\": 2, \"vector\": [0.43742130801983836f, -0.5597502546264526f, 0.6457887650909682f, 0.7894058910881185f, 0.20785793220625592f], \"color\": \"orange_6781\"}", JsonObject.class),
            gson.fromJson("{\"id\": 3, \"vector\": [0.3172005263489739f, 0.9719044792798428f, -0.36981146090600725f, -0.4860894583077995f, 0.95791889146345f], \"color\": \"pink_9298\"}", JsonObject.class),
            gson.fromJson("{\"id\": 4, \"vector\": [0.4452349528804562f, -0.8757026943054742f, 0.8220779437047674f, 0.46406290649483184f, 0.30337481143159106f], \"color\": \"red_4794\"}", JsonObject.class),
            gson.fromJson("{\"id\": 5, \"vector\": [0.985825131989184f, -0.8144651566660419f, 0.6299267002202009f, 0.1206906911183383f, -0.1446277761879955f], \"color\": \"yellow_4222\"}", JsonObject.class),
            gson.fromJson("{\"id\": 6, \"vector\": [0.8371977790571115f, -0.015764369584852833f, -0.31062937026679327f, -0.562666951622192f, -0.8984947637863987f], \"color\": \"red_9392\"}", JsonObject.class),
            gson.fromJson("{\"id\": 7, \"vector\": [-0.33445148015177995f, -0.2567135004164067f, 0.8987539745369246f, 0.9402995886420709f, 0.5378064918413052f], \"color\": \"grey_8510\"}", JsonObject.class),
            gson.fromJson("{\"id\": 8, \"vector\": [0.39524717779832685f, 0.4000257286739164f, -0.5890507376891594f, -0.8650502298996872f, -0.6140360785406336f], \"color\": \"white_9381\"}", JsonObject.class),
            gson.fromJson("{\"id\": 9, \"vector\": [0.5718280481994695f, 0.24070317428066512f, -0.3737913482606834f, -0.06726932177492717f, -0.6980531615588608f], \"color\": \"purple_4976\"}", JsonObject.class)
    );
    private static List<JsonObject> upsertData = Arrays.asList(
            gson.fromJson("{\"id\": 0, \"vector\": [-0.619954382375778, 0.4479436794798608, -0.17493894838751745, -0.4248030059917294, -0.8648452746018911], \"color\": \"black_9898\"}", JsonObject.class),
            gson.fromJson("{\"id\": 1, \"vector\": [0.4762662251462588, -0.6942502138717026, -0.4490002642657902, -0.628696575798281, 0.9660395877041965], \"color\": \"red_7319\"}", JsonObject.class),
            gson.fromJson("{\"id\": 2, \"vector\": [-0.8864122635045097, 0.9260170474445351, 0.801326976181461, 0.6383943392381306, 0.7563037341572827], \"color\": \"white_6465\"}", JsonObject.class),
            gson.fromJson("{\"id\": 3, \"vector\": [0.14594326235891586, -0.3775407299900644, -0.3765479013078812, 0.20612075380355122, 0.4902678929632145], \"color\": \"orange_7580\"}", JsonObject.class),
            gson.fromJson("{\"id\": 4, \"vector\": [0.4548498669607359, -0.887610217681605, 0.5655081329910452, 0.19220509387904117, 0.016513983433433577], \"color\": \"red_3314\"}", JsonObject.class),
            gson.fromJson("{\"id\": 5, \"vector\": [0.11755001847051827, -0.7295149788999611, 0.2608115847524266, -0.1719167007897875, 0.7417611743754855], \"color\": \"black_9955\"}", JsonObject.class),
            gson.fromJson("{\"id\": 6, \"vector\": [0.9363032158314308, 0.030699901477745373, 0.8365910312319647, 0.7823840208444011, 0.2625222076909237], \"color\": \"yellow_2461\"}", JsonObject.class),
            gson.fromJson("{\"id\": 7, \"vector\": [0.0754823906014721, -0.6390658668265143, 0.5610517334334937, -0.8986261118798251, 0.9372056764266794], \"color\": \"white_5015\"}", JsonObject.class),
            gson.fromJson("{\"id\": 8, \"vector\": [-0.3038434006935904, 0.1279149203380523, 0.503958664270957, -0.2622661156746988, 0.7407627307791929], \"color\": \"purple_6414\"}", JsonObject.class),
            gson.fromJson("{\"id\": 9, \"vector\": [-0.7125086947677588, -0.8050968321012257, -0.32608864121785786, 0.3255654958645424, 0.26227968923834233], \"color\": \"brown_7231\"}", JsonObject.class)
    );

    private static FloatVec singleQuery = new FloatVec(new float[]{0.3580376395471989f, -0.6023495712049978f, 0.18414012509913835f, -0.26286205330961354f, 0.9029438446296592f});
    private static List<BaseVector> bulkQuery = Arrays.asList(
            new FloatVec(new float[]{0.041732933f, 0.013779674f, -0.027564144f, -0.013061441f, 0.009748648f}),
            new FloatVec(new float[]{0.0039737443f, 0.003020432f, -0.0006188639f, 0.03913546f, -0.00089768134f})
    );

    public static void main(String[] args) {
        EmbeddingService embeddingService = new EmbeddingService();
        MilvusClientV2 client = MilvusClient.connect(CLUSTER_ENDPOINT);
        MilvusCollection collection = MilvusCollection.init(client);

//        // 删除所有集合
//        List<String> collectionNames = collection.queryCollections();
//        for (String collectionName : collectionNames) {
//            collection.drop(collectionName);
//        }
//        // 创建集合
//        String collectionName = "hybrid_search_collection";
//        collection.create(collectionName);
//
//        // 插入数据
//        MilvusData data = MilvusData.init(client);
//        data.insert(collectionName, insertData);
//        data.delete(collectionName, "color in ['red_3314', 'purple_7392']");

        String searchText = "Artificial intelligence was founded as an academic discipline in 1956.";
        List<Float> denseVectors = embeddingService.getTextDenseVectors(searchText);
        FloatVec singleQuery = new FloatVec(denseVectors);

        // 向量搜索
        String collectionName = "hybrid_search_collection";
        MilvusSearch search = MilvusSearch.init(client);
        MilvusSearch.Query query = new MilvusSearch.Query(collectionName, singleQuery, 10);
        query.setSearchParams(Collections.singletonMap("anns_field", "dense"));
        List<List<SearchResp.SearchResult>> resultList = search.search(query);
        System.out.println("\n向量搜索:");
        for (List<SearchResp.SearchResult> list : resultList) {
            for (SearchResp.SearchResult result : list) {
                if (null == result) {
                    continue;
                }
                System.out.println(result);
            }
        }
    }

}


