package org.yeamo.lothar.watcher;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.yeamo.lothar.common.LongAdder;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 观察点，每个观察点有自己的子节点，形成一颗观察树
 * 
 * @author: jingzhuo
 * @since: 2017/10/12
 */
public class MonitNode {

    // 别名
    private String alias;

    // 子节点
    private Cache<String, MonitNode> children;

    // 请求历史,一分钟粒度，最长一小时
    private Map<Long,LongAdder> history = new ConcurrentHashMap<Long, LongAdder>();

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public Cache<String, MonitNode> getChildren() {
        return children;
    }

    public Cache<String, MonitNode> getChildren(int topSize) {
        if(children == null){
            children = CacheBuilder.newBuilder().maximumSize(topSize).build();
        }
        return children;
    }

    public void setChildren(Cache<String, MonitNode> children) {
        this.children = children;
    }

    public Map<Long, LongAdder> getHistory() {
        return history;
    }

}
