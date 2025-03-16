package ai.nvwa.agent.arrange;

import ai.nvwa.agent.components.util.DateUtil;
import ai.nvwa.agent.model.chat.ChatService;
import ai.nvwa.agent.model.chat.mode.AlibabaChatRequest;
import ai.nvwa.agent.model.chat.mode.ChatRequest;
import ai.nvwa.agent.model.chat.mode.ChatResponse;
import ai.nvwa.agent.model.chat.mode.ChatResult;
import ai.nvwa.agent.prompt.ExtensionPrompt;
import ai.nvwa.agent.prompt.ReAct;
import ai.nvwa.agent.tool.extension.Extension;
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
    protected Map<String, Extension> extensionMap;
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
        for (Extension extension : this.association()) {
            descs.append("```\n")
                    .append("Action: ").append(extension.action()).append("\n")
                    .append("Action Input: ").append(extension.inputFormat().toJSONString()).append("\n")
                    .append(extension.desc()).append("\n")
                    .append("```\n");
            names.append(extension.action()).append(",");
        }
        return ExtensionPrompt.DEFAULT
                .replaceFirst(ReAct.TOOL_DESC, descs.toString())
                .replaceFirst(ReAct.TOOL_NAMES, names.toString());
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
                String action = null;
                JSONObject actionRequest = null;
                try {
                    action = this.getAction(content);
                    actionRequest = this.getRequest(content);
                    String actionResult = extensionMap.get(action + "Extension").call(actionRequest);
//                    request.assistant(content).user("Action[" + action + "] Response: " + actionResult);
                    request.user("Action[" + action + "] Response: " + actionResult);
                } catch (Exception e) {
                    log.error("[执行任务] action={}, request={}, Action执行异常, 当轮对话重试: {}",
                            action, actionRequest, e.getMessage(), e);
                    result.setErrorMsg(e.getMessage());
                }
                continue;
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
    protected boolean isPause(String content) {
        return content.contains("Action:") && content.contains("Action Input:");
    }

    /**
     * @description 获取扩展方法名
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    protected String getAction(String content) {
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
     * @description 获取扩展请求
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    protected JSONObject getRequest(String content) {
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
    protected boolean isAnswer(String content) {
        return content.contains("Answer:");
    }

    /**
     * @description 获取回答内容
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    protected String getAnswer(String content) {
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


