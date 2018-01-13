package org.yeamo.lothar.watcher;

import java.util.ArrayList;
import java.util.List;

/**
 * 请求点，用于描述监控的请求所解析出的结构化数据
 * 将请求打到的点构建成树形，以便计数器统计
 *
 * @author: jingzhuo
 * @since: 2017/10/12
 */
public class RequestNode {

    private String alias;
    private String key;
    private List<RequestNode> children = new ArrayList<RequestNode>();
    private int topSize;

    public RequestNode(String alias, String key, int topSize) {
        this.alias = alias;
        this.key = key;
        this.topSize = topSize;
    }

    public String getAlias() {
        return alias;
    }

    public String getKey() {
        return key;
    }

    public List<RequestNode> getChildren() {
        return children;
    }

    public int getTopSize() {
        return topSize;
    }

}
