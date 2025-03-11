package ai.nvwa.agent.tool.datastore.milvus;

import io.milvus.orm.iterator.QueryIterator;
import io.milvus.response.QueryResultsWrapper;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.common.ConsistencyLevel;
import io.milvus.v2.service.vector.request.GetReq;
import io.milvus.v2.service.vector.request.QueryIteratorReq;
import io.milvus.v2.service.vector.request.QueryReq;
import io.milvus.v2.service.vector.request.SearchReq;
import io.milvus.v2.service.vector.request.data.BaseVector;
import io.milvus.v2.service.vector.response.GetResp;
import io.milvus.v2.service.vector.response.QueryResp;
import io.milvus.v2.service.vector.response.SearchResp;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * Milvus搜索
 * <p>
 * Enhancing ANN Search 增强 ANN 搜索
 *
 * Filtered Search：
 *      你可以在搜索请求中加入过滤条件，这样 Milvus 在进行 ANN 搜索之前进行元数据过滤，从而将搜索范围从整个集合缩小到只搜索符合指定过滤条件的实体
 * Range Search：
 *      您可以通过限制特定范围内返回的实体的距离或分数来提高搜索结果相关性。
 *      在 Milvus 中，范围搜索涉及绘制两个同心圆，其中向量嵌入与查询向量最相似。
 *      搜索请求指定两个圆的半径，Milvus 返回落在外圆内但不落在内圆内的所有向量嵌入
 * Grouping Search：
 *      如果返回的实体在特定字段中具有相同的值，则搜索结果可能无法表示向量空间中所有向量嵌入的分布。
 *      要使搜索结果多样化，请考虑使用分组搜索
 * Hybrid Search：
 *      一个集合最多可以包含四个向量字段，以保存使用不同嵌入模型生成的向量嵌入。
 *      这样，您可以使用混合搜索对这些向量字段的搜索结果进行重新排序，从而提高召回率
 * Search Iterator：
 *      单个 ANN 搜索最多返回 16,384 个实体。如果您需要在单个搜索中返回更多实体，请考虑使用搜索迭代器
 * Full-Text Search：
 *      全文搜索是一项功能，可检索文本数据集中包含特定术语或短语的文档，然后根据相关性对结果进行排名。
 *      此功能克服了语义搜索限制，这些限制可能会忽略精确的术语，从而确保您收到最准确且与上下文相关的结果。
 *      此外，它还通过接受原始文本输入来简化向量搜索，自动将文本数据转换为稀疏嵌入，而无需手动生成向量嵌入
 * Text Match：
 *      Milvus 中的文本匹配支持基于特定术语的精确文档检索。
 *      此功能主要用于筛选搜索以满足特定条件，并且可以合并标量筛选来优化查询结果，从而允许在满足标量条件的向量内进行相似性搜索
 * Use Partition Key：
 *      在元数据筛选中涉及多个标量字段并使用相当复杂的筛选条件可能会影响搜索效率。
 *      将标量字段设置为分区键并在搜索请求中使用涉及分区键的筛选条件后，它可以帮助限制与指定分区键值对应的分区内的搜索范围
 * Use mmap：
 *      在 Milvus 中，内存映射文件允许将文件内容直接映射到内存中。
 *      此功能提高了内存效率，尤其是在可用内存稀缺但无法完成数据加载的情况下。
 *      这种优化机制可以在保证性能达到一定限制的同时增加数据容量;但是，当数据量超过内存过多时，搜索和查询性能可能会严重下降。
 *      请根据需要选择打开或关闭此功能
 * Clustering Compaction：
 *      集群压缩旨在提高搜索性能并降低大型集合的成本。
 *      本指南将帮助您了解集群压缩以及此功能如何提高搜索性能
 * </p>
 *
 * @author 陈晨
 */
public class MilvusSearch {

    private final MilvusClientV2 client;

