package kaba.yucata.atacuy;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.xml.bind.DatatypeConverter;

class User {
    private final static SecureRandom secrand = new SecureRandom();
    private final MessageDigest digest;
    private final String name, secret;
    private final byte[] secret_bytes;
    private int waiting, all, invites; // games waiting to be played, all current games, current personal invitations
    private byte[] token_bytes = new byte[8];
    private String token;

    User(String n, String s) throws NoSuchAlgorithmException {
        name = n;
        secret = s;
        secret_bytes = secret.getBytes();
        all = secrand.nextInt(10);
        waiting = secrand.nextInt(all + 1);
        invites = secrand.nextInt(waiting + 1);
        digest = MessageDigest.getInstance("SHA-256");
        prepareNextToken();
    }

    public String getCurrentToken() {
        return token;
    }

    public String getNextToken() {
        prepareNextToken();
        return token;
    }

    String getState() {
        final String state = "" + waiting + "\n" + all + "\n" + invites;
        // construct next state
        if (waiting >= all) {
            all += secrand.nextInt(11) - 5;
            if (all < 1)
                all = 1;
            waiting = secrand.nextInt(1 + all / 3);
        } else {
            waiting += secrand.nextInt(all + 1 - waiting);
        }
        invites = secrand.nextInt(waiting+1);
        return state;
    }

    private void prepareNextToken() {
        secrand.nextBytes(token_bytes);
        token = DatatypeConverter.printBase64Binary(token_bytes);
    }

    public String getCurrentHash() {
        final byte[] concat = new byte[ token_bytes.length + secret_bytes.length ];
        System.arraycopy(token_bytes,0,concat,0,token_bytes.length);
        System.arraycopy(secret_bytes,0,concat,token_bytes.length,secret_bytes.length);
        final byte[] digested = digest.digest( concat );
        return DatatypeConverter.printBase64Binary(digested);
    }
}
