package kaba.yucata.envoy.service;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;

import java.util.List;

import static android.app.job.JobInfo.NETWORK_TYPE_ANY;
import static android.app.job.JobScheduler.RESULT_SUCCESS;

/**
 * Created by kaba on 12/08/17.
 */

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class LolliDataService extends DataService {
    private final JobScheduler scheduler;
    private static final long flexMillis=60*1000;

    protected LolliDataService(Context context, int iterval) {
        super(context,iterval);
        System.out.println("+LDS: constuctor");
        scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
    }

    @Override
    public boolean resetTimer() {
        System.out.println("will change scheduling interval to "+interval+" min");
        scheduler.cancel(JOB_ID);
        return schedule();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean ensureRunning() {
        System.out.println("+LDS: ensure running");
        final long running = getRunningInterval();
        if(running<1)  // not running
            return schedule();
        if( running != interval*60000 ) {
            System.out.println("will change scheduling interval from " + running / 60000 + " min to " + interval+ " min");
            return resetTimer();
        }
        return true;
    }

    @Override
    public boolean isRunning() {
        return ( getRunningInterval() > 0 );
    }

    private long getRunningInterval() {
        final List<JobInfo> allPendingJobs = scheduler.getAllPendingJobs();
        if(allPendingJobs!=null) {
            for( JobInfo pending : allPendingJobs ) {
                if(pending.getId()==JOB_ID)
                    return pending.getIntervalMillis();
            }
        }
        return 0;  // not running
    }

    private boolean schedule() {
        JobInfo job = new JobInfo.Builder(
                JOB_ID, new ComponentName(context, LolliJobService.class))
                .setPeriodic(interval * 60000)
                .setPersisted(false)
                .setRequiredNetworkType(NETWORK_TYPE_ANY)
//                .setRequiresBatteryNotLow(true)  API O
                .build();
        System.out.println("+LDS: sheduling job");
        return scheduler.schedule(job) == RESULT_SUCCESS;
    }

    @Override
    public boolean ensureStopped() {
        scheduler.cancel(JOB_ID);
        System.out.println("+LDS: canceled job");
        return true;
    }
}
