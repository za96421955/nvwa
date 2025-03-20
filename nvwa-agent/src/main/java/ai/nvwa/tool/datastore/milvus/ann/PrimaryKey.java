package ai.nvwa.tool.datastore.milvus.ann;

import java.lang.annotation.*;

/**
 * @description TODO
 * <p> <功能详细描述> </p>
 *
 * @author 陈晨
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PrimaryKey {

    /** 自增ID */
    boolean autoID() default true;

}


