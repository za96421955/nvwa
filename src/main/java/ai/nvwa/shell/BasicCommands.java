package ai.nvwa.shell;

import ai.nvwa.context.MainContext;
import ai.nvwa.context.SecurityLevel;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * 基础指令集
 * <p> <功能详细描述> </p>
 *
 * @author 陈晨
 * @version 1.0
 * @date 2026/2/28
 */
public abstract class BasicCommands {

    private BasicCommands() {}

    /**
     * @description 指令执行结果
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     * @date 2026/2/28 10:10
     */
    @Getter
    public enum Result {
        EXIT(0, "退出"),
        CONTINUE(1, "继续"),
        SKIP(-1, "跳过")
        ;

        private final int result;
        private final String desc;
        Result(int result, String desc) {
            this.result = result;
            this.desc = desc;
        }
    }

    /**
     * @description 执行指令
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     * @date 2026/2/28 10:05
     * @return 0: 退出, 1: 继续, -1: 忽略
     */
    public static Result execute(String input) {
        if (StringUtils.isBlank(input)) {
            return Result.CONTINUE;
        }
        String[] commands = input.split(" ");
        String command = commands[0].toLowerCase();
        // 退出
        switch (command) {
            case "exit", "quit" -> {
                return Result.EXIT;
            }

            // 设置安全级别
            case "/sl" -> {
                SecurityLevel sl = SecurityLevel.get(Integer.parseInt(commands[1]));
                if (sl != null) {
                    MainContext.setSecurityLevel(sl);
                    System.out.println("当前安全级别: " + sl);
                }
                return Result.CONTINUE;
            }

            // 新对话
            case "/new" -> {
                MainContext.clearAgent();
                MainContext.clearContext();
                System.out.println("新会话: " + MainContext.getContext().threadId().get());
                return Result.CONTINUE;
            }

            // 执行工作流
            case "/wf" -> {
                execWorkFlow(commands[1]);
                return Result.CONTINUE;
            }

            // 新会话执行工作流
            case "/nwf" -> {
                MainContext.clearAgent();
                MainContext.clearContext();
                System.out.println("新会话: " + MainContext.getContext().threadId().get());
                execWorkFlow(commands[1]);
                return Result.CONTINUE;
            }
        }

        return Result.SKIP;
    }

    /**
     * @description 执行工作流
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     * @date 2026/3/2 16:25
     */
    private static void execWorkFlow(String filePath) {
        try {
            List<String> wfs = Files.readAllLines(Paths.get(filePath));
            for (String input : wfs) {
                System.out.println("执行工作流: " + input);

                // 指令执行
                Result commandResult;
                try {
                    commandResult = execute(input);
                } catch (Exception e) {
                    System.err.println("指令执行失败: " + e.getMessage());
                    continue;
                }
                if (!Result.SKIP.equals(commandResult)) {
                    continue;
                }

                // Agent执行
                try {
                    MainContext.getAgent().chat(input, MainContext.getContext());
                } catch (Exception e) {
                    System.err.println("工作流执行失败: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("工作流读取失败: " + e.getMessage());
        }
    }

//    /**
//     * @description 执行指令
//     * <p> <功能详细描述> </p>
//     *
//     * @author 陈晨
//     * @date 2026/2/28 10:05
//     * @return 0: 退出, 1: 继续, -1: 忽略
//     */
//    public static Result execute(String input, NvwaAgent agent) {
//        Result result = execute(input);
//        if (!BasicCommands.Result.SKIP.equals(result)) {
//            return result;
//        }
//        String[] commands = input.split(" ");
//        String command = commands[0].toLowerCase();
//        // 清除
//        if (command.equals("clear")) {
//            agent.clear();
//            System.out.println("已清除");
//            return Result.CONTINUE;
//        }
//        return Result.SKIP;
//    }

}


