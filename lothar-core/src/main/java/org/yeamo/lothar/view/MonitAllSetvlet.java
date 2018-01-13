package org.yeamo.lothar.view;

import com.alibaba.fastjson.JSON;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 图形化的页面
 *
 * @author: jingzhuo
 * @since: 2017/10/13
 */
@WebServlet("/lothar/all/get")
public class MonitAllSetvlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        List ls = new ArrayList();
        MonitViewSetvlet.loadData(ls, null, System.currentTimeMillis(), null);

        resp.setCharacterEncoding("utf-8");
        resp.getWriter().write(JSON.toJSONString(MonitViewSetvlet.getPage(ls,1,Integer.MAX_VALUE,3)));
        resp.getWriter().close();
    }
}
