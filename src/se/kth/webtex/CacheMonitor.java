package se.kth.webtex;

import java.util.concurrent.Callable;

import se.kth.sys.util.ApplicationMonitor;
import se.kth.sys.util.ApplicationMonitor.Status;

public class CacheMonitor extends ApplicationMonitor implements
Callable<Status> {
    private Cache cache;

    public void setCache(Cache cache) {
        this.cache = cache;
    }

    @Override
    public Status call() throws Exception {
        return Status.OK(cache.size() + " items in cache. Totals: " 
                + cache.getAdditions() + " additions, " 
                + cache.getExpired() + " expirations.");
    }
}
