package ai.nvwa.context;

import lombok.Getter;

/**
 * 安全级别
 * <p> <功能详细描述> </p>
 *
 * @author 陈晨
 * @version 1.0
 * @date 2026/3/2
 */
@Getter
public enum SecurityLevel {
    PRUDENT(0, "谨慎操作"),
    READ_ONLY(1, "文件只读"),
    RISK_CONTROL(2, "风险控制"),
    COMPLETE_CONFIDENCE(3, "完全信任"),
    ;

    private final int level;
    private final String desc;
    SecurityLevel(int level, String desc) {
        this.level = level;
        this.desc = desc;
    }

    public static SecurityLevel get(int level) {
        for (SecurityLevel securityLevel : values()) {
            if (securityLevel.level == level) {
                return securityLevel;
            }
        }
        return null;
    }

    public static boolean isPrudent(SecurityLevel securityLevel) {
        if (securityLevel == null) {
            return false;
        }
        return securityLevel.level == PRUDENT.level;
    }

    public static boolean isReadOnly(SecurityLevel securityLevel) {
        if (securityLevel == null) {
            return false;
        }
        return securityLevel.level == READ_ONLY.level;
    }

    public static boolean isRiskControl(SecurityLevel securityLevel) {
        if (securityLevel == null) {
            return false;
        }
        return securityLevel.level == RISK_CONTROL.level;
    }

    public static boolean isCompleteConfidence(SecurityLevel securityLevel) {
        if (securityLevel == null) {
            return false;
        }
        return securityLevel.level == COMPLETE_CONFIDENCE.level;
    }

}
