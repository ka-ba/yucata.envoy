package kaba.yucata.envoy.datalink;

import java.io.IOException;

import kaba.yucata.envoy.ConfigurationException;

/**
 * Created by kaba on 03/09/17.
 */

public abstract class ServerAbstraction {
    abstract public SessionAbstraction recoverSession() throws ConfigurationException;
    abstract public SessionAbstraction requestSession() throws ConfigurationException, SecurityException;
    abstract public StateInfo loadInfo(SessionAbstraction session) throws IOException, SecurityException, ConfigurationException;
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
    public StateInfo coldCallLoadInfo() throws IOException, SecurityException, ConfigurationException {
        SessionAbstraction session;
        try {
            session = recoverSession();
            final StateInfo info = loadInfo(session);
            info.setSession(session);
            return info;
        } catch(SecurityException e) {  // session invalid => try the full cycle exactly once
            session = requestSession();
            final StateInfo info = loadInfo(session);
            info.setSession(session);
            return info;
        }
    }
    abstract protected class SessionAbstraction {} // TODO: fill with abstract methods
}
