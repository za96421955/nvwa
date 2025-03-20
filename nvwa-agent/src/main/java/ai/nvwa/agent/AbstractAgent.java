package ai.nvwa.agent;

import ai.nvwa.components.util.DateUtil;
import ai.nvwa.model.ModelAssembler;
import ai.nvwa.model.chat.ChatService;
import ai.nvwa.model.chat.mode.AlibabaChatRequest;
import ai.nvwa.model.chat.mode.ChatRequest;
import ai.nvwa.model.chat.mode.ChatResult;
import ai.nvwa.agent.prompt.AgentPrompt;
import ai.nvwa.tool.datastore.DataSet;
import ai.nvwa.tool.datastore.Document;
import ai.nvwa.tool.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Agent抽象
 * <p> <功能详细描述> </p>
 *
 * @author 陈晨
 */
@Slf4j
public abstract class AbstractAgent implements Agent {

    protected static final Pattern THOUGHT_PATTERN = Pattern.compile(
            "Thought[：:]\\s*" +      // 匹配 Thought:（支持中英文冒号）
                    "(?i)" +                      // 忽略大小写（如 ANSWER: Address）
                    "(.*?)" +                        // 非贪婪匹配所有后续内容
                    "(?=\\b(Action|Answer)\\b)" // Action前结束
            , Pattern.DOTALL
    );

    protected static final Pattern ACTION_PATTERN = Pattern.compile(
            "Action[：:]\\s*" +              // 匹配 "Action: "（允许空格和制表符）
            "(?i)" +                    // 忽略大小写（如 ACTION: Address）
            "(\\w+(?:[-_\\.]\\w+)*)" +     // 支持字母、数字、.-_ 的方法名
            "(?<=\\b)"                  // 确保单词边界（不消耗字符）
    );

    protected static final Pattern ACTION_INPUT_PATTERN = Pattern.compile(
            "(?i)" +                                // 忽略大小写
            "\\bAction\\sInput[：:]\\s*" +              // 参数关键字后的空白符
            "(\\{(?:[^{}]|\\{[^{}]*\\})*\\})" +     // 完整匹配嵌套JSON
            "(?=\\s*(Observation|Thought|Action|Answer))" // 动态终止关键字
    );

    protected static final Pattern ANSWER_PATTERN = Pattern.compile(
            "(?i)" +                      // 忽略大小写
            "Answer[：:]\\s*" +      // 匹配 Answer:（支持中英文冒号）
            "(.*)"                        // 非贪婪匹配所有后续内容
            , Pattern.DOTALL
    );

    @Autowired
    protected Map<String, Function> functionMap;

    @Autowired
    protected ModelAssembler modelAssembler;
    @Autowired
    protected ChatService chatService;
    @Autowired
    protected DataSet dataSet;
    @Override
    public String action(String ip, String question, Process process) {
        // 构建对话请求
        ChatRequest request = this.buildRequest(ip, question);
        // 开启多轮对话
        int loop = 1;
        Map<Integer, ChatResult> chatResultMap = new HashMap<>();
        String answer;
        do {
            // 对话前
            this.assistantBefore(loop, request);
            if (null != process) {
                process.assistantBefore(loop, request);
            }
            // 对话开始
            chatResultMap.put(loop, new ChatResult());
            ChatResult result = chatResultMap.get(loop);
            result.setRequest(modelAssembler.copy(request));
            int finalLoop = loop;
            chatService.chat(request, (reasoning, content, usage) -> {
                if (StringUtils.isNotBlank(reasoning)) {
                    result.getReasoning().append(reasoning);
                }
                if (StringUtils.isNotBlank(content)) {
                    result.getContent().append(content);
                }
                if (null != usage) {
                    result.setUsage(usage);
                }
                // 对话中
                if (null != process) {
                    process.assistant(finalLoop, result);
                }
            });
            // 对话处理
            answer = this.assistantHandle(loop, request, result);
            // 对话后
            this.assistantAfter(loop, result);
            if (null != process) {
                process.assistantAfter(loop, result);
            }
            loop++;
        } while (StringUtils.isBlank(answer));

        // 执行结束/终止通知
        this.termination(chatResultMap);
        // 返回答案
        return answer;
    }

    /**
     * @description 处理LLM回答
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    protected abstract String assistantHandle(int loop, ChatRequest request, ChatResult result);

    /**
     * @description 构建对话请求
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    protected ChatRequest buildRequest(String ip, String question) {
        return AlibabaChatRequest.llama()
                .user(this.formatPrompt().replaceFirst(AgentPrompt.DATASTORE_INFO, this.datastore(question)))
                .user("现在是: " + DateUtil.formatDateTime(DateUtil.now()))
                .user("我的IP是: " + ip)
                .user(question)
                .stream();
    }

    /**
     * @description 格式化提示词
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    protected String formatPrompt() {
        StringBuilder descs = new StringBuilder();
        StringBuilder names = new StringBuilder("{");
        for (Function function : this.association()) {
            descs.append("\n```\n")
                    .append("Action: ").append(function.action()).append("\n")
                    .append("Action Input: ").append(function.inputFormat().toJSONString()).append("\n")
                    .append(function.desc()).append("\n")
                    .append("```\n");
            names.append(function.action()).append(",");
        }
        names.append("}");
        return this.prompt()
                .replaceFirst(AgentPrompt.TOOL_DESC, descs.toString())
                .replaceFirst(AgentPrompt.TOOL_NAMES, names.toString());
    }

    @Override
    public String datastore(String question) {
        List<Document.Entity> entityList = dataSet.query(question, 0.66f);
        StringBuilder datastore = new StringBuilder();
        datastore.append("```\n");
        for (Document.Entity entity : entityList) {
            datastore.append("- ").append(entity.getContent()).append("\n");
        }
        datastore.append("```\n");
        return datastore.toString();
    }

    /**
     * @description 对话前
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    protected void assistantBefore(int loop, ChatRequest request) {
        // Agent内部处理
    }

    /**
     * @description 对话后
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    protected void assistantAfter(int loop, ChatResult result) {
        // Agent内部处理
    }

    /**
     * @description 执行结束/终止
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    protected void termination(Map<Integer, ChatResult> chatResultMap) {
        // Agent内部处理
    }

}


