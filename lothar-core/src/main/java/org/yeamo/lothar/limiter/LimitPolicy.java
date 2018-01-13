package org.yeamo.lothar.limiter;

import com.google.common.util.concurrent.RateLimiter;
import org.yeamo.lothar.common.CommonUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 限流策略
 *
 * @author: jingzhuo
 * @since: 2017/10/12
 */
public class LimitPolicy {

    private String method;
    private String desc;
    private String join;
    private String threshold;
    private Map<String, String> conditionMap = new ConcurrentHashMap<String, String>();
    private String conditions;
    private RateLimiter limiter;

    enum JoinLogic {
        AND, OR
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getJoin() {
        return join;
    }

    public void setJoin(String join) {
        this.join = join;
    }

    public String getThreshold() {
        return threshold;
    }

    public void setThreshold(String threshold) {

        double max = Double.valueOf(threshold) / 60;

        if (limiter == null) {
            limiter = RateLimiter.create(max);
        } else {
            limiter.setRate(max);
        }

        this.threshold = threshold;
    }

    public Map<String, String> getConditionMap() {
        return conditionMap;
    }

    public void setConditionMap(Map<String, String> conditionMap) {
        this.conditionMap = conditionMap;
    }

    public RateLimiter getLimiter() {
        return limiter;
    }

    public void setLimiter(RateLimiter limiter) {
        this.limiter = limiter;
    }

    public String getConditions() {
        return CommonUtil.map2Str(conditionMap);
    }

    public void setConditions(String conditionStr) {
        CommonUtil.str2Map(conditionStr, conditionMap);
    }
}
