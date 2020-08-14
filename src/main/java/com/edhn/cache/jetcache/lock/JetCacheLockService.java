package com.edhn.cache.jetcache.lock;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import com.alicp.jetcache.AutoReleaseLock;
import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.CreateCache;
import com.edhn.cache.jetcache.lock.exception.CacheLockTimeoutException;

/**
 * JetCacheLockService
 * 
 * @author fengyq
 * @version 1.0
 * @date 2019-07-26
 * 
 */
public class JetCacheLockService {

    private static final int SLEEP_INTERVAL = 100;
    
    @CreateCache(name = "lockCache.", cacheType = CacheType.REMOTE)
    private Cache<String, String> lockCache;
    
    /**
     * 尝试获取锁并执行内容
     * @param <U>
     * @param lockKey  加锁的key
     * @param expireMillis 超时时间，单位毫秒，如果获取到锁是锁超时时间，如果获取不到锁是阻塞等待时间
     * @param supplier supplier
     * @return result of supplier
     * @throws CacheLockTimeoutException 异常
     */
    public <U> U retryLockAndSupply(String lockKey, long expireMillis, Supplier<U> supplier) throws CacheLockTimeoutException {
        return retryLockAndSupply(lockKey, expireMillis, expireMillis, supplier);
    }

    
    /**
     * 尝试获取锁并执行内容
     * @param <U>
     * @param lockKey  加锁的key
     * @param waitExpireMillis 加锁成功之前的阻塞等待时间，单位毫秒
     * @param lockExpireMillis 加锁成功后的给锁设置的锁超时时间，单位毫秒
     * @param supplier supplier
     * @return result of supplier
     * @throws CacheLockTimeoutException 异常
     */
    public <U> U retryLockAndSupply(String lockKey, long waitExpireMillis, long lockExpireMillis,
            Supplier<U> supplier) throws CacheLockTimeoutException {
        /*
         * jetcache的tryLock获取锁的操作是非阻塞的，如果对应的key还没有锁，返回一个AutoReleaseLock； 否则立即返回空
         * 本方法变为阻塞锁，确保等待waitExpireMillis的时间
         */
        long waitExpireTime = System.currentTimeMillis() + waitExpireMillis;
        while (System.currentTimeMillis() < waitExpireTime) {
            try (AutoReleaseLock lock = lockCache.tryLock(lockKey, lockExpireMillis, TimeUnit.MILLISECONDS)) {
                if (lock != null) {
                    return supplier.get();
                }
                try {
                    // 锁占用期间的等待间歇
                    long sleepTime = Math.min(SLEEP_INTERVAL, waitExpireTime - System.currentTimeMillis());
                    if (sleepTime > 0) {
                        TimeUnit.MILLISECONDS.sleep(sleepTime);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        throw new CacheLockTimeoutException("lock wait reached expire time!");
    }

}
