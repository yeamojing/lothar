package org.yeamo.lothar.view;

import java.util.ArrayList;
import java.util.List;

/**
 * 请求历史数据
 *
 * @author: jingzhuo
 * @since: 2017/10/14
 */
public class ChartData {
    private String name;
    private List<Long> data = new ArrayList<Long>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Long> getData() {
        return data;
    }

    public void setData(List<Long> data) {
        this.data = data;
    }
}
