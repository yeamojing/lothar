package org.yeamo.lothar.watcher;

import org.yeamo.lothar.common.CommonUtil;
import org.yeamo.lothar.common.SpringContextUtil;
import org.yeamo.lothar.exception.LimitException;
import org.yeamo.lothar.limiter.Doorkeeper;
import org.yeamo.lothar.watcher.annotation.MonitorMethod;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.Order;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 监控埋点处理类
 *
 * @author: jingzhuo
 * @since: 2017/10/12
 */
@Aspect
@Order(Integer.MAX_VALUE)
public class MonitorHandler implements ApplicationContextAware {

    // 包名映射，缩短类名
    Map<String, String> packageMapping = new HashMap<String, String>();
    String limitMsg;
    public static volatile boolean stop = false;

    @Before("@annotation(monitor)")
    public void invoke(JoinPoint jp, MonitorMethod monitor) throws Throwable {

        try {

            if (stop) return;

            Method method = ((MethodSignature) jp.getSignature()).getMethod();
            String className = jp.getTarget().getClass().getCanonicalName();

            // 替换包名为映射的字符串
            for (Map.Entry<String,String> entry : packageMapping.entrySet()) {
                if(className.startsWith(entry.getKey())){
                    className = className.replaceFirst(entry.getKey(),entry.getValue());
                    break;
                }
            }

            String methodName = className + "." + method.getName();
            if (StringUtils.isNotBlank(monitor.alias()))
                methodName = methodName + "_" + monitor.alias();

            if (!Doorkeeper.ask4Access(methodName, jp.getArgs(), monitor))
                throw new LimitException(StringUtils.isNotBlank(limitMsg) ? limitMsg : "Request is restricted!");

            RequestCounter.record(methodName, jp.getArgs(), monitor);
        } catch (LimitException e) {
            throw e;
        } catch (Throwable e) {
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringContextUtil.setContext(applicationContext);
    }

    public void setLimitMsg(String limitMsg) {
        this.limitMsg = limitMsg;
    }

    public void setPackageMapping(String packMap) {
        CommonUtil.str2Map(packMap, packageMapping);
    }
}