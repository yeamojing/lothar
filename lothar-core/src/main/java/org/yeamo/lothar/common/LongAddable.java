package org.yeamo.lothar.common;

/**
 * 拷贝自com.google.common.cache.LongAddable
 *
 * @author: jingzhuo
 * @since: 2017/10/27
 */
public interface LongAddable {
    void increment();

    void add(long x);

    long sum();

}
