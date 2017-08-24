package kaba.yucata.envoy.datalink;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Loader;
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
    private final LolliJobService jobService;
    private static final long flexMillis=60*1000;

    protected LolliDataService(Context context, int iterval) {
        super(context,iterval);
        scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobService=new LolliJobService(this);
    }

    @Override
    void resetTimer() {

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    boolean ensureRunning() {
        final List<JobInfo> allPendingJobs = scheduler.getAllPendingJobs();
        if(allPendingJobs!=null) {
            for( JobInfo pending : allPendingJobs ) {
                if(pending.getId()==JOB_ID)
                    return true;
            }
        }
//        if( null != scheduler.getPendingJob(JOB_ID)) API M?
//            return  true;
        JobInfo job =  new JobInfo.Builder(
                JOB_ID,new ComponentName(context,jobService.getClass()))
                // FIXME: following into config
        .setPeriodic(interval*1000)
                .setPersisted(false)
                .setRequiredNetworkType(NETWORK_TYPE_ANY)
//                .setRequiresBatteryNotLow(true)  API O
                .build();
        return scheduler.schedule(job) == RESULT_SUCCESS;
    }

    @Override
    boolean ensureStopped() {
        scheduler.cancel(JOB_ID);
        return true;
    }

    private class LolliJobService extends JobService {

        private final LolliDataService dataService;

        private LolliJobService(LolliDataService ds) {
            dataService=ds;
        }
        @Override
        public boolean onStartJob(JobParameters jobParameters) {
            return false;
        }

        @Override
        public boolean onStopJob(JobParameters jobParameters) {
            return false;
        }
    }
}
