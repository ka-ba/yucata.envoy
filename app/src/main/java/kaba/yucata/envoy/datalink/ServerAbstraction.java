package kaba.yucata.envoy.datalink;

import android.support.annotation.NonNull;

import java.io.IOException;

import kaba.yucata.envoy.ConfigurationException;

/**
 * Created by kaba on 03/09/17.
 */

public abstract class ServerAbstraction {
    abstract public @NonNull SessionAbstraction recoverSession() throws ConfigurationException, CommunicationException.NoSessionException;
    abstract public @NonNull SessionAbstraction requestSession() throws ConfigurationException, SecurityException, CommunicationException;
    abstract public StateInfo loadInfo(@NonNull SessionAbstraction session) throws CommunicationException, SecurityException, ConfigurationException;

    public StateInfo coldCallLoadInfo(SessionAbstraction session) throws IOException, SecurityException, ConfigurationException {
        if(session==null)
            return coldCallLoadInfo();
        try {
            return loadInfo(session);
        } catch(SecurityException e) {  // session invalid => try the full cycle exactly once
            session = requestSession();
            final StateInfo info = loadInfo(session);
            info.setSession(session);
            return info;
        }
    }

    public StateInfo coldCallLoadInfo() throws SecurityException, ConfigurationException {
        SessionAbstraction session;
        try {
            session = recoverSession();
            final StateInfo info = loadInfo(session);
            info.setSession(session);
            return info;
        } catch(SecurityException|CommunicationException e) {  // session invalid => try the full cycle exactly once
            session = requestSession();
            final StateInfo info = loadInfo(session);
            info.setSession(session);
            return info;
        }
    }
    abstract protected class SessionAbstraction {} // TODO: fill with abstract methods
}
