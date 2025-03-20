package ai.nvwa.agent.prompt;

/**
 * Agent提示词
 * <p> <功能详细描述> </p>
 *
 * @author 陈晨
 */
public interface AgentPrompt {

    String TOOL_DESC = "\\{tool_desc\\}";
    String TOOL_NAMES = "\\{tool_names\\}";
    String DATASTORE_INFO = "\\{datastore_info\\}";

}


