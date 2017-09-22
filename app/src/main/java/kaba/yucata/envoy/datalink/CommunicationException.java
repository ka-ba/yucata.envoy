package kaba.yucata.envoy.datalink;

/**
 * Created by kaba on 09/09/17.
 */

public class CommunicationException extends Exception {
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

    public static class IllegalSessionException extends CommunicationException {

        public IllegalSessionException(String message) {
            super(message);
        }

        public IllegalSessionException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class LoginFailedException extends CommunicationException {

        public LoginFailedException(String message) {
            super(message);
        }

        public LoginFailedException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class IOException extends CommunicationException {

        public IOException(String message) {
            super(message);
        }

        public IOException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
