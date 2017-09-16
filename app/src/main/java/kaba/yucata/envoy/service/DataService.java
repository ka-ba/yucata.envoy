package kaba.yucata.envoy.service;

import android.content.Context;
import android.os.Build;

import static kaba.yucata.envoy.datalink.LoaderHelper.LOADER_ID;

/** starts the loader every now and then
 * Created by kaba on 12/08/17.
 */

public abstract class DataService {
    protected static final int JOB_ID= LOADER_ID;  // recycle number
    protected final Context context;
    /** in minutes! */
    protected int interval;

    public void setParamenters( int interval ) {
        this.interval=interval;
        resetTimer();
    }

    public DataService (Context context, int interval) {
        this.context=context;
        this.interval=interval;
    }
    public abstract boolean resetTimer();
    public abstract boolean ensureRunning();
    public abstract boolean ensureStopped();

    public static DataService getService(Context context, int interval) {
        System.out.println("+DS: getService");
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            return new LolliDataService(context,interval);
        else {
            try {
                return new FirebaseDataService(context,interval);
            } catch( Exception e ) {
                return new BasicDataService(context,interval);
            }
        }
    }
}
