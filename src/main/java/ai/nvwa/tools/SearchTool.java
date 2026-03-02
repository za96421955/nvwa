package ai.nvwa.tools;

import ai.nvwa.util.HttpClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;

import java.util.function.BiFunction;

/**
 * 联网搜索工具
 * <p> <功能详细描述> </p>
 *
 * @author 陈晨
 * @version 1.0
 * @date 2026/2/28
 */
public class SearchTool extends AbstractTool implements BiFunction<String, ToolContext, String> {

    // 普通模型，可以按自定义的 ReAct 循环执行
    // 深度思考模型，会一次性把事情做完，ReAct 智能体慎用
    private static final String MODEL = "qwen-flash";
    private static final String MODEL_THINKING = "qwen3.5-plus";

    private SearchTool() {}
    public static ToolCallback build() {
        return FunctionToolCallback.builder("internet_search", new SearchTool())
                .description("联网搜索")
                .inputType(String.class)
                .build();
    }

    @Override
    public String apply(String content, ToolContext toolContext) {
        String checkAuthor = this.author("internetSearch " + content);
        if (StringUtils.isNotBlank(checkAuthor)) {
            return "用户拒绝: " + checkAuthor;
        }

        String result = HttpClient.post("https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + System.getenv("AI_API_KEY"))
                .body("{\n" +
                        "    \"model\": \"" + MODEL_THINKING + "\",\n" +
                        "    \"messages\": [\n" +
                        "        {\n" +
                        "            \"role\": \"user\",\n" +
                        "            \"content\": \"" + content + "\"\n" +
                        "        }\n" +
                        "    ],\n" +
                        "    \"enable_search\": true,\n" +
                        "    \"enable_thinking\": false\n" +
                        "}")
                .asString();
        System.out.println("搜索结果: " + result);
        return result;
    }

}


