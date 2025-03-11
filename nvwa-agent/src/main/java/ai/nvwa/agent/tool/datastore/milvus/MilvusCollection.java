package ai.nvwa.agent.tool.datastore.milvus;

import io.milvus.param.Constant;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.common.IndexParam;
import io.milvus.v2.service.collection.request.*;
import io.milvus.v2.service.collection.response.DescribeCollectionResp;
import io.milvus.v2.service.partition.request.*;
import io.milvus.v2.service.utility.request.AlterAliasReq;
import io.milvus.v2.service.utility.request.CreateAliasReq;
import io.milvus.v2.service.utility.request.DescribeAliasReq;
import io.milvus.v2.service.utility.request.ListAliasesReq;
import io.milvus.v2.service.utility.response.DescribeAliasResp;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Milvus集合服务
 * <p> <功能详细描述> </p>
 *
 * @author 陈晨
 */
public class MilvusCollection {

    private final MilvusClientV2 client;

    private MilvusCollection(MilvusClientV2 client) {
        this.client = client;
    }

    public static MilvusCollection init(MilvusClientV2 client) {
        return new MilvusCollection(client);
    }

    /**
     * @description 查询集合列表
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public List<String> queryCollections() {
        return client.listCollections().getCollectionNames();
    }

    /**
     * @description 检查集合是否存在
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public boolean checkCollection(String collectionName) {
        if (StringUtils.isBlank(collectionName)) {
            return false;
        }
        return this.queryCollections().contains(collectionName);
    }

    /**
     * @description 查询指定集合
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public DescribeCollectionResp queryCollection(String collectionName) {
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
    public boolean loadCollection(String collectionName) {
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
    public void releaseCollection(String collectionName) {
        if (!this.checkCollection(collectionName)) {
            return;
        }
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
        if (this.checkCollection(createReq.getCollectionName())) {
            return false;
        }
        client.createCollection(createReq);
        // 加载集合
        return this.loadCollection(createReq.getCollectionName());
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
    public void collectionSetTTL(String collectionName, int ttl) {
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
        // 释放分区
        this.releasePartition(collectionName);
        // 释放集合
        this.releaseCollection(collectionName);
        // 删除集合
        DropCollectionReq dropQuickSetupParam = DropCollectionReq.builder()
                .collectionName(collectionName)
                .build();
        client.dropCollection(dropQuickSetupParam);
    }

    /**
     * @description 创建别名
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public void createAlias(String collectionName, String alias) {
        if (StringUtils.isBlank(collectionName) || StringUtils.isBlank(alias)) {
            return;
        }
        // 9. Manage aliases
        // 9.1 Create alias
        CreateAliasReq createAliasReq = CreateAliasReq.builder()
                .collectionName(collectionName)
                .alias(alias)
                .build();
        client.createAlias(createAliasReq);
    }

    /**
     * @description 更改别名
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public void alterAlias(String collectionName, String newAlias) {
        if (StringUtils.isBlank(collectionName) || StringUtils.isBlank(newAlias)) {
            return;
        }
        // 9.4 Reassign alias to other collections
        AlterAliasReq alterAliasReq = AlterAliasReq.builder()
                .collectionName(collectionName)
                .alias(newAlias)
                .build();
        client.alterAlias(alterAliasReq);
    }

    /**
     * @description 查询别名列表
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public List<String> queryAliases(String collectionName) {
        if (StringUtils.isBlank(collectionName)) {
            return Collections.emptyList();
        }
        // 9.2 List alises
        ListAliasesReq listAliasesReq = ListAliasesReq.builder()
                .collectionName(collectionName)
                .build();
        return client.listAliases(listAliasesReq).getAlias();
    }

    /**
     * @description 查询指定别名
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public DescribeAliasResp queryAlias(String alias) {
        if (StringUtils.isBlank(alias)) {
            return null;
        }
        // 9.3 Describe alias
        DescribeAliasReq describeAliasReq = DescribeAliasReq.builder()
                .alias(alias)
                .build();
        return client.describeAlias(describeAliasReq);
    }

    /**
     * @description 查询分区列表
     * <p>
     *     在创建集合时，Milvus 还会在集合中创建一个名为 _default 的分区
     *     如果不打算添加任何其他分区，则插入集合的所有实体都将进入 default 分区，并且所有搜索和查询也会在 default 分区内执行
     *     可以添加更多分区，并根据特定条件将实体插入其中。然后，您可以将搜索和查询限制在某些分区内，从而提高搜索性能
     *     一个集合最多可以有 1,024 个分区
     * </p>
     *
     * @author 陈晨
     */
    public List<String> queryPartitions(String collectionName) {
        if (!this.checkCollection(collectionName)) {
            return Collections.emptyList();
        }
        ListPartitionsReq listPartitionsReq = ListPartitionsReq.builder()
                .collectionName(collectionName)
                .build();
        return client.listPartitions(listPartitionsReq);
    }

