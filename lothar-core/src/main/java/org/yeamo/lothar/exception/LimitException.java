package org.yeamo.lothar.exception;

/**
 * 请求被限制
 *
 * @author: jingzhuo
 * @since: 2017/10/17
 */
public class LimitException extends RuntimeException{

    public LimitException() {
        super();
    }
    public LimitException(String message) {
        super(message);
    }
    public LimitException(String message, Throwable cause) {
        super(message, cause);
    }
    public LimitException(Throwable cause) {
        super(cause);
    }
}
