package ai.nvwa.tool.function.mode;

import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 执行
 * <p> <功能详细描述> </p>
 *
 * @author 陈晨
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Action implements Serializable {
    private static final long serialVersionUID = -993585501587766955L;

    /** 思考 */
    private String thought;
    /** 动作 */
    private String action;
    /** 输入 */
    private JSONObject input;
    /** 起始位置 */
    private int start;
    /** 响应 */
    private String response;

    @Override
    public String toString() {
        return JSONObject.toJSONString(this);
    }

}


