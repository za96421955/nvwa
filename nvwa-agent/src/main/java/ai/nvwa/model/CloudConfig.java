package ai.nvwa.model;

/**
 * 云厂商配置
 * <p> <功能详细描述> </p>
 *
 * @author 陈晨
 */
public interface CloudConfig {

    /**
     * 阿里配置
     */
    interface Alibaba {
        /** 认证 */
        String AUTH = "Bearer sk-2b416845775f4f028deecc3ce219b27e";
        /** URL */
        interface Url {
            /** 对话 */
            String CHAT = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";
            /** 嵌入 */
            String EMBEDDING = "https://dashscope.aliyuncs.com/compatible-mode/v1/embeddings";
            /** 多模态嵌入 */
            String EMBEDDING_MULTIMODAL = "https://dashscope.aliyuncs.com/api/v1/services/embeddings/multimodal-embedding/multimodal-embedding";
        }
        /** 向量维度 */
        interface Dimensions {
            int D_512 = 512;
            int D_768 = 768;
            int D_1024 = 1024;
        }
    }

}


