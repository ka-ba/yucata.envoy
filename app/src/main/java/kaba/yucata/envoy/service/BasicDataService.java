package kaba.yucata.envoy.service;

import android.content.Context;

/**
 * Created by kaba on 22/08/17.
 */

class BasicDataService extends DataService {
    public BasicDataService(Context context, int interval) {
        super(context,interval);
        System.out.println("+BDS: constuctor - Exception");
        throw new RuntimeException("not implemented");
    }

    @Override
    public boolean resetTimer() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public boolean ensureRunning() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public boolean ensureStopped() {
        throw new RuntimeException("not implemented");
    }
}
