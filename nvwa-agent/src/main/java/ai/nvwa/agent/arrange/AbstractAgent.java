package ai.nvwa.agent.arrange;

import ai.nvwa.agent.components.util.DateUtil;
import ai.nvwa.agent.model.chat.ChatService;
import ai.nvwa.agent.model.chat.mode.AlibabaChatRequest;
import ai.nvwa.agent.model.chat.mode.ChatRequest;
import ai.nvwa.agent.model.chat.mode.ChatResponse;
import ai.nvwa.agent.model.chat.mode.ChatResult;
import ai.nvwa.agent.prompt.FunctionPrompt;
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
    protected ChatService chatService;

    /**
     * @description 获取提示词
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public String prompt() {
        StringBuilder descs = new StringBuilder();
        StringBuilder names = new StringBuilder("{");
        for (Function function : this.association()) {
            descs.append("```\n")
                    .append("Action: ").append(function.action()).append("\n")
                    .append("Action Input: ").append(function.inputFormat().toJSONString()).append("\n")
                    .append(function.desc()).append("\n")
                    .append("```\n");
            names.append(function.action()).append(",");
        }
        return FunctionPrompt.DEFAULT
                .replaceFirst(FunctionPrompt.TOOL_DESC, descs.toString())
                .replaceFirst(FunctionPrompt.TOOL_NAMES, names.toString());
    }

    /**
     * @description 执行任务
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public String action(String ip, String question) {
        ChatRequest request = AlibabaChatRequest.llama()
                .user(this.prompt())
                .user("现在是: " + DateUtil.formatDateTime(DateUtil.now()))
                .user("我的IP是: " + ip)
                .user(question)
                .stream();
        // 开启多轮对话
        int loop = 1;
        Map<Integer, ChatResult> chatResultMap = new HashMap<>();
        String answer;
        while (true) {
            chatResultMap.put(loop, new ChatResult());
            ChatResult result = chatResultMap.get(loop);
            result.setRequest(request.toString());
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
                // 执行过程通知
                this.process(finalLoop, reasoning, content, usage);
            });
            // 本轮对话处理
            loop++;
            String content = result.getContent().toString();
            // 终止对话
            if (loop > this.chatMax()) {
                if (this.isAnswer(content)) {
                    answer = this.getAnswer(content);
                } else {
                    answer = content;
                }
                result.setErrorMsg("超出最大对话轮次, 对话终止");
                log.error("[执行任务] loop={}, max={}, 超出最大对话轮次, 对话终止", loop, this.chatMax());
                break;
            }
            // 行动
            if (this.isPause(content)) {
                Action action = this.doAction(request, result, content);
                if (null == action) {
                    continue;
                }
                // 函数: 返回输入参数
                answer = action.getInput().toJSONString();
                break;
            }
            // 回答
            if (this.isAnswer(content)) {
                answer = this.getAnswer(content);
                break;
            }
        }
        // 执行结束/终止通知
        this.termination(chatResultMap);
        // 返回答案
        return answer;
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
        Action action = null;
        try {
            action = Action.builder()
                    .action(this.getAction(content))
                    .input(this.getActionInput(content))
                    .build();
            Function function = functionMap.get(action.getAction());
            if (function instanceof Extension) {
                String actionResult = ((Extension) function).call(action.getInput());
//                request.assistant(content).user("Action[" + action + "] Response: " + actionResult);
                request.user("Action[" + action.getAction() + "] Response: " + actionResult);
                return null;
            }
            return action;
        } catch (Exception e) {
            log.error("[执行任务] action={}, Action执行异常, 当轮对话重试: {}",
                    action, e.getMessage(), e);
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
     * @description 执行过程
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    protected abstract void process(int loop, String reasoning, String content, ChatResponse.Usage usage);

    /**
     * @description 执行结束/终止
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    protected abstract void termination(Map<Integer, ChatResult> chatResultMap);

}


