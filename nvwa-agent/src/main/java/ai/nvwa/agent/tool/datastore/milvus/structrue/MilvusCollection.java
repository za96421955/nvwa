package ai.nvwa.agent.tool.datastore.milvus.structrue;

import io.milvus.param.Constant;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.common.IndexParam;
import io.milvus.v2.service.collection.request.*;
import io.milvus.v2.service.collection.response.DescribeCollectionResp;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;

/**
 * Milvus集合
 * <p> <功能详细描述> </p>
 *
 * @author 陈晨
 */
@Component
public class MilvusCollection {

    @Autowired
    private MilvusClientV2 client;
    @Autowired
    private MilvusPartition partition;

    /**
     * @description 查询集合列表
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public List<String> queryAllNames() {
        return client.listCollections().getCollectionNames();
    }

    /**
     * @description 检查集合是否存在
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public boolean check(String collectionName) {
        if (StringUtils.isBlank(collectionName)) {
            return false;
        }
        return this.queryAllNames().contains(collectionName);
    }

    /**
     * @description 查询指定集合
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public DescribeCollectionResp query(String collectionName) {
        if (StringUtils.isBlank(collectionName)) {
            return null;
        }
        DescribeCollectionReq request = DescribeCollectionReq.builder()
                .collectionName(collectionName)
                .build();
        return client.describeCollection(request);
    }

    /**
     * @description 集合加载至内存, 返回加载状态
     * <p>
     *     未加载的集合, 无法查询
     *     当你加载一个集合时，Milvus 会将索引文件和所有字段的原始数据加载到内存中，以便快速响应搜索和查询
     *     在集合加载后插入的实体会自动编制索引并加载
     * </p>
     *
     * @author 陈晨
     */
    public boolean load(String collectionName) {
        if (StringUtils.isBlank(collectionName)) {
            return false;
        }
        // 加载集合
        LoadCollectionReq loadCollectionReq = LoadCollectionReq.builder()
                .collectionName(collectionName)
                .build();
        client.loadCollection(loadCollectionReq);
        // 查询加载状态
        GetLoadStateReq loadStateReq = GetLoadStateReq.builder()
                .collectionName(collectionName)
                .build();
        return client.getLoadState(loadStateReq);
    }

    /**
     * @description 释放集合, 返回加载状态
     * <p>
     *     未释放的集合, 无法删除
     *     搜索和查询是内存密集型作。为了节省成本，建议您释放当前未使用的集合
     * </p>
     *
     * @author 陈晨
     */
    public void release(String collectionName) {
//        if (!this.check(collectionName)) {
//            return;
//        }
        // 释放集合
        ReleaseCollectionReq releaseCollectionReq = ReleaseCollectionReq.builder()
                .collectionName(collectionName)
                .build();
        client.releaseCollection(releaseCollectionReq);
    }

    /**
     * @description 创建集合
     * <p>
     *     集合中的动态字段是名为 $meta 的保留 JavaScript 对象表示法 （JSON） 字段
     *     启用此字段后，Milvus 会将每个实体中携带的所有非 schema 定义的字段及其值作为键值对保存在保留字段中
     * </p>
     *
     * @author 陈晨
     */
    public boolean create(CreateCollectionReq createReq) {
        if (null == createReq) {
            return false;
        }
        // 检查集合, 已存在则直接返回
        if (this.check(createReq.getCollectionName())) {
            return false;
        }
        client.createCollection(createReq);
        return this.check(createReq.getCollectionName());
    }

