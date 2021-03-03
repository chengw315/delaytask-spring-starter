package tech.chengw.www.annocation;

import tech.chengw.www.task.MethodTaskExecutorProxy;

import java.lang.annotation.*;

/**
 * 使用此注解的方法会被自动代理成 {@link MethodTaskExecutorProxy}
 * @author chengw
 */
@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TaskExecutor {

    /**
     * 是否自动ack，此参数的优先级高于全局配置的autoAck
     * @return
     */
    boolean autoAck() default true;
}
