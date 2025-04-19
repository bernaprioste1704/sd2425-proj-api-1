package fctreddit.impl.server.grpc;

import com.google.protobuf.Empty;
import fctreddit.api.User;
import fctreddit.api.java.Content;
import fctreddit.impl.grpc.generated_java.ContentGrpc;
import fctreddit.impl.grpc.generated_java.ContentProtoBuf;
import fctreddit.impl.grpc.generated_java.ContentGrpc;
import fctreddit.impl.grpc.generated_java.ContentProtoBuf;
import fctreddit.impl.grpc.util.DataModelAdaptor;
import fctreddit.impl.server.java.JavaContent;
import io.grpc.BindableService;
import io.grpc.ServerServiceDefinition;
import io.grpc.stub.StreamObserver;
import fctreddit.api.Post;
import fctreddit.api.java.Result;
import fctreddit.api.java.Content;
import fctreddit.impl.grpc.generated_java.ContentProtoBuf.CreatePostArgs;
import fctreddit.impl.grpc.generated_java.ContentProtoBuf.CreatePostResult;
import fctreddit.impl.grpc.generated_java.ContentProtoBuf.GetPostArgs;

import fctreddit.impl.grpc.generated_java.ContentProtoBuf.*;

import fctreddit.impl.grpc.generated_java.ContentProtoBuf.GetPostsArgs;
import fctreddit.impl.grpc.generated_java.ContentProtoBuf.GrpcPost;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class GrpcContentServerStub implements ContentGrpc.AsyncService, BindableService{

    Content impl = new JavaContent();

    @Override
    public final ServerServiceDefinition bindService() {
        return ContentGrpc.bindService(this);
    }

    @Override
    public void createPost(CreatePostArgs request, StreamObserver<CreatePostResult> responseObserver) {
        Result<String> res = impl.createPost( DataModelAdaptor.GrpcPost_to_Post(request.getPost()), request.getPassword());
        if( ! res.isOK() )
            responseObserver.onError(errorCodeToStatus(res.error()));
        else {
            responseObserver.onNext( CreatePostResult.newBuilder().setPostId( res.value() ).build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void getPosts(GetPostsArgs request, StreamObserver<GetPostsResult> responseObserver) {
        Result<List<String>> res = impl.getPosts(request.getTimestamp(), request.getSortOrder());
        if( ! res.isOK() )
            responseObserver.onError(errorCodeToStatus(res.error()));
        else {
            GetPostsResult.Builder resultBuilder = GetPostsResult.newBuilder();

            for (String postId : res.value()) {
                Result<Post> postResult = impl.getPost(postId);
                if (postResult.isOK()) {
                    GrpcPost grpcPost = DataModelAdaptor.Post_to_GrpcPost(postResult.value());
                    resultBuilder.addPostId(grpcPost.getPostId());
                }
            }

            responseObserver.onNext(resultBuilder.build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void getPost(GetPostArgs request, StreamObserver<GrpcPost> responseObserver) {
        Result<Post> res = impl.getPost(request.getPostId());
        if( ! res.isOK() )
            responseObserver.onError(errorCodeToStatus(res.error()));
        else {
            GrpcPost grpcPost = DataModelAdaptor.Post_to_GrpcPost(res.value());
            responseObserver.onNext(grpcPost);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void getPostAnswers(GetPostAnswersArgs request, StreamObserver<GetPostsResult> responseObserver) {
        Result<List<String>> res = impl.getPostAnswers(request.getPostId(), request.getTimeout());
        if (!res.isOK()) {
            responseObserver.onError(errorCodeToStatus(res.error()));
        } else {
            GetPostsResult.Builder resultBuilder = GetPostsResult.newBuilder();
            List<String> postIds = res.value();

            // Executor for parallel fetching
            ExecutorService executor = Executors.newFixedThreadPool(10);  // Adjust pool size based on load
            List<Future<Post>> futures = new ArrayList<>();

            for (String postId : postIds) {
                futures.add(executor.submit(() -> impl.getPost(postId).value()));
            }

            // Collect results
            for (Future<Post> future : futures) {
                try {
                    Post post = future.get();  // Blocks until the post is fetched
                    if (post != null) {
                        GrpcPost grpcPost = DataModelAdaptor.Post_to_GrpcPost(post);
                        resultBuilder.addPostId(grpcPost.getPostId());
                    }
                } catch (InterruptedException | ExecutionException e) {
                    // Handle error
                }
            }

            executor.shutdown();  // Properly shut down the executor
            responseObserver.onNext(resultBuilder.build());
            responseObserver.onCompleted();
        }
    }

    public void updatePost(UpdatePostArgs request, StreamObserver<GrpcPost> responseObserver ) {
        Result<Post> res = impl.updatePost(request.getPostId(), request.getPassword(), DataModelAdaptor.GrpcPost_to_Post(request.getPost()));
        if( ! res.isOK() )
            responseObserver.onError(errorCodeToStatus(res.error()));
        else {
            GrpcPost grpcPost = DataModelAdaptor.Post_to_GrpcPost(res.value());
            responseObserver.onNext(grpcPost);
            responseObserver.onCompleted();
        }
    }

    public void deletePost(DeletePostArgs request, StreamObserver<EmptyMessage> responseObserver) {
        Result<Void> res = impl.deletePost(request.getPostId(), request.getPassword());
        if( ! res.isOK() )
            responseObserver.onError(errorCodeToStatus(res.error()));
        else {
            responseObserver.onNext(EmptyMessage.newBuilder().build());
            responseObserver.onCompleted();
        }
    }

    public void upVotePost(ChangeVoteArgs request, StreamObserver<EmptyMessage> responseObserver) {
        Result<Void> res = impl.upVotePost(request.getPostId(), request.getUserId(), request.getPassword());
        if( ! res.isOK() )
            responseObserver.onError(errorCodeToStatus(res.error()));
        else {
            responseObserver.onNext(EmptyMessage.newBuilder().build());
            responseObserver.onCompleted();
        }
    }

    public void removeUpVotePost(ChangeVoteArgs request, StreamObserver<EmptyMessage> responseObserver) {
        Result<Void> res = impl.removeUpVotePost(request.getPostId(), request.getUserId(), request.getPassword());
        if( ! res.isOK() )
            responseObserver.onError(errorCodeToStatus(res.error()));
        else {
            responseObserver.onNext(EmptyMessage.newBuilder().build());
            responseObserver.onCompleted();
        }
    }

    public void downVotePost(ChangeVoteArgs request, StreamObserver<EmptyMessage> responseObserver) {
        Result<Void> res = impl.downVotePost(request.getPostId(), request.getUserId(), request.getPassword());
        if( ! res.isOK() )
            responseObserver.onError(errorCodeToStatus(res.error()));
        else {
            responseObserver.onNext(EmptyMessage.newBuilder().build());
            responseObserver.onCompleted();
        }
    }

    public void removeDownVotePost(ChangeVoteArgs request, StreamObserver<EmptyMessage> responseObserver) {
        Result<Void> res = impl.removeDownVotePost(request.getPostId(), request.getUserId(), request.getPassword());
        if( ! res.isOK() )
            responseObserver.onError(errorCodeToStatus(res.error()));
        else {
            responseObserver.onNext(EmptyMessage.newBuilder().build());
            responseObserver.onCompleted();
        }
    }

    public void getUpVotes(GetPostArgs request, StreamObserver<VoteCountResult> responseObserver) {
        Result<Integer> res = impl.getupVotes(request.getPostId());
        if( ! res.isOK() )
            responseObserver.onError(errorCodeToStatus(res.error()));
        else {
            VoteCountResult result = VoteCountResult.newBuilder().setCount((res.value())).build();
            responseObserver.onNext(result);
            responseObserver.onCompleted();
        }
    }

    public void getDownVotes(GetPostArgs request, StreamObserver<VoteCountResult> responseObserver) {
        Result<Integer> res = impl.getDownVotes(request.getPostId());
        if( ! res.isOK() )
            responseObserver.onError(errorCodeToStatus(res.error()));
        else {
            VoteCountResult result = VoteCountResult.newBuilder().setCount((res.value())).build();
            responseObserver.onNext(result);
            responseObserver.onCompleted();
        }
    }

    protected static Throwable errorCodeToStatus( Result.ErrorCode error ) {
        var status =  switch( error) {
            case NOT_FOUND -> io.grpc.Status.NOT_FOUND;
            case CONFLICT -> io.grpc.Status.ALREADY_EXISTS;
            case FORBIDDEN -> io.grpc.Status.PERMISSION_DENIED;
            case NOT_IMPLEMENTED -> io.grpc.Status.UNIMPLEMENTED;
            case BAD_REQUEST -> io.grpc.Status.INVALID_ARGUMENT;
            default -> io.grpc.Status.INTERNAL;
        };

        return status.asException();
    }
}
