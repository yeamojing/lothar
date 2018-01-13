package org.yeamo.lothar.view;

import com.alibaba.fastjson.JSON;
import org.yeamo.lothar.limiter.Doorkeeper;
import org.yeamo.lothar.limiter.LimitPolicy;

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
@WebServlet("/lothar/policy/add")
public class PolicyAddSetvlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String method = req.getParameter("m");
        String data = req.getParameter("d");

        Doorkeeper.setPolicy(method, JSON.parseArray(data, LimitPolicy.class));

        resp.getWriter().write("success");
        resp.getWriter().close();
    }
}
