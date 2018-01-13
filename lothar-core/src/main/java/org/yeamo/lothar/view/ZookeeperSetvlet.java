package org.yeamo.lothar.view;

import org.yeamo.lothar.common.SpringContextUtil;
import org.yeamo.lothar.limiter.Commander;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 图形化的页面
 *
 * @author: jingzhuo
 * @since: 2017/10/13
 */
@WebServlet("/lothar/zk")
public class ZookeeperSetvlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String m = req.getParameter("m");
        String p = req.getParameter("p");
        Commander cmd = SpringContextUtil.getBean(Commander.class);

        if (cmd == null) return;

        resp.setCharacterEncoding("utf-8");
        resp.setContentType("text/html");

        if ("create".equals(m)) {
            cmd.createPath(p);
        }
        if ("child".equals(m)) {
            List ls = cmd.getChild(p);
            for (Object l : ls) {
                resp.getWriter().write(l.toString()+"<br/>");
            }

        }
        if ("data".equals(m)) {
            resp.getWriter().write(cmd.getData(p));
        }

        resp.getWriter().close();
    }
}
