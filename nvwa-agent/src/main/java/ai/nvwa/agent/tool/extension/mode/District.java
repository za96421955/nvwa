package ai.nvwa.agent.tool.extension.mode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 行政区划
 * <p> <功能详细描述> </p>
 *
 * @author 陈晨
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class District implements Serializable {
    private static final long serialVersionUID = 8594417022157962431L;

    private String id;
    private String name;
    private String fullName;
    /** 纬度 */
    private String lat;
    /** 经度 */
    private String lng;

}


