package ai.nvwa.components.util;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.HibernateValidator;
import org.springframework.util.CollectionUtils;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.*;

/**
 * Validation 入参验证工具
 * <p> <功能详细描述> </p>
 *
 * @author 陈晨
 */
public abstract class ValidationUtil {
    private static final Validator validator = Validation.byProvider(HibernateValidator.class)
            .configure().failFast(true).buildValidatorFactory().getValidator();

    private ValidationUtil() {}

    /**
     * @description 参数校验
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public static <T> String validate(T t) {
        if (null == t) {
            return "request args is null";
        }
        // Map
        if (t instanceof Map) {
            Map tMap = (Map) t;
            for (Object key : tMap.keySet()) {
                String result = validate(key);
                if (StringUtils.isNotBlank(result)) {
                    return result;
                }
            }
            for (Object value : tMap.values()) {
                String result = validate(value);
                if (StringUtils.isNotBlank(result)) {
                    return result;
                }
            }
        }
        // Collection
        if (t instanceof Collection) {
            Iterator iter = ((Collection) t).iterator();
            while (iter.hasNext()) {
                String result = validate(iter.next());
                if (StringUtils.isNotBlank(result)) {
                    return result;
                }
            }
        }
        // 参数验证
        Set<ConstraintViolation<T>> constraintViolations = validator.validate(t);
        if (CollectionUtils.isEmpty(constraintViolations)) {
            return "";
        }
        List<String> tipList = new ArrayList<>();
        constraintViolations.forEach(cv -> tipList.add(cv.getMessage()));
        return StringUtils.join(tipList, ",");
    }

}


