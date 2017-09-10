package kaba.yucata.envoy;

import android.content.SharedPreferences;

/**
 * Created by kaba on 08/09/17.
 */

public class PrefsHelper {
    public static final String PREF_KEY_USERNAME = "username";
    public static final String PREF_KEY_USER_ID = "userid";
    public static final String PREF_KEY_SECRET = "secret";
    public static final String PREF_KEY_GAMES_WAITING = "games_waiting";
    public static final String PREF_KEY_GAMES_TOTAL = "games_total";
    public static final String PREF_KEY_INVITES = "pers_invites";
    /** @deprecated for dummy server use only */
    public static final String PREF_KEY_TOKEN_BASE64 = "token_b64";
    public static final String PREF_KEY_YUCATA_TOKEN = "token";
    public static final String PREF_KEY_SESSION_ID = "session";
    public static final String PREF_KEY_LAST_RESPONSE = "last_response_code";

    public static void clearSessionPrefs(SharedPreferences sharedPrefs) {
        sharedPrefs.edit().
                remove(PREF_KEY_SESSION_ID).
                remove(PREF_KEY_YUCATA_TOKEN).
                remove(PREF_KEY_TOKEN_BASE64).apply();
    }

    public static void setStrings(SharedPreferences sharedPrefs, String... keys_values) {
        if( keys_values.length%2 == 1 )  // must always be even!
            throw new IllegalArgumentException("must always be called with even number of strings, always complete key-value-pairs");
        final SharedPreferences.Editor editor = sharedPrefs.edit();
        for( int i=0; i<keys_values.length; i+=2 )
            editor.putString( keys_values[i], keys_values[i+1] );
        editor.apply();
    }
}
