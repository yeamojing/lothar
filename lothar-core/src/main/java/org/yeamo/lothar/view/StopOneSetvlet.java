package org.yeamo.lothar.view;

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
@WebServlet("/lothar/stop/one")
public class StopOneSetvlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        MonitorHandler.stop = "true".equals(req.getParameter("s"));
    }
}
