package ai.nvwa.agent.impl;

import ai.nvwa.agent.NvwaAgent;

/**
 * @description 执行智能体
 * <p> <功能详细描述> </p>
 *
 * @author 陈晨
 * @date 2026/2/28 10:18
 */
public class SubAgent extends MainAgent implements NvwaAgent {

    @Override
    public String getPrompt() {
        return "1. 你直接使用 CLI 工具完成任务" +
                "\n2. 如果有必要，你可以编写 shell 脚本通过 CLI 执行" +
                "\n3. 如果有必要，你可以创建 subAgent 分配子任务";
    }

}


