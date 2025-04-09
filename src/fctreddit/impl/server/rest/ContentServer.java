package fctreddit.impl.server.rest;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.net.InetAddress;
import java.net.URI;
import java.util.logging.Logger;

import fctreddit.impl.server.rest.Discovery;

public class ContentServer {
    private static Logger Log = Logger.getLogger(ContentServer.class.getName());

    static {
        System.setProperty("java.net.preferIPv4Stack", "true");
        System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %5$s\n");
    }

    public static final int PORT = 8081;
    public static final String SERVICE = "Content";
    private static final String SERVER_URI_FMT = "http://%s:%s/rest";


    public static void main(String[] args) {

        try {


            ResourceConfig config = new ResourceConfig();
            config.register(UsersResource.class);

            String ip = InetAddress.getLocalHost().getHostAddress();
            String serverURI = String.format(SERVER_URI_FMT, ip, PORT);
            JdkHttpServerFactory.createHttpServer( URI.create(serverURI), config);

            Log.info(String.format("%s Server ready @ %s\n",  SERVICE, serverURI));

            //More code can be executed here...


            String host = InetAddress.getLocalHost().getHostAddress();
            Discovery disc = new Discovery(Discovery.DISCOVERY_ADDR, SERVICE, "tcp://" + host + ":" + PORT);
            disc.start();
        } catch( Exception e) {
            Log.severe(e.getMessage());
        }
    }
}
