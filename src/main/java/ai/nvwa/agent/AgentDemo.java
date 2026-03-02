package ai.nvwa.agent;

import ai.nvwa.util.HttpClient;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.hook.hip.HumanInTheLoopHook;
import com.alibaba.cloud.ai.graph.agent.hook.modelcalllimit.ModelCallLimitHook;
import com.alibaba.cloud.ai.graph.agent.hook.pii.PIIDetectionHook;
import com.alibaba.cloud.ai.graph.agent.hook.pii.PIIType;
import com.alibaba.cloud.ai.graph.agent.hook.pii.RedactionStrategy;
import com.alibaba.cloud.ai.graph.agent.hook.shelltool.ShellToolAgentHook;
import com.alibaba.cloud.ai.graph.agent.hook.skills.SkillsAgentHook;
import com.alibaba.cloud.ai.graph.agent.hook.summarization.SummarizationHook;
import com.alibaba.cloud.ai.graph.agent.interceptor.ToolCallHandler;
import com.alibaba.cloud.ai.graph.agent.interceptor.ToolCallRequest;
import com.alibaba.cloud.ai.graph.agent.interceptor.ToolCallResponse;
import com.alibaba.cloud.ai.graph.agent.interceptor.ToolInterceptor;
import com.alibaba.cloud.ai.graph.agent.interceptor.todolist.TodoListInterceptor;
import com.alibaba.cloud.ai.graph.agent.interceptor.toolretry.ToolRetryInterceptor;
import com.alibaba.cloud.ai.graph.agent.tools.ShellTool2;
import com.alibaba.cloud.ai.graph.checkpoint.BaseCheckpointSaver;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.alibaba.cloud.ai.graph.skills.registry.SkillRegistry;
import com.alibaba.cloud.ai.graph.skills.registry.classpath.ClasspathSkillRegistry;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.deepseek.DeepSeekChatOptions;
import org.springframework.ai.deepseek.api.DeepSeekApi;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;
import java.util.function.BiFunction;

/**
 * TODO
 * <p> <功能详细描述> </p>
 *
 * @author 陈晨
 * @version 1.0
 * @date 2026/2/25
 */
public class AgentDemo {

    // 普通模型，可以按自定义的 ReAct 循环执行
    // 深度思考模型，会一次性把事情做完，ReAct 智能体慎用
    private static final String MODEL = "qwen-flash";
    private static final String MODEL_THINKING = "qwen3.5-plus";

//    public static RedissonClient redissonClient;

