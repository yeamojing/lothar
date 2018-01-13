package org.yeamo.lothar.watcher.annotation;

import java.lang.annotation.*;

/**
 * 埋点参数监控
 *
 * @author: jingzhuo
 * @since: 2017/10/12
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MonitorArgs {

    int level() default 1;
    int topSize() default 2000;
    int index();
    String alias();
    String path() default "";
}
