package ai.nvwa.tools;

import ai.nvwa.agent.impl.SubAgent;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;
import java.util.function.BiFunction;

/**
 * 子智能体工具
 * <p> <功能详细描述> </p>
 *
 * @author 陈晨
 * @version 1.0
 * @date 2026/2/28
 */
public class SubAgentTool extends AbstractTool implements BiFunction<SubAgentTool.Task, ToolContext, String> {

    private SubAgentTool() {}
    public static ToolCallback build() {
        return FunctionToolCallback.builder("sub_agent", new SubAgentTool())
                .description("如果你不关心某个任务的具体过程，可以创建子智能体完成，直接获取结果")
                .inputType(Task.class)
                .build();
    }

    @Override
    public String apply(Task task, ToolContext toolContext) {
        String checkAuthor = this.author("createSubAgent " + task.getTaskContent());
        if (StringUtils.isNotBlank(checkAuthor)) {
            return "用户拒绝: " + checkAuthor;
        }

        System.out.println("任务: " + task);
        try {
            SubAgent agent = new SubAgent();
            String result = agent.chat(
                    task.toString(),
                    RunnableConfig.builder().threadId(UUID.randomUUID().toString()).build()
            );
            System.out.println("任务完成: " + result);
            return result;
        } catch (Exception e) {
            System.err.println("任务失败: " + e.getMessage());
            return e.getMessage();
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class Task implements Serializable {
        @Serial
        private static final long serialVersionUID = -837347855346360816L;

        private String referenceInformation;
        private String taskContent;

        @Override
        public String toString() {
            return "## 参考信息: " +
                    "\n```" +
                    "\n" + referenceInformation +
                    "\n```" +
                    "## 任务内容: " +
                    "\n```" +
                    "\n" + taskContent +
                    "\n```";
        }

    }

}


