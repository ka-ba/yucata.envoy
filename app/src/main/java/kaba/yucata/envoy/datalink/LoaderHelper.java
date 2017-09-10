package kaba.yucata.envoy.datalink;

import android.content.SharedPreferences;
import android.support.v4.app.LoaderManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.widget.Toast;

import kaba.yucata.envoy.ConfigurationException;
import kaba.yucata.envoy.GameCountActivity;

import static kaba.yucata.envoy.PrefsHelper.PREF_KEY_GAMES_TOTAL;
import static kaba.yucata.envoy.PrefsHelper.PREF_KEY_GAMES_WAITING;
import static kaba.yucata.envoy.PrefsHelper.PREF_KEY_INVITES;
import static kaba.yucata.envoy.GameCountActivity.STATES.STATE_ERROR;
import static kaba.yucata.envoy.GameCountActivity.STATES.STATE_OK;

/**
 * Created by kaba on 08/08/17.
 */

public class LoaderHelper implements LoaderManager.LoaderCallbacks<StateInfo> {
    protected final static int LOADER_ID=1502228361;
    private static final long graceMillis = 60000;
    private final Context context;
    private SharedPreferences sharedPrefs;
    private final ServerAbstraction server;
    private ServerAbstraction.SessionAbstraction session=null;
    private long lastInvoked;

    public LoaderHelper(Context context)
            throws Error {
        this.context=context;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
//        server = new DummyServerAbstraction(context);
        server = new YucataServerAbstraction(context);
//        renewSession();
    }

    public void loadInfoFromServer(LoaderManager loaderManager) {
        lastInvoked = System.currentTimeMillis();
        Bundle bundle=new Bundle();
        final Loader<StateInfo> loader = loaderManager.getLoader(LOADER_ID);
        if(loader==null)
            loaderManager.initLoader(LOADER_ID,bundle,this);
        else
            loaderManager.restartLoader(LOADER_ID,bundle,this);
    }

    public void loadInfoFromServerGrace(LoaderManager loaderManager, String username) {
        if(System.currentTimeMillis()-lastInvoked > graceMillis )
            loadInfoFromServer(loaderManager/*,username*/);
    }

    public void renewSession() throws ConfigurationException {
        try {
            session = server.recoverSession();
        } catch( CommunicationException.NoSessionException e ) {
            session = server.requestSession();
        }
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
                // fetch from network here
                try {
                    final StateInfo info = server.coldCallLoadInfo(session);
                    if(info.hasSession())
                        session=info.getSession();
                    return info;
                } catch (Exception e) {
                    e.printStackTrace();
                    return new StateInfo(e.getMessage());
                }
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<StateInfo> loader, StateInfo stateInfo) {
        // use loaded info here
        if(stateInfo==null)
            return;
        if( ! stateInfo.wasErronous() ) {
            final SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putInt(PREF_KEY_GAMES_WAITING, stateInfo.getGamesWaiting());
            editor.putInt(PREF_KEY_GAMES_TOTAL, stateInfo.getGamesTotal());
            editor.putInt(PREF_KEY_INVITES, stateInfo.getPersonalInvites());
            editor.apply();
            if( context instanceof GameCountActivity)
                ((GameCountActivity)context).setState(STATE_OK);
        } else {  // there was an error
            // FIXME: react on error type
            if( context instanceof AppCompatActivity)
                Toast.makeText(context, stateInfo.getErrorMessage(), Toast.LENGTH_LONG).show();
            if( context instanceof GameCountActivity)
                ((GameCountActivity)context).setState(STATE_ERROR);
        }
    }

    @Override
    public void onLoaderReset(Loader<StateInfo> loader) {
        // FIXME: anything to do here?
    }
}
