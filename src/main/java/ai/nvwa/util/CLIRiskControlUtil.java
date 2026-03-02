package ai.nvwa.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * CLI 风险控制
 * <p> <功能详细描述> </p>
 *
 * @author 陈晨
 * @version 1.0
 * @date 2026/3/2
 */
public abstract class CLIRiskControlUtil {

    // 系统类型枚举
    public enum OSType {
        WINDOWS, LINUX, MACOS, OTHER
    }

    // 风险级别枚举
    public enum RiskLevel {
        READ_ONLY,  // 只读
        LOW_RISK,   // 低风险
        HIGH_RISK   // 高风险
    }

    // 只读命令列表
    private static final List<String> READ_ONLY_COMMANDS = Arrays.asList(
            "ls", "dir", "pwd", "echo", "type", "where", "which",
            "find", "locate", "grep", "cat", "more", "less", "head",
            "tail", "file", "stat", "du", "df", "free", "top",
            "htop", "ps", "netstat", "ifconfig", "ip", "ping",
            "traceroute", "who", "w", "whoami", "id", "env", "printenv"
    );

    // 高风险命令列表
    private static final List<String> HIGH_RISK_COMMANDS = Arrays.asList(
            "rm", "del", "erase", "delete", "rd", "rmdir", "format",
            "mkfs", "dd", "shutdown", "reboot", "halt", "poweroff",
            "chmod", "chown", "chgrp", "chattr", "mv", "move", "ren",
            "rename", "cp", "copy", "xcopy", "robocopy", "scp", "rsync",
            "wget", "curl", "tar", "zip", "unzip", "gzip", "gunzip",
            "mount", "umount", "fdisk", "parted", "lsof", "kill",
            "killall", "pkill", "service", "systemctl", "crontab",
            "useradd", "userdel", "groupadd", "groupdel", "passwd",
            "visudo", "vim", "vi", "nano", "sed", "awk", "python",
            "python3", "perl", "ruby", "bash", "sh", "zsh", "tcsh",
            "expect", "ssh", "scp", "sftp", "telnet", "ftp", "nc",
            "netcat", "nmap", "tcpdump", "wireshark", "tcpflow",
            "sqlplus", "mysql", "psql", "mongo", "redis-cli",
            "docker", "kubectl", "ansible", "salt", "puppet",
            "chef", "vagrant", "terraform", "packer", "aws",
            "gcloud", "az", "kubectl", "helm", "istioctl",
            "linkerd", "consul", "vault", "nomad", "jenkins",
            "git", "svn", "hg", "make", "cmake", "ant", "mvn",
            "gradle", "npm", "yarn", "pip", "gem", "cargo", "go"
    );

    // Windows系统高风险路径
    private static final List<String> WINDOWS_HIGH_RISK_PATHS = Arrays.asList(
            "C:\\\\Windows", "C:\\\\Program Files", "C:\\\\Program Files (x86)",
            "C:\\\\System32", "C:\\\\SysWOW64", "C:\\\\Users\\\\Administrator",
            "C:\\\\Users\\\\Default", "C:\\\\PerfLogs", "C:\\\\Boot",
            "C:\\\\inetpub", "C:\\\\$", "C:\\\\ProgramData", "C:\\\\Windows.old"
    );

    // Linux/macOS系统高风险路径
    private static final List<String> LINUX_HIGH_RISK_PATHS = Arrays.asList(
            "/bin", "/sbin", "/usr", "/usr/bin", "/usr/sbin", "/usr/local/bin",
            "/usr/local/sbin", "/etc", "/lib", "/lib64", "/root", "/var",
            "/boot", "/dev", "/proc", "/sys", "/opt", "/home", "/mnt", "/media",
            "/tmp", "/var/log", "/var/run", "/var/spool", "/var/tmp", "/var/cache"
    );

    // 重定向操作符
    private static final List<String> REDIRECTION_SYMBOLS = Arrays.asList(">", ">>", "1>", "2>", "&>");

    // 管道符
    private static final String PIPE_SYMBOL = "|";

