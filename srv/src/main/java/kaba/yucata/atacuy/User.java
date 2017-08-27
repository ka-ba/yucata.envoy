package kaba.yucata.atacuy;

import java.util.Random;
import java.security.SecureRandom;

import javax.xml.bind.DatatypeConverter;

import sun.misc.BASE64Encoder;

class User {
    private final static SecureRandom secrand = new SecureRandom();
    private final String name;
    private int waiting, all;
    private byte[] token_bytes = new byte[8];
    private String token;

    User(String n) {
        name = n;
        all = secrand.nextInt(10);
        waiting = secrand.nextInt(all + 1);
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
        final String state = "" + waiting + "\n" + all;
        // construct next state
        if (waiting >= all) {
            all += secrand.nextInt(11) - 5;
            if (all < 1)
                all = 1;
            waiting = secrand.nextInt(all / 3);
        } else {
            waiting += secrand.nextInt(all + 1 - waiting);
        }
        return state;
    }

    private void prepareNextToken() {
        secrand.nextBytes(token_bytes);
        token = DatatypeConverter.printBase64Binary(token_bytes);
    }
}
