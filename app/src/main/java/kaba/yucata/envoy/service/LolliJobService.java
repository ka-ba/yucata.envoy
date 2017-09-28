package kaba.yucata.envoy.service;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.preference.PreferenceManager;

import kaba.yucata.envoy.PrefsHelper;
import kaba.yucata.envoy.datalink.LoaderTask;
import kaba.yucata.envoy.util.DebugHelper;

/**
 * Created by kaba on 12/09/17.
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class LolliJobService extends JobService {

    public LolliJobService() {
        super();
        System.out.println("+LJS: default constructor");
    }

//    private LolliJobService(LolliDataService ds) {
//        super();
//        dataService = ds;
//        System.out.println("+LJS: ds constructor");
//    }

    @Override
    public boolean onStartJob(JobParameters jobParameters) {  // TODO: generalize all JobServices
        System.out.println("+LJS: onStartJob");
        final SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        try {
            if (sharedPrefs == null) {
                System.out.println("+LDS: no shared prefs");
                return false;
            }
            if (PrefsHelper.canReload(sharedPrefs))
                new LoaderTask.LTService(this, sharedPrefs).execute(this);
            sharedPrefs.edit().remove(PrefsHelper.PREF_KEY_LAST_SERVICE_ERROR).apply();
        } catch(Throwable t) {
            PrefsHelper.setServiceError(sharedPrefs, null, DebugHelper.allToString(t));
        }
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        System.out.println("+LJS: onStopJob");
        return false;
    }
}
