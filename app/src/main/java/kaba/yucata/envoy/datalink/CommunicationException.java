package kaba.yucata.envoy.datalink;

/**
 * Created by kaba on 09/09/17.
 */

public class CommunicationException extends RuntimeException {
    public CommunicationException(String message) {
        super(message);
    }

    public CommunicationException(String message, Throwable cause) {
        super(message, cause);
    }

    public static class NoSessionException extends CommunicationException {

        public NoSessionException(String message) {
            super(message);
        }

        public NoSessionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
