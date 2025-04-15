package fctreddit.impl.server.java;

import fctreddit.api.User;
import fctreddit.api.java.Image;
import fctreddit.api.java.Result;
import fctreddit.api.java.Users;

import fctreddit.clients.GetUsersClient;

import fctreddit.impl.server.persistence.Hibernate;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.io.IOException;
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
        Path pathToFile = Paths.get(IMAGES_DIR, userId, imageId + ".png");

        try {
            Files.createDirectories(pathToFile.getParent());
            Files.write(pathToFile, image);
        } catch (Exception e) {
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }

        // Return a URI to access the image via the GET endpoint
        //return "http://172.18.0.3:8082/rest/image/" + userId + "/" + imageId;

        return userId + "/" + imageId;
    }


    @Override
    public Result<String> createImage(String userId, byte[] imageContents, String password) {
        Log.info("createImage : user = " + userId + "; pwd = " + password);


        if (userId == null)
            return Result.error(Result.ErrorCode.BAD_REQUEST);

        try {
            Users client = new GetUsersClient().getClient();

            Result<User> result = client.getUser(userId, password);

            if( result.isOK()  )
                Log.info("Get user:" + result.value() );
            else {
                Log.info("Get user failed with error: " + result.error());
                return Result.error(result.error());
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
        Log.info("getImage : user = " + userId + ", imageId = " + imageId);

        if (userId == null || imageId == null) {
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        }
        Path pathToFile = Paths.get(IMAGES_DIR, userId, imageId + ".png");

        try {
            if (!Files.exists(pathToFile)) {
                return Result.error(Result.ErrorCode.NOT_FOUND);
            }
            byte[] imageBytes = Files.readAllBytes(pathToFile);
            return Result.ok(imageBytes);

        } catch (IOException e) {
            Log.severe("Error reading image file: " + e.getMessage());
            return Result.error(Result.ErrorCode.INTERNAL_ERROR);
        }
    }



    @Override
    public Result<Void> deleteImage(String userId, String imageId, String password) {
        Log.info("Not implemented yett");
        return Result.error(Result.ErrorCode.NOT_IMPLEMENTED);
    }

}
