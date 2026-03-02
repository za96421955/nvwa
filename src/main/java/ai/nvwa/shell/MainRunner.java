package ai.nvwa.shell;

import ai.nvwa.context.MainContext;
import ai.nvwa.context.SecurityLevel;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 主程序
 * <p> <功能详细描述> </p>
 *
 * @author 陈晨
 * @version 1.0
 * @date 2026/2/26
 */
@Component
public class MainRunner implements ApplicationRunner {

    @Override
    public void run(ApplicationArguments args) {
        System.out.println("=== Nvwa 已就位 ===");
        System.out.println("输入 'exit' 或 Ctrl + C 退出");

        MainContext.setSecurityLevel(SecurityLevel.RISK_CONTROL);
        while (true) {
            // 用户输入
            System.out.print("\n> ");
            String input = MainContext.getScanner().nextLine();

            // 指令执行
            BasicCommands.Result commandResult;
            try {
                commandResult = BasicCommands.execute(input);
            } catch (Exception e) {
                System.err.println("指令执行失败: " + e.getMessage());
                continue;
            }
            if (BasicCommands.Result.EXIT.equals(commandResult)) {
                break;
            }
            if (BasicCommands.Result.CONTINUE.equals(commandResult)) {
                continue;
            }

            // 上下文对话
            try {
                MainContext.getAgent().chat(input, MainContext.getContext());
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }

        // 清除输入
        MainContext.clearScanner();
    }

}


