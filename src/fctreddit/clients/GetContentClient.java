package fctreddit.clients;


import fctreddit.api.java.Content;
import fctreddit.clients.grpc.GrpcContentClient;
import fctreddit.clients.rest.RestContentClient;
import fctreddit.impl.server.Discovery;
import fctreddit.impl.server.DiscoveryHolder;
import fctreddit.impl.server.rest.ContentServer;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Logger;


public class GetContentClient {

    Discovery discoveryInstance = null;
    Content client = null;
    private static Logger Log = Logger.getLogger(GetContentClient.class.getName());

    public GetContentClient() throws IOException, InterruptedException {

        discoveryInstance = DiscoveryHolder.INSTANCE;
        URI serviceURI = null;
        serviceURI = discoveryInstance.knownUrisOf(ContentServer.SERVICE, 1)[0];


        if(serviceURI.toString().endsWith("rest"))
            client = new RestContentClient(serviceURI);
        else
            client = new GrpcContentClient(serviceURI);

    }

    public Content getClient() {
        return client;
    }

}

