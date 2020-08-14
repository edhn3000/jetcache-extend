package com.edhn.cache.jetcache.lock.exception;

import java.io.IOException;

/**
 * CacheLockTimeoutException
 * 
 * @author fengyq
 * @version 1.0
 * @date 2019-07-26
 * 
 */
public class CacheLockTimeoutException extends IOException {

    /**
     * 
     */
    private static final long serialVersionUID = -2467046351675721335L;
    
    /**
     * @param message
     */
    public CacheLockTimeoutException(String message) {
        super(message);
    }

}
