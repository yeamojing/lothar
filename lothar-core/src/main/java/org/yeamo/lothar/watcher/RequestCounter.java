package org.yeamo.lothar.watcher;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.yeamo.lothar.common.LongAdder;
import org.yeamo.lothar.watcher.annotation.MonitorArgs;
import org.yeamo.lothar.watcher.annotation.MonitorMethod;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

/**
 * 请求计数器，用于构建树形的请求记录
 *
 * @author: jingzhuo
 * @since: 2017/10/12
 */
public class RequestCounter {

    static private ExecutorService es = new ThreadPoolExecutor(10, 20, 1L, TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>(10000), new ThreadFactoryBuilder()
            .setNameFormat("lothar-counter-%d")
            .setDaemon(true)
            .build(), new ThreadPoolExecutor.DiscardPolicy());

    public static MonitNode monitRoot = new MonitNode();

    public static final int maxMinutes = 60;

    /**
     * 请求计数
     *
     * @param method 方法
     * @param args   参数
     */
    public static void record(final String method, final Object[] args, final MonitorMethod monitor) {

        es.submit(new Runnable() {
            @Override
            public void run() {

                try {
                    /*
                     * 1、先将参数按照观察点定义的level构建成一个二维数据，一维就是level，二维是参数值
                     * 2、然后再将这个而数组排列成一个符合level的请求树
                     * 3、通过递归这个树去计数
                     *
                     * 构建树的目的主要是考虑一次性统计所有观察点，因为guava的LRU算法在get的时候会影响淘汰的顺序，所以要避免多次get
                     */
                    MonitorArgs[] marr = monitor.monitorArgs();
                    Map<Integer, Set<RequestNode>> map = new HashMap<Integer, Set<RequestNode>>();

                    for (MonitorArgs ma : marr) {
                        if (ma.level() >= 10) continue;

                        Object obj = StringUtils.isBlank(ma.path()) ? args[ma.index() - 1] : BeanUtils.getNestedProperty(args[ma.index() - 1], ma.path());
                        if (obj == null) continue;

                        if (map.get(ma.level()) == null) map.put(ma.level(), new HashSet<RequestNode>());
                        map.get(ma.level()).add(new RequestNode(ma.alias(), obj.toString(), ma.topSize()));
                    }

                    // 将二维数组构建成请求树

                    RequestNode requestMethod = new RequestNode("", method, 2000);

                    if (map.get(0) == null) map.put(0, new HashSet<RequestNode>());
                    map.get(0).add(requestMethod);

                    for (int i = 1; i < map.size(); i++) {

                        // level不连续，则忽略中断后的level
                        if (map.get(i - 1) == null) break;
                        for (RequestNode parent : map.get(i - 1)) {
                            for (RequestNode child : map.get(i)) {
                                parent.getChildren().add(child);
                            }
                        }
                    }

                    // 当前分钟数
                    long minute = System.currentTimeMillis() / (maxMinutes * 1000);
                    _recursiveMonitTree(requestMethod, monitRoot, minute);

                } catch (Throwable e) {
                }
            }
        });
    }

    /**
     * 递归计数
     *
     * @param requestNode 请求数据节点
     * @param monitNode   观察数据节点
     * @param minuteNow   当前分钟
     * @throws ExecutionException
     */
    private static void _recursiveMonitTree(final RequestNode requestNode, MonitNode monitNode, long minuteNow) throws ExecutionException {

        // 通过请求点获取观察点，然后计数
        MonitNode monitChild = monitNode.getChildren(requestNode.getTopSize()).get(requestNode.getKey(), new Callable<MonitNode>() {
            @Override
            public MonitNode call() throws Exception {
                MonitNode mn = new MonitNode();
                mn.setAlias(requestNode.getAlias());
                return mn;
            }
        });

        // 以分钟为粒度记录访问的频次dz
        LongAdder count = monitChild.getHistory().get(minuteNow);
        if (count == null) {

            // 在新的一分钟开始的时候，清理过期的计数器，确保缓存可控
            _clean(minuteNow, monitChild.getHistory());

            count = new LongAdder();
            monitChild.getHistory().put(minuteNow, count);
        }
        count.increment();

        for (RequestNode requestChild : requestNode.getChildren()) {
            _recursiveMonitTree(requestChild, monitChild, minuteNow);
        }
    }

    /**
     * 清理过期的计数器
     */
    private static void _clean(Long now, Map<Long, LongAdder> history) {

        for (Long time : history.keySet()) {
            if (time + 60 < now) {
                history.remove(time);
            }
        }
    }
}
