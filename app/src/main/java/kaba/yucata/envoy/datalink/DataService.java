package kaba.yucata.envoy.datalink;

import android.content.Context;
import android.opengl.EGLExt;
import android.os.Build;

/** starts the loader every now and then
 * Created by kaba on 12/08/17.
 */

public abstract class DataService {
    protected static final int JOB_ID=LoaderHelper.LOADER_ID;  // recycle number
    protected final Context context;
    protected int interval;

    public void setParamenters( int interval ) {
        this.interval=interval;
        // FIXME: reschedule?
    }

    public DataService (Context context, int interval) {
        this.context=context;
        setParamenters(interval);
    }
    abstract void resetTimer();
    abstract boolean ensureRunning();
    abstract boolean ensureStopped();

    public static DataService getService(Context context, int interval) {
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