    /**
     * @description 创建集合
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public boolean create(String collectionName, CreateCollectionReq.CollectionSchema schema, List<IndexParam> indexParams) {
        CreateCollectionReq createReq = CreateCollectionReq.builder()
                .collectionName(collectionName)
                .collectionSchema(schema)
                .build();
        if (!CollectionUtils.isEmpty(indexParams)) {
            createReq.setIndexParams(indexParams);
        }
        return this.create(createReq);
    }

    /**
     * @description 创建集合
     * <p>
     *     集合中的动态字段是名为 $meta 的保留 JavaScript 对象表示法 （JSON） 字段
     *     启用此字段后，Milvus 会将每个实体中携带的所有非 schema 定义的字段及其值作为键值对保存在保留字段中
     * </p>
     *
     * @author 陈晨
     */
    public boolean create(String collectionName, List<AddFieldReq> fieldList, List<IndexParam> indexParamList,
                          Map<String, String> properties) {
        if (StringUtils.isBlank(collectionName)) {
            return false;
        }
        // 3.4 Create a collection with schema and index parameters
        CreateCollectionReq createReq = CreateCollectionReq.builder()
                .collectionName(collectionName)
                // 每当预期吞吐量增加 500 MB/s 或要插入的数据量增加 100 GB 时，请考虑将分片数增加 1
                .numShards(1)
                // 创建集合时，您可以设置集合中搜索和查询的一致性级别。您还可以在特定搜索或查询期间更改集合的一致性级别
//                .consistencyLevel(ConsistencyLevel.BOUNDED)
                .build();
        // 设置字段
        if (!CollectionUtils.isEmpty(fieldList)) {
            // 3.1 Create schema
            CreateCollectionReq.CollectionSchema schema = client.createSchema();
            // 3.2 Add fields to schema
            for (AddFieldReq field : fieldList) {
                if (null == field) {
                    continue;
                }
                schema.addField(field);
            }
            createReq.setCollectionSchema(schema);
        }
        // 设置索引
        if (!CollectionUtils.isEmpty(indexParamList)) {
            createReq.setIndexParams(indexParamList);
        }
        // 设置属性
        if (!CollectionUtils.isEmpty(properties)) {
            // Milvus默认在所有集合上启用mmap，允许Milvus将原始字段数据映射到内存中，而不是完全加载它们。这减少了内存占用并增加了收集容量
//            .property(Constant.MMAP_ENABLED, "false")
            // 如果需要在特定时间段内删除集合，请考虑设置其生存时间 （TTL） （以秒为单位）。一旦 TTL 超时，Milvus 就会删除集合中的实体并丢弃该集合。删除是异步的，这表示在删除完成之前仍可以进行搜索和查询
//            .property(Constant.TTL_SECONDS, "86400")
            createReq.getProperties().putAll(properties);
        }
        // 创建集合
        return this.create(createReq);
    }

    /**
     * @description 快速创建集合
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public boolean create(String collectionName) {
        if (StringUtils.isBlank(collectionName)) {
            return false;
        }
        // 以这种方式创建的 Collection 只有两个字段，名为 id 和 vector
        CreateCollectionReq createReq = CreateCollectionReq.builder()
                .collectionName(collectionName)
                .dimension(5)
                .build();
        // 创建集合
        return this.create(createReq);
    }

    /**
     * @description 集合重命名
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public void rename(String collectionName, String newCollectionName) {
        if (StringUtils.isBlank(collectionName) || StringUtils.isBlank(newCollectionName)) {
            return;
        }
        RenameCollectionReq renameCollectionReq = RenameCollectionReq.builder()
                .collectionName(collectionName)
                .newCollectionName(newCollectionName)
                .build();
        client.renameCollection(renameCollectionReq);
    }

    /**
     * @description 设置集合过期时间, 单位: 秒
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public void ttl(String collectionName, int ttl) {
        if (StringUtils.isBlank(collectionName)) {
            return;
        }
        AlterCollectionReq alterCollectionReq = AlterCollectionReq.builder()
                .collectionName(collectionName)
                .property(Constant.TTL_SECONDS, ttl + "")
                .build();
        client.alterCollection(alterCollectionReq);
    }

    /**
     * @description 删除集合
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public void drop(String collectionName) {
        if (StringUtils.isBlank(collectionName)) {
            return;
        }
        // 删除分区
        partition.drop(collectionName);
        // 释放集合
        this.release(collectionName);
        // 删除集合
        DropCollectionReq dropQuickSetupParam = DropCollectionReq.builder()
                .collectionName(collectionName)
                .build();
        client.dropCollection(dropQuickSetupParam);
    }

}


