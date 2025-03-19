package ai.nvwa.agent.tool.function.mode;

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
 * @version 1.0
 * @date 2025/3/17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Action implements Serializable {
    private static final long serialVersionUID = -993585501587766955L;

    /** 名称 */
    private String action;
    /** 输入 */
    private JSONObject input;
    /** 响应 */
    private String response;

}


