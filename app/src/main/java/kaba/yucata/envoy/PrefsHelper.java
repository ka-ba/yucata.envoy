package kaba.yucata.envoy;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

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
}
