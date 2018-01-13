package org.yeamo.lothar.limiter;

import com.alibaba.fastjson.JSON;
import org.yeamo.lothar.common.SpringContextUtil;
import org.yeamo.lothar.watcher.annotation.MonitorArgs;
import org.yeamo.lothar.watcher.annotation.MonitorMethod;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 接口访问管理员，根据限流策略来拒绝请求
 *
 * @author: jingzhuo
 * @since: 2017/10/16
 */
public class Doorkeeper {


    public static Map<String, List<LimitPolicy>> limitbook = new ConcurrentHashMap<String, List<LimitPolicy>>();

    /**
     * zk触发回调
     */
    static void bookChange(String method, List<LimitPolicy> policies) {

        // 每次都对单个方法的所有策略进行删除重建
        if (policies == null) {
            limitbook.remove(method);
        } else {
            limitbook.put(method, policies);
        }
    }

    /**
     * 设置限流策略，如果没有配置zk，则只设置单机配置
     *
     * @param method
     * @param policies
     */
    public static void setPolicy(String method, List<LimitPolicy> policies) {

        List<LimitPolicy> pls = new ArrayList<LimitPolicy>();
        if (CollectionUtils.isNotEmpty(policies)) {
            for (LimitPolicy policy : policies) {
                if (StringUtils.isNotBlank(policy.getThreshold())) pls.add(policy);
            }
        }

        if (SpringContextUtil.getContext() != null) {
            try {
                Commander cmd = SpringContextUtil.getBean(Commander.class);
                if (cmd != null) {
                    cmd.setData(method, JSON.toJSONString(pls));
                    Thread.sleep(2000);
                    return;
                }
            } catch (Throwable e) {
            }
        }

        // 设置单机策略
        bookChange(method, pls);
    }

    /**
     * 请求访问
     *
     * @param method  要访问的方法
     * @param args    方法入参
     * @param monitor 监控描述
     * @return true 可以访问 | false 已经达到阈值
     */
    public static boolean ask4Access(String method, Object[] args, MonitorMethod monitor) {

        try {
            List<LimitPolicy> policies = limitbook.get(method);
            if (CollectionUtils.isNotEmpty(policies)) {
                for (LimitPolicy policy : policies) {
                    if (method.equals(policy.getMethod()) && !_isAcquired(args, monitor, policy))
                        return false;
                }
            }

        } catch (Throwable e) {
        }

        return true;
    }

    /**
     * 是否达到请求上限
     */
    private static boolean _isAcquired(Object[] args, MonitorMethod monitor, LimitPolicy policy) throws Exception {

        if (policy.getConditionMap().size() == 0) return policy.getLimiter().tryAcquire();

        int matchCount = 0;
        for (MonitorArgs ma : monitor.monitorArgs()) {
            String val = policy.getConditionMap().get(ma.alias());

            if (StringUtils.isNotBlank(val)) {
                Object obj = StringUtils.isBlank(ma.path()) ? args[ma.index() - 1] : BeanUtils.getNestedProperty(args[ma.index() - 1], ma.path());

                if (obj != null && Arrays.asList(StringUtils.split(val, "|")).contains(obj.toString())) {

                    // 限流策略之间如果是 OR 的关系，任意满足任意一个条件即可匹配
                    if (LimitPolicy.JoinLogic.OR.name().equalsIgnoreCase(policy.getJoin())) {
                        return policy.getLimiter().tryAcquire();
                    }

                    // 如果是AND 关系，则需求满足所有条件才可匹配，因为单策略内条件不可重复，所以简单的使用计数方式即可知道是否完全匹配
                    matchCount++;
                }
            }
        }

        return matchCount == policy.getConditionMap().size() ? policy.getLimiter().tryAcquire() : true;
    }

}
