package org.yeamo.lothar.view;

import com.alibaba.fastjson.JSON;
import org.yeamo.lothar.common.LongAdder;
import org.yeamo.lothar.watcher.MonitNode;
import org.yeamo.lothar.watcher.MonitorHandler;
import org.yeamo.lothar.watcher.RequestCounter;
import org.apache.commons.lang.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * 图形化的页面
 *
 * @author: jingzhuo
 * @since: 2017/10/13
 */
@WebServlet("/lothar")
public class MonitViewSetvlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        long now = System.currentTimeMillis();
        List ls = new ArrayList();

        String pstr = req.getParameter("pstr");
        String page = req.getParameter("page");
        String size = req.getParameter("size");
        String last = req.getParameter("last");
        String match = req.getParameter("match");

        if (pstr != null) pstr = new String(pstr.getBytes("ISO-8859-1"), "UTF-8");
        if (match != null) match = new String(match.getBytes("ISO-8859-1"), "UTF-8");

        int topn = StringUtils.isBlank(size) ? 20 : Integer.valueOf(size);
        int lastmin = StringUtils.isBlank(last) ? 5 : Integer.valueOf(last);
        int startPage = StringUtils.isBlank(page) ? 1 : Integer.valueOf(page);
        startPage = startPage < 1 ? 1 : startPage;

        topn = topn > 1000 ? 1000 : topn;
        lastmin = lastmin > RequestCounter.maxMinutes ? RequestCounter.maxMinutes : lastmin;

        req.setAttribute("pstr", StringUtils.isBlank(pstr) ? "" : pstr);
        String alias = loadData(ls, match, now, StringUtils.split(pstr, "->"));
        req.setAttribute("alias", alias == null ? "" : alias);
        req.setAttribute("data", JSON.toJSONString(getPage(ls, startPage, topn, lastmin)));
        req.setAttribute("timeLine", JSON.toJSONString(getTimeLine(now)));
        req.setAttribute("stop", MonitorHandler.stop);

        Map pi = new HashMap();
        pi.put("page", startPage);
        pi.put("size", topn);
        pi.put("last", lastmin);
        pi.put("match", match);
        pi.put("pstr", pstr);
        req.setAttribute("pageinfo", JSON.toJSONString(pi));

        req.getRequestDispatcher("/view.jsp").forward(req, resp);

    }

    public static String loadData(List ls, String match, long now, String... parents) {

        long minuteNow = now / (RequestCounter.maxMinutes * 1000);
        MonitNode mn = RequestCounter.monitRoot;

        if (parents != null && parents.length > 0) {
            for (String parent : parents) {
                if (mn.getChildren() != null) {
                    mn = mn.getChildren().asMap().get(parent);
                }
            }
        }

        if (mn.getChildren() != null) {
            Map<String, MonitNode> map = mn.getChildren().asMap();
            for (Map.Entry<String, MonitNode> o : map.entrySet()) {

                if (StringUtils.isNotBlank(match) && !isMatch(o, match)) continue;

                ChartData cd = new ChartData();
                ls.add(cd);

                cd.setName(StringUtils.isBlank(o.getValue().getAlias()) ? o.getKey() : "(" + o.getValue().getAlias() + ")" + o.getKey());

                for (int i = RequestCounter.maxMinutes; i > 0; i--) {
                    LongAdder al = o.getValue().getHistory().get(minuteNow - i + 1);
                    cd.getData().add(al == null ? 0 : al.longValue());
                }
            }
        }

        return mn.getAlias();
    }

    public static List getPage(List<ChartData> ls, int page, int topn, final int lastmin) {
        Collections.sort(ls, new Comparator<ChartData>() {
            @Override
            public int compare(ChartData o1, ChartData o2) {

                long a = 0;
                long b = 0;
                for (int i = 1; i <= lastmin; i++) {

                    a += o1.getData().get(RequestCounter.maxMinutes - i);
                    b += o2.getData().get(RequestCounter.maxMinutes - i);
                }

                return a == b ? 0 : a > b ? -1 : 1;
            }
        });

        int start = (page - 1) * topn;
        int end = start + topn;
        end = end > ls.size() ? ls.size() : end;
        start = start > end ? end : start;
        return ls.subList(start, end);
    }

    private static boolean isMatch(Map.Entry<String, MonitNode> entry, String match) {

        String[] arr = StringUtils.split(match, "|");

        int i = 0;
        String matchAlias = arr[i].trim();
        if (match.trim().startsWith("^")) {
            if (!matchAlias.substring(1, matchAlias.length()).trim().equals(entry.getValue().getAlias())) return false;
            if (arr.length == 1) return true;
            i++;
        }

        for (; i < arr.length; i++) {
            String s = arr[i].trim();
            if (s.startsWith("*")) {
                if (entry.getKey().toString().toLowerCase().contains(s.substring(1, s.length()).toLowerCase().trim()))
                    return true;
            } else {
                if (entry.getKey().toString().toLowerCase().equals(s.toLowerCase().trim())) return true;
            }
        }
        return false;
    }

    private String[] getTimeLine(long minuteNow) {

        String[] arr = new String[RequestCounter.maxMinutes];

        Calendar cl = Calendar.getInstance();
        cl.setTimeInMillis(minuteNow);

        for (int i = RequestCounter.maxMinutes; i > 0; i--) {

            int h = cl.get(Calendar.HOUR_OF_DAY);
            int m = cl.get(Calendar.MINUTE);
            arr[i - 1] = (h < 10 ? "0" + h : h) + ":" + (m < 10 ? "0" + m : m);
            cl.add(Calendar.MINUTE, -1);
        }
        return arr;
    }
}
