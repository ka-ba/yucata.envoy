package kaba.yucata.envoy.datalink;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import kaba.yucata.envoy.ConfigurationException;
import kaba.yucata.envoy.PrefsHelper;
import kaba.yucata.envoy.R;

import static kaba.yucata.envoy.PrefsHelper.PREF_KEY_SECRET;
import static kaba.yucata.envoy.PrefsHelper.PREF_KEY_SESSION_ID;
import static kaba.yucata.envoy.PrefsHelper.PREF_KEY_USERNAME;
import static kaba.yucata.envoy.PrefsHelper.PREF_KEY_USER_ID;
import static kaba.yucata.envoy.PrefsHelper.PREF_KEY_YUCATA_TOKEN;

/**
 * Created by kaba on 07/09/17.
 */

public class YucataServerAbstraction extends ServerAbstraction {
// diy   {
//        CookieHandler.setDefault(new CookieManager());
//    }

    private static final boolean DEBUG = true;
    public final static String LOGIN_URL_S="http://www.yucata.de/en";
    public final static String GETGAMES_URL_S="http://www.yucata.de/Services/YucataService.svc/GetCurrentGames";
    public final static String GETGAMES_REFERER="http://www.yucata.de/en/CurrentGames";
    private static final String MP_BOUNDARY = "GREATWALLOFCHINA";
    private static final String POST = "--"+MP_BOUNDARY+
            "\r\nContent-Disposition: form-data; name=\"__VIEWSTATE\"\r\n\r\ny4x9hvWyAIuf96efDj1cRboeBRnRQ/8hVKSNfo6/4KOouVJSyokh4j3oZALdT3tg/2R9UJUKogZCyYwQutbmIWMLhv+Zl1HrYQ3EYiXKSFfiihZyfwiGLvV7PYsU8dNCu9B/3yOCvwAqStrwbzi/ixuFVZc3qfLP+ZWbkap/zh2y0e9hzn15BG/B2sdtpWD7XN97cFRmoTnGxJ4+p78yKnzncjw2NND7tstmTYpY5D9ayi5q8PtgaJtblkg6Q/quH2PfEZqFlyYl0RpZn17IPu23xoLsj2qT0aP4MSaEwep1dOnTOqFlppwqcFxafwRNhw5KSxpw+kcA5PMwkk6ZhnPFOJaDYZ/lbPWn4X+6a7wrukEzS9EBwqeHUKSLOZm0Emyg3zt/aP/QJR1n6MUPCggPWEhOgjHN2NptA6jzlWKs4VZxFito2eUayU6zCIjA24TkPoIeUdNUkF+ShNEpXLdATwKbrTKqW9qgl6GP3GoV4AmYpndlVNLZf+8iQPeKUjN7+1FwYODbXNalKsagNNTCb6LKt4036vml04PQ6sBkq1INAB8fwU7DNlNMxZuHDA4eElvS9rOgjmYbR4nVGYyHTBftS5s93b2ocFneoEDz6bK7N9dpmAHRb0GVhlIQx+OybKcYvLI5WNi9B8NVaOmiT19w49jo8QT+Zg+cqWuKVblCb2xYzYdA4V4gQoLdUHKYR3m26GtGNwkq0GD/nBzlcdaiZo5rJbX25x146GRuOPba0BvXipy2P1Rtk8c2qOkQzpPuitu32oqdHewRG9rYwGg7/EorfbARovpMboN0K0JBLmTTF9vWEzx4B/gAntqFIlgQUTVBIzD9kehI9R7VrC6663kbmG5JIkPfVllITmbwfu72U/3WUlJ9CQASJ84TF6OqqeHI78Bn6nVndY7p2Mfda5N2DQLC9AywF51kAK6AcC7C7Pc3MWD52Tyg9FrsFQ==\r\n"+
            "--"+MP_BOUNDARY+
            "\r\nContent-Disposition: form-data; name=\"__VIEWSTATEGENERATOR\"\r\n\r\n84412C77\r\n"+
            "--"+MP_BOUNDARY+
            "\r\nContent-Disposition: form-data; name=\"ctl00$ctl07$btnLogin\"\r\n\r\nLogin\r\n"+
            "--"+MP_BOUNDARY+
            "\r\nContent-Disposition: form-data; name=\"ctl00$ctl07$edtLogin\"\r\n\r\n%s\r\n"+
            "--"+MP_BOUNDARY+
            "\r\nContent-Disposition: form-data; name=\"ctl00$ctl07$edtPassword\"\r\n\r\n%s\r\n"+
            "--"+MP_BOUNDARY+"--\r\n";
    public final URL LOGIN_URL;
    public final URL GETGAMES_URL;

