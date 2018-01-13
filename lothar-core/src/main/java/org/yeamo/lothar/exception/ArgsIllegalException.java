package org.yeamo.lothar.exception;

/**
 * 参数不合法
 *
 * @author: jingzhuo
 * @since: 2017/10/17
 */
public class ArgsIllegalException extends RuntimeException{

    public ArgsIllegalException() {
        super();
    }
    public ArgsIllegalException(String message) {
        super(message);
    }
    public ArgsIllegalException(String message, Throwable cause) {
        super(message, cause);
    }
    public ArgsIllegalException(Throwable cause) {
        super(cause);
    }
}
