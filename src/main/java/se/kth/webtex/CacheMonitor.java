package se.kth.webtex;

/*
  Copyright (C) 2012 KTH, Kungliga tekniska hogskolan, http://www.kth.se

  This file is part of WebTex.

  WebTex is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  WebTex is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with WebTex.  If not, see <http://www.gnu.org/licenses/>
 */

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
                cache.getDiskSize() / 1024,
                uptime()));
    }

    private String uptime() {
        long uptimeInSeconds = cache.getUptime() / 1000;

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
