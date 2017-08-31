package kaba.yucata.envoy.datalink;

import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v4.app.LoaderManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Base64;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.UnknownServiceException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import kaba.yucata.envoy.StateInfo;

import static kaba.yucata.envoy.GameCountActivity.PREF_KEY_GAMES_TOTAL;
import static kaba.yucata.envoy.GameCountActivity.PREF_KEY_GAMES_WAITING;
import static kaba.yucata.envoy.GameCountActivity.PREF_KEY_INVITES;
import static kaba.yucata.envoy.GameCountActivity.PREF_KEY_LAST_RESPONSE;
import static kaba.yucata.envoy.GameCountActivity.PREF_KEY_SECRET;
import static kaba.yucata.envoy.GameCountActivity.PREF_KEY_TOKEN;
import static kaba.yucata.envoy.LocalConsts.BASEURL;

/**
 * Created by kaba on 08/08/17.
 */

public class LoaderHelper implements LoaderManager.LoaderCallbacks<StateInfo> {
    protected final static int LOADER_ID=1502228361;
    private static final String USERNAME_KEY = "KEY-Username";
    private static final long graceMillis = 60000;
    private final Context context;
    private SharedPreferences sharedPrefs;
    private final MessageDigest digest;
    private long lastInvoked;

    public LoaderHelper(Context context)
            throws NoSuchAlgorithmException {
        this.context=context;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        digest = MessageDigest.getInstance("SHA-256");
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
                final String username = bundle.getString(USERNAME_KEY);  // FIXME: get username from prefs instead?
                if( username==null || TextUtils.isEmpty(username) )
                    return null;
                StateInfo info;
                // fetch from network here
                try {
                    return loadCurrentState(username);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                    return null;
                }
            }
        };
    }

    private StateInfo loadCurrentState(String username)
            throws IOException, SecurityException {
        final String token = sharedPrefs.getString(PREF_KEY_TOKEN, null);
        if((token!=null)&&(token.length()>0)) {
            // normal case: we known the current token from the previous operation
            try {
                return loadWithCommand(username, "state", true );
            } catch(SecurityException e) {
                // try again, 'cause token might just have been outdated
                return loadWithCommand(username, "state", true );
            }
        } else {
            // edge case: no knowledge of the current token, not even outdated knowledge
            loadWithCommand(username, "token", false);  // token set to preferences as side effect
            return loadWithCommand(username, "state", true );
        }
    }

    private StateInfo loadWithCommand(String username, String rest_cmd, boolean result)  // FIXME: result currently unused
            throws IOException, SecurityException {
        HttpURLConnection urlConnection=null;
        int responseCode=666;
        final byte[] token = Base64.decode(
                sharedPrefs.getString(PREF_KEY_TOKEN, null),0);
        final String secret = sharedPrefs.getString(PREF_KEY_SECRET, null);
        if((secret==null)||(secret.length()<1))
            throw new IllegalStateException("no secret defined");
        try {
            int okCode;  // expected result code if everything performs smooth
            if ("state".equals(rest_cmd))
                okCode = 200;
            else if ("token".equals(rest_cmd))
                okCode = 204;
            else
                throw new UnknownServiceException("internal: unknown REST command " + rest_cmd);
            final URL url = buildUrl(username, rest_cmd);
            urlConnection = (HttpURLConnection) url.openConnection();
            if ("state".equals(rest_cmd)) {
                urlConnection.addRequestProperty("Token", getHash(token,secret.getBytes()));
            }
            responseCode = urlConnection.getResponseCode();
            final String new_token = urlConnection.getHeaderField("Token");
            System.out.println("answer token  : '" + new_token + "'");
            if ((new_token != null) && (new_token.length() > 0))
                sharedPrefs.edit().putString(PREF_KEY_TOKEN,new_token).apply();
            if (responseCode == okCode) {
                // everything seems in order
                if ((new_token == null) || (new_token.length() < 1))
                    throw new ProtocolException("server: no new token received");
                if(responseCode==200) {
                    InputStream in = urlConnection.getInputStream();
                    final BufferedReader reader = new BufferedReader(new InputStreamReader((in)));
                    final int waiting = Integer.parseInt(reader.readLine());
                    System.out.println("answer waiting      : '" + waiting + "'");
                    final int all = Integer.parseInt(reader.readLine());
                    System.out.println("answer all          : '" + all + "'");
                    final int invites = Integer.parseInt(reader.readLine());
                    System.out.println("personal invitations: '" + invites + "'");
                    return new StateInfo(all,waiting,invites);
                }
                return new StateInfo(0,0,0);  // fake numbers but no error
            } else switch(responseCode) {
                // a problem ... handle response codes we _can_ handle
                case 401:
                    // wrong token
                    throw new SecurityException("token mismatch");
                    // break;
                case 404:
                    // wrong username
                    throw new IllegalArgumentException("user unknown: "+username);
                    // break;
                default:
                    throw new IOException("internal: unexpected response code "+responseCode);
            }
        } catch(NumberFormatException e){
            e.printStackTrace();
            throw new ProtocolException(e.getMessage());
        } finally {
            if(urlConnection!=null)
                urlConnection.disconnect();
            sharedPrefs.edit().putInt(PREF_KEY_LAST_RESPONSE,responseCode).apply();
        }
    }

    private String getHash(byte[] token_bytes, byte[] secret_bytes) {
        final byte[] concat = new byte[ token_bytes.length + secret_bytes.length ];
        System.arraycopy(token_bytes,0,concat,0,token_bytes.length);
        System.arraycopy(secret_bytes,0,concat,token_bytes.length,secret_bytes.length);
        final byte[] digested = digest.digest( concat );
        return Base64.encodeToString(digested,Base64.NO_WRAP);
    }

    @Override
    public void onLoadFinished(Loader<StateInfo> loader, StateInfo stateInfo) {
        // use loaded info here
        if(stateInfo==null)
            return;
        final SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putInt(PREF_KEY_GAMES_WAITING,stateInfo.getGamesWaiting());
        editor.putInt(PREF_KEY_GAMES_TOTAL,stateInfo.getGamesTotal());
        editor.putInt(PREF_KEY_INVITES,stateInfo.getPersonalInvites());
        editor.apply();
    }

    @Override
    public void onLoaderReset(Loader<StateInfo> loader) {
        // FIXME: anything to do here?
    }

    private URL buildUrl(String username, String action) {
        Uri uri = Uri.parse(BASEURL).buildUpon()
                .appendPath(username)
                .appendPath(action).build();
        try {
            return new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
