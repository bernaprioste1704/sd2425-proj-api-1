package fctreddit.clients.rest;

import fctreddit.api.Post;
import fctreddit.api.java.Result;
import fctreddit.api.rest.RestContent;
import fctreddit.clients.java.ContentClient;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.*;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;

import java.net.URI;
import java.util.List;
import java.util.logging.Logger;

public class RestContentClient extends ContentClient {
    private static Logger Log = java.util.logging.Logger.getLogger(RestContentClient.class.getName());

    final URI serverURI;
    final Client client;
    final ClientConfig config;

    final WebTarget target;
    protected static final int MAX_RETRIES = 10;
    protected static final int RETRY_SLEEP = 5000;

    public RestContentClient( URI serverURI ) {
        this.serverURI = serverURI;

        this.config = new ClientConfig();

        config.property( ClientProperties.READ_TIMEOUT, READ_TIMEOUT);
        config.property( ClientProperties.CONNECT_TIMEOUT, CONNECT_TIMEOUT);


        this.client = ClientBuilder.newClient(config);

        target = client.target( serverURI ).path( RestContent.PATH );
    }

    public Result<String> createPost(Post post, String userPassword) {
        Response r = executeOperationPost(target.request()
                .accept(MediaType.APPLICATION_JSON), Entity.entity(post, MediaType.APPLICATION_JSON));

        if (r == null){
            return Result.error(  Result.ErrorCode.TIMEOUT );
        }
        int status = r.getStatus();
        if( status != Response.Status.OK.getStatusCode() )
            return Result.error( getErrorCodeFrom(status));
        else
            return Result.ok( r.readEntity( String.class ));
    }

    public Result<List<String>> getPosts(long timestamp, String sortOrder) {
        WebTarget targetWithParams = target.queryParam(RestContent.TIMESTAMP, timestamp)
                .queryParam(RestContent.SORTBY, sortOrder);

        Response r = executeOperationGet(targetWithParams.request()
                .accept(MediaType.APPLICATION_JSON));

        if (r == null){
            return Result.error(  Result.ErrorCode.TIMEOUT );
        }
        int status = r.getStatus();
        if( status != Response.Status.OK.getStatusCode() )
            return Result.error( getErrorCodeFrom(status));
        else
            return Result.ok( r.readEntity(new GenericType<List<String>>(){}));
    }

    public Result<Post> getPost(String postId) {
        Response r = executeOperationGet(target.path(postId).request()
                .accept(MediaType.APPLICATION_JSON));

        if (r == null){
            return Result.error(  Result.ErrorCode.TIMEOUT );
        }
        int status = r.getStatus();
        if( status != Response.Status.OK.getStatusCode() )
            return Result.error( getErrorCodeFrom(status));
        else
            return Result.ok( r.readEntity(Post.class));
    }

    public Result<List<String>> getPostAnswers(String postId, long maxTimeout) {
        Response r = executeOperationGet(target.path(postId)
                .path(RestContent.REPLIES)
                .queryParam(RestContent.TIMEOUT, maxTimeout)
                .request()
                .accept(MediaType.APPLICATION_JSON));

        if (r == null){
            return Result.error(  Result.ErrorCode.TIMEOUT );
        }
        int status = r.getStatus();
        if( status != Response.Status.OK.getStatusCode() )
            return Result.error( getErrorCodeFrom(status));
        else
            return Result.ok( r.readEntity(new GenericType<List<String>>(){}));
    }

    public Result<Post> updatePost(String postId, String userPassword, Post post) {
        Response r = executeOperationPut(target.path(postId)
                .queryParam(RestContent.PASSWORD, userPassword)
                .request()
                .accept(MediaType.APPLICATION_JSON), Entity.entity(post, MediaType.APPLICATION_JSON));

        if (r == null){
            return Result.error(  Result.ErrorCode.TIMEOUT );
        }
        int status = r.getStatus();
        if( status != Response.Status.OK.getStatusCode() )
            return Result.error( getErrorCodeFrom(status));
        else
            return Result.ok( r.readEntity(Post.class));
    }

    public Result<Void> deletePost(String postId, String userPassword) {
        Response r = executeOperationDelete(target.path(postId)
                .queryParam(RestContent.PASSWORD, userPassword)
                .request()
                .accept(MediaType.APPLICATION_JSON));

        if (r == null){
            return Result.error(  Result.ErrorCode.TIMEOUT );
        }
        int status = r.getStatus();
        if( status != Response.Status.OK.getStatusCode() )
            return Result.error( getErrorCodeFrom(status));
        else
            return Result.ok(null);
    }

    public Result<Void> upVotePost(String postId, String userId, String userPassword) {
        Response r = executeOperationPost(target.path(postId)
                .queryParam(RestContent.PASSWORD, userPassword)
                .queryParam(RestContent.USERID, userId)
                .request()
                .accept(MediaType.APPLICATION_JSON), Entity.entity(null, MediaType.APPLICATION_JSON));

        if (r == null){
            return Result.error(  Result.ErrorCode.TIMEOUT );
        }
        int status = r.getStatus();
        if( status != Response.Status.OK.getStatusCode() )
            return Result.error( getErrorCodeFrom(status));
        else
            return Result.ok(null);
    }

