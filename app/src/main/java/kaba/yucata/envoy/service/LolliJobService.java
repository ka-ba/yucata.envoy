package kaba.yucata.envoy.service;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.preference.PreferenceManager;

import kaba.yucata.envoy.ConfigurationException;
import kaba.yucata.envoy.datalink.LoaderHelper;
import kaba.yucata.envoy.datalink.LoaderTask;
import kaba.yucata.envoy.datalink.YucataServerAbstraction;

import static kaba.yucata.envoy.PrefsHelper.PREF_KEY_GAMES_TOTAL;
import static kaba.yucata.envoy.PrefsHelper.PREF_KEY_INVITES;
import static kaba.yucata.envoy.PrefsHelper.PREF_KEY_SESSION_ID;
import static kaba.yucata.envoy.PrefsHelper.PREF_KEY_USERNAME;

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
//        System.out.println("+LJS: username=" + sharedPrefs.getString(PREF_KEY_USERNAME, "<none set>"));
//        System.out.println("+LJS: session=" + sharedPrefs.getString(PREF_KEY_SESSION_ID, "<none set>"));
//        System.out.println("+LJS: games total=" + sharedPrefs.getInt(PREF_KEY_GAMES_TOTAL, -1));
//        final int inv = sharedPrefs.getInt(PREF_KEY_INVITES, 1);
//        sharedPrefs.edit().putInt(PREF_KEY_INVITES,inv+1).apply();
//        System.out.println("+LJS: incremented invites");
        new LoaderTask.LTService(this,sharedPrefs).execute(this);

// throws NetworkOnMainThread:
//        final YucataServerAbstraction server = new YucataServerAbstraction(this);
//        try {
//            server.coldCallLoadInfo();
//        } catch (Throwable e) {
//            e.printStackTrace();
//        }
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        System.out.println("+LJS: onStopJob");
        return false;
    }
}
