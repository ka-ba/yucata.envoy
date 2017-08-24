package kaba.yucata.envoy.datalink;

import android.content.Context;

/**
 * Created by kaba on 22/08/17.
 */

class FirebaseDataService extends DataService {
    public FirebaseDataService(Context context, int interval) {
        super(context,interval);
    }

    @Override
    void resetTimer() {
        throw new RuntimeException("no implemented");
    }

    @Override
    boolean ensureRunning() {
        throw new RuntimeException("no implemented");
    }

    @Override
    boolean ensureStopped() {
        throw new RuntimeException("no implemented");
    }
}