    private MilvusSearch(MilvusClientV2 client) {
        this.client = client;
    }
    public static MilvusSearch init(MilvusClientV2 client) {
        return new MilvusSearch(client);
    }

    @Data
    public static class Query {
        private String collectionName;
        private List<String> partitionNames;
        private List<BaseVector> vectors;
        private int topK;
        private String filter;
        private List<String> outputFields;
        private Map<String, Object> searchParams;
        private String groupField;
        private int groupSize;
        private int page;
        private long pageSize;

        public Query(String collectionName, List<BaseVector> vectors, int topK) {
            this.collectionName = collectionName;
            this.vectors = vectors;
            this.topK = topK;
        }

        public Query(String collectionName, BaseVector vector, int topK) {
            this.collectionName = collectionName;
            this.vectors = Collections.singletonList(vector);
            this.topK = topK;
        }
    }

    /**
     * @description 向量搜索
     * <p>
     *     在 ANN 搜索中，单向量搜索是指只涉及一个查询向量的搜索
     *     根据预先构建的索引和搜索请求中携带的指标类型，Milvus 会找到与查询向量最相似的 top-K 向量
     *     .filter("color like \"red%\" and likes > 50")
     * </p>
     *
     * @author 陈晨
     */
    public List<List<SearchResp.SearchResult>> search(Query query) {
        // 加载collection
        MilvusCollection.init(client).loadCollection(query.getCollectionName());
        // 查询
//        FloatVec queryVector = new FloatVec(new float[]{0.3580376395471989f, -0.6023495712049978f, 0.18414012509913835f, -0.26286205330961354f, 0.9029438446296592f});
        SearchReq searchReq = SearchReq.builder()
                .collectionName(query.getCollectionName())
                .data(query.getVectors())
                .topK(query.getTopK())
                .build();
        // 设置分区
        if (!CollectionUtils.isEmpty(query.getPartitionNames())) {
            searchReq.setPartitionNames(query.getPartitionNames());
        }
        // 设置过滤信息
        if (StringUtils.isNotBlank(query.getFilter())) {
            searchReq.setFilter(query.getFilter());
        }
        // 设置查询参数
        if (!CollectionUtils.isEmpty(query.getSearchParams())) {
            searchReq.setSearchParams(query.getSearchParams());
        }
        // 设置查询字段
        if (!CollectionUtils.isEmpty(query.getOutputFields())) {
            // 在搜索结果中，Milvus 默认包含包含 top-K 向量嵌入的实体的主字段值和相似性距离/分数。您可以在搜索请求中包含目标字段名称作为输出字段，以使搜索结果包含这些实体中其他字段的值
            // 不指定输出字段, 输出entity则为空
            searchReq.setOutputFields(query.getOutputFields());
        }
        // 设置分组信息
        if (StringUtils.isNotBlank(query.getGroupField())) {
            searchReq.setGroupByFieldName(query.getGroupField());
        }
        if (query.getGroupSize() > 0) {
            searchReq.setGroupSize(query.getGroupSize());
            // 严格组大小：当 strict_group_size=True 时，系统将尝试为每个组返回指定数量的实体 （group_size），除非该组中没有足够的数据
            // 此设置可确保每个组的实体计数一致，但可能会因数据分布不均匀或资源有限而导致性能下降
            // 如果不需要严格的实体计数，则设置 strict_group_size=False 可以提高查询速度
            searchReq.setStrictGroupSize(true);
        }
        // 设置分页信息
        if (query.getPage() > 0) {
            // 在单个 ANN 搜索中应小于 16,384
            long offset = (query.getPage() - 1) * query.getPageSize();
            // Limit 参数设置为要包含在当前查询结果中的 Entities 数量，并将 Offset 设置为已返回的 Entities 总数
            // 在单个 ANN 搜索中应小于 16,384
            searchReq.setLimit(query.getPageSize());
            searchReq.setOffset(offset);
        }
        return client.search(searchReq).getSearchResults();
    }

