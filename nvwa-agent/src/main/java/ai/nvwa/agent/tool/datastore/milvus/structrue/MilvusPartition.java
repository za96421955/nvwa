package ai.nvwa.agent.tool.datastore.milvus.structrue;

import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.collection.request.GetLoadStateReq;
import io.milvus.v2.service.partition.request.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;

/**
 * Milvus集合分区
 * <p> <功能详细描述> </p>
 *
 * @author 陈晨
 */
@Component
public class MilvusPartition {

    @Autowired
    private MilvusClientV2 client;
//    @Autowired
//    private MilvusCollection collection;

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
    public List<String> queryAllNames(String collectionName) {
//        if (!collection.check(collectionName)) {
//            return Collections.emptyList();
//        }
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
    public boolean check(String collectionName, String partitionName) {
//        if (StringUtils.isBlank(partitionName)) {
//            return false;
//        }
//        if (!collection.check(collectionName)) {
//            return false;
//        }
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
    public List<String> create(String collectionName, String partitionName) {
//        if (StringUtils.isBlank(partitionName)) {
//            return Collections.emptyList();
//        }
//        if (!collection.check(collectionName)) {
//            return Collections.emptyList();
//        }
        CreatePartitionReq createPartitionReq = CreatePartitionReq.builder()
                .collectionName(collectionName)
                .partitionName(partitionName)
                .build();
        client.createPartition(createPartitionReq);
        return this.queryAllNames(collectionName);
    }

    /**
     * @description 加载分区, 返回分区加载状态
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public boolean load(String collectionName, String partitionName) {
//        if (StringUtils.isBlank(partitionName)) {
//            return false;
//        }
//        if (!collection.check(collectionName)) {
//            return false;
//        }
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
    public void release(String collectionName) {
//        if (!collection.check(collectionName)) {
//            return;
//        }
        List<String> partitionNames = this.queryAllNames(collectionName);
        if (CollectionUtils.isEmpty(partitionNames)) {
            return;
        }
        ReleasePartitionsReq releasePartitionsReq = ReleasePartitionsReq.builder()
                .collectionName(collectionName)
                .partitionNames(partitionNames)
                .build();
        client.releasePartitions(releasePartitionsReq);
    }

    /**
     * @description 释放集合指定分区
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public void release(String collectionName, String partitionName) {
//        if (StringUtils.isBlank(partitionName)) {
//            return;
//        }
//        if (!collection.check(collectionName)) {
//            return;
//        }
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
    public void drop(String collectionName, String partitionName) {
//        if (StringUtils.isBlank(partitionName)) {
//            return;
//        }
//        if (!collection.check(collectionName)) {
//            return;
//        }
        // 1, 释放分区
        this.release(collectionName, partitionName);
        // 2, 删除分区
        DropPartitionReq dropPartitionReq = DropPartitionReq.builder()
                .collectionName(collectionName)
                .partitionName(partitionName)
                .build();
        client.dropPartition(dropPartitionReq);
    }

    /**
     * @description 删除分区
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public void drop(String collectionName) {
//        if (!collection.check(collectionName)) {
//            return;
//        }
        List<String> partitionNames = this.queryAllNames(collectionName);
        // 1, 释放分区
        this.release(collectionName);
        // 2, 删除分区
        for (String partitionName : partitionNames) {
            if ("_default".equals(partitionName)) {
                continue;
            }
            DropPartitionReq dropPartitionReq = DropPartitionReq.builder()
                    .collectionName(collectionName)
                    .partitionName(partitionName)
                    .build();
            client.dropPartition(dropPartitionReq);
        }
    }

}