    public Result<Void> removeUpVotePost(String postId, String userId, String userPassword) {
        Response r = executeOperationDelete(target.path(postId)
                .queryParam(RestContent.PASSWORD, userPassword)
                .queryParam(RestContent.USERID, userId)
                .request()
                .accept(MediaType.APPLICATION_JSON));

        if (r == null){
            return Result.error(  Result.ErrorCode.TIMEOUT );
        }
        int status = r.getStatus();
        if( status != Response.Status.OK.getStatusCode() )
            return Result.error( getErrorCodeFrom(status));
        else
            return Result.ok(null);
    }

    public Result<Void> downVotePost(String postId, String userId, String userPassword) {
        Response r = executeOperationPost(target.path(postId)
                .queryParam(RestContent.PASSWORD, userPassword)
                .queryParam(RestContent.USERID, userId)
                .request()
                .accept(MediaType.APPLICATION_JSON), Entity.entity(null, MediaType.APPLICATION_JSON));

        if (r == null){
            return Result.error(  Result.ErrorCode.TIMEOUT );
        }
        int status = r.getStatus();
        if( status != Response.Status.OK.getStatusCode() )
            return Result.error( getErrorCodeFrom(status));
        else
            return Result.ok(null);
    }

    public Result<Void> removeDownVotePost(String postId, String userId, String userPassword) {
        Response r = executeOperationDelete(target.path(postId)
                .queryParam(RestContent.PASSWORD, userPassword)
                .queryParam(RestContent.USERID, userId)
                .request()
                .accept(MediaType.APPLICATION_JSON));

        if (r == null){
            return Result.error(  Result.ErrorCode.TIMEOUT );
        }
        int status = r.getStatus();
        if( status != Response.Status.OK.getStatusCode() )
            return Result.error( getErrorCodeFrom(status));
        else
            return Result.ok(null);
    }

    public Result<Integer> getupVotes(String postId) {
        Response r = executeOperationGet(target.path(postId)
                .path(RestContent.UPVOTE)
                .request()
                .accept(MediaType.APPLICATION_JSON));

        if (r == null){
            return Result.error(  Result.ErrorCode.TIMEOUT );
        }
        int status = r.getStatus();
        if( status != Response.Status.OK.getStatusCode() )
            return Result.error( getErrorCodeFrom(status));
        else
            return Result.ok( r.readEntity(Integer.class));
    }

    public Result<Integer> getDownVotes(String postId) {
        Response r = executeOperationGet(target.path(postId)
                .path(RestContent.DOWNVOTE)
                .request()
                .accept(MediaType.APPLICATION_JSON));

        if (r == null){
            return Result.error(  Result.ErrorCode.TIMEOUT );
        }
        int status = r.getStatus();
        if( status != Response.Status.OK.getStatusCode() )
            return Result.error( getErrorCodeFrom(status));
        else
            return Result.ok( r.readEntity(Integer.class));
    }




    private Response executeOperationPost(Invocation.Builder req, Entity<?> entity) {
        for (int i = 0; i < MAX_RETRIES; i++) {
            try {
                return req.post(entity);
            } catch (ProcessingException x) {
                Log.info(x.getMessage());
                try {
                    Thread.sleep(RETRY_SLEEP);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            } catch (Exception x) {
                x.printStackTrace();
            }
        }
        return null;
    }

    private Response executeOperationGet(Invocation.Builder req) {
        for (int i = 0; i < MAX_RETRIES; i++) {
            try {
                return req.get();
            } catch (ProcessingException x) {
                Log.info(x.getMessage());
                try {
                    Thread.sleep(RETRY_SLEEP);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            } catch (Exception x) {
                x.printStackTrace();
            }
        }
        return null;
    }

    private Response executeOperationPut(Invocation.Builder req, Entity<?> entity) {
        for (int i = 0; i < MAX_RETRIES; i++) {
            try {
                return req.put(entity);
            } catch (ProcessingException x) {
                Log.info(x.getMessage());
                try {
                    Thread.sleep(RETRY_SLEEP);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            } catch (Exception x) {
                x.printStackTrace();
            }
        }
        return null;
    }

    private Response executeOperationDelete(Invocation.Builder req) {
        for (int i = 0; i < MAX_RETRIES; i++) {
            try {
                return req.delete();
            } catch (ProcessingException x) {
                Log.info(x.getMessage());
                try {
                    Thread.sleep(RETRY_SLEEP);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            } catch (Exception x) {
                x.printStackTrace();
            }
        }
        return null;
    }

    public static Result.ErrorCode getErrorCodeFrom(int status) {
        return switch (status) {
            case 200, 209 -> Result.ErrorCode.OK;
            case 409 -> Result.ErrorCode.CONFLICT;
            case 403 -> Result.ErrorCode.FORBIDDEN;
            case 404 -> Result.ErrorCode.NOT_FOUND;
            case 400 -> Result.ErrorCode.BAD_REQUEST;
            case 500 -> Result.ErrorCode.INTERNAL_ERROR;
            case 501 -> Result.ErrorCode.NOT_IMPLEMENTED;
            default -> Result.ErrorCode.INTERNAL_ERROR;
        };
    }
}