    /**
     * @description 向量查询
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public List<List<SearchResp.SearchResult>> vectorSearch(String collectionName, List<BaseVector> vectors) {
        return this.search(new Query(collectionName, vectors, 10));
    }

    /**
     * @description 单向量查询
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public List<SearchResp.SearchResult> singleVectorSearch(String collectionName, BaseVector vector) {
        List<List<SearchResp.SearchResult>> resultList = this.search(new Query(collectionName, vector, 10));
        if (CollectionUtils.isEmpty(resultList)) {
            return Collections.emptyList();
        }
        return resultList.get(0);
    }

    /**
     * @description 过滤查询
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public List<List<SearchResp.SearchResult>> filterSearch(String collectionName, List<BaseVector> vectors, String filter) {
        Query query = new Query(collectionName, vectors, 10);
        query.setFilter(filter);
        // 要使用迭代筛选执行筛选搜索，您可以执行以下作
        // TODO 没理解参数的含义
        query.setSearchParams(Collections.singletonMap("hints", "iterative_filter"));
        return this.search(query);
    }

    /**
     * @description 范围查询
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public List<List<SearchResp.SearchResult>> rangeSearch(String collectionName, List<BaseVector> vectors, float radius, float rangeFilter) {
        if (radius < 0 || radius > 1 || rangeFilter < 0 || rangeFilter > 1 || radius > rangeFilter) {
            // 范围参数错误, 0 - 1 之间
            return Collections.emptyList();
        }
        Query query = new Query(collectionName, vectors, 10);
        Map<String,Object> extraParams = new HashMap<>();
        extraParams.put("radius", radius);
        extraParams.put("range_filter", rangeFilter);
        query.setSearchParams(extraParams);
        return this.search(query);
    }

    /**
     * @description Get
     * <p>By primary keys</p>
     *
     * @author 陈晨
     */
    public List<QueryResp.QueryResult> get(String collectionName, String partitionName, List<Long> ids, List<String> fields) {
        GetReq request = GetReq.builder()
                .collectionName(collectionName)
                .ids(Collections.singletonList(ids))
                .outputFields(fields)
                .build();
        if (StringUtils.isNotBlank(partitionName)) {
            request.setPartitionName(partitionName);
        }
        GetResp resp = client.get(request);
        return resp.getGetResults();
    }

    /**
     * @description Query
     * <p>By filtering expressions</p>
     *
     * @author 陈晨
     */
    public List<QueryResp.QueryResult> query(String collectionName, String partitionName, String filter, List<String> fields, int limit) {
        QueryReq request = QueryReq.builder()
                .collectionName(collectionName)
                .filter(filter)
                .outputFields(fields)
                .limit(limit)
                .build();
        if (StringUtils.isNotBlank(partitionName)) {
            request.setPartitionNames(Collections.singletonList(partitionName));
        }
        QueryResp resp = client.query(request);
        return resp.getQueryResults();
    }

    /**
     * @description QueryAll
     * <p>By filtering expressions</p>
     *
     * @author 陈晨
     */
    public List<QueryResultsWrapper.RowRecord> queryAll(String collectionName, String partitionName, String filter, List<String> fields) {
        QueryIteratorReq request = QueryIteratorReq.builder()
                .collectionName(collectionName)
                .expr(filter)
                .batchSize(50L)
                .outputFields(fields)
                .consistencyLevel(ConsistencyLevel.BOUNDED)
                .build();
        if (StringUtils.isNotBlank(partitionName)) {
            request.setPartitionNames(Collections.singletonList(partitionName));
        }
        QueryIterator iterator = client.queryIterator(request);
        List<QueryResultsWrapper.RowRecord> resultsAll = new ArrayList<>();
        while (true) {
            List<QueryResultsWrapper.RowRecord> results = iterator.next();
            if (results.isEmpty()) {
                iterator.close();
                break;
            }
            resultsAll.addAll(results);
        }
        return resultsAll;
    }

}


