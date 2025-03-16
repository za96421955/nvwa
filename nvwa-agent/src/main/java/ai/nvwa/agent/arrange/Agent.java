package ai.nvwa.agent.arrange;

import ai.nvwa.agent.tool.extension.Extension;
import ai.nvwa.agent.tool.function.Function;

import java.util.List;

/**
 * 智能体
 * <p> <功能详细描述> </p>
 *
 * @author 陈晨
 */
public interface Agent {

    /**
     * @description 执行名 (智能体名)
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    String action();

    /**
     * @description 名称
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    String name();

    /**
     * @description 描述
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    String desc();

    /**
     * @description 关联
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    List<Function> association();

    /**
     * @description 提示词
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    String prompt();

    /**
     * @description 最大对话轮次
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    int chatMax();

    /**
     * @description 执行
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    String action(String ip, String question);

}


