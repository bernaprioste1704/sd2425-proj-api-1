package fctreddit.impl.grpc.util;


import fctreddit.api.User;
import fctreddit.api.Post;
import fctreddit.impl.grpc.generated_java.UsersProtoBuf.GrpcUser;
import fctreddit.impl.grpc.generated_java.UsersProtoBuf.GrpcUser.Builder;

import fctreddit.impl.grpc.generated_java.ContentProtoBuf.GrpcPost;
//import fctreddit.impl.grpc.generated_java.ContentProtoBuf.GrpcPost.Builder;

public class DataModelAdaptor {

    //Notice that optional values in a Message might not have an
    //assigned value (although for Strings default value is "") so
    //before assigning we check if the field has a value, if not
    //we assign null.
    public static User GrpcUser_to_User( GrpcUser from )  {
        return new User(
                from.hasUserId() ? from.getUserId() : null,
                from.hasFullName() ? from.getFullName() : null,
                from.hasEmail() ? from.getEmail() : null,
                from.hasPassword() ? from.getPassword() : null,
                from.hasAvatarUrl() ? from.getAvatarUrl() : null);
    }

    //Notice that optional values might not have a value, and 
    //you should never assign null to a field in a Message
    public static GrpcUser User_to_GrpcUser( User from )  {
        GrpcUser.Builder b = GrpcUser.newBuilder();

        if(from.getUserId() != null)
            b.setUserId( from.getUserId());

        if(from.getPassword() != null)
            b.setPassword( from.getPassword());

        if(from.getEmail() != null)
            b.setEmail( from.getEmail());

        if(from.getFullName() != null)
            b.setFullName( from.getFullName());

        if(from.getAvatarUrl() != null)
            b.setAvatarUrl( from.getAvatarUrl());

        return b.build();
    }


    public static Post GrpcPost_to_Post(GrpcPost from) {
        String postId = from.hasPostId() ? from.getPostId() : null;
        String authorId = from.hasAuthorId() ? from.getAuthorId() : null;
        long creationTimestamp = from.hasCreationTimestamp() ? from.getCreationTimestamp() : -1;
        String content = from.hasContent() ? from.getContent() : null;
        String mediaUrl = from.hasMediaUrl() ? from.getMediaUrl() : null;
        String parentUrl = from.hasParentUrl() ? from.getParentUrl() : null;
        int upVote = from.hasUpVote() ? from.getUpVote() : -1;
        int downVote = from.hasDownVote() ? from.getDownVote() : -1;
        //int directReplies = from.hasDirectReplies() ? from.getDirectReplies() : -1;

        return new Post(postId, authorId, creationTimestamp, content, mediaUrl, parentUrl, upVote, downVote);
    }


    public static GrpcPost Post_to_GrpcPost (Post from) {
        GrpcPost.Builder b = GrpcPost.newBuilder();

        if(from.getPostId() != null)
            b.setPostId(from.getPostId());

        if(from.getAuthorId() != null)
            b.setAuthorId(from.getAuthorId());

        if(from.getCreationTimestamp() != -1)
            b.setCreationTimestamp(from.getCreationTimestamp());

        if(from.getContent() != null)
            b.setContent(from.getContent());

        if(from.getMediaUrl() != null)
            b.setMediaUrl(from.getMediaUrl());

        if(from.getParentUrl() != null)
            b.setParentUrl(from.getParentUrl());

        if(from.getUpVote() != -1)
            b.setUpVote(from.getUpVote());

        if(from.getDownVote() != -1)
            b.setDownVote(from.getDownVote());

        //if(from.getDirectReplies() != -1)
         //   b.setDirectReplies(from.getDirectReplies());

        return b.build();
    }

}

