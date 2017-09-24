package kaba.yucata.envoy;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import kaba.yucata.envoy.datalink.CommunicationException;

/**
 * Created by kaba on 08/09/17.
 */

public class PrefsHelper {
    public static final String PREF_KEY_USERNAME = "username";
    public static final String PREF_KEY_USER_ID = "userid";
    public static final String PREF_KEY_SECRET = "secret";
    public static final String PREF_KEY_INTERVAL_MIN = "interval_minutes";
    public static final String PREF_KEY_GAMES_WAITING = "games_waiting";
    public static final String PREF_KEY_GAMES_TOTAL = "games_total";
    public static final String PREF_KEY_INVITES = "pers_invites";
    /** @deprecated for dummy server use only */
    public static final String PREF_KEY_TOKEN_BASE64 = "token_b64";
    public static final String PREF_KEY_YUCATA_TOKEN = "token";
    public static final String PREF_KEY_SESSION_ID = "session";
    public static final String PREF_KEY_LAST_RESPONSE = "last_response_code";
    public static final String PREF_KEY_TIME_LAST_LOAD = "last_load_time_stamp";
    private static final String PREF_KEY_STATE_NETWORK_OK = "state_network_ok";

    public static final String PREF_KEY_STATE_USERNAME = "state_username";
    private static final int PREF_VALUE_S_UN_UNKNOWN  = 11;
    private static final int PREF_VALUE_S_UN_CHANGED  = 12;
    private static final int PREF_VALUE_S_UN_FAILED   = 13;
    private static final int PREF_VALUE_S_UN_ACCEPTED = 14;
    private static final int PREF_VALUE_S_UN_REJECTED = 15;
    public static final String PREF_KEY_STATE_PASSWORD = "state_password";
    private static final int PREF_VALUE_S_PW_UNKNOWN  = 21;
    private static final int PREF_VALUE_S_PW_CHANGED  = 22;
    private static final int PREF_VALUE_S_PW_FAILED   = 23;
    private static final int PREF_VALUE_S_PW_ACCEPTED = 24;
    private static final int PREF_VALUE_S_PW_REJECTED = 25;

    public static String[] PREF_KEYS = {
        PREF_KEY_USERNAME,
        PREF_KEY_USER_ID,
        PREF_KEY_SECRET,
        PREF_KEY_INTERVAL_MIN,
        PREF_KEY_GAMES_WAITING,
        PREF_KEY_GAMES_TOTAL,
        PREF_KEY_INVITES,
        PREF_KEY_TOKEN_BASE64,
        PREF_KEY_YUCATA_TOKEN,
        PREF_KEY_SESSION_ID,
        PREF_KEY_LAST_RESPONSE,
        PREF_KEY_TIME_LAST_LOAD,
        PREF_KEY_STATE_NETWORK_OK,
        PREF_KEY_STATE_USERNAME,
        PREF_KEY_STATE_PASSWORD
    };

    public static boolean DEBUG=true;

    public static void clearSessionPrefs(SharedPreferences sharedPrefs) {
        clearPrefs( sharedPrefs, PREF_KEY_SESSION_ID, PREF_KEY_YUCATA_TOKEN, PREF_KEY_TOKEN_BASE64 );
    }

    public static void clearInfoPrefs(SharedPreferences sharedPrefs) {
        clearPrefs( sharedPrefs, PREF_KEY_GAMES_WAITING, PREF_KEY_GAMES_TOTAL, PREF_KEY_INVITES);
    }

    /** @return true, if session prefs cleared, false else
     */
    public static boolean clearPrefsBecausePrefChanged(SharedPreferences sharedPrefs, String key_updated) {
        if( PREF_KEY_USERNAME.equals(key_updated) ) {
            clearPrefs(sharedPrefs, PREF_KEY_USER_ID);
            clearInfoPrefs(sharedPrefs);
            clearSessionPrefs(sharedPrefs);
            return true;
        } else if( PREF_KEY_SECRET.equals(key_updated) ) {
            clearInfoPrefs(sharedPrefs);
            clearSessionPrefs(sharedPrefs);
            return true;
        } else if( PREF_KEY_SESSION_ID.equals(key_updated) ) {
            clearInfoPrefs(sharedPrefs);
        }
        return false;
    }

    static void clearPrefs(SharedPreferences sharedPrefs, String... keys) {
        final SharedPreferences.Editor editor = sharedPrefs.edit();
        for( int i=0; i<keys.length; i++ )
            editor.remove( keys[i] );
        editor.apply();
    }

    public static void setStrings(SharedPreferences sharedPrefs, String... keys_values) {
        if( keys_values.length%2 == 1 )  // must always be even!
            throw new IllegalArgumentException("must always be called with even number of strings, always complete key-value-pairs");
        final SharedPreferences.Editor editor = sharedPrefs.edit();
        for( int i=0; i<keys_values.length; i+=2 )
            editor.putString( keys_values[i], keys_values[i+1] );
        editor.apply();
    }

    public static int stringPrefToInt(SharedPreferences sharedPrefs, String key, int def, Context context) {
        final String pref = sharedPrefs.getString(key, Integer.toString(def));
        try {
            return Integer.parseInt(pref);
        } catch(NumberFormatException e) {
            if(context!=null)
                Toast.makeText(context, String.format("cannot parse to int: %s (%s) using default: %d",pref,key,def),Toast.LENGTH_LONG).show();
        }
        return def;
    }

