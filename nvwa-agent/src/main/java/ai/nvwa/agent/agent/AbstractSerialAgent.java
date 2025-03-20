package ai.nvwa.agent.agent;

import ai.nvwa.agent.model.chat.mode.ChatRequest;
import ai.nvwa.agent.model.chat.mode.ChatResult;
import ai.nvwa.agent.prompt.SerialPrompt;
import ai.nvwa.agent.tool.extension.Extension;
import ai.nvwa.agent.tool.function.Function;
import ai.nvwa.agent.tool.function.mode.Action;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

/**
 * 串行Agent
 * <p> <功能详细描述> </p>
 *
 * @author 陈晨
 */
@Slf4j
public abstract class AbstractSerialAgent extends AbstractAgent {

    @Override
    public String prompt() {
        return SerialPrompt.DEFAULT;
    }

    @Override
    protected String assistantHandle(int loop, ChatRequest request, ChatResult result) {
        String content = result.getContent().toString();
        request.assistant(content);
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
    }

    /**
     * @description 执行Action
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    private Action doAction(ChatRequest request, ChatResult result, String content) {
        try {
            Action action = Action.builder()
                    .action(this.getAction(content))
                    .input(this.getActionInput(content))
                    .build();
            result.setActions(List.of(action));
            Function function = functionMap.get(action.getAction());
            if (function instanceof Extension) {
                action.setResponse(((Extension) function).call(action.getInput()));
                request.user(action.getAction() + " Response: " + action.getResponse());
                return null;
            }
            return action;
        } catch (Exception e) {
            log.error("[执行任务] action={}, Action执行异常, 当轮对话重试: {}",
                    result.getActions(), e.getMessage(), e);
            result.setErrorMsg(e.getMessage());
        }
        return null;
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
        Matcher matcher = ACTION_PATTERN.matcher(content);
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
     * @description 获取输入
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    private JSONObject getActionInput(String content) {
        Matcher matcher = ACTION_INPUT_PATTERN.matcher(content);
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
        Matcher matcher = ANSWER_PATTERN.matcher(content);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }

}


