package kaba.yucata.envoy.datalink;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.NotificationCompat;
import android.widget.Toast;

import kaba.yucata.envoy.GameCountActivity;
import kaba.yucata.envoy.PrefsHelper;
import kaba.yucata.envoy.R;

//import static kaba.yucata.envoy.GameCountActivity.STATES.STATE_ERROR;
//import static kaba.yucata.envoy.GameCountActivity.STATES.STATE_OK;
//import static kaba.yucata.envoy.PrefsHelper.PREF_KEY_GAMES_TOTAL;
//import static kaba.yucata.envoy.PrefsHelper.PREF_KEY_GAMES_WAITING;
//import static kaba.yucata.envoy.PrefsHelper.PREF_KEY_INVITES;
//import static kaba.yucata.envoy.PrefsHelper.PREF_KEY_TIME_LAST_LOAD;

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
        try {
            final ServerAbstraction server = new YucataServerAbstraction(context);
            return server.coldCallLoadInfo();
        } catch (Throwable e) {
            e.printStackTrace();
            return new StateInfo(e);
        }
    }
    @Override
    protected void onPostExecute(StateInfo info) {
        if( ! info.wasErronous() ) {
            final SharedPreferences.Editor editor = PrefsHelper.begin(sharedPrefs);
            PrefsHelper.setGamesWaiting( null, editor, info.getGamesWaiting() );
            PrefsHelper.setGamesTotal( null, editor, info.getGamesTotal() );
            PrefsHelper.setPersInvites( null, editor, info.getPersonalInvites() );
            PrefsHelper.setTimeLastLoad( null, editor,System.currentTimeMillis() );
            PrefsHelper.commit(editor);
//            sharedPrefs.edit()
//                    .putInt(PREF_KEY_GAMES_WAITING, info.getGamesWaiting())
//                    .putInt(PREF_KEY_GAMES_TOTAL, info.getGamesTotal())
//                    .putInt(PREF_KEY_INVITES, info.getPersonalInvites())
//                    .putLong(PREF_KEY_TIME_LAST_LOAD,System.currentTimeMillis())
//                    .apply();
        } else {  // was erronous
            PrefsHelper.interpretLoadError(sharedPrefs,info.getThrowable());
        }
    }
    // ... invalidateSession...

    public static class LTActivity extends LoaderTask {
        public LTActivity(GameCountActivity context, SharedPreferences sharedPrefs) {
            super(context,sharedPrefs);
        }
        @Override
        protected void onPostExecute(StateInfo info) {
            super.onPostExecute(info);
            if( info.wasErronous() ) {
                Toast.makeText(context, "LTA error obtaining session\n"+info.getThrowable(), Toast.LENGTH_LONG).show();
                PrefsHelper.interpretLoadError(sharedPrefs,info.getThrowable());
//                ((GameCountActivity)context).setState(STATE_ERROR);
            }
        }
    }

    public static class LTService extends LoaderTask {
        public final static int NOTIFICATION_ID=1502228361;
        private static final int PENDING_INTENT_ID = NOTIFICATION_ID;
        private final NotificationManager notificationMgr;
        private final PendingIntent pendingIntent;
        public LTService(Context context, SharedPreferences sharedPrefs) {
            super(context,sharedPrefs);
            notificationMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            final Intent intent = new Intent(context, GameCountActivity.class);
            pendingIntent = PendingIntent.getActivity(context, PENDING_INTENT_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        @Override
        protected void onPostExecute(StateInfo info) {
            super.onPostExecute(info);  // stores values to prefs
            if( sharedPrefs.getBoolean(context.getText( R.string.k_pref_notifications ).toString(),true)) {
                if (!info.wasErronous())  // FIXME: show different notification on error
                    popupNotification( PrefsHelper.getGamesWaiting(sharedPrefs,0), PrefsHelper.getGamesTotal(sharedPrefs,0) );
//                    popupNotification(sharedPrefs.getInt(PREF_KEY_GAMES_WAITING, 0), sharedPrefs.getInt(PREF_KEY_GAMES_TOTAL, 0));
            } else
                notificationMgr.cancel(NOTIFICATION_ID);
        }
        private void popupNotification(int waiting, int total) {
            if(waiting<1) {
                notificationMgr.cancel(NOTIFICATION_ID);
                System.out.println("notification canceled");
                return;
            }
            final NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
            builder.setSmallIcon(R.drawable.ic_stat_notify)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent);
            if(waiting==1)
                builder.setContentTitle( context.getString(R.string.noti_waiting_sin,waiting) );
            else
                builder.setContentTitle( context.getString(R.string.noti_waiting_plu,waiting) );
            if(total==1)
                builder.setContentText( context.getString(R.string.noti_total_sin,total) );
            else
                builder.setContentText( context.getString(R.string.noti_total_plu,total) );
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                builder.setCategory(Notification.CATEGORY_SOCIAL);
            }
            final Notification noti = builder.build();
            notificationMgr.notify( NOTIFICATION_ID, noti );
            System.out.println("notification posted for "+waiting+","+total);
        }
    }
}
