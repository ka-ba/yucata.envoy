package kaba.yucata.envoy.datalink;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.preference.PreferenceManager;
import android.util.Base64;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import kaba.yucata.envoy.ConfigurationException;

import static kaba.yucata.envoy.PrefsHelper.PREF_KEY_LAST_RESPONSE;
import static kaba.yucata.envoy.PrefsHelper.PREF_KEY_SECRET;
import static kaba.yucata.envoy.PrefsHelper.PREF_KEY_TOKEN_BASE64;
import static kaba.yucata.envoy.PrefsHelper.PREF_KEY_USERNAME;
import static kaba.yucata.envoy.LocalConsts.BASEURL;

/**
 * Created by kaba on 03/09/17.
 */

class DummyServerAbstraction extends ServerAbstraction {

    private final Context context;
    private final SharedPreferences sharedPrefs;
    private final MessageDigest digest;

    DummyServerAbstraction(Context c)
            throws NoSuchAlgorithmException {
        context=c;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        digest = MessageDigest.getInstance("SHA-256");
    }

    @NonNull
    @Override
    public ServerAbstraction.SessionAbstraction recoverSession()
            throws ConfigurationException, CommunicationException.NoSessionException {
        try {
            return new DummySession(false);
        } catch (IllegalStateException e) { // if no secret known from previous exchanges
            throw new CommunicationException.NoSessionException("no session data to recover",e);
        }
    }

    @NonNull
    @Override
    public SessionAbstraction requestSession()
            throws ConfigurationException, CommunicationException.NoSessionException { // FIXME: really SecExc?
        final DummySession session = new DummySession(false);
        try {
            loadWithCommand(session,"token");
            return session;
        } catch (Exception e) { // if no secret known from previous exchanges
            throw new CommunicationException.NoSessionException("could not obtain session",e);
        }
    }

    @Override
    public StateInfo loadInfo(@NonNull SessionAbstraction session) throws
            CommunicationException, ConfigurationException {
        return loadWithCommand((DummySession) session, "state" );
    }

    private StateInfo loadWithCommand(DummySession session, String rest_cmd)
            throws CommunicationException, ConfigurationException {
        HttpURLConnection urlConnection=null;
        int responseCode=666;
        final String secret = session.getSecret();
        if((secret==null)||(secret.length()<1))
            throw new ConfigurationException("no secret defined");
        try {
            int okCode;  // expected result code if everything performs smoothly
            if ("state".equals(rest_cmd))
                okCode = 200;
            else if ("token".equals(rest_cmd))
                okCode = 204;
            else
                throw new CommunicationException("internal: unknown REST command " + rest_cmd);
            final URL url = buildUrl(session.getUsername(), rest_cmd);
            urlConnection = (HttpURLConnection) url.openConnection();
            if ("state".equals(rest_cmd)) {
                final byte[] token = session.getToken();
                urlConnection.addRequestProperty("Token", getHash(token,secret.getBytes()));
            }
            responseCode = urlConnection.getResponseCode();
            final String new_token = urlConnection.getHeaderField("Token");
            System.out.println("answer token  : '" + new_token + "'");
            if ((new_token != null) && (new_token.length() > 0)) {
                session.setTokenBase64(new_token);
                sharedPrefs.edit().putString(PREF_KEY_TOKEN_BASE64, new_token).apply();
            }
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
                    throw new CommunicationException.IllegalSessionException("token mismatch");
                    // break;
                case 404:
                    // wrong username
                    throw new ConfigurationException("user unknown: "+session.getUsername());
                    // break;
                default:
                    throw new CommunicationException("internal: unexpected response code "+responseCode);
            }
        } catch(NumberFormatException e){
//            e.printStackTrace();
            throw new CommunicationException("unparsable number",e);
        } catch (IOException e) {
            throw new CommunicationException("could not load info",e);
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

    class DummySession extends SessionAbstraction {
        private byte[] token;
        private final String username;
        private final String secret;

        DummySession(boolean need_token)
                throws ConfigurationException, IllegalStateException {
            final String token_base64 = sharedPrefs.getString(PREF_KEY_TOKEN_BASE64, null);
            token = ( token_base64!=null ? Base64.decode(token_base64,0) : new byte[0] );
            if( need_token && (token.length<1) )
                throw new IllegalStateException("mandatory token missing");
            username = sharedPrefs.getString(PREF_KEY_USERNAME, null);
            if((username==null)||(username.length()<1))
                throw new ConfigurationException("please configure a username");
            secret = sharedPrefs.getString(PREF_KEY_SECRET, null);
            if((secret==null)||(secret.length()<1))
                throw new ConfigurationException("please configure a secret");
        }

        public byte[] getToken() {
            return token;
        }

        public String getUsername() {
            return username;
        }

        public String getSecret() {
            return secret;
        }

        public void setTokenBase64(String new_token_b64)
                throws IllegalStateException {
            final byte[] new_token = (new_token_b64 != null ? Base64.decode(new_token_b64, 0) : new byte[0]);
            if(new_token.length<1)
                throw new IllegalStateException("mandatory token missing");
            token=new_token;
        }
    }
}