    // FIXME: only public to test from service => return o default (no keyword)
    public YucataServerAbstraction(Context c)
            throws Error {
        super(c);
        try {
            LOGIN_URL=new URL(LOGIN_URL_S);
            GETGAMES_URL=new URL(GETGAMES_URL_S);
        } catch (MalformedURLException e) {
            throw new Error("internal error",e);
        }
    }

    @Override @NonNull
    public SessionAbstraction recoverSession() throws ConfigurationException,CommunicationException.NoSessionException {
            return new YucataSession();
    }

    @Override @NonNull
    public SessionAbstraction requestSession() throws ConfigurationException, CommunicationException {
        final String username = sharedPrefs.getString(PREF_KEY_USERNAME, null);
        if( (username==null) || (username.isEmpty()) )
            throw new ConfigurationException(context.getString(R.string.hint_username));
        final String password = sharedPrefs.getString(PREF_KEY_SECRET, null);
        if( (password==null) || (password.isEmpty()) )
            throw new ConfigurationException(context.getString(R.string.hint_password));
        PrefsHelper.clearSessionPrefs(sharedPrefs);
        HttpURLConnection connection=null;
        try {
            // send
            connection = (HttpURLConnection) LOGIN_URL.openConnection();
            connection.setRequestProperty("Content-Type","multipart/form-data; boundary="+MP_BOUNDARY);
            connection.setRequestProperty("Referer",LOGIN_URL_S);
            connection.setRequestProperty("User-Agent","YucataEnvoy");
            connection.setInstanceFollowRedirects(false);
            final String post = String.format(POST,username,password);  // FIXME: introduce MultipartHelper!?
            connection.setDoOutput(true);
            connection.setFixedLengthStreamingMode(post.length());
            if(true&&DEBUG) {
                System.out.println("--OUT--requestSession--");
                System.out.println("--> " + connection.getURL());
                printHeaders(System.out, connection.getRequestProperties());
                System.out.println("-->\n"+post);
            }
            final OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
            writer.write(post);
            writer.close();
            // receive
            connection.getResponseCode();
            final Collection<String> cookies = extractCookies(connection);
            String session_id=null, token=null;
            for( String s : cookies ) {
                if( s.startsWith("ASP.NET_SessionId=")) {
                    session_id=s;
                } else if( s.startsWith("Yucata=")) {
                    token=s;
                }
            }
            if(true&&DEBUG) {
                System.out.println("---IN--requestSession--");
                printHeaders(System.out, connection.getHeaderFields());
            }
            if( (session_id==null) || (token==null) )
                throw new CommunicationException.LoginFailedException("login failed ("+connection.getResponseCode()+")");  // FIXME: subclass CommunicationExc.?
            PrefsHelper.setStrings( sharedPrefs, PREF_KEY_SESSION_ID, session_id, PREF_KEY_YUCATA_TOKEN, token );
            if(true&&DEBUG) {
                System.out.println("received session tokens:\n session: "+session_id+"\n yucata: "+token);
            }
        } catch (IOException e) {
            throw new CommunicationException.IOException(context.getString(R.string.e_obtainingsession),e);  // FIXME: more specific exception?
        } finally {
            if(connection!=null)
                connection.disconnect();
        }
        return new YucataSession();
    }

    private void printHeaders(PrintStream out, Map<String, List<String>> headers) {
        if( headers == null ) {
            out.println("<<no headers>>");
            return;
        }
        keyloop:
        for( String k : headers.keySet() ) {
            if(k==null) {
                out.println("<<null key>>");
                continue keyloop;
            }
            List<String> vs = headers.get(k);
            if(vs==null) {
                out.println(k+": <<no values>>");
                continue keyloop;
            }
            valueloop:
            for( String v : vs ) {
                if(v==null) {
                    out.println(k+": <<null value>>");
                    continue valueloop;
                }
                out.println(k+": "+v);
            }
        }
    }

