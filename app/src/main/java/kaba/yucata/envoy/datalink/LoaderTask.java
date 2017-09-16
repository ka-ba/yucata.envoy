package kaba.yucata.envoy.datalink;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.NotificationCompat;
import android.widget.Toast;

import kaba.yucata.envoy.ConfigurationException;
import kaba.yucata.envoy.GameCountActivity;
import kaba.yucata.envoy.R;

import static kaba.yucata.envoy.GameCountActivity.STATES.STATE_ERROR;
import static kaba.yucata.envoy.GameCountActivity.STATES.STATE_OK;
import static kaba.yucata.envoy.PrefsHelper.PREF_KEY_GAMES_TOTAL;
import static kaba.yucata.envoy.PrefsHelper.PREF_KEY_GAMES_WAITING;
import static kaba.yucata.envoy.PrefsHelper.PREF_KEY_INVITES;

/**
 * Created by kaba on 13/09/17.
 */

public abstract class LoaderTask extends AsyncTask<Context,Void,StateInfo> {
    public final static int NOTIFICATION_ID=1502228361;
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
            if( ! info.wasErronous() )  // FIXME: show different notification on error
                popupNotification( sharedPrefs.getInt(PREF_KEY_GAMES_WAITING,0),sharedPrefs.getInt(PREF_KEY_GAMES_TOTAL,0) );
        }
        private void popupNotification(int waiting, int total) {
            final NotificationManager notiService = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if(waiting<1) {
                notiService.cancel(NOTIFICATION_ID);
                System.out.println("notification canceled");
                return;
            }
            final Notification noti = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.drawable.ic_stat_notify)
                    .setContentTitle( ""+waiting+(waiting==1?" game":" games")+" waiting for your move" )
                    .setContentText("out of "+total+" games in total")
                    .setCategory(Notification.CATEGORY_SOCIAL)
                    .setAutoCancel(true)
                    .build();
            notiService.notify( NOTIFICATION_ID, noti );
            System.out.println("notification posted for "+waiting+","+total);
        }
    }
}
