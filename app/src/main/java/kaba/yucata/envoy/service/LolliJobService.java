package kaba.yucata.envoy.service;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.preference.PreferenceManager;

import kaba.yucata.envoy.datalink.LoaderTask;

/**
 * Created by kaba on 12/09/17.
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class LolliJobService extends JobService {

    private final LolliDataService dataService;

    public LolliJobService() {
        super();
        dataService = null;
        System.out.println("+LJS: default constructor");
    }

    private LolliJobService(LolliDataService ds) {
        super();
        dataService = ds;
        System.out.println("+LJS: ds constructor");
    }

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        System.out.println("+LJS: onStartJob");
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPrefs == null) {
            System.out.println("+LDS: no shared prefs");
            return false;
        }
        new LoaderTask.LTService(this,sharedPrefs).execute(this);
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        System.out.println("+LJS: onStopJob");
        return false;
    }
}
