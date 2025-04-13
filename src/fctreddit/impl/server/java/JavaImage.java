package fctreddit.impl.server.java;

import fctreddit.api.User;
import fctreddit.api.java.Image;
import fctreddit.api.java.Result;
import fctreddit.clients.CreateUserClient;
import fctreddit.clients.grpc.GrpcUsersClient;
import fctreddit.clients.java.UsersClient;
import fctreddit.clients.rest.RestUsersClient;
import fctreddit.impl.server.Discovery;
import fctreddit.impl.server.persistence.Hibernate;
import fctreddit.impl.server.rest.UsersServer;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.logging.Logger;

public class JavaImage implements Image {

    private static Logger Log = Logger.getLogger(JavaImage.class.getName());

    private Hibernate hibernate;

    private static final String IMAGES_DIR = "images";


    public JavaImage() {
        hibernate = Hibernate.getInstance();
    }


    private String associateImage(String userId, byte[] image) {
        Log.info("associate an avatar : user = " + userId + "; avatarSize = " + image.length);

        if (image.length == 0) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        String imageId = UUID.randomUUID().toString();
        Path pathToFile = Paths.get(IMAGES_DIR +
                File.separator +
                userId + File.separator +
                imageId + ".png");

        try {

            Files.createDirectories(pathToFile.getParent());

            Files.deleteIfExists(pathToFile);
            Files.write(pathToFile, image);
        } catch (Exception e) {
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
        return pathToFile.toString();
    }

    private Result<User> verifyUserIdAuxiliarFunction(String userId, String password, URI serviceURI) {
        UsersClient client = null;

        //if(serverUrl.endsWith("rest"))
            client = new RestUsersClient(serviceURI);
        /*else
            client = new GrpcUsersClient(serviceURI);*/

        Result<User> result = client.getUser(userId, password);


        return result;
    }
    @Override
    public Result<String> createImage(String userId, byte[] imageContents, String password) {
        Log.info("createImage : user = " + userId + "; pwd = " + password);
        if (userId == null || imageContents == null)
            return Result.error(Result.ErrorCode.BAD_REQUEST);

        try {
            Discovery discovery = new Discovery(Discovery.DISCOVERY_ADDR);
            discovery.start();
            URI serviceURI = discovery.knownUrisOf(UsersServer.SERVICE, 1)[0];

            Result<User> result = verifyUserIdAuxiliarFunction(userId, password, serviceURI);

            if( result.isOK()  )
                Log.info("Get user:" + result.value() );
            else {
                Log.info("Get user failed with error: " + result.error());
                return Result.error(Result.ErrorCode.INTERNAL_ERROR);
            }

            return Result.ok(associateImage(userId, imageContents));

        } catch (IOException e) {
            Log.info("Error while persisting image: " + e.getMessage());
            return Result.error(Result.ErrorCode.INTERNAL_ERROR);
        } catch (Exception e) {
            Log.info("Else: " + e.getMessage());
            return Result.error(Result.ErrorCode.INTERNAL_ERROR);
        }


        //return Result.ok(user);
    }


    @Override
    public Result<byte[]> getImage(String userId, String imageId) {
        Log.info("Not implemented yet");
        return Result.error(Result.ErrorCode.NOT_IMPLEMENTED);
    }


    @Override
    public Result<Void> deleteImage(String userId, String imageId, String password) {
        Log.info("Not implemented yet");
        return Result.error(Result.ErrorCode.NOT_IMPLEMENTED);
    }

}
