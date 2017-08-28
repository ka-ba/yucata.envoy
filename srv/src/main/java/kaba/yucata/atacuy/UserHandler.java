package kaba.yucata.atacuy;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

class UserHandler implements HttpHandler {
    private static final long SKIPPY = 1000;
    private static final SecureRandom rng = new SecureRandom();
    public static final String CONTEXT = "/user";
    private final HashMap<String, User> users = new HashMap<>();

    UserHandler() throws NoSuchAlgorithmException {
        users.put("Rachael", new User("Rachael","Owl"));
        users.put("Rick", new User("Rick","memories"));
    }

    @Override
    public void handle(HttpExchange exch) throws IOException {
        try {
            if (!"GET".equals(exch.getRequestMethod())) {
                answer(exch, 405, null, "not allowed to " + exch.getRequestMethod());
                return;
            }
            System.out.println("uri: " + exch.getRequestURI());
            // expected path: /user/<username>/<action>
            String[] uri_tokens = exch.getRequestURI().getPath().split("/");
            for (int i = 0; i < uri_tokens.length; i++)
                System.out.println(".... "+i+" - " + uri_tokens[i]);
            InputStream req = exch.getRequestBody();
            InputStreamReader read = new InputStreamReader(req);
            //while( read.skip(SKIPPY) < SKIPPY );  // consume
            // were we provided with username an action?
            if(uri_tokens.length<4) {
                answer(exch,400, null, "malformed request URI");
                return;
            }
            final User user = users.get(uri_tokens[2]);
            if(user==null) {
                answer(exch,404, null, "unknown username");
                return;
            }
            // process request if action is known
            if("token".equals(uri_tokens[3])){
                answer(exch,200, user.getNextToken(), "");
                return;
            } else if("state".equals(uri_tokens[3])){
                final String token = exch.getRequestHeaders().getFirst("Token");
                System.out.println("got token '"+token+"', expected '"+user.getCurrentHash()+"'");
                answer(exch,200, user.getNextToken(), user.getState());
                return;
            } else {
                answer(exch,418, null, "don't ask for coffee");
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw(e);
        }
    }

    private void answer(HttpExchange exch, int http_code, String token, String content) throws IOException {
        Headers headers = exch.getResponseHeaders();
        switch (http_code) {
            case 405:
                headers.set("Allow", "GET");
                break;
        }
        if((token!=null)&&(token.length()>0))
            headers.set("Token", token);
        final byte[] response = content.getBytes();
//        exch.sendResponseHeaders(http_code, content.length());
        exch.sendResponseHeaders(http_code, response.length);
        OutputStream res = exch.getResponseBody();
//		OutputStreamWriter write = new OutputStreamWriter(res);
//		write.append(content);
        res.write(response);
        exch.close();
    }
}
