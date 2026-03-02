package ai.nvwa.executor;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @description Shell 脚本执行器
 * <p> <功能详细描述> </p>
 *
 * @author 陈晨
 * @date 2026/2/26 17:13
 */
public class CLIExecutor {

    private Process runningProcess;
    private final AtomicBoolean shouldStop = new AtomicBoolean(false);

    public CLIExecutor() {}

    private String path;
    public CLIExecutor(String path) {
        this.path = path;
    }

    /**
     * @description 获取路径
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     * @date 2026/2/26 17:23
     */
    public String getPath() {
        if (path != null) {
            return path;
        }
        Path itemDir = Paths.get("").toAbsolutePath();
        return itemDir.toString();
    }

    /**
     * @description 判断是否 windows 系统
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     * @date 2026/2/27 16:43
     */
    private boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
//        ProcessBuilder processBuilder = this.isWindows() ?
//                new ProcessBuilder("bash", "-c", String.join(" ", command)) :
//                new ProcessBuilder(command);
    }

    /**
     * 执行普通命令（无控制功能）
     */
    public String command(String command) throws IOException, InterruptedException {
        // 构建执行器
        ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", command);
        processBuilder.directory(new File(this.getPath()));
        processBuilder.redirectErrorStream(true);

        // 执行
        Process process = processBuilder.start();
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                sb.append(line).append("\n");
            }
        }

        // 终止
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            System.out.println("命令执行失败，退出码: " + exitCode);
            return "命令执行失败: " + sb;
        }
        // 执行成功
        return sb.toString();
    }

    /**
     * 执行可控制的命令（支持停止）
     */
    public void command2Thread(String command) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", command);
        processBuilder.directory(new File(this.getPath()));
        processBuilder.redirectErrorStream(true);

        runningProcess = processBuilder.start();

        // 启动输出读取线程
        Thread outputThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(runningProcess.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                    // 运行结束
                    if (line.equals(">>> Project Running Complete <<<")) {
                        stopApplication();
                    }
                }
            } catch (IOException e) {
                if (!shouldStop.get()) {
                    System.err.println("读取进程输出时出错: " + e.getMessage());
                }
            }
        });
        outputThread.start();

        // 启动用户输入监听线程
        Thread inputThread = new Thread(() -> {
            try (Scanner scanner = new Scanner(System.in)) {
                while (!shouldStop.get()) {
                    if (scanner.hasNextLine()) {
                        String input = scanner.nextLine().trim().toLowerCase();

                        if ("stop".equals(input) || "exit".equals(input) || "quit".equals(input)) {
                            System.out.println("接收到停止指令，正在停止应用...");
                            shouldStop.set(true);
                            if (runningProcess != null && runningProcess.isAlive()) {
                                // 先尝试优雅关闭
                                System.out.println("发送关闭信号给 Spring Boot 应用...");
                                runningProcess.destroy();

                                // 等待3秒
                                try {
                                    Thread.sleep(3000);
                                } catch (InterruptedException e) {
                                    // 忽略
                                }

                                // 如果进程仍然存活，强制终止
                                if (runningProcess.isAlive()) {
                                    System.out.println("应用未正常关闭，强制终止进程...");
                                    runningProcess.destroyForcibly();
                                }
                            }
                            break;
                        } else if (!input.isEmpty()) {
                            System.out.println("可用命令: stop/exit/quit - 停止应用");
                        }
                    }
                }
            }
        });
        inputThread.setDaemon(true);
        inputThread.start();

        // 添加关闭钩子，确保程序退出时清理进程
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            shouldStop.set(true);
            if (runningProcess != null && runningProcess.isAlive()) {
                System.out.println("正在关闭 Spring Boot 应用...");
                runningProcess.destroyForcibly();
            }
        }));

        try {
            // 等待进程结束
            int exitCode = runningProcess.waitFor();

            // 标记为停止状态
            shouldStop.set(true);

            if (exitCode == 0) {
                System.out.println("Spring Boot 应用已正常停止");
            } else if (exitCode == 130 || exitCode == 143) {
                System.out.println("Spring Boot 应用被中断 (Ctrl+C)");
            } else {
                System.out.println("Spring Boot 应用异常停止，退出码: " + exitCode);
            }

        } catch (InterruptedException e) {
            System.err.println("等待进程时被中断: " + e.getMessage());
            Thread.currentThread().interrupt();
        } finally {
            // 清理资源
            if (runningProcess != null && runningProcess.isAlive()) {
                runningProcess.destroyForcibly();
            }
            runningProcess = null;
        }
    }

    /**
     * 停止正在运行的应用（可从其他线程调用）
     */
    public void stopApplication() {
        shouldStop.set(true);
        if (runningProcess != null && runningProcess.isAlive()) {
            System.out.println("正在停止应用...");
            runningProcess.destroy();

            // 等待3秒，如果还没停止则强制终止
            new Thread(() -> {
                try {
                    Thread.sleep(3000);
                    if (runningProcess != null && runningProcess.isAlive()) {
                        runningProcess.destroyForcibly();
                        System.out.println("已强制终止应用");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }
    }

}


