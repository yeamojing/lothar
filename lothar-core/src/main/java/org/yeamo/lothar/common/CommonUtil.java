package org.yeamo.lothar.common;

import org.yeamo.lothar.exception.ArgsIllegalException;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

/**
 * 通用工具包
 *
 * @author: jingzhuo
 * @since: 2017/11/6
 */
public class CommonUtil {


    public static String map2Str(Map<String, String> map) {

        if (map == null) return null;
        if (map.size() == 0) return "";

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            sb.append(entry.getKey());
            sb.append(":");
            sb.append(entry.getValue());
            sb.append(",");
        }
        return sb.substring(0, sb.length() - 1);
    }


    public static void str2Map(String str, Map map) throws ArgsIllegalException {

        try {
            if (StringUtils.isNotBlank(str)) {
                String[] kvArr = StringUtils.split(str, ",");
                for (String kvstr : kvArr) {
                    String[] kv = StringUtils.split(kvstr, ":");
                    map.put(kv[0], kv.length == 1 ? "" : kv[1]);
                }
            }
        } catch (Exception e) {
            throw new ArgsIllegalException("The str format should be like this: {k1:v1,k2:v2}");
        }
    }
}
