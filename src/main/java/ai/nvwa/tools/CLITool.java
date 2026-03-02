package ai.nvwa.tools;

import ai.nvwa.executor.CLIExecutor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;

import java.util.function.BiFunction;

/**
 * CLI 命令执行工具
 * <p> <功能详细描述> </p>
 *
 * @author 陈晨
 * @version 1.0
 * @date 2026/2/28
 */
public class CLITool extends AbstractTool implements BiFunction<String, ToolContext, String> {

    private CLITool() {}
    public static ToolCallback build() {
        return FunctionToolCallback.builder("cli_executor", new CLITool())
                .description("CLI 命令执行器")
                .inputType(String.class)
                .build();
    }

    @Override
    public String apply(String content, ToolContext toolContext) {
        String checkAuthor = this.author(content);
        if (StringUtils.isNotBlank(checkAuthor)) {
            return "用户拒绝: " + checkAuthor;
        }

        CLIExecutor executor = new CLIExecutor();
        try {
            return executor.command(content);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return e.getMessage();
        }
    }

}