    public static void interpretLoadError(SharedPreferences sharedPrefs, Throwable throwable) {
        sharedPrefs.edit().putBoolean(PREF_KEY_STATE_NETWORK_OK,true).apply();
        if(throwable instanceof ConfigurationException) {
            // TODO: haeh?
        } else if(throwable instanceof CommunicationException.LoginFailedException) {
            final int state_username = sharedPrefs.getInt(PREF_KEY_STATE_USERNAME,PREF_VALUE_S_UN_UNKNOWN);
            switch(state_username) {
                case PREF_VALUE_S_UN_UNKNOWN:
                case PREF_VALUE_S_UN_CHANGED:
                    sharedPrefs.edit()
                            .putInt(PREF_KEY_STATE_USERNAME,PREF_VALUE_S_UN_FAILED)
                            .putInt(PREF_KEY_STATE_PASSWORD,PREF_VALUE_S_PW_FAILED).apply();
                    return;
                case PREF_VALUE_S_UN_ACCEPTED:
                    final int state_password = sharedPrefs.getInt(PREF_KEY_STATE_PASSWORD,PREF_VALUE_S_PW_UNKNOWN);
                    switch (state_password) {
                        case PREF_VALUE_S_PW_UNKNOWN:
                        case PREF_VALUE_S_PW_CHANGED:
                            sharedPrefs.edit()
                                    .putInt(PREF_KEY_STATE_PASSWORD,PREF_VALUE_S_PW_REJECTED).apply();
                            return;
                        case PREF_VALUE_S_PW_ACCEPTED:
                            sharedPrefs.edit()
                                    .putInt(PREF_KEY_STATE_USERNAME,PREF_VALUE_S_UN_FAILED)
                                    .putInt(PREF_KEY_STATE_PASSWORD,PREF_VALUE_S_PW_FAILED).apply();
                            return;
                    }
            }
            return;
        } else if(throwable instanceof CommunicationException.IOException) {
            sharedPrefs.edit().putBoolean(PREF_KEY_STATE_NETWORK_OK,false).apply();
        }
        // FIXME: do someting about session exceptions?
        // TODO: implement correct reaction to internal error
    }

    public static void rememberLoginSuccess(SharedPreferences sharedPrefs) {
        sharedPrefs.edit()
                .putBoolean(PREF_KEY_STATE_NETWORK_OK,true)
                .putInt(PREF_KEY_STATE_USERNAME,PREF_VALUE_S_UN_ACCEPTED)
                .putInt(PREF_KEY_STATE_PASSWORD,PREF_VALUE_S_PW_ACCEPTED).apply();

    }

    public static String getCurrentStateText(Context context, SharedPreferences sharedPrefs) {
        if( isStringPrefNullEmpty(sharedPrefs,PREF_KEY_USERNAME)
                || isStringPrefNullEmpty(sharedPrefs,PREF_KEY_SECRET) )
            return context.getString(R.string.s_missing_login_info);
        if( PREF_VALUE_S_UN_FAILED == sharedPrefs.getInt(PREF_KEY_STATE_USERNAME,-1) )
            return context.getString(R.string.s_login_failed);
        if( PREF_VALUE_S_PW_REJECTED == sharedPrefs.getInt(PREF_KEY_STATE_PASSWORD,-1) )
            return context.getString(R.string.s_password_rejected);
        if( !(sharedPrefs.getBoolean(PREF_KEY_STATE_NETWORK_OK,true)) )
            return context.getString(R.string.s_network_failed);
        return null;
    }

    public static void prefChangeUsername(SharedPreferences sharedPrefs) {
        sharedPrefs.edit()
                .putInt( PREF_KEY_STATE_USERNAME, PREF_VALUE_S_UN_CHANGED )
                .putInt( PREF_KEY_STATE_PASSWORD, PREF_VALUE_S_PW_UNKNOWN ).apply();
        // session prefs handeled in clearPrefsBecausePrefChange
    }

    public static void prefChangePassword(SharedPreferences sharedPrefs) {
        final int state_username = sharedPrefs.getInt(PREF_KEY_STATE_USERNAME,PREF_VALUE_S_UN_UNKNOWN);
        final SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putInt( PREF_KEY_STATE_PASSWORD, PREF_VALUE_S_PW_CHANGED );
        if( (state_username==PREF_VALUE_S_UN_FAILED) || (state_username==PREF_VALUE_S_UN_REJECTED) )
            editor.putInt( PREF_KEY_STATE_USERNAME, PREF_VALUE_S_UN_UNKNOWN );
        editor.apply();
        // session prefs handeled in clearPrefsBecausePrefChange
    }

    public static boolean canReload(SharedPreferences sharedPrefs) {
        if( isStringPrefNullEmpty(sharedPrefs,PREF_KEY_USERNAME) )
            return false;
        if( isStringPrefNullEmpty(sharedPrefs,PREF_KEY_SECRET) )
            return false;
        switch( sharedPrefs.getInt(PREF_KEY_STATE_USERNAME,-1)) {
            case PREF_VALUE_S_UN_FAILED:
            case PREF_VALUE_S_UN_REJECTED:
                return false;
        }
        switch( sharedPrefs.getInt(PREF_KEY_STATE_PASSWORD,-1)) {
            case PREF_VALUE_S_PW_FAILED:
            case PREF_VALUE_S_PW_REJECTED:
                return false;
        }
        return true;
    }

    private static boolean isStringPrefNullEmpty(SharedPreferences sharedPrefs, String key) {
        final String pref = sharedPrefs.getString(key,null);
        if( (pref==null) || (pref.isEmpty()) )
            return true;
        return false;
    }
}