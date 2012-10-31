package se.kth.webtex;

import java.util.concurrent.Callable;

import se.kth.sys.util.ApplicationMonitor;
import se.kth.sys.util.ApplicationMonitor.Status;

public class CacheMonitor extends ApplicationMonitor implements
Callable<Status> {
    private static final int SECONDS_PER_HOUR = 3600;
    private static final int SECONDS_PER_MINUTE = 60;
    private static final int SECONDS_PER_DAY = SECONDS_PER_HOUR * 24;
    private Cache cache;

    public void setCache(Cache cache) {
        this.cache = cache;
    }

    @Override
    public Status call() throws Exception {
        return Status.OK(String.format(
	    "%d items in cache. Totals: %d additions, %d expirations, size: %d kb. Uptime: %s",
	    cache.size(),
	    cache.getAdditions(),
	    cache.getExpired(),
	    cache.getSize() / 1024,
	    uptime()));
    }
    
    private String uptime() {
        int uptimeInSeconds = (int) cache.getUptime() / 1000;
        
        String res = "";
        if (uptimeInSeconds / SECONDS_PER_DAY > 0) {
            res += String.format("%d days, ", uptimeInSeconds / SECONDS_PER_DAY); 
        }
        return res += String.format("%02d:%02d:%02d",
                    (uptimeInSeconds % SECONDS_PER_DAY) / SECONDS_PER_HOUR,
                    (uptimeInSeconds % SECONDS_PER_HOUR) / SECONDS_PER_MINUTE,
                    (uptimeInSeconds % SECONDS_PER_MINUTE));
    }
}
