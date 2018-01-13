package org.yeamo.lothar.watcher.annotation;

import java.lang.annotation.*;

/**
 * 方法监控埋点
 *
 * @author: jingzhuo
 * @since: 2017/10/12
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MonitorMethod {
    String alias() default "";
    MonitorArgs[] monitorArgs() default {};
}
