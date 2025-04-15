package fctreddit.clients;

import fctreddit.api.User;
import fctreddit.api.java.Result;
import fctreddit.api.java.Users;
import fctreddit.clients.grpc.GrpcUsersClient;
import fctreddit.clients.java.UsersClient;
import fctreddit.clients.rest.RestUsersClient;
import fctreddit.impl.server.Discovery;
import fctreddit.impl.server.rest.UsersServer;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Logger;

import static java.lang.Thread.sleep;


public class GetUsersClient {

    private static Discovery discoveryInstance;


    Users client = null;
    private static Logger Log = Logger.getLogger(GetUsersClient.class.getName());

    public GetUsersClient() throws IOException, InterruptedException {

        if (discoveryInstance == null) {
            discoveryInstance = new Discovery(Discovery.DISCOVERY_ADDR);
            discoveryInstance.start();
        }

        URI serviceURI = null;

        serviceURI = discoveryInstance.knownUrisOf(UsersServer.SERVICE, 1)[0];


        if(serviceURI.toString().endsWith("rest"))
            client = new RestUsersClient(serviceURI);
        else
            client = new GrpcUsersClient(serviceURI);

    }

    public Users getClient() {
        return client;
    }

}
