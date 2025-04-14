package fctreddit.clients.rest;

import java.net.URI;
import java.util.List;
import java.util.logging.Logger;

import fctreddit.api.User;
import fctreddit.api.java.Result;
import fctreddit.api.rest.RestUsers;
import fctreddit.clients.java.UsersClient;
import jakarta.ws.rs.client.*;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.GenericType;

import fctreddit.api.java.Result.ErrorCode;

public class RestUsersClient extends UsersClient {
    private static Logger Log = Logger.getLogger(RestUsersClient.class.getName());

    final URI serverURI;
    final Client client;
    final ClientConfig config;

    final WebTarget target;
    protected static final int MAX_RETRIES = 10;
    protected static final int RETRY_SLEEP = 5000;

    public RestUsersClient( URI serverURI ) {
        this.serverURI = serverURI;

        this.config = new ClientConfig();

        config.property( ClientProperties.READ_TIMEOUT, READ_TIMEOUT);
        config.property( ClientProperties.CONNECT_TIMEOUT, CONNECT_TIMEOUT);


        this.client = ClientBuilder.newClient(config);

        target = client.target( serverURI ).path( RestUsers.PATH );
    }

    public Result<String> createUser(User user) {

        Response r = executeOperationPost(target.request()
                     .accept( MediaType.APPLICATION_JSON), Entity.entity(user, MediaType.APPLICATION_JSON));
        if (r == null){
            return Result.error(  ErrorCode.TIMEOUT );
        }
        int status = r.getStatus();
        if( status != Status.OK.getStatusCode() )
            return Result.error( getErrorCodeFrom(status));
        else
            return Result.ok( r.readEntity( String.class ));


    }

    public Result<User> getUser(String userId, String pwd) {
        Response r = executeOperationGet(target.path( userId )
                .queryParam(RestUsers.PASSWORD, pwd).request()
                .accept(MediaType.APPLICATION_JSON));
        if (r == null){
            return Result.error(  ErrorCode.TIMEOUT );
        }
        int status = r.getStatus();
        if( status != Status.OK.getStatusCode() )
            return Result.error( getErrorCodeFrom(status));
        else
            return Result.ok( r.readEntity( User.class ));
    }
    /*
    public Result<User> getUser(String userId, String pwd) {
        Response r = target.path( userId )
                .queryParam(RestUsers.PASSWORD, pwd).request()
                .accept(MediaType.APPLICATION_JSON)
                .get();

        int status = r.getStatus();
        if( status != Status.OK.getStatusCode() )
            return Result.error( getErrorCodeFrom(status));
        else
            return Result.ok( r.readEntity( User.class ));
    }*/


    public Result<User> updateUser(String userId, String oldPassword, User user) {
        Response r = executeOperationPut(target.path(userId)
                .queryParam(RestUsers.PASSWORD, oldPassword).request()
                .accept(MediaType.APPLICATION_JSON), Entity.entity(user, MediaType.APPLICATION_JSON));
        if (r == null){
            return Result.error(  ErrorCode.TIMEOUT );
        }
        int status = r.getStatus();
        if (status != Status.OK.getStatusCode())
            return Result.error(getErrorCodeFrom(status));
        else
            return Result.ok(r.readEntity(User.class));
    }

    public Result<User> deleteUser(String userId, String password) {
        Response r = executeOperationDelete(target.path(userId)
                .queryParam(RestUsers.PASSWORD, password).request()
                .accept(MediaType.APPLICATION_JSON));
        if (r == null){
            return Result.error(  ErrorCode.TIMEOUT );
        }
        int status = r.getStatus();
        if (status != Status.OK.getStatusCode())
            return Result.error(getErrorCodeFrom(status));
        else
            return Result.ok(r.readEntity(User.class));

    }


    public Result<List<User>> searchUsers(String pattern) {
        Response r = target.queryParam(RestUsers.QUERY, pattern).request()
                .accept(MediaType.APPLICATION_JSON)
                .get();
        int status = r.getStatus();
        if (status != Status.OK.getStatusCode())
            return Result.error(getErrorCodeFrom(status));
        else
            return Result.ok(r.readEntity(new GenericType<List<User>>() {} ));
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

    public static ErrorCode getErrorCodeFrom(int status) {
        return switch (status) {
            case 200, 209 -> ErrorCode.OK;
            case 409 -> ErrorCode.CONFLICT;
            case 403 -> ErrorCode.FORBIDDEN;
            case 404 -> ErrorCode.NOT_FOUND;
            case 400 -> ErrorCode.BAD_REQUEST;
            case 500 -> ErrorCode.INTERNAL_ERROR;
            case 501 -> ErrorCode.NOT_IMPLEMENTED;
            default -> ErrorCode.INTERNAL_ERROR;
        };
    }
}

