package se.kth.webtex;

import java.util.concurrent.Callable;

import se.kth.sys.util.ApplicationMonitor;
import se.kth.sys.util.ApplicationMonitor.Status;

public class CacheMonitor extends ApplicationMonitor implements
        Callable<Status> {

    @Override
    public Status call() throws Exception {
        // TODO Auto-generated method stub
        return Status.OK("I'm alive");
    }

}
