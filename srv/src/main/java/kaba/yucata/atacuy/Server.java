package kaba.yucata.atacuy;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;

import kaba.yucata.envoy.LocalConsts;
import sun.net.InetAddressCachePolicy;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;

import static kaba.yucata.envoy.LocalConsts.SERVERHOST_B;

public class Server {

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
//        InetAddress addr = InetAddress.getByAddress(new byte[]{127, 0, 0, 1});   // getByName("localhost");
        InetAddress addr = InetAddress.getByAddress(SERVERHOST_B);
        int port = 11121; //Integer.parseInt(System.getenv("PORT"));
        InetSocketAddress sock = new InetSocketAddress(addr, port);
        HttpServer httpserv = HttpServer.create(sock, 64);
        HttpContext bla_cntxt = httpserv.createContext(UserHandler.CONTEXT, new UserHandler());
        httpserv.start();
    }
}
