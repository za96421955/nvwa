package ai.nvwa.agent.model.embedding;

import ai.nvwa.agent.model.CloudConfig;
import ai.nvwa.agent.components.util.HttpClient;
import ai.nvwa.agent.model.ModelsEnum;
import ai.nvwa.agent.model.embedding.mode.EmbeddingMultimodalRequest;
import ai.nvwa.agent.model.embedding.mode.EmbeddingMultimodalResponse;
import ai.nvwa.agent.model.embedding.mode.EmbeddingRequest;
import ai.nvwa.agent.model.embedding.mode.EmbeddingResponse;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @description 嵌入服务
 * <p> <功能详细描述> </p>
 *
 * @author 陈晨
 */
@Service
public class EmbeddingService {

    /**
     * @description 获取多模态密集向量
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public List<Float> getMultimodalDenseVectors(String text) {
        String result = HttpClient.post(CloudConfig.Alibaba.Url.EMBEDDING_MULTIMODAL)
                .authorization(CloudConfig.Alibaba.AUTH)
                .body(EmbeddingMultimodalRequest.text(text).toString())
                .asString();
        EmbeddingMultimodalResponse response = JSONObject.parseObject(result, EmbeddingMultimodalResponse.class);
        return response.getOutput().getEmbeddings().get(0).getEmbedding();
    }

    /**
     * @description 获取文本密集向量
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public List<Float> getTextDenseVectors(String text) {
        String result = HttpClient.post(CloudConfig.Alibaba.Url.EMBEDDING)
                .authorization(CloudConfig.Alibaba.AUTH)
                .body(new EmbeddingRequest(ModelsEnum.TEXT_EMBEDDING_V3).input(text).dimensions(768).toString())
                .asString();
        EmbeddingResponse response = JSONObject.parseObject(result, EmbeddingResponse.class);
        return response.getData().get(0).getEmbedding();
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
            if (denseVectors.get(i) > 0) {
                sparseVectors.put(i, denseVectors.get(i));
            }
        }
        return sparseVectors;
    }

//    public static void main(String[] args) {
//        String text = "Artificial intelligence was founded as an academic discipline in 1956.";
//
//        EmbeddingService embeddingService = new EmbeddingService();
//        List<Float> denseVectors = embeddingService.getTextDenseVectors(text);
//        System.out.println(denseVectors);
//        System.out.println("size: " + denseVectors.size());
////        Map<Integer, Float> sparseVectors = embeddingService.toSparseVector(denseVectors);
//    }

}


