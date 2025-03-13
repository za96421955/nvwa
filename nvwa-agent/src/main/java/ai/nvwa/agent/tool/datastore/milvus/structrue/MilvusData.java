package ai.nvwa.agent.tool.datastore.milvus.structrue;

import com.google.gson.JsonObject;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.vector.request.DeleteReq;
import io.milvus.v2.service.vector.request.InsertReq;
import io.milvus.v2.service.vector.request.UpsertReq;
import io.milvus.v2.service.vector.response.DeleteResp;
import io.milvus.v2.service.vector.response.InsertResp;
import io.milvus.v2.service.vector.response.UpsertResp;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Milvus数据
 * <p> <功能详细描述> </p>
 *
 * @author 陈晨
 */
@Component
public class MilvusData {

    @Autowired
    private MilvusClientV2 client;
//    @Autowired
//    private MilvusCollection collection;

    /**
     * @description 插入数据
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public InsertResp insert(String collectionName, String partitionName, List<JsonObject> data) {
//        if (!collection.check(collectionName)) {
//            return null;
//        }
//        Gson gson = new Gson();
//        List<JsonObject> data = Arrays.asList(
//                gson.fromJson("{\"id\": 0, \"vector\": [0.3580376395471989f, -0.6023495712049978f, 0.18414012509913835f, -0.26286205330961354f, 0.9029438446296592f], \"color\": \"pink_8682\"}", JsonObject.class),
//                gson.fromJson("{\"id\": 1, \"vector\": [0.19886812562848388f, 0.06023560599112088f, 0.6976963061752597f, 0.2614474506242501f, 0.838729485096104f], \"color\": \"red_7025\"}", JsonObject.class),
//                gson.fromJson("{\"id\": 2, \"vector\": [0.43742130801983836f, -0.5597502546264526f, 0.6457887650909682f, 0.7894058910881185f, 0.20785793220625592f], \"color\": \"orange_6781\"}", JsonObject.class),
//                gson.fromJson("{\"id\": 3, \"vector\": [0.3172005263489739f, 0.9719044792798428f, -0.36981146090600725f, -0.4860894583077995f, 0.95791889146345f], \"color\": \"pink_9298\"}", JsonObject.class),
//                gson.fromJson("{\"id\": 4, \"vector\": [0.4452349528804562f, -0.8757026943054742f, 0.8220779437047674f, 0.46406290649483184f, 0.30337481143159106f], \"color\": \"red_4794\"}", JsonObject.class),
//                gson.fromJson("{\"id\": 5, \"vector\": [0.985825131989184f, -0.8144651566660419f, 0.6299267002202009f, 0.1206906911183383f, -0.1446277761879955f], \"color\": \"yellow_4222\"}", JsonObject.class),
//                gson.fromJson("{\"id\": 6, \"vector\": [0.8371977790571115f, -0.015764369584852833f, -0.31062937026679327f, -0.562666951622192f, -0.8984947637863987f], \"color\": \"red_9392\"}", JsonObject.class),
//                gson.fromJson("{\"id\": 7, \"vector\": [-0.33445148015177995f, -0.2567135004164067f, 0.8987539745369246f, 0.9402995886420709f, 0.5378064918413052f], \"color\": \"grey_8510\"}", JsonObject.class),
//                gson.fromJson("{\"id\": 8, \"vector\": [0.39524717779832685f, 0.4000257286739164f, -0.5890507376891594f, -0.8650502298996872f, -0.6140360785406336f], \"color\": \"white_9381\"}", JsonObject.class),
//                gson.fromJson("{\"id\": 9, \"vector\": [0.5718280481994695f, 0.24070317428066512f, -0.3737913482606834f, -0.06726932177492717f, -0.6980531615588608f], \"color\": \"purple_4976\"}", JsonObject.class)
//        );
        InsertReq insertReq = InsertReq.builder()
                .collectionName(collectionName)
                .data(data)
                .build();
        if (StringUtils.isNotBlank(partitionName)) {
            insertReq.setPartitionName(partitionName);
        }
        return client.insert(insertReq);
    }

    /**
     * @description 插入数据
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public long insert(String collectionName, List<JsonObject> data) {
        return this.insert(collectionName, null, data).getInsertCnt();
    }

    /**
     * @description 更新数据
     * <p>
     *     需要更新 Collection 中的 Entity 或不确定是更新还是插入时，可以尝试使用 Upsert作
     *     使用此作时，必须确保 Upsert 请求中包含的 Entity 包含主键;否则，将发生错误
     * </p>
     *
     * @author 陈晨
     */
    public UpsertResp upsert(String collectionName, String partitionName, List<JsonObject> data) {
//        if (!collection.check(collectionName)) {
//            return null;
//        }
//        Gson gson = new Gson();
//        List<JsonObject> data = Arrays.asList(
//                gson.fromJson("{\"id\": 0, \"vector\": [-0.619954382375778, 0.4479436794798608, -0.17493894838751745, -0.4248030059917294, -0.8648452746018911], \"color\": \"black_9898\"}", JsonObject.class),
//                gson.fromJson("{\"id\": 1, \"vector\": [0.4762662251462588, -0.6942502138717026, -0.4490002642657902, -0.628696575798281, 0.9660395877041965], \"color\": \"red_7319\"}", JsonObject.class),
//                gson.fromJson("{\"id\": 2, \"vector\": [-0.8864122635045097, 0.9260170474445351, 0.801326976181461, 0.6383943392381306, 0.7563037341572827], \"color\": \"white_6465\"}", JsonObject.class),
//                gson.fromJson("{\"id\": 3, \"vector\": [0.14594326235891586, -0.3775407299900644, -0.3765479013078812, 0.20612075380355122, 0.4902678929632145], \"color\": \"orange_7580\"}", JsonObject.class),
//                gson.fromJson("{\"id\": 4, \"vector\": [0.4548498669607359, -0.887610217681605, 0.5655081329910452, 0.19220509387904117, 0.016513983433433577], \"color\": \"red_3314\"}", JsonObject.class),
//                gson.fromJson("{\"id\": 5, \"vector\": [0.11755001847051827, -0.7295149788999611, 0.2608115847524266, -0.1719167007897875, 0.7417611743754855], \"color\": \"black_9955\"}", JsonObject.class),
//                gson.fromJson("{\"id\": 6, \"vector\": [0.9363032158314308, 0.030699901477745373, 0.8365910312319647, 0.7823840208444011, 0.2625222076909237], \"color\": \"yellow_2461\"}", JsonObject.class),
//                gson.fromJson("{\"id\": 7, \"vector\": [0.0754823906014721, -0.6390658668265143, 0.5610517334334937, -0.8986261118798251, 0.9372056764266794], \"color\": \"white_5015\"}", JsonObject.class),
//                gson.fromJson("{\"id\": 8, \"vector\": [-0.3038434006935904, 0.1279149203380523, 0.503958664270957, -0.2622661156746988, 0.7407627307791929], \"color\": \"purple_6414\"}", JsonObject.class),
//                gson.fromJson("{\"id\": 9, \"vector\": [-0.7125086947677588, -0.8050968321012257, -0.32608864121785786, 0.3255654958645424, 0.26227968923834233], \"color\": \"brown_7231\"}", JsonObject.class)
//        );
        UpsertReq upsertReq = UpsertReq.builder()
                .collectionName(collectionName)
                .data(data)
                .build();
        if (StringUtils.isNotBlank(partitionName)) {
            upsertReq.setPartitionName(partitionName);
        }
        return client.upsert(upsertReq);
    }