    /**
     * @description 检查分区
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public boolean checkPartition(String collectionName, String partitionName) {
        if (StringUtils.isBlank(collectionName) || StringUtils.isBlank(partitionName)) {
            return false;
        }
        HasPartitionReq hasPartitionReq = HasPartitionReq.builder()
                .collectionName(collectionName)
                .partitionName(partitionName)
                .build();
        return client.hasPartition(hasPartitionReq);
    }

    /**
     * @description 创建分区, 返回分区列表
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public List<String> createPartition(String collectionName, String partitionName) {
        if (StringUtils.isBlank(collectionName) || StringUtils.isBlank(partitionName)) {
            return Collections.emptyList();
        }
        CreatePartitionReq createPartitionReq = CreatePartitionReq.builder()
                .collectionName(collectionName)
                .partitionName(partitionName)
                .build();
        client.createPartition(createPartitionReq);
        return this.queryPartitions(collectionName);
    }

    /**
     * @description 加载分区, 返回分区加载状态
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public boolean loadPartition(String collectionName, String partitionName) {
        if (StringUtils.isBlank(collectionName) || StringUtils.isBlank(partitionName)) {
            return false;
        }
        LoadPartitionsReq loadPartitionsReq = LoadPartitionsReq.builder()
                .collectionName(collectionName)
                .partitionNames(Collections.singletonList(partitionName))
                .build();
        client.loadPartitions(loadPartitionsReq);
        // load state
        GetLoadStateReq getLoadStateReq = GetLoadStateReq.builder()
                .collectionName(collectionName)
                .partitionName(partitionName)
                .build();
        return client.getLoadState(getLoadStateReq);
    }

    /**
     * @description 释放集合分区
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public void releasePartition(String collectionName) {
        if (!this.checkCollection(collectionName)) {
            return;
        }
        List<String> partitionNameList = this.queryPartitions(collectionName);
        if (CollectionUtils.isEmpty(partitionNameList)) {
            return;
        }
        ReleasePartitionsReq releasePartitionsReq = ReleasePartitionsReq.builder()
                .collectionName(collectionName)
                .partitionNames(partitionNameList)
                .build();
        client.releasePartitions(releasePartitionsReq);
    }

    /**
     * @description 释放集合指定分区
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public void releasePartition(String collectionName, String partitionName) {
        if (StringUtils.isBlank(collectionName) || StringUtils.isBlank(partitionName)) {
            return;
        }
        ReleasePartitionsReq releasePartitionsReq = ReleasePartitionsReq.builder()
                .collectionName(collectionName)
                .partitionNames(Collections.singletonList(partitionName))
                .build();
        client.releasePartitions(releasePartitionsReq);
    }

    /**
     * @description 删除分区
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public void dropPartition(String collectionName, String partitionName) {
        if (StringUtils.isBlank(collectionName) || StringUtils.isBlank(partitionName)) {
            return;
        }
        // 1, 释放分区
        this.releasePartition(collectionName, partitionName);
        // 2, 删除分区
        DropPartitionReq dropPartitionReq = DropPartitionReq.builder()
                .collectionName(collectionName)
                .partitionName(partitionName)
                .build();
        client.dropPartition(dropPartitionReq);
    }

}


