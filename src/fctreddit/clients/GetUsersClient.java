package fctreddit.clients;


import fctreddit.api.java.Users;
import fctreddit.clients.grpc.GrpcUsersClient;
import fctreddit.clients.rest.RestUsersClient;
import fctreddit.impl.server.Discovery;
import fctreddit.impl.server.DiscoveryHolder;
import fctreddit.impl.server.rest.UsersServer;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Logger;


public class GetUsersClient {

    Discovery discoveryInstance = null;
    Users client = null;
    private static Logger Log = Logger.getLogger(GetUsersClient.class.getName());

    public GetUsersClient() throws IOException, InterruptedException {


        discoveryInstance = DiscoveryHolder.INSTANCE;

        URI serviceURI = null;
        Log.info("bananas before");
        serviceURI = discoveryInstance.knownUrisOf(UsersServer.SERVICE, 1)[0];

        Log.info("bananas: serviceURI: " + serviceURI.toString());
        if(serviceURI.toString().endsWith("rest"))
            client = new RestUsersClient(serviceURI);
        else
            client = new GrpcUsersClient(serviceURI);

    }

    public Users getClient() {
        return client;
    }

}
