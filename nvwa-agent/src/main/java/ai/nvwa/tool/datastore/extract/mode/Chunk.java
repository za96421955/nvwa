package ai.nvwa.tool.datastore.extract.mode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * 分块
 * <p> <功能详细描述> </p>
 *
 * @author 陈晨
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Chunk implements Serializable {
    private static final long serialVersionUID = 4947400380627631825L;

    private String text;
    private Map<String, Object> metas;

}


