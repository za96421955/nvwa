package ai.nvwa.agent.arrange;

import ai.nvwa.agent.model.chat.mode.ChatRequest;
import ai.nvwa.agent.model.chat.mode.ChatResult;
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
     * @description 知识库
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    String datastore(String question);

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
    String action(String ip, String question, Process process);

    /**
     * @description 执行过程
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    interface Process {
        /**
         * @description 对话前
         * <p> <功能详细描述> </p>
         *
         * @author 陈晨
         */
        void assistantBefore(int loop, ChatRequest request);

        /**
         * @description 思考中
         * <p> <功能详细描述> </p>
         *
         * @author 陈晨
         */
        void reasoning(int loop, String reasoning);

        /**
         * @description 回答中
         * <p> <功能详细描述> </p>
         *
         * @author 陈晨
         */
        void content(int loop, String content);

        /**
         * @description 对话后
         * <p> <功能详细描述> </p>
         *
         * @author 陈晨
         */
        void assistantAfter(int loop, ChatResult result);

//        /**
//         * @description 执行结束/终止
//         * <p> <功能详细描述> </p>
//         *
//         * @author 陈晨
//         */
//        void termination(Map<Integer, ChatResult> chatResultMap);
    }

}


