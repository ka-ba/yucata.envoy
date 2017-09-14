package kaba.yucata.envoy.service;

import android.content.Context;

/**
 * Created by kaba on 22/08/17.
 */

class BasicDataService extends DataService {
    public BasicDataService(Context context, int interval) {
        super(context,interval);
        System.out.println("+BDS: constuctor");
    }

    @Override
    public boolean resetTimer() {
        throw new RuntimeException("no implemented");
    }

    @Override
    public boolean ensureRunning() {
        throw new RuntimeException("no implemented");
    }

    @Override
    public boolean ensureStopped() {
        throw new RuntimeException("no implemented");
    }
}
