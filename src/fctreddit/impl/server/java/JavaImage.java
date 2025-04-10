package fctreddit.impl.server.java;

import fctreddit.api.java.Image;
import fctreddit.api.java.Result;
import fctreddit.impl.server.persistence.Hibernate;

import java.util.logging.Logger;

public class JavaImage implements Image {

    private static Logger Log = Logger.getLogger(JavaImage.class.getName());

    private Hibernate hibernate;

    public JavaImage() {
        hibernate = Hibernate.getInstance();
    }

    @Override
    public Result<String> createImage(String userId, byte[] imageContents, String password) {
        Log.info("Not implemented yet");
        return Result.error(Result.ErrorCode.NOT_IMPLEMENTED);
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
