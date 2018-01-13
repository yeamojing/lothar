package org.yeamo.lothar.limiter;

import com.alibaba.fastjson.JSON;
import org.yeamo.lothar.exception.UnexpectedException;
import org.yeamo.lothar.watcher.MonitorHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.zookeeper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * zk用于存储方法的限流策略集合，并负责将策略的变化推送到集群中所有实例
 * 其存储路径为【/用户指定根目录/用户自定义目录+appid/json形式的LimitPolicy数组】
 *
 * @author: jingzhuo
 * @since: 2017/10/17
 */
public class Commander implements Watcher {

    Logger logger = LoggerFactory.getLogger("lothar-log");

    ZooKeeper zk;

    private String server;
    private String rootPath;
    private int timeout = 30000;

    public Commander(String server, String root, String path) {

        this.server = server;
        this.rootPath = root + path + "_" + System.getProperty("deploy.app.id");

        try {

            logger.info("init_commander-server:"+server+",root:"+root+",path:"+rootPath);

            if (_getZk().exists(rootPath, true) == null)
                _getZk().create(this.rootPath, "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);

            _initPolicyBook();

            // 启动一个定时设置watcher的线程，避免因为监听中断导致的配置无法更新，一分钟执行一次即可
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        Thread.currentThread().setName("lothar-time-watch");
                        logger.info("timer_watch_run...");
                        _watchAll();
                    } catch (Throwable e) {
                        logger.error("timer_watch_error", e);
                    }
                }
            }, 3000, 60000);

        } catch (Exception e) {
            logger.error("init_zookeeper_error.", e);
        }
    }

    /**
     * 初始化限流策略缓存
     */
    private void _initPolicyBook() {
        try {
            MonitorHandler.stop = "true".equals(getData(this.rootPath));

            List<String> pathls = _getZk().getChildren(this.rootPath, true);
            for (String path : pathls) {
                _load2Book(rootPath + "/" + path);
            }

            logger.info("initbook_done...");
        } catch (Exception e) {
            logger.error("initbook_error.", e);
        }
    }

    /**
     * 获取方法节点下的限流策略，并将其设置到策略缓存中
     */
    private void _load2Book(String path) {

        if (!path.startsWith(rootPath)) return;

        String method = path.substring(path.lastIndexOf("/") + 1, path.length());
        String dataStr = getData(path);
        dataStr = StringUtils.isBlank(dataStr) ? null : dataStr;
        Doorkeeper.bookChange(method, JSON.parseArray(dataStr, LimitPolicy.class));

        logger.info("load2book-method:" + method + ",data:" + dataStr);
    }

    public String getData(String path) {
        try {
            byte[] bt = _getZk().getData(path, true, null);
            return new String(bt, "UTF-8");
        } catch (Exception e) {
            logger.error("getdata_error.", e);
            throw new UnexpectedException(e);
        }
    }

    /**
     * 对所有方法设置监听事件
     *
     * @throws KeeperException
     * @throws InterruptedException
     */
    private void _watchAll() {

        try {
            // 监听根节点数据变换，存的是
            _getZk().exists(rootPath, true);

            // 同步leader，确保数据一致
            _getZk().sync(rootPath, null, null);

            List<String> pathls = _getZk().getChildren(this.rootPath, true);
            for (String path : pathls) {

                logger.info("watchall-path:" + path);
                _getZk().exists(this.rootPath + "/" + path, true);
            }
        } catch (Exception e) {
            logger.error("watchall_error.", e);
            throw new UnexpectedException(e);
        }
    }

    /**
     * 获取zk客户端对象
     *
     * @return 如果不存在或者断开，则新建一个
     */
    private ZooKeeper _getZk() {

        try {
            if (zk != null) {
                if (zk.getState().isAlive()) {
                    return zk;
                }
                zk.close();
            }

            if (StringUtils.isBlank(server))
                throw new UnexpectedException("zk server is empty.");

            zk = new ZooKeeper(server, timeout, this);
        } catch (Exception e) {
            logger.error("getzk_error.", e);
            throw new UnexpectedException(e);
        }
        return zk;
    }


    @Override
    public void process(WatchedEvent event) {

        logger.info("process-path:" + event.getPath() + ",type:" + event.getType());

        // 链接断开后，需要重新链接并设置监听
        if (event.getType() == Event.EventType.None) {
            _watchAll();

        } else if (event.getType() == Event.EventType.NodeChildrenChanged) {
            _watchAll();

        } else if (event.getType() == Event.EventType.NodeDataChanged) {

            if (rootPath.equals(event.getPath())) {
                MonitorHandler.stop = "true".equals(getData(event.getPath()));
            } else {
                _load2Book(event.getPath());
            }
        }
    }


    /**
     * 给方法设置限流策略，并推送到所有实例
     */
    public void setStop(boolean isStop) {

        try {
            _getZk().setData(rootPath, Boolean.valueOf(isStop).toString().getBytes(), -1);
        } catch (Exception e) {
            logger.error("setstop_error-data:" + isStop, e);
            throw new UnexpectedException(e);
        }
    }

    /**
     * 给方法设置限流策略，并推送到所有实例
     *
     * @param method
     * @param data
     */
    public void setData(String method, String data) {

        String path = rootPath + "/" + method;

        try {
            if (_getZk().exists(path, true) == null) {
                _getZk().create(path, "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);

                // 暂停3秒，等待新增的节点已经设置watcher
                Thread.sleep(3000);
            }

            _getZk().setData(path, data.getBytes("UTF-8"), -1);

        } catch (Exception e) {
            logger.error("setdata_error-data:" + data, e);
            throw new UnexpectedException(e);
        }
    }

    public List<String> getChild(String path){
        try {
            return _getZk().getChildren(path, false);
        } catch (Exception e) {
            throw new UnexpectedException(e);
        }finally {
            _watchAll();
        }
    }

    public void createPath(String path){
        try {
            _getZk().create(path, "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        } catch (Exception e) {
            throw new UnexpectedException(e);
        }
    }
}
