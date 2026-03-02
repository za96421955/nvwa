package ai.nvwa.context;

import ai.nvwa.agent.NvwaAgent;
import ai.nvwa.agent.impl.MainAgent;
import com.alibaba.cloud.ai.graph.RunnableConfig;

import java.util.Scanner;
import java.util.UUID;

/**
 * 主线程上下文
 * <p> <功能详细描述> </p>
 *
 * @author 陈晨
 * @version 1.0
 * @date 2026/3/2
 */
public class MainContext {

    /**
     * Scanner
     */
    private static Scanner scanner;
    public static Scanner getScanner() {
        if (scanner == null) {
            scanner = new Scanner(System.in);
        }
        return scanner;
    }
    public static void clearScanner() {
        if (scanner == null) {
            return;
        }
        scanner.close();
        scanner = null;
    }

    /**
     * Agent
     */
    private static NvwaAgent agent;
    public static NvwaAgent getAgent() {
        if (agent != null) {
            return agent;
        }
        agent = new MainAgent();
        return agent;
    }
    public static void clearAgent() {
        if (agent != null) {
            agent.clear();
            agent = null;
        }
    }

    /**
     * context
     */
    private static RunnableConfig context;
    public static RunnableConfig getContext() {
        if (context != null) {
            return context;
        }
        context = RunnableConfig.builder()
                .threadId(UUID.randomUUID().toString())
                .build();
        return context;
    }
    public static void clearContext() {
        context = null;
    }

    /**
     * 安全级别
     */
    private static SecurityLevel securityLevel;
    public static SecurityLevel getSecurityLevel() {
        if (securityLevel == null) {
            // 默认谨慎操作, 完全受控
            securityLevel = SecurityLevel.get(0);
        }
        return securityLevel;
    }
    public static void setSecurityLevel(SecurityLevel securityLevel) {
        if (securityLevel == null) {
            return;
        }
        MainContext.securityLevel = securityLevel;
    }
    public static boolean isPrudent() {
        return SecurityLevel.isPrudent(getSecurityLevel());
    }
    public static boolean isReadOnly() {
        return SecurityLevel.isReadOnly(getSecurityLevel());
    }
    public static boolean isRiskControl() {
        return SecurityLevel.isRiskControl(getSecurityLevel());
    }
    public static boolean isCompleteConfidence() {
        return SecurityLevel.isCompleteConfidence(getSecurityLevel());
    }

}


