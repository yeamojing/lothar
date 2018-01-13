package org.yeamo.lothar.view;

import org.yeamo.lothar.common.SpringContextUtil;
import org.yeamo.lothar.exception.UnexpectedException;
import org.yeamo.lothar.limiter.Commander;
import org.yeamo.lothar.watcher.MonitorHandler;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 图形化的页面
 *
 * @author: jingzhuo
 * @since: 2017/10/13
 */
@WebServlet("/lothar/stop/all")
public class StopAllSetvlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        Boolean b = "true".equals(req.getParameter("s"));

        Commander cmd = null;
        try {
            cmd = SpringContextUtil.getBean(Commander.class);
        } catch (Throwable e) {
        }

        // 没有声明zk相关配置，则直接修改单机状态
        if (cmd == null) {
            MonitorHandler.stop = b;
            return;
        }

        cmd.setStop(b);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new UnexpectedException(e);
        }
    }
}
