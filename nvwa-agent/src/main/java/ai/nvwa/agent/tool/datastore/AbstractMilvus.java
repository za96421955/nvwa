package ai.nvwa.agent.tool.datastore;

import ai.nvwa.agent.model.embedding.EmbeddingService;
import ai.nvwa.agent.model.embedding.mode.EmbeddingContent;
import ai.nvwa.agent.tool.datastore.extract.Extractor;
import ai.nvwa.agent.tool.datastore.milvus.MilvusTemplate;
import io.milvus.v2.common.ConsistencyLevel;
import io.milvus.v2.common.IndexParam;
import io.milvus.v2.service.collection.request.CreateCollectionReq;
import io.milvus.v2.service.vector.request.AnnSearchReq;
import io.milvus.v2.service.vector.request.HybridSearchReq;
import io.milvus.v2.service.vector.request.SearchReq;
import io.milvus.v2.service.vector.request.data.BaseVector;
import io.milvus.v2.service.vector.request.data.FloatVec;
import io.milvus.v2.service.vector.request.ranker.RRFRanker;
import io.milvus.v2.service.vector.response.SearchResp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Milvus抽象
 * <p> <功能详细描述> </p>
 *
 * @author 陈晨
 */
@Slf4j
public abstract class AbstractMilvus {

    @Autowired
    protected MilvusTemplate milvusTemplate;
    @Autowired
    protected EmbeddingService embeddingService;
    @Autowired
    protected Extractor extractor;

    @PostConstruct
    public void init() {
        // 创建数据库
        if (!milvusTemplate.database().check(this.getDbName())) {
            milvusTemplate.database().create(this.getDbName());
            log.info("[datastore-文档] 创建数据库: {}", this.getDbName());
        }
        // 创建集合
//        milvusTemplate.collection().drop(this.getCollectionName());
        if (!milvusTemplate.collection().check(this.getCollectionName())) {
            milvusTemplate.collection().create(this.getCollectionName(), this.generateSchema(), this.generateIndex());
            log.info("[datastore-文档] 创建集合: {}", this.getCollectionName());
        }
        // 加载集合
        milvusTemplate.collection().load(this.getCollectionName());
        log.info("[datastore-文档] 加载集合: {}", this.getCollectionName());
    }

    /**
     * @description 数据库名
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    protected abstract String getDbName();

    /**
     * @description 集合名
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    protected abstract String getCollectionName();

    /**
     * @description 生成Schema
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    protected abstract CreateCollectionReq.CollectionSchema generateSchema();

    /**
     * @description 生成索引
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    protected abstract List<IndexParam> generateIndex();


    /**
     * @description content查询
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public List<Entity> query(String content, float minScore) {
        EmbeddingContent titleEmbedding = embeddingService.getEmbeddingContent(content, 1024);
        List<BaseVector> queryDenseVectors = Collections.singletonList(new FloatVec(titleEmbedding.getDenseVectors()));
        List<Entity> entityList = this.buildEntity(milvusTemplate.search().search(SearchReq.builder()
                .collectionName(this.getCollectionName())
                .data(queryDenseVectors)
                .annsField(this.vectorField())
                .metricType(IndexParam.MetricType.COSINE)
                .outputFields(this.outputFields())
                .topK(10)
                .build()));
        return entityList.stream().filter(entity -> entity.getScore() > minScore).collect(Collectors.toList());
    }

    /**
     * @description 向量字段名
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    protected abstract String vectorField();

    /**
     * @description 输出字段
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    protected abstract List<String> outputFields();

    /**
     * @description contents查询
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public List<Entity> queryRanker(List<String> contents) {
        List<AnnSearchReq> searchRequests = new ArrayList<>();
        for (String content : contents) {
            EmbeddingContent titleEmbedding = embeddingService.getEmbeddingContent(content, 1024);
            List<BaseVector> queryDenseVectors = Collections.singletonList(new FloatVec(titleEmbedding.getDenseVectors()));
            searchRequests.add(AnnSearchReq.builder()
                    .vectorFieldName(this.vectorField())
                    .vectors(queryDenseVectors)
                    .metricType(IndexParam.MetricType.COSINE)
                    /**
                     要搜索的群集数量
                     搜索候选集群的集群数。数值越大，搜索的集群数越多，通过扩大搜索范围提高召回率，但代价是查询延迟增加。

                     值范围:
                     类型：整数
                     范围： [1, nlist[1，nlist］
                     默认值：8

                     调整建议:
                     增加该值可提高召回率，但可能会减慢搜索速度。设置nprobe 与nlist 成比例，以平衡速度和准确性。
                     在大多数情况下，我们建议您在此范围内设置一个值：[1，nlist]。
                     */
                    .params("{\"nprobe\": 8}")     // 默认：8，<100万向量：16
                    .topK(3)
                    .build());
        }
        return this.buildEntity(milvusTemplate.search().search(HybridSearchReq.builder()
                .collectionName(this.getCollectionName())
                .searchRequests(searchRequests)
                .ranker(new RRFRanker(100))
                .outFields(this.outputFields())
                .topK(Math.max(searchRequests.size(), 10))
                .consistencyLevel(ConsistencyLevel.BOUNDED)
                .build()));
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
     * @description 构建entity
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    protected List<Entity> buildEntity(List<List<SearchResp.SearchResult>> resultList) {
        if (CollectionUtils.isEmpty(resultList)) {
            return Collections.emptyList();
        }
        List<Entity> entityList = new ArrayList<>();
        for (List<SearchResp.SearchResult> list : resultList) {
            for (SearchResp.SearchResult result : list) {
                entityList.add(this.buildEntity(result));
            }
        }
        return entityList;
    }

    /**
     * @description 构建entity
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    protected abstract Entity buildEntity(SearchResp.SearchResult result);

    /**
     * @description 重复数据检查
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    protected boolean isRepeat(String content) {
        return !CollectionUtils.isEmpty(this.query(content, 0.99f));
    }

}


