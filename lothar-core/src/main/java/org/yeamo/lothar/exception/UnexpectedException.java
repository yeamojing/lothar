package org.yeamo.lothar.exception;

/**
 * 意料之外的异常
 *
 * @author: jingzhuo
 * @since: 2017/10/17
 */
public class UnexpectedException extends RuntimeException{

    public UnexpectedException() {
        super();
    }
    public UnexpectedException(String message) {
        super(message);
    }
    public UnexpectedException(String message, Throwable cause) {
        super(message, cause);
    }
    public UnexpectedException(Throwable cause) {
        super(cause);
    }
}
