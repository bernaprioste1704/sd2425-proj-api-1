package fctreddit.clients.grpc;

import fctreddit.api.Post;
import fctreddit.api.java.Result;
import fctreddit.clients.java.ContentClient;
import fctreddit.impl.grpc.generated_java.ContentGrpc;
import fctreddit.impl.grpc.generated_java.UsersGrpc;
import fctreddit.impl.grpc.util.DataModelAdaptor;
import io.grpc.*;
import io.grpc.internal.PickFirstLoadBalancerProvider;

import fctreddit.impl.grpc.generated_java.ContentProtoBuf.*;


import java.net.URI;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class GrpcContentClient extends ContentClient {

    static {
        LoadBalancerRegistry.getDefaultRegistry().register(new PickFirstLoadBalancerProvider());
    }

    final ContentGrpc.ContentBlockingStub stub;


    public GrpcContentClient(URI serverURI) {
        Channel channel = ManagedChannelBuilder.forAddress(serverURI.getHost(), serverURI.getPort()).usePlaintext().build();
        stub = ContentGrpc.newBlockingStub(channel).withDeadlineAfter(READ_TIMEOUT, TimeUnit.MILLISECONDS);
    }

    @Override
    public Result<String> createPost(Post post, String userPassword) {
        try {
            CreatePostResult rest = stub.createPost(CreatePostArgs.newBuilder()
                    .setPost(DataModelAdaptor.Post_to_GrpcPost(post))
                    .build());
            return Result.ok(rest.getPostId());
        } catch (StatusRuntimeException sre) {
            return Result.error(statusToErrorCode(sre.getStatus()));
        }
    }

    @Override
    public Result<List<String>> getPosts(long timestamp, String sortOrder) {
        try {
            GetPostsResult rest = stub.getPosts(GetPostsArgs.newBuilder()
                    .setTimestamp(timestamp)
                    .setSortOrder(sortOrder)
                    .build());
            return Result.ok(rest.getPostIdList());
        } catch (StatusRuntimeException sre) {
            return Result.error(statusToErrorCode(sre.getStatus()));
        }
    }

    @Override
    public Result<Post> getPost (String postId) {
        try {
            GrpcPost rest = stub.getPost(GetPostArgs.newBuilder()
                    .setPostId(postId)
                    .build());
            return Result.ok(DataModelAdaptor.GrpcPost_to_Post(rest));
        } catch (StatusRuntimeException sre) {
            return Result.error(statusToErrorCode(sre.getStatus()));
        }
    }

    @Override
    public Result<List<String>> getPostAnswers(String postId, long maxTimeout) {
        try {
            GetPostsResult rest = stub.getPostAnswers(GetPostAnswersArgs.newBuilder()
                    .setPostId(postId)
                    .setTimeout(maxTimeout)
                    .build());
            return Result.ok(rest.getPostIdList());
        } catch (StatusRuntimeException sre) {
            return Result.error(statusToErrorCode(sre.getStatus()));
        }
    }

    @Override
    public Result<Post> updatePost(String postId, String userPassword, Post post) {
        try {
            GrpcPost rest = stub.updatePost(UpdatePostArgs.newBuilder()
                    .setPostId(postId)
                    .setPassword(userPassword)
                    .setPost(DataModelAdaptor.Post_to_GrpcPost(post))
                    .build());
            return Result.ok(DataModelAdaptor.GrpcPost_to_Post(rest));
        } catch (StatusRuntimeException sre) {
            return Result.error(statusToErrorCode(sre.getStatus()));
        }
    }

    @Override
    public Result<Void> deletePost (String postId, String userPassword) {
        try {
            EmptyMessage rest = stub.deletePost(DeletePostArgs.newBuilder()
                    .setPostId(postId)
                    .setPassword(userPassword)
                    .build());
            return Result.ok();
        } catch (StatusRuntimeException sre) {
            return Result.error(statusToErrorCode(sre.getStatus()));
        }
    }

    @Override
    public Result<Void> upVotePost(String postId, String userId, String userPassword) {
        try {
            EmptyMessage rest = stub.upVotePost(ChangeVoteArgs.newBuilder()
                    .setPostId(postId)
                    .setUserId(userId)
                    .setPassword(userPassword)
                    .build());
            return Result.ok();
        } catch (StatusRuntimeException sre) {
            return Result.error(statusToErrorCode(sre.getStatus()));
        }
    }

    @Override
    public Result<Void> removeUpVotePost(String postId, String userId, String userPassword) {
        try {
            EmptyMessage rest = stub.removeUpVotePost(ChangeVoteArgs.newBuilder()
                    .setPostId(postId)
                    .setUserId(userId)
                    .setPassword(userPassword)
                    .build());
            return Result.ok();
        } catch (StatusRuntimeException sre) {
            return Result.error(statusToErrorCode(sre.getStatus()));
        }
    }

    @Override
    public Result<Void> downVotePost(String postId, String userId, String userPassword) {
        try {
            EmptyMessage rest = stub.downVotePost(ChangeVoteArgs.newBuilder()
                    .setPostId(postId)
                    .setUserId(userId)
                    .setPassword(userPassword)
                    .build());
            return Result.ok();
        } catch (StatusRuntimeException sre) {
            return Result.error(statusToErrorCode(sre.getStatus()));
        }
    }

    @Override
    public Result<Void> removeDownVotePost(String postId, String userId, String userPassword) {
        try {
            EmptyMessage rest = stub.removeDownVotePost(ChangeVoteArgs.newBuilder()
                    .setPostId(postId)
                    .setUserId(userId)
                    .setPassword(userPassword)
                    .build());
            return Result.ok();
        } catch (StatusRuntimeException sre) {
            return Result.error(statusToErrorCode(sre.getStatus()));
        }
    }

    @Override
    public Result<Integer> getupVotes(String postId) {
        try {
            VoteCountResult rest = stub.getUpVotes(GetPostArgs.newBuilder()
                    .setPostId(postId)
                    .build());
            return Result.ok(rest.getCount());
        } catch (StatusRuntimeException sre) {
            return Result.error(statusToErrorCode(sre.getStatus()));
        }
    }

    @Override
    public Result<Integer> getDownVotes(String postId) {
        try {
            VoteCountResult rest = stub.getDownVotes(GetPostArgs.newBuilder()
                    .setPostId(postId)
                    .build());
            return Result.ok(rest.getCount());
        } catch (StatusRuntimeException sre) {
            return Result.error(statusToErrorCode(sre.getStatus()));
        }
    }


    static Result.ErrorCode statusToErrorCode(Status status) {
        return switch (status.getCode()) {
            case OK -> Result.ErrorCode.OK;
            case NOT_FOUND -> Result.ErrorCode.NOT_FOUND;
            case ALREADY_EXISTS -> Result.ErrorCode.CONFLICT;
            case PERMISSION_DENIED -> Result.ErrorCode.FORBIDDEN;
            case INVALID_ARGUMENT -> Result.ErrorCode.BAD_REQUEST;
            case UNIMPLEMENTED -> Result.ErrorCode.NOT_IMPLEMENTED;
            default -> Result.ErrorCode.INTERNAL_ERROR;
        };
    }
}

