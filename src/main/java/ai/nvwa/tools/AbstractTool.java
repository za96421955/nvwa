package ai.nvwa.tools;

import ai.nvwa.context.MainContext;
import ai.nvwa.util.CLIRiskControlUtil;
import org.apache.commons.lang3.StringUtils;

/**
 * 工具抽象
 * <p> <功能详细描述> </p>
 *
 * @author 陈晨
 * @version 1.0
 * @date 2026/2/28
 */
public class AbstractTool {

    /**
     * @description 执行授权
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     * @date 2026/2/28 15:26
     */
    protected String author(String command) {
        System.out.println("\n即将执行: [" + command + "]");
        /*
         * 风险控制
         */
        if (!this.riskControl(command)) {
            return null;
        }

        /*
         * 申请授权
         */
        System.out.println("申请授权: y/n");
        String input;
        do {
            System.out.print("> ");
            input = MainContext.getScanner().nextLine();
        } while (StringUtils.isBlank(input));
        // 同意则返回空, 否则返回拒绝原因
        if ("y".equalsIgnoreCase(input)) {
            return null;
        }
        return input;
    }

    private boolean riskControl(String command) {
        // 完全信任
        if (MainContext.isCompleteConfidence()) {
            return false;
        }
        // 自定义工具, 需授权
        if ("internetSearch".equalsIgnoreCase(command)
                || "createSubAgent".equalsIgnoreCase(command)) {
            return true;
        }
        // 风险控制
        CLIRiskControlUtil.RiskLevel riskLevel = CLIRiskControlUtil.getRiskLevel(command);
        if (MainContext.isRiskControl() && !this.isRisk(riskLevel)) {
            return false;
        }
        // 文件只读
        if (MainContext.isReadOnly() && this.isReadOnly(riskLevel)) {
            return false;
        }
        // 其他, 谨慎操作
        return true;
    }

    private boolean isReadOnly(CLIRiskControlUtil.RiskLevel riskLevel) {
        return CLIRiskControlUtil.RiskLevel.READ_ONLY.equals(riskLevel);
    }

    private boolean isRisk(CLIRiskControlUtil.RiskLevel riskLevel) {
        return CLIRiskControlUtil.RiskLevel.HIGH_RISK.equals(riskLevel);
    }

}