    @Override
    public StateInfo loadInfo(@NonNull SessionAbstraction session) throws CommunicationException, ConfigurationException {
        final YucataSession y_session = (YucataSession) session;
        HttpURLConnection connection=null;
        try {
            // send
            connection = (HttpURLConnection) GETGAMES_URL.openConnection();
            connection.setRequestProperty("Accept","application/json");
            connection.setRequestProperty("Referer",GETGAMES_REFERER);
            connection.setRequestProperty("User-Agent","YucataEnvoy");
            connection.setRequestProperty("Cookie",y_session.getCookieHeaderValue());
            // todo: set User-Agent (and test if overridden..)
            connection.setDoOutput(true);  // zero length post
            connection.setFixedLengthStreamingMode(0);
            // receive
            final int responseCode = connection.getResponseCode();
            if( (responseCode<200) || (responseCode>299) )
                throw new CommunicationException.IOException( context.getString(R.string.e_loadinginfo2,responseCode) );
^            final String json_str = readStreamToString(connection.getInputStream());
            return parseJSON(y_session,json_str);
        } catch (JSONException e) {
            throw new CommunicationException(context.getString(R.string.e_parsingjson),e);  // FIXME: specialized Exc.?
        } catch (IOException e) {
            throw new CommunicationException.IOException(context.getString(R.string.e_loadinginfo),e);
        } finally {
            if(connection!=null)
                connection.disconnect();
        }
    }

    private StateInfo parseJSON(YucataSession y_session, String json_str) throws JSONException {
        final JSONObject json = new JSONObject(json_str);
        final JSONObject json_d = json.getJSONObject("d");
        final int totalGames = json_d.getInt("TotalGames");
        System.out.println("read "+totalGames+" total games from json");
        int onTurnCount=0;
        final int onTurnGame = json_d.getInt("NextGameOnTurn");  // easy if zero
        if(onTurnGame!=0) {  // complicated
            final JSONArray games = json_d.getJSONArray("Games");
            if( y_session.isUserIdUnknown() )
                y_session.parseJsonUserId(games,onTurnGame);
            onTurnCount = parseJsonCountOnTurn(games,y_session.getUserId());
        }
        // FIXME: invitation count ?
        if(true&&DEBUG)
            System.out.println("received games info: "+totalGames+" total, "+onTurnCount+" on turn");
        return new StateInfo(totalGames,onTurnCount,-1);
    }

    private int parseJsonCountOnTurn(JSONArray games, int userId)
            throws JSONException {
        int count=0;
        for( int i=0; i<games.length(); i++ ) {
            if( games.isNull(i))
                continue;
            final JSONObject game = games.getJSONObject(i);
            if(game.getInt("PlayerOnTurn") == userId)
                count++;
        }
        return count;
    }

    private String readStreamToString(InputStream stream) {
        Scanner scanner = new Scanner(stream).useDelimiter("\\A");
        return scanner.hasNext() ? scanner.next() : "";
    }

    private Collection<String> extractCookies(HttpURLConnection connection) {
        final Map<String, List<String>> headers = connection.getHeaderFields();
        final List<String> cookies = headers.get("Set-Cookie");
        return ( cookies!=null ? cookies : new ArrayList<String>(0) );
    }

    class YucataSession extends SessionAbstraction {
        private final String sessionId;
        private final String yucataToken;
        private int userId=-1;
        YucataSession()
                throws CommunicationException.NoSessionException {
            sessionId = sharedPrefs.getString(PREF_KEY_SESSION_ID, null);
            if( (sessionId==null) || (sessionId.isEmpty()) )
                throw new CommunicationException.NoSessionException("no current session");
            yucataToken = sharedPrefs.getString(PREF_KEY_YUCATA_TOKEN, null);
            if( (yucataToken==null) || (yucataToken.isEmpty()) )
                throw new CommunicationException.NoSessionException("no current authentication token");
            userId = sharedPrefs.getInt(PREF_KEY_USER_ID,-1);
            if(true&&DEBUG) {
                System.out.println("new session object:\n session: "+sessionId+"\n yucata: "+yucataToken+"\n userid: "+userId);
            }
        }

        public int getUserId() {
            return userId;
        }

        public String getSessionId() {
            return sessionId;
        }

        public String getYucataToken() {
            return yucataToken;
        }

        public String getCookieHeaderValue() {
            return sessionId+"; "+yucataToken;
        }

        public boolean isUserIdUnknown() {
            return userId == -1;
        }

        public void parseJsonUserId(JSONArray games, int onTurnGame)
                throws JSONException {
            for( int i=0; i<games.length(); i++ ) {
                if( games.isNull(i))
                    continue;
                final JSONObject game = games.getJSONObject(i);
                if(game.getInt("ID") == onTurnGame) {
                    final int pid = game.getInt("PlayerOnTurn");  // must be this user
                    userId=pid;
                    sharedPrefs.edit().putInt(PREF_KEY_USER_ID,pid).apply();
                    return;
                }
            }
        }
    }
}
