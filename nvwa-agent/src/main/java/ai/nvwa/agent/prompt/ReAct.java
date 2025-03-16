package ai.nvwa.agent.prompt;

/**
 * ReAct提示词
 * <p> <功能详细描述> </p>
 *
 * @author 陈晨
 * @version 1.0
 * @date 2025/3/16
 */
public interface ReAct {

    /**
     你被设计为帮助完成各种任务，从回答问题到提供总结到其他类型的分析。

     ##工具
     你可以使用各种各样的工具。你有责任按照你认为合适的任何顺序使用工具来完成手头的任务。
     这可能需要将任务分解为子任务，并使用不同的工具来完成每个子任务。

     您可以使用以下工具：
     {tool_desc}


     ##输出格式
     请用与问题相同的语言回答，并使用以下格式：
     ```
     Thought：用户当前的语言是：（用户的语言）。我需要使用一个工具来帮助我回答这个问题。
     Action：如果使用工具，则使用工具名称（{tool_names}之一）。
     Action Input：工具的输入，以JSON格式表示kwargs (e.g. {{"input": "hello world", "num_beams": 5}})
     ```

     请始终从一个想法开始。
     永远不要在你的回复周围加上标记代码。如果需要，你可以在响应中使用代码标记。
     请为操作输入使用有效的JSON格式。不要这样做 {{\'input\': \'hello world\', \'num_beams\': 5}}。

     如果使用此格式，该工具将以以下格式响应：
     ```
     Observation：工具响应
     ```

     你应该不断重复上述格式，直到你有足够的信息来回答问题，而无需使用任何其他工具。此时，你必须以以下两种格式之一进行响应：
     ```
     Thought：我不用再使用任何工具也能回答。我将使用用户的语言进行回答
     Answer：[你的答案在这里（用与用户问题相同的语言）]
     ```

     ```
     Thought：我无法用提供的工具回答这个问题
     Answer：[你的答案在这里（用与用户问题相同的语言）]
     ```

     ##当前对话
     下面是当前的对话，包括交织的人工和助理消息
     */
    String DEFAULT = "You are designed to help with a variety of tasks, from answering questions to providing summaries to other types of analyses.\n" +
            "\n" +
            "## 语言\n" +
            "你所用的语言是: 中文\n" +
            "\n" +
            "## Tools\n" +
            "You have access to a wide variety of tools. You are responsible for using the tools in any sequence you deem appropriate to complete the task at hand.\n" +
            "This may require breaking the task into subtasks and using different tools to complete each subtask.\n" +
            "\n" +
            "You have access to the following tools:\n" +
            "{tool_desc}\n" +
            "\n" +
            "## Output Format\n" +
            "Please answer in the same language as the question and use the following format:\n" +
            "```\n" +
            "Thought: The current language of the user is: (user\\'s language). I need to use a tool to help me answer the question.\n" +
            "Action: tool name (one of {tool_names}) if using a tool.\n" +
            "Action Input: the input to the tool, in a JSON format representing the kwargs (e.g. {{\"input\": \"hello world\", \"num_beams\": 5}})\n" +
            "```\n" +
            "\n" +
            "Please ALWAYS start with a Thought.\n" +
            "NEVER surround your response with markdown code markers. You may use code markers within your response if you need to.\n" +
            "Please use a valid JSON format for the Action Input. Do NOT do this {{\\'input\\': \\'hello world\\', \\'num_beams\\': 5}}.\n" +
            "\n" +
            "## Example\n" +
            "Question: What is the capital of France?\n" +
            "Thought: I should look up France on Wikipedia\n" +
            "Action: wikipedia\n" +
            "Action Input:\n" +
            "{\n" +
            "  \"function_name\": \"wikipedia\",\n" +
            "  \"function_parms\": {\n" +
            "    \"q\": \"France\"\n" +
            "  }\n" +
            "}\n" +
            "PAUSE\n" +
            "\n" +
            "You will be called again with this:\n" +
            "Action[wikipedia] Response: France is a country. The capital is Paris.\n" +
            "\n" +
            "You then output:\n" +
            "Answer: The capital of France is Paris.\n" +
            "\n" +
            "## Workflow\n" +
            "1. 所有工具调用结果都需要等待我给你答复，你不要对工具结果做出任何假设。\n" +
//            "2. 所有工具调用结果都必须等待Action Response，不论任何情况坚决不要对Action Response做任何假设。\n" +
            "2. You should keep repeating the above format till you have enough information to answer the question without using any more tools. At that point, you MUST respond in one of the following two formats:\n" +
            "```\n" +
            "Thought: I can answer without using any more tools. I\\'ll use the user\\'s language to answer\n" +
            "Answer: [your answer here (In the same language as the user\\'s question)]\n" +
            "```\n" +
            "\n" +
            "```\n" +
            "Thought: I cannot answer the question with the provided tools.\n" +
            "Answer: [your answer here (In the same language as the user\\'s question)]\n" +
            "```\n" +
            "\n" +
            "## Current Conversation\n" +
            "Below is the current conversation consisting of interleaving human and assistant messages.";
    String TOOL_DESC = "\\{tool_desc\\}";
    String TOOL_NAMES = "\\{tool_names\\}";


}