    public static void main(String[] args) throws GraphRunnerException {
        // 初始化 ChatModel
        DeepSeekApi scopeApi = DeepSeekApi.builder()
                .baseUrl(System.getenv("AI_BASE_URL"))
                .apiKey(System.getenv("AI_API_KEY"))
                .build();
        ChatModel chatModel = DeepSeekChatModel.builder()
                .deepSeekApi(scopeApi)
                .defaultOptions(DeepSeekChatOptions.builder()
                        .model(MODEL)
                        .build())
                .build();

        /**
         * tool（工具）
         */
        // 天气 tool
        ToolCallback weatherTool = FunctionToolCallback.builder("getWeather", new WeatherTool())
                .description("查询指定城市的天气情况")
                .inputType(String.class)
                .build();
        // 联网搜索 tool
        ToolCallback searchTool = FunctionToolCallback.builder("search", new SearchTool())
                .description("联网搜索")
                .inputType(String.class)
                .build();

        /**
         * Hook（AOP）
         */
        // https://java2ai.com/docs/frameworks/agent-framework/tutorials/hooks
        // 创建消息压缩 Hook（上下文压缩）
        SummarizationHook summarizationHook = SummarizationHook.builder()
                .model(chatModel)
                .maxTokensBeforeSummary(4000)
                .messagesToKeep(20)
                .build();
        // 模型调用限制
        ModelCallLimitHook modelCallLimitHook = ModelCallLimitHook.builder()
                .runLimit(5)
                .build();
        // PII 检测（Personally Identifiable Information）
        PIIDetectionHook pii = PIIDetectionHook.builder()
                .piiType(PIIType.EMAIL)
                .strategy(RedactionStrategy.REDACT)
                .applyToInput(true)
                .build();
        /*
         * 人类在环
         */
        // 创建 Human-in-the-Loop Hook
        HumanInTheLoopHook humanReviewHook = HumanInTheLoopHook.builder()
//                .approvalOn("sendEmailTool", "Please confirm sending the email.")
                .approvalOn("search", "确认发起搜索")
                .build();

        /**
         * 拦截器
         */
        // https://java2ai.com/docs/frameworks/agent-framework/tutorials/hooks
        // 自定义：工具调用错误处理
        ToolErrorInterceptor toolErrorInterceptor = new ToolErrorInterceptor();
        // 工具调用重试
        ToolRetryInterceptor toolRetryInterceptor = ToolRetryInterceptor.builder()
                .maxRetries(2)
                .onFailure(ToolRetryInterceptor.OnFailureBehavior.RETURN_MESSAGE)
                .build();
        // Planning（规划）
        TodoListInterceptor todoListInterceptor = TodoListInterceptor.builder().build();
        // LLM Tool Selector（LLM 工具选择器），利用最后一条用户消息作为工具选择依据
//        ToolSelectionInterceptor.builder().build();
        // LLM Tool Emulator（LLM 工具模拟器）
//        ToolEmulatorInterceptor.builder().model(chatModel).build();
        // Context Editing（上下文编辑）
//        ContextEditingInterceptor.builder().trigger(120000).clearAtLeast(60000).build();

        /**
         * Skills 技能
         */
        // https://java2ai.com/docs/frameworks/agent-framework/tutorials/skills
        // 技能通常需配合脚本执行（如技能目录下的 Python 脚本）和 Shell 命令才能在生产环境中正常使用。
        // 技能名称一致：name、read_skill 参数、groupedTools 的 key 保持一致。
        // 按需使用 groupedTools：仅需「随技能激活」的工具用 groupedTools，其余用 Agent 的 .tools() 即可。
        // 控制 SKILL.md 大小：单文件建议约 1.5k–2k tokens，长内容放 references/ 并在正文中列路径。
        SkillRegistry registry = ClasspathSkillRegistry.builder()
                .classpathPath("skills")
                .build();
        SkillsAgentHook skillsAgentHook = SkillsAgentHook.builder()
                .skillRegistry(registry)
                .autoReload(true)       // 自动重载技能
                .build();
        // Shell Hook：提供 Shell 命令执行（工作目录可指定，如当前工程目录）
        ShellToolAgentHook shellHook = ShellToolAgentHook.builder()
                .shellTool2(ShellTool2.builder(System.getProperty("user.dir")).build())
                .build();
        // PythonTool 提供 Python 执行能力
//        PythonTool.createPythonToolCallback(PythonTool.DESCRIPTION);

        /**
         * 记忆
         */
        BaseCheckpointSaver saver = new MemorySaver();
//        saver = RedisSaver.builder()
//                .redisson(null)
//                .stateSerializer(null)
//                .build();

        /**
         * 结构化输出
         */
        // 使用 BeanOutputConverter 生成 outputSchema
        BeanOutputConverter<TodayInfo> outputConverter = new BeanOutputConverter<>(TodayInfo.class);
        String outputFormat = outputConverter.getFormat();


        // 创建 agent
        /*
            执行顺序
            ReactAgent agent = ReactAgent.builder()
              .name("my_agent")
              .model(chatModel)
              .hooks(hook1, hook2, hook3)
              .interceptors(interceptor1, interceptor2)
              .interceptors(toolInterceptor1, toolInterceptor2)
              .build();

            执行流程：
                Before Agent Hooks（按顺序）:
                    hook1.beforeAgent()
                    hook2.beforeAgent()
                    hook3.beforeAgent()

                Agent 循环开始
                    Before Model Hooks（按顺序）:
                        hook1.beforeModel()
                        hook2.beforeModel()
                        hook3.beforeModel()
                    Model Interceptors（嵌套调用）:
                        interceptor1 → interceptor2 → 模型调用
                    After Model Hooks（逆序）:
                        hook3.afterModel()
                        hook2.afterModel()
                        hook1.afterModel()
                    Tool Interceptors（如果有工具调用，嵌套调用）:
                        toolInterceptor1 → toolInterceptor2 → 工具执行
                Agent 循环结束

                After Agent Hooks（逆序）:
                    hook3.afterAgent()
                    hook2.afterAgent()
                    hook1.afterAgent()

                关键规则：
                    before_* hooks: 从第一个到最后一个
                    after_* hooks: 从最后一个到第一个（逆序）
                    Interceptors: 嵌套调用（第一个拦截器包装所有其他的）
         */
        ReactAgent agent = ReactAgent.builder()
                .name("demo_agent")
                .model(chatModel)
                .tools(weatherTool, searchTool)
                .systemPrompt("You are a helpful assistant")
//                .hooks(skillsAgentHook, shellHook)
                .hooks(summarizationHook, modelCallLimitHook, pii)
//                .hooks(humanReviewHook)
                .interceptors(toolErrorInterceptor)
//                .interceptors(todoListInterceptor)
                .interceptors(toolRetryInterceptor)
                .saver(saver)
                .enableLogging(true)
//                .outputType(String.class)       // outputType(Class<?> type): 提供 Java 类 - 使用 BeanOutputConverter 自动转换为 JSON schema（推荐方式，类型安全）
                .outputSchema(outputFormat)
                .build();

        // 使用 thread_id 维护对话上下文
        String threadId = UUID.randomUUID().toString();

        // RunnableConfig 全局传递上下文
        RunnableConfig config = RunnableConfig.builder()
                .threadId(threadId)
                .build();

        // 运行 agent
        AssistantMessage response = agent.call("南京现在的天气？有什么重大的事情？", config);
        System.out.println("\n------------------------------------------");
        System.out.println(response.getText());
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class TodayInfo implements Serializable {
        @Serial
        private static final long serialVersionUID = 137559070021522207L;

        private String date;
        private String weather;
        private String news;

    }

    // 定义天气查询工具
    static class WeatherTool implements BiFunction<String, ToolContext, String> {
        @Override
        public String apply(String city, ToolContext toolContext) {
            System.out.println("\n------------------------------------------");
            System.out.println("city: " + city);
            System.out.println("context: " + toolContext.getContext());
            String result = HttpClient.post("https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions")
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + System.getenv("AI_API_KEY"))
                    .body("{\n" +
                            "    \"model\": \"" + MODEL_THINKING + "\",\n" +
                            "    \"messages\": [\n" +
                            "        {\n" +
                            "            \"role\": \"user\",\n" +
                            "            \"content\": \"查询 " + city + " 天气\"\n" +
                            "        }\n" +
                            "    ],\n" +
                            "    \"enable_search\": true,\n" +
                            "    \"enable_thinking\": false\n" +
                            "}")
                    .asString();
            System.out.println("天气查询结果: " + result);
            System.out.println("------------------------------------------\n");

//            return "Today " + DateUtil.formatDateTime(DateUtil.now()) + ", " + result;
            return result;
        }
    }

    // 联网搜索工具
    static class SearchTool implements BiFunction<String, ToolContext, String> {
        @Override
        public String apply(String content, ToolContext toolContext) {
            System.out.println("\n------------------------------------------");
            System.out.println("联网搜索: " + content);
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
            System.out.println("联网搜索结果: " + result);
            System.out.println("------------------------------------------\n");

            return result;
        }
    }

    // 工具错误处理
    static class ToolErrorInterceptor extends ToolInterceptor {
        @Override
        public ToolCallResponse interceptToolCall(ToolCallRequest request, ToolCallHandler handler) {
            try {
                return handler.call(request);
            } catch (Exception e) {
                return ToolCallResponse.of(request.getToolCallId(), request.getToolName(),
                        "Tool failed: " + e.getMessage());
            }
        }

        @Override
        public String getName() {
            return "ToolErrorInterceptor";
        }
    }

}


