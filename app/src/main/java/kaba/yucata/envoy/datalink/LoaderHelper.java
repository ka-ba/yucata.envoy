package kaba.yucata.envoy.datalink;

import android.content.SharedPreferences;
import android.support.v4.app.LoaderManager;
//import android.app.LoaderManager;
//import android.content.AsyncTaskLoader;
import android.content.Context;
//import android.content.Loader;
import android.icu.text.ScientificNumberFormatter;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.preference.PreferenceManager;
import android.text.TextUtils;

import java.util.Random;

import kaba.yucata.envoy.GameCountActivity;
import kaba.yucata.envoy.StateInfo;

import static kaba.yucata.envoy.GameCountActivity.PREF_KEY_GAMES_TOTAL;
import static kaba.yucata.envoy.GameCountActivity.PREF_KEY_GAMES_WAITING;

/**
 * Created by kaba on 08/08/17.
 */

public class LoaderHelper implements LoaderManager.LoaderCallbacks<StateInfo> {
    protected final static int LOADER_ID=1502228361;
    private static final String USERNAME_KEY = "KEY-Username";
    private static final long graceMillis = 60000;
    private final Context context;
    private SharedPreferences sharedPrefs;
    private long lastInvoked;

    public LoaderHelper(Context context) {
        this.context=context;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void loadInfoFromServer(LoaderManager loaderManager, String username) {
        lastInvoked = System.currentTimeMillis();
        Bundle bundle=new Bundle();
        bundle.putString(USERNAME_KEY,username);
        final Loader<StateInfo> loader = loaderManager.getLoader(LOADER_ID);
        if(loader==null)
            loaderManager.initLoader(LOADER_ID,bundle,this);
        else
            loaderManager.restartLoader(LOADER_ID,bundle,this);
        // FIXM E: else restart??
    }

    public void loadInfoFromServerGrace(LoaderManager loaderManager, String username) {
        if(System.currentTimeMillis()-lastInvoked > graceMillis )
            loadInfoFromServer(loaderManager,username);
    }

    @Override
    public Loader<StateInfo> onCreateLoader(int i, final Bundle bundle) {
        return new AsyncTaskLoader<StateInfo>(context) {
            @Override
            protected void onStartLoading() {
                super.onStartLoading();
                forceLoad();
            }

            @Override
            protected void onStopLoading() {
                super.onStopLoading();
            }

            @Override
            public StateInfo loadInBackground() {
                final String username = bundle.getString(USERNAME_KEY);
                if( username==null || TextUtils.isEmpty(username) )
                    return null;
                StateInfo info;
                // fetch from network here
                // FIXME: dummy code
                final Random random = new Random(System.currentTimeMillis());
                final int total = random.nextInt(25);
                info = new StateInfo(total,random.nextInt(total+1));
                return info;
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<StateInfo> loader, StateInfo stateInfo) {
        // use loaded info here
        final SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putInt(PREF_KEY_GAMES_WAITING,stateInfo.getGamesWaiting());
        editor.putInt(PREF_KEY_GAMES_TOTAL,stateInfo.getGamesTotal());
        editor.apply();
    }

    @Override
    public void onLoaderReset(Loader<StateInfo> loader) {

    }
}
