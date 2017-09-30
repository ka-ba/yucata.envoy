package kaba.yucata.envoy.datalink;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.preference.PreferenceManager;

import java.io.IOException;

import kaba.yucata.envoy.BuildConfig;
import kaba.yucata.envoy.ConfigurationException;
import kaba.yucata.envoy.PrefsHelper;

/**
 * Created by kaba on 03/09/17.
 */

public abstract class ServerAbstraction {
    private final static boolean DEBUG= BuildConfig.DEBUG;
    public final Context context;
    public final SharedPreferences sharedPrefs;
    protected ServerAbstraction(Context context) {
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        this.context=context;
    }
    abstract public @NonNull SessionAbstraction recoverSession() throws ConfigurationException, CommunicationException.NoSessionException;
    abstract public @NonNull SessionAbstraction requestSession() throws ConfigurationException, CommunicationException;
    abstract public StateInfo loadInfo(@NonNull SessionAbstraction session) throws CommunicationException, ConfigurationException;

    public StateInfo coldCallLoadInfo(SessionAbstraction session) throws ConfigurationException, CommunicationException {
        if(session==null)
            return coldCallLoadInfo();
        try {
            return loadInfo(session);
        } catch(SecurityException e) {  // session invalid => try the full cycle exactly once
            session = requestSession();
            PrefsHelper.rememberLoginSuccess(sharedPrefs);
            final StateInfo info = loadInfo(session);
            info.setSession(session);
            return info;
        }
    }

    public StateInfo coldCallLoadInfo() throws ConfigurationException, CommunicationException {
        SessionAbstraction session;
        try {
            session = recoverSession();
            final StateInfo info = loadInfo(session);
            info.setSession(session);
            return info;
        } catch(SecurityException|CommunicationException e) {  // session invalid => try the full cycle exactly once
            if(true&&DEBUG)
                System.out.println("retrying fetch because of "+e.toString());
            session = requestSession();
            PrefsHelper.rememberLoginSuccess(sharedPrefs);
            final StateInfo info = loadInfo(session);
            info.setSession(session);
            return info;
        }
    }
    abstract protected class SessionAbstraction {} // TODO: fill with abstract methods
}