    /**
     * @description 更新数据
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public long upsert(String collectionName, List<JsonObject> data) {
        return this.upsert(collectionName, null, data).getUpsertCnt();
    }

    /**
     * @description 删除数据
     * <p>
     *     在批量删除共享某些属性的多个实体时，可以使用过滤器表达式
     *     .filter("color in ['red_3314', 'purple_7392']")
     * </p>
     *
     * @author 陈晨
     */
    public DeleteResp delete(String collectionName, String partitionName, String filter) {
//        if (!collection.check(collectionName)) {
//            return null;
//        }
        DeleteReq deleteReq = DeleteReq.builder()
                .collectionName(collectionName)
                .filter(filter)
                .build();
        if (StringUtils.isNotBlank(partitionName)) {
            deleteReq.setPartitionName(partitionName);
        }
        return client.delete(deleteReq);
    }

    /**
     * @description 删除数据
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public long delete(String collectionName, String filter) {
        return this.delete(collectionName, null, filter).getDeleteCnt();
    }

    /**
     * @description 删除数据
     * <p>
     *     在大多数情况下，主键唯一标识实体。您可以通过在删除请求中设置实体的主键来删除实体
     *     .ids(Arrays.asList(18, 19))
     * </p>
     *
     * @author 陈晨
     */
    public DeleteResp delete(String collectionName, String partitionName, List<Object> idList) {
//        if (!collection.check(collectionName)) {
//            return null;
//        }
        DeleteReq deleteReq = DeleteReq.builder()
                .collectionName(collectionName)
                .ids(idList)
                .build();
        if (StringUtils.isNotBlank(partitionName)) {
            deleteReq.setPartitionName(partitionName);
        }
        DeleteResp deleteResp = client.delete(deleteReq);
        System.out.println(deleteResp);
        return deleteResp;
    }

    /**
     * @description 删除数据
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public long delete(String collectionName, List<Object> idList) {
        return this.delete(collectionName, null, idList).getDeleteCnt();
    }

}