    // 当前系统类型
    private static final OSType OS_TYPE = getOS();
    public static OSType getOS() {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("win")) {
            return OSType.WINDOWS;
        } else if (osName.contains("linux")) {
            return OSType.LINUX;
        } else if (osName.contains("mac")) {
            return OSType.MACOS;
        } else {
            return OSType.OTHER;
        }
    }

    private CLIRiskControlUtil() {}

    /**
     * 判断命令的风险级别
     * @param command 输入的命令字符串
     * @return 风险级别枚举值
     */
    public static RiskLevel getRiskLevel(String command) {
        if (command == null || command.trim().isEmpty()) {
            return RiskLevel.LOW_RISK;
        }

        String trimmedCommand = command.trim();
        boolean containsHighRiskPath = containsHighRiskPath(trimmedCommand);
        String commandName = extractFirstCommand(trimmedCommand);

        if (commandName == null) {
            return containsHighRiskPath ? RiskLevel.HIGH_RISK : RiskLevel.LOW_RISK;
        }

        String lowerCommandName = commandName.toLowerCase();

        // 判断是否为只读命令
        boolean isReadOnly = false;
        for (String readOnlyCmd : READ_ONLY_COMMANDS) {
            if (readOnlyCmd.equalsIgnoreCase(lowerCommandName)) {
                isReadOnly = isPureReadOperation(trimmedCommand, lowerCommandName);
                break;
            }
        }

        if (isReadOnly) {
            return containsHighRiskPath ? RiskLevel.HIGH_RISK : RiskLevel.READ_ONLY;
        }

        // 判断是否为高风险命令
        for (String highRiskCmd : HIGH_RISK_COMMANDS) {
            if (highRiskCmd.equalsIgnoreCase(lowerCommandName)) {
                return RiskLevel.HIGH_RISK;
            }
        }

        // 既不是只读也不是高风险命令
        if (containsHighRiskPath) {
            return RiskLevel.HIGH_RISK;
        }

        return RiskLevel.LOW_RISK;
    }

    /**
     * 判断是否为纯读取操作
     * 需要考虑命令是否可以写入，如cat可以写入，ls只能读取
     */
    private static boolean isPureReadOperation(String command, String commandName) {
        // 这些命令通常只读取，不会写入
        String[] pureReadCommands = {"ls", "dir", "pwd", "type", "where", "which", "find", "locate", "grep", "stat", "who", "w", "whoami", "id", "env", "printenv"};
        for (String pureCmd : pureReadCommands) {
            if (pureCmd.equalsIgnoreCase(commandName)) {
                return true;
            }
        }

        // 对于可能有写入操作的命令，需要检查是否有重定向
        boolean hasRedirection = false;
        for (String symbol : REDIRECTION_SYMBOLS) {
            if (command.contains(symbol)) {
                hasRedirection = true;
                break;
            }
        }

        // 有重定向意味着可能写入，不是纯读取
        return !hasRedirection;
    }

    /**
     * 提取第一个命令（管道前的第一个命令）
     */
    private static String extractFirstCommand(String command) {
        // 按管道分割
        String[] parts = command.split("\\" + PIPE_SYMBOL);
        if (parts.length == 0) {
            return null;
        }

        String firstPart = parts[0].trim();
        String[] tokens = firstPart.split("\\s+");

        // 找到第一个非重定向符号的单词
        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            if (isRedirectionSymbol(token)) {
                // 跳过重定向符号和它的参数
                if (i + 1 < tokens.length) {
                    i++; // 跳过文件名
                }
            } else if (!token.isEmpty()) {
                // 返回命令名
                return token;
            }
        }

        return null;
    }

    /**
     * 判断是否为重定向符号
     */
    private static boolean isRedirectionSymbol(String token) {
        for (String symbol : REDIRECTION_SYMBOLS) {
            if (symbol.equals(token)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查是否包含高风险路径
     */
    private static boolean containsHighRiskPath(String command) {
        List<String> highRiskPaths = OS_TYPE == OSType.WINDOWS ?
                WINDOWS_HIGH_RISK_PATHS : LINUX_HIGH_RISK_PATHS;

        for (String path : highRiskPaths) {
            // 检查路径是否在命令中
            if (command.contains(path)) {
                return true;
            }
        }

        // 检查Windows驱动器根目录
        if (OS_TYPE == OSType.WINDOWS) {
            Pattern drivePattern = Pattern.compile("[A-Za-z]:\\\\[^\\\\]");
            Matcher matcher = drivePattern.matcher(command);
            if (matcher.find()) {
                return true;
            }
        }

        return false;
    }

    /**
     * 批量判断命令风险级别
     */
    public static List<RiskLevel> getRiskLevels(List<String> commands) {
        List<RiskLevel> results = new ArrayList<>();
        for (String command : commands) {
            results.add(getRiskLevel(command));
        }
        return results;
    }

    // 示例用法
    public static void main(String[] args) {
        // 测试用例
        String[] testCommands = {
                "ls -la",  // Linux只读
                "dir C:\\Windows",  // Windows查看系统目录
                "cat /etc/passwd",  // Linux读取文件
                "cat > /etc/config.txt",  // 写入系统文件
                "rm -rf /tmp/*",  // Linux删除文件
                "del C:\\Windows\\System32\\*.dll",  // Windows删除系统文件
                "echo Hello World",  // 只读输出
                "echo Hello > C:\\Windows\\test.txt",  // Windows写入系统目录
                "python3 script.py",  // 高风险命令
                "ping google.com",  // 只读网络命令
                "cp file1.txt file2.txt",  // 复制文件
                "mv /etc/nginx.conf /etc/nginx.conf.backup",  // 移动系统文件
                "find /etc -name '*.conf'",  // 搜索系统目录
                "ls /var/log"  // 查看系统目录
        };

        System.out.println("\n=== Linux/macOS 系统 ===");
        for (String cmd : testCommands) {
            RiskLevel level = CLIRiskControlUtil.getRiskLevel(cmd);
            System.out.printf("%-50s -> %s%n", cmd, level);
        }

        // 批量测试
        System.out.println("\n=== 批量测试 ===");
        List<String> batchCommands = Arrays.asList("ls -l", "rm -rf /", "cat /etc/passwd");
        List<RiskLevel> levels = CLIRiskControlUtil.getRiskLevels(batchCommands);
        for (int i = 0; i < batchCommands.size(); i++) {
            System.out.printf("%-20s -> %s%n", batchCommands.get(i), levels.get(i));
        }
    }

}


