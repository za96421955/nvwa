package ai.nvwa.model.embedding;

import ai.nvwa.components.redis.RedisService;
import ai.nvwa.components.util.HashUtil;
import ai.nvwa.components.util.HttpClient;
import ai.nvwa.model.CloudConfig;
import ai.nvwa.model.ModelsEnum;
import ai.nvwa.model.embedding.mode.*;
import ai.nvwa.model.embedding.mode.*;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @description 嵌入服务
 * <p> <功能详细描述> </p>
 *
 * @author 陈晨
 */
@Service
public class EmbeddingService {
    private static final String TEXT_DENSE = "nvwa:ds:milvus:dense:text:";

    @Autowired
    private RedisService redisService;

    /**
     * @description 获取多模态密集向量
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public List<Float> getMultimodalDenseVectors(String text) {
        String key = TEXT_DENSE + HashUtil.hash32(text);
        List<Double> densesCache = redisService.getList(key);
        if (!CollectionUtils.isEmpty(densesCache)) {
            return densesCache.stream().map(Double::floatValue).collect(Collectors.toList());
        }
        String result = HttpClient.post(CloudConfig.Alibaba.Url.EMBEDDING_MULTIMODAL)
                .authorization(CloudConfig.Alibaba.AUTH)
                .body(EmbeddingMultimodalRequest.text(text).toString())
                .asString();
        EmbeddingMultimodalResponse response = JSONObject.parseObject(result, EmbeddingMultimodalResponse.class);
        List<Float> denses = response.getOutput().getEmbeddings().get(0).getEmbedding();
        redisService.setList(key, denses);
        return denses;
    }

    /**
     * @description 获取文本密集向量
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public List<Float> getTextDenseVectors(String text, int dimension) {
        String key = TEXT_DENSE + HashUtil.hash32(text) + ":" + dimension;
        List<Double> densesCache = redisService.getList(key);
        if (!CollectionUtils.isEmpty(densesCache)) {
            return densesCache.stream().map(Double::floatValue).collect(Collectors.toList());
        }
        String result = HttpClient.post(CloudConfig.Alibaba.Url.EMBEDDING)
                .authorization(CloudConfig.Alibaba.AUTH)
                .body(new EmbeddingRequest(ModelsEnum.TEXT_EMBEDDING_V3).input(text).dimensions(dimension).toString())
                .asString();
        EmbeddingResponse response = JSONObject.parseObject(result, EmbeddingResponse.class);
        List<Float> denses = response.getData().get(0).getEmbedding();
        redisService.setList(key, denses);
        return denses;
    }

    /**
     * @description 密集向量 => 稀疏向量
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public Map<Integer, Float> toSparseVector(List<Float> denseVectors) {
        Map<Integer, Float> sparseVectors = new HashMap<>();
        for (int i = 0; i < denseVectors.size(); i++) {
            float vector = denseVectors.get(i);
            if (vector > 0) {
                sparseVectors.put(i, vector);
            }
        }
        return sparseVectors;
    }

    /**
     * @description 获取嵌入内容
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public EmbeddingContent getEmbeddingContent(String content, int dimension) {
        List<Float> denseVectors = this.getTextDenseVectors(content, dimension);
        Map<Integer, Float> sparseVectors = this.toSparseVector(denseVectors);
        return EmbeddingContent.builder()
                .content(content)
                .denseVectors(denseVectors)
                .sparseVectors(sparseVectors)
                .build();
    }

}


