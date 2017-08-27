package kaba.yucata.atacuy;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import sun.net.InetAddressCachePolicy;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;

public class Server {

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        InetAddress addr = InetAddress.getByAddress(new byte[]{127, 0, 0, 1});   // getByName("localhost");
        int port = 11121; //Integer.parseInt(System.getenv("PORT"));
        InetSocketAddress sock = new InetSocketAddress(addr, port);
        HttpServer httpserv = HttpServer.create(sock, 64);
        HttpContext bla_cntxt = httpserv.createContext(UserHandler.CONTEXT, new UserHandler());
        httpserv.start();
    }
}
