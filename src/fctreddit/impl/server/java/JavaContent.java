package fctreddit.impl.server.java;

import fctreddit.api.Post;
import fctreddit.api.User;
import fctreddit.api.java.Content;
import fctreddit.api.java.Result;
import fctreddit.api.java.Users;
import fctreddit.clients.GetUsersClient;
import fctreddit.impl.server.persistence.Hibernate;

import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public class JavaContent implements Content {

    private static Logger Log = Logger.getLogger(JavaContent.class.getName());

    private Hibernate hibernate;

    public JavaContent() {
        hibernate = Hibernate.getInstance();
    }

    @Override
    public Result<String> createPost(Post post, String userPassword) {
        Log.info("createPost : " + post);

        if (post == null) {
            Log.info("Post object invalid.");
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        }
        try {

            Users client = new GetUsersClient().getClient();
            Result<User> result = client.getUser(post.getAuthorId(), userPassword);

            if( result.isOK()  )
                Log.info("Get User:" + result.value() );
            else {
                Log.info("Get User failed with error: " + result.error());
                return Result.error(result.error());
            }
            if (post.getPostId() == null)
                post.setPostId(UUID.randomUUID().toString());
            hibernate.persist(post);
        } catch (Exception e) {
            e.printStackTrace();
            Log.info("Post already exists.");
            return Result.error(Result.ErrorCode.CONFLICT);
        }

        return Result.ok(post.getPostId());
    }

    @Override
    public Result<List<String>> getPosts(long timestamp, String sortOrder) {
        Log.info("Not implemented yet");
        return Result.error(Result.ErrorCode.NOT_IMPLEMENTED);
    }
    @Override
    public Result<Post> getPost(String postId) {
        Log.info("getPost : postId = " + postId);
        if (postId == null) {
            Log.info("postId is null.");
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        }

        Post post = null;

        try {
            post = hibernate.get(Post.class, postId);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(Result.ErrorCode.NOT_FOUND);

        }
        if (post == null) {
            Log.info("Post does not exist.");
            return Result.error(Result.ErrorCode.NOT_FOUND);
        }
        return Result.ok(post);
    }

    @Override
    public Result<List<String>> getPostAnswers(String postId, long maxTimeout) {
        Log.info("Not implemented yet");
        return Result.error(Result.ErrorCode.NOT_IMPLEMENTED);
    }
    @Override
    public Result<Post> updatePost(String postId, String userPassword, Post post) {
        Log.info("Not implemented yet");
        return Result.error(Result.ErrorCode.NOT_IMPLEMENTED);
    }
    @Override
    public Result<Void> deletePost(String postId, String userPassword)
    {
        Log.info("Not implemented yet");
        return Result.error(Result.ErrorCode.NOT_IMPLEMENTED);
    }

    @Override
    public Result<Void> upVotePost(String postId, String userId, String userPassword)
    {
        Log.info("Not implemented yet");
        return Result.error(Result.ErrorCode.NOT_IMPLEMENTED);
    }

    @Override
    public Result<Void> removeUpVotePost(String postId, String userId, String userPassword)
    {
        Log.info("Not implemented yet");
        return Result.error(Result.ErrorCode.NOT_IMPLEMENTED);
    }

    @Override
    public Result<Void> downVotePost(String postId, String userId, String userPassword)
    {
        Log.info("Not implemented yet");
        return Result.error(Result.ErrorCode.NOT_IMPLEMENTED);
    }

    @Override
    public Result<Void> removeDownVotePost(String postId, String userId, String userPassword)
    {
        Log.info("Not implemented yet");
        return Result.error(Result.ErrorCode.NOT_IMPLEMENTED);
    }

    @Override
    public Result<Integer> getupVotes(String postId)
    {
        Log.info("Not implemented yet");
        return Result.error(Result.ErrorCode.NOT_IMPLEMENTED);
    }

    @Override
    public Result<Integer> getDownVotes(String postId)
    {
        Log.info("Not implemented yet");
        return Result.error(Result.ErrorCode.NOT_IMPLEMENTED);
    }


}
