package org.yeamo.lothar.view;

import com.alibaba.fastjson.JSON;
import org.yeamo.lothar.limiter.Doorkeeper;
import org.yeamo.lothar.limiter.LimitPolicy;
import org.apache.commons.lang.StringUtils;

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
@WebServlet("/lothar/policy/get")
public class PolicyGetSetvlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String method = req.getParameter("m");

        if (method != null) method = new String(method.getBytes("ISO-8859-1"), "UTF-8");

        List<LimitPolicy> ls = new ArrayList<LimitPolicy>();
        if (StringUtils.isNotBlank(method)) {
            ls = Doorkeeper.limitbook.get(method);
        } else {
            for (List<LimitPolicy> limitPolicies : Doorkeeper.limitbook.values()) {
                ls.addAll(limitPolicies);
            }
        }

        resp.setCharacterEncoding("utf-8");
        resp.getWriter().write(JSON.toJSONString(ls));
        resp.getWriter().close();
    }
}
