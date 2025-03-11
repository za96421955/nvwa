package ai.nvwa.agent.model.embedding.mode;

import ai.nvwa.agent.model.ModelsEnum;
import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * 嵌入请求
 * <p> <功能详细描述> </p>
 *
 * @author 陈晨
 */
@Data
@AllArgsConstructor
public class EmbeddingRequest implements Serializable {
    private static final long serialVersionUID = 8601062970625317876L;

    private String model;
    private String input;
    /** 512/768/1024 */
    private Integer dimensions;
    private String encoding_format;

    public EmbeddingRequest() {
        this.encoding_format = "float";
    }

    public EmbeddingRequest(String model) {
        this.model = model;
    }

    public EmbeddingRequest(ModelsEnum model) {
        this(model.getModel());
    }

    public EmbeddingRequest input(String text) {
        this.input = text;
        return this;
    }

    public EmbeddingRequest dimensions(int dimensions) {
        this.dimensions = dimensions;
        return this;
    }

    @Override
    public String toString() {
        return JSONObject.toJSONString(this);
    }

}


