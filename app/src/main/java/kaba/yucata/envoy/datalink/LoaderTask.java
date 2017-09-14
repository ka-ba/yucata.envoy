package kaba.yucata.envoy.datalink;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.widget.Toast;

import kaba.yucata.envoy.ConfigurationException;
import kaba.yucata.envoy.GameCountActivity;

import static kaba.yucata.envoy.GameCountActivity.STATES.STATE_ERROR;
import static kaba.yucata.envoy.GameCountActivity.STATES.STATE_OK;
import static kaba.yucata.envoy.PrefsHelper.PREF_KEY_GAMES_TOTAL;
import static kaba.yucata.envoy.PrefsHelper.PREF_KEY_GAMES_WAITING;
import static kaba.yucata.envoy.PrefsHelper.PREF_KEY_INVITES;

/**
 * Created by kaba on 13/09/17.
 */

public abstract class LoaderTask extends AsyncTask<Context,Void,StateInfo> {
    public final Context context;
    public final SharedPreferences sharedPrefs;

    protected LoaderTask(Context context,SharedPreferences sharedPrefs) {
        super();
        this.context=context;
        this.sharedPrefs=sharedPrefs;
    }
    @Override
    protected StateInfo doInBackground(Context... contextA) {
        final Context context = contextA[0];
        final ServerAbstraction server = new YucataServerAbstraction(context);
        StateInfo info=null;
        try {
            info = server.coldCallLoadInfo();
            return info;
        } catch (ConfigurationException e) {
            e.printStackTrace();
            return new StateInfo(e.toString());
        }
    }
    @Override
    protected void onPostExecute(StateInfo info) {
        if( ! info.wasErronous() ) {
            sharedPrefs.edit()
                    .putInt(PREF_KEY_GAMES_WAITING, info.getGamesWaiting())
                    .putInt(PREF_KEY_GAMES_TOTAL, info.getGamesTotal())
                    .putInt(PREF_KEY_INVITES, info.getPersonalInvites())
                    .apply();
        }
    }
    // ... onPostExecute...
    // ... invalidateSession...

    public static class LTActivity extends LoaderTask {
        public LTActivity(GameCountActivity context, SharedPreferences sharedPrefs) {
            super(context,sharedPrefs);
        }
        @Override
        protected void onPostExecute(StateInfo info) {
            super.onPostExecute(info);
            if( ! info.wasErronous() ) {
                ((GameCountActivity)context).setState(STATE_OK);
            } else {
                Toast.makeText(context, "LTA error obtaining session\n"+info.getErrorMessage(), Toast.LENGTH_LONG).show();
                ((GameCountActivity)context).setState(STATE_ERROR);
            }
        }
    }

    public static class LTService extends LoaderTask {
        public LTService(Context context,SharedPreferences sharedPrefs) {
            super(context,sharedPrefs);
        }
        @Override
        protected void onPostExecute(StateInfo info) {
            super.onPostExecute(info);
        }
    }
}
