package ai.nvwa.agent.model.embedding.mode;

import ai.nvwa.agent.model.ModelsEnum;
import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 多模态嵌入请求
 * <p> <功能详细描述> </p>
 *
 * @author 陈晨
 * @version 1.0
 */
@Data
@AllArgsConstructor
public class EmbeddingMultimodalRequest implements Serializable {
    private static final long serialVersionUID = 7901813459352037992L;

    private String model;
    private Input input;

    public EmbeddingMultimodalRequest() {
        this.model = ModelsEnum.MULTIMODAL_EMBEDDING_V1.getModel();
        this.input = new Input();
    }

    public EmbeddingMultimodalRequest(ModelsEnum model) {
        this.model = model.getModel();
    }

    public EmbeddingMultimodalRequest content(String text, String image, String video) {
        this.input.content(text, image, video);
        return this;
    }

    public static EmbeddingMultimodalRequest text(String text) {
        return new EmbeddingMultimodalRequest().content(text, null, null);
    }

    public static EmbeddingMultimodalRequest image(String image) {
        return new EmbeddingMultimodalRequest().content(null, image, null);
    }

    public static EmbeddingMultimodalRequest video(String video) {
        return new EmbeddingMultimodalRequest().content(null, null, video);
    }


    @Override
    public String toString() {
        return JSONObject.toJSONString(this);
    }

    /**
     {
         "contents": [
             {"text": "通用多模态表征模型"},
             {"image": "https://xxxx.com/xxx/images/xxx.jpg"},
             {"video": "https://xxx.com/xxx/video/xxx.mp4"}
         ]
     }
     */
    @Data
    @AllArgsConstructor
    public static class Input implements Serializable {
        private static final long serialVersionUID = -2592676220821628683L;

        private List<Content> contents;

        public Input() {
            this.contents = new ArrayList<>();
        }

        public Input content(String text, String image, String video) {
            this.contents.add(new Content(text, image, video));
            return this;
        }

//        public static Input text(String text) {
//            return new Input().content(text, null, null);
//        }
//
//        public static Input image(String image) {
//            return new Input().content(null, image, null);
//        }
//
//        public static Input video(String video) {
//            return new Input().content(null, null, video);
//        }

    }

    /**
     * 多模态表征输入的具体内容
     * 其中text embedding相关的输入可直接传入string字段，不需要通过dict进行类型传入
     * 多模态类型支持text、image、video类型, 不同类型的参数值均为String类型，格式参考如下{"模态类型": "输入字符串或图像、视频url"}
     * 图片使用BASE64数据：将编码后的BASE64数据传递给image参数，格式为data:image/{format};base64,{base64_image}，其中：
     * image/{format}：本地图像的格式。请根据实际的图像格式，例如图片为jpg格式，则设置为image/jpeg
     * base64_image：图像的BASE64数据
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Content implements Serializable {
        private static final long serialVersionUID = -3619251953765842328L;

        private String text;
        private String image;
        private String video;

//        public static Content text(String text) {
//            return new Content(text, null, null);
//        }
//
//        public static Content image(String image) {
//            return new Content(null, image, null);
//        }
//
//        public static Content video(String video) {
//            return new Content(null, null, video);
//        }

    }

}


