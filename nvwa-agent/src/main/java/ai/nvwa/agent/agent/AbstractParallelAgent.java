package ai.nvwa.agent.agent;

import ai.nvwa.agent.model.chat.mode.ChatRequest;
import ai.nvwa.agent.model.chat.mode.ChatResult;
import ai.nvwa.agent.prompt.ParallelPrompt;
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
 * 并行Agent
 * <p> <功能详细描述> </p>
 *
 * @author 陈晨
 */
@Slf4j
public abstract class AbstractParallelAgent extends AbstractAgent {

    @Override
    public int chatMax() {
        return 2;
    }

    @Override
    public String prompt() {
        return ParallelPrompt.DEFAULT;
    }

    @Override
    protected String assistantHandle(int loop, ChatRequest request, ChatResult result) {
        String content = result.getContent().toString();
        request.assistant(content);
        // 行动
        result.setActions(this.extractActions(content));
        if (loop < this.chatMax() && !CollectionUtils.isEmpty(result.getActions())) {
            this.doAction(request, result.getActions());
            return null;
        }
        // 回答
        String answer = this.getAnswer(content);
        return StringUtils.isBlank(answer) ? "null" : content;
    }

    /**
     * @description 执行Action
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    private void doAction(ChatRequest request, List<Action> actions) {
        for (Action action : actions) {
            try {
                Function function = functionMap.get(action.getAction());
                if (function instanceof Extension) {
                    action.setResponse(((Extension) function).call(action.getInput()));
                    request.user(action.getAction() + " Response: " + action.getResponse());
                }
            } catch (Exception e) {
                log.error("[执行任务] action={}, Action执行异常: {}",
                        action.getAction(), e.getMessage(), e);
            }
        }
    }

    /**
     * @description 提取Action
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    private List<Action> extractActions(String content) {
        List<Action> actions = new ArrayList<>();
        // 匹配Thought
        Matcher thoughtMatcher = THOUGHT_PATTERN.matcher(content);
        while (thoughtMatcher.find()) {
            String thought = thoughtMatcher.group(1).trim();
            actions.add(Action.builder()
                    .thought(thought)
                    .start(thoughtMatcher.start())
                    .build());
        }
        // 匹配Action
        Matcher actionMatcher = ACTION_PATTERN.matcher(content);
        while (actionMatcher.find()) {
            String action = actionMatcher.group(1).trim();
            Action pair = this.findNotMatchedAction(actions, actionMatcher.start());
            if (pair != null) {
                pair.setAction(action);
            }
        }
        // 匹配Action Input
        Matcher actionInputMatcher = ACTION_INPUT_PATTERN.matcher(content);
        while (actionInputMatcher.find()) {
            JSONObject input = this.parseJson(actionInputMatcher.group(1).trim());
            Action pair = this.findNotMatchedAction(actions, actionInputMatcher.start());
            if (pair != null) {
                pair.setInput(input);
            }
        }
        return actions;
    }

    /**
     * @description 查找最近未匹配的Action
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    private Action findNotMatchedAction(List<Action> actions, int start) {
        Action matched = null;
        for (Action action : actions) {
            if ((action.getAction() == null || action.getInput() == null)
                    && action.getStart() < start) {
                if (matched == null || action.getStart() > matched.getStart()) {
                    matched = action;
                }
            }
        }
        return matched;
    }

    /**
     * @description input 转 json
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    private JSONObject parseJson(String rawJson) {
        try {
            String input = rawJson
                    .replaceAll("\\s+", "")     // 删除所有空格
                    .replace("\\\\\"", "\"");   // 转义反斜杠
            if (StringUtils.isNotBlank(input)) {
                return JSONObject.parseObject(input);
            }
        } catch (Exception e) {
            log.error("[执行任务] rawJson={}, JSON格式化失败: {}",
                    rawJson, e.getMessage(), e);
        }
        return null;
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



    public static void main(String[] args) {
        String content = "Thought: 用户提供了IP地址并询问位置和天气情况，我需要先使用addressExtension工具通过IP定位所在地，然后使用weatherExtension查询天气。\n" +
                "\n" +
                "Action: addressExtension\n" +
                "Action Input: {\"ip\":\"183.213.85.230\"}\n" +
                "\n" +
                "Observation: {'province': '广东省', 'city': '深圳市', 'adcode': '440300'}\n" +
                "\n" +
                "Thought: 经过定位，用户位于广东省深圳市，现在使用adcode:440300查询当地天气。\n" +
                "\n" +
                "Action: weatherExtension\n" +
                "Action Input: {\"adcode\":\"440300\"}\n" +
                "\n" +
                "Observation: 天气情况：晴天，温度：22摄氏度，湿度：60%。\n" +
                "\n" +
                "Thought: 已经获取了足够的信息，可以回答用户的问题。\n" +
                "\n" +
                "Answer: 你现在在广东省深圳市，当前天气晴朗，温度为22摄氏度，湿度60%。";

        AbstractParallelAgent agent = new AbstractParallelAgent() {
            @Override
            public String action() {
                return null;
            }

            @Override
            public String name() {
                return null;
            }

            @Override
            public String desc() {
                return null;
            }

            @Override
            public List<Function> association() {
                return null;
            }
        };
        System.out.println(agent.extractActions(content));
        System.out.println();
        System.out.println(agent.getAnswer(content));
        System.out.println();
    }

}


