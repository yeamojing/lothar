package org.yeamo.lothar.common;

import org.springframework.context.ApplicationContext;

/**
 * @author: jingzhuo
 * @since: 2017/10/23
 */
public class SpringContextUtil {

    private static ApplicationContext context;

    public static ApplicationContext getContext() {
        return context;
    }

    public static void setContext(ApplicationContext context) {
        SpringContextUtil.context = context;
    }

    public static <T>T getBean(Class<T> clz) {

        String[] names = context.getBeanNamesForType(clz);
        if (names.length == 0) return null;
        return (T) context.getBean(names[0], clz);
    }
}
