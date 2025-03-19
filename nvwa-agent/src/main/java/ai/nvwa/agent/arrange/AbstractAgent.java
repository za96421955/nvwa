package ai.nvwa.agent.arrange;

import ai.nvwa.agent.components.util.DateUtil;
import ai.nvwa.agent.model.ModelAssembler;
import ai.nvwa.agent.model.chat.ChatService;
import ai.nvwa.agent.model.chat.mode.AlibabaChatRequest;
import ai.nvwa.agent.model.chat.mode.ChatRequest;
import ai.nvwa.agent.model.chat.mode.ChatResult;
import ai.nvwa.agent.prompt.ExtensionPrompt;
import ai.nvwa.agent.tool.datastore.DataSet;
import ai.nvwa.agent.tool.datastore.Document;
import ai.nvwa.agent.tool.extension.Extension;
import ai.nvwa.agent.tool.function.Function;
import ai.nvwa.agent.tool.function.mode.Action;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 智能体抽象
 * <p> <功能详细描述> </p>
 *
 * @author 陈晨
 */
@Slf4j
public abstract class AbstractAgent implements Agent {

    @Autowired
    protected Map<String, Function> functionMap;

    @Autowired
    protected ModelAssembler modelAssembler;
    @Autowired
    protected ChatService chatService;
    @Autowired
    protected DataSet dataSet;

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
                    if (null != process) {
                        process.reasoning(finalLoop, result.getReasoning().toString());
                    }
                }
                if (StringUtils.isNotBlank(content)) {
                    result.getContent().append(content);
                    if (null != process) {
                        process.content(finalLoop, result.getContent().toString());
                    }
                }
                if (null != usage) {
                    result.setUsage(usage);
                }
            });
            // 对话处理
            answer = this.assistantHandle(loop++, request, result);
            // 对话后
            this.assistantAfter(loop, result);
            if (null != process) {
                process.assistantAfter(loop, result);
            }
        } while (StringUtils.isBlank(answer));

        // 执行结束/终止通知
        this.termination(chatResultMap);
//        if (null != process) {
//            process.termination(chatResultMap);
//        }
        // 返回答案
        return answer;
    }

    /**
     * @description 构建对话请求
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    protected ChatRequest buildRequest(String ip, String question) {
        return AlibabaChatRequest.llama()
                .user(this.formatPrompt().replaceFirst(ExtensionPrompt.DATASTORE_INFO, this.datastore(question)))
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
                .replaceFirst(ExtensionPrompt.TOOL_DESC, descs.toString())
                .replaceFirst(ExtensionPrompt.TOOL_NAMES, names.toString());
    }

    /**
     * @description 处理LLM回答
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    private String assistantHandle(int loop, ChatRequest request, ChatResult result) {
        String content = result.getContent().toString();
        // 终止对话
        if (loop >= this.chatMax()) {
            log.error("[执行任务] loop={}, max={}, 超出最大对话轮次, 对话终止", loop, this.chatMax());
            result.setErrorMsg("超出最大对话轮次, 对话终止");
            if (this.isAnswer(content)) {
                return this.getAnswer(content);
            }
            return StringUtils.isBlank(content) ? "null" : content;
        }
        // 行动
        if (this.isPause(content)) {
            Action action = this.doAction(request, result, content);
            if (null == action) {
                return null;
            }
            // 函数: 返回输入参数
            return action.getInput().toJSONString();
        }
        // 回答
        if (this.isAnswer(content)) {
            return this.getAnswer(content);
        }
        return StringUtils.isBlank(content) ? "null" : content;
//        return null;
    }

    /**
     * @description 是否暂停
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    private boolean isPause(String content) {
        return content.contains("Action:") && content.contains("Action Input:");
    }

    /**
     * @description 获取方法名
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    private String getAction(String content) {
        Pattern pattern = Pattern.compile(
                "Action:\\s*" +             // 匹配 "Action: "（允许空格和制表符）
                "(?i)" +                    // 忽略大小写（如 ACTION: Address）
                "(\\w+(?:[-_][\\w]+)*)" +   // 方法名规则：字母数字+可选下划线/连字符组合
                "\\b"                       // 单词边界防止过度匹配
        );
        Matcher matcher = pattern.matcher(content);
        List<String> methods = new ArrayList<>();
        while (matcher.find()) {
            String method = matcher.group(1).trim();
            if (!method.isEmpty()) {
                methods.add(method);
            }
        }
        if (CollectionUtils.isEmpty(methods)) {
            return null;
        }
        return methods.get(0);
    }

    /**
     * @description 执行Action
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    private Action doAction(ChatRequest request, ChatResult result, String content) {
        try {
            result.setAction(Action.builder()
                    .action(this.getAction(content))
                    .input(this.getActionInput(content))
                    .build());
            Function function = functionMap.get(result.getAction().getAction());
            if (function instanceof Extension) {
                result.getAction().setResponse(((Extension) function).call(result.getAction().getInput()));
                request.assistant(content).user(result.getAction().getAction() + " Response: " + result.getAction().getResponse());
                return null;
            }
            return result.getAction();
        } catch (Exception e) {
            log.error("[执行任务] action={}, Action执行异常, 当轮对话重试: {}",
                    result.getAction(), e.getMessage(), e);
            result.setErrorMsg(e.getMessage());
        }
        return null;
    }

    /**
     * @description 获取输入
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    private JSONObject getActionInput(String content) {
        Pattern pattern = Pattern.compile("Action\\s+Input:\\s*(\\{[^{}]*\\})");
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            String rawJson = matcher.group(1)
                    .replaceAll("\\s+", "")     // 删除所有空格
                    .replace("\\\\\"", "\"");   // 转义反斜杠
            try {
                return JSONObject.parseObject(rawJson);
            } catch (Exception e) {
                log.error("rawJson={}, Invalid Action Input JSON: {}", rawJson, e.getMessage(), e);
                return null;
            }
        }
        return null;
    }

    /**
     * @description 是否最终回答
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    private boolean isAnswer(String content) {
        return content.contains("Answer:");
    }

    /**
     * @description 获取回答内容
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    private String getAnswer(String content) {
        // 匹配 Answer: 开头的内容（跨行支持）
        Pattern pattern = Pattern.compile(
                "Answer[：:]\\s*" +      // 匹配 Answer:（支持中英文冒号）
                "(?i)" +                      // 忽略大小写（如 ANSWER: Address）
                "(.*)"                        // 非贪婪匹配所有后续内容
                , Pattern.DOTALL);
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1).trim();   // 去除首尾空格
        }
        return null;
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


