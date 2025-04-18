package fctreddit.impl.server.java;

import fctreddit.api.Post;

import fctreddit.api.User;
import fctreddit.api.Vote;
import fctreddit.api.java.Content;
import fctreddit.api.java.Result;
import fctreddit.api.java.Users;
import fctreddit.clients.GetContentClient;
import fctreddit.clients.GetUsersClient;
import fctreddit.impl.server.persistence.Hibernate;
import jakarta.persistence.EntityExistsException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class JavaContent implements Content {

    private static Logger Log = Logger.getLogger(JavaContent.class.getName());

    private Hibernate hibernate;

    private Users globalClient;

    private final Object createPostLock = new Object();

    public final static boolean UPVOTE = true;
    public final static boolean DOWNVOTE = false;

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
            authenticateUser(post.getAuthorId(), userPassword);

            if (post.getPostId() == null)
                post.setPostId(UUID.randomUUID().toString());

            hibernate.persist(post);

            String parentUrl = post.getParentUrl();

            if (parentUrl != null) {
                changeParentReplies(true, parentUrl);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.info("Post already exists.");
            return Result.error(Result.ErrorCode.CONFLICT);
        }

        synchronized (createPostLock) {
            createPostLock.notifyAll();  // Notify all threads waiting on this postId lock
        }

        return Result.ok(post.getPostId());
    }

    private Result<Post> internalUpdatePost(String postId, Post post) {
        Post existingPost = null;
        try {
            existingPost = hibernate.get(Post.class, postId);

            if (existingPost == null) {
                Log.info("Post does not exist.");
                return Result.error(Result.ErrorCode.NOT_FOUND);
            }

            if (post.getContent() != null) {
                existingPost.setContent(post.getContent());
            }
            if (post.getMediaUrl() != null) {
                existingPost.setMediaUrl(post.getMediaUrl());
            }
            if (post.getDirectReplies() != -1) {
                existingPost.setDirectReplies(post.getDirectReplies());
            }
            hibernate.update(existingPost);

        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(Result.ErrorCode.INTERNAL_ERROR);
        }
        return Result.ok(existingPost);
    }
    private void changeParentReplies(boolean addOrRemove, String parentUrl) {
        Log.info("Changing Parent Replies");
        String[] parentUrlArray = parentUrl.split("/");
        String parentId = parentUrlArray[parentUrlArray.length-1];
        Post parentPost = getPost(parentId).value();
        if (addOrRemove)
            parentPost.increaseDirectReplies();
        else
            parentPost.decreaseDirectReplies();
        Post updatedPost = new Post(null, null,
                -1, null,
                null, null,
                -1, -1,
                parentPost.getDirectReplies()+1) ;
        Log.info("Parent current replies: " + parentPost.getDirectReplies());
        try {
            internalUpdatePost(parentId, updatedPost);
        } catch (Exception e) {
            e.printStackTrace();
            Log.info("Post does not exist.");
        }
    }
    @Override
    public Result<List<String>> getPosts(long timestamp, String sortOrder) {
        Log.info("getPosts : timestamp = " + timestamp + "; sortOrder = " + sortOrder);
        List<String> postList =  null;


        try {
            String jpql_statement = null;
            if (sortOrder == null || sortOrder == "") {
                jpql_statement = "SELECT p.id FROM Post p WHERE p.parentUrl IS NULL AND p.creationTimestamp >= " + timestamp;
            } else if (sortOrder.equals(MOST_UP_VOTES)) {
                jpql_statement = "SELECT p.id FROM Post p WHERE p.parentUrl IS NULL AND p.creationTimestamp >= " + timestamp +
                        " ORDER BY p.upVote DESC";
            } else if (sortOrder.equals(MOST_REPLIES)) {
                jpql_statement = "SELECT p.id FROM Post p WHERE p.parentUrl IS NULL AND p.creationTimestamp >= " + timestamp +
                        " ORDER BY p.directReplies DESC";
            }

            postList = hibernate.jpql(jpql_statement, String.class);
        } catch (Exception e) {
            e.printStackTrace();
            Log.info("Error");
        }
        return Result.ok(postList);
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
        Log.info("getPostAnswers : postId = " + postId);

        if (postId == null) {
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        }
        if (maxTimeout > 0) {
            synchronized (createPostLock) {
                try {
                    createPostLock.wait(maxTimeout); // Waits until notified or timeout
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return Result.error(Result.ErrorCode.INTERNAL_ERROR);
                }
            }
        }

        // After wait: always run the query once
        String jpqlQuery = "FROM Post p WHERE p.parentUrl LIKE '%" + postId + "'";
        List<Post> initialAnswers = hibernate.jpql(jpqlQuery, Post.class);
        initialAnswers.sort(Comparator.comparingLong(Post::getCreationTimestamp));
        List<String> ids = initialAnswers.stream()
                .map(Post::getPostId)
                .collect(Collectors.toList());

        return Result.ok(ids);
    }









    @Override
    public Result<Post> updatePost(String postId, String userPassword, Post post) {
        Log.info("updatePost : postId = " + postId + "; userPassword = " + userPassword + "; post = " + post);
        if (postId == null || post == null) {
            Log.info("postId or post is null.");
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        }
        Post existingPost = null;
        try {
            existingPost = hibernate.get(Post.class, postId);

            if (existingPost == null) {
                Log.info("Post does not exist.");
                return Result.error(Result.ErrorCode.NOT_FOUND);
            } else if (existingPost.getDirectReplies() != 0 ||
                    existingPost.getDownVote() != 0 || existingPost.getUpVote() != 0) {
                Log.info("Post has replies or votes.");
                return Result.error(Result.ErrorCode.CONFLICT);
            }

            authenticateUser(post.getAuthorId(), userPassword);

            if (post.getContent() != null) {
                existingPost.setContent(post.getContent());
            }
            if (post.getMediaUrl() != null) {
                existingPost.setMediaUrl(post.getMediaUrl());
            }
            hibernate.update(existingPost);

        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(Result.ErrorCode.INTERNAL_ERROR);
        }
        return Result.ok(existingPost);
    }
    @Override
    public Result<Void> deletePost(String postId, String userPassword) {
        Log.info("deletePost : postId = " + postId + "; userPassword = " + userPassword);

        Post post = getPost(postId).value();
        String userId = post.getAuthorId();
        try {
            authenticateUser(userId, userPassword);
            if (post.getDirectReplies() != 0 ||
                    post.getDownVote() != 0 || post.getUpVote() != 0) {
                Log.info("Post has replies or votes.");
                return Result.error(Result.ErrorCode.CONFLICT);
            }

            hibernate.delete(post);
        } catch (Exception e) {
            e.printStackTrace();
            Log.info("Post does not exist.");
            return Result.error(Result.ErrorCode.NOT_FOUND);
        }
        return Result.ok();
    }

    @Override
    public Result<Void> upVotePost(String postId, String userId, String userPassword) {
        Log.info("upVotePost : postId = " + postId + "; userId = " + userId);
        return votePost(postId, userId, userPassword, UPVOTE);
    }

    private Result<Void> votePost(String postId, String userId, String userPassword, boolean vote) {
        try {
            authenticateUser(userId, userPassword);

            Vote newVote = new Vote(userId, postId, vote);
            if (hibernate.get(Vote.class, newVote.getId()) != null)
                throw new EntityExistsException("Vote already exists");
            hibernate.persist(newVote);

            addVoteCount(postId, vote);

        } catch (Exception e) {
            e.printStackTrace();
            Log.info("Vote already exists.");
            return Result.error(Result.ErrorCode.CONFLICT);
        }

        return Result.ok();
    }

    private Result<Post> addVoteCount(String postId, boolean vote) {
        try {
            GetContentClient contentClient = new GetContentClient();
            Content cl = contentClient.getClient();

            Post post = cl.getPost(postId).value();
            if (vote)
                post.setUpVote(post.getUpVote() + 1);
            else
                post.setDownVote(post.getDownVote() + 1);

            hibernate.update(post);
            return Result.ok(post);


            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
        }

    }

    @Override
    public Result<Void> removeUpVotePost(String postId, String userId, String userPassword) {
        Log.info("removeUpVotePost : postId = " + postId + "; userId = " + userId);
        try {
            authenticateUser(userId, userPassword);

            Vote vote = new Vote(userId, postId, UPVOTE);
            hibernate.delete(vote);

            Post post = hibernate.get(Post.class, postId);
            if (post == null) {
                Log.info("Post does not exist.");
                return Result.error(Result.ErrorCode.NOT_FOUND);
            }
            post.setUpVote(post.getUpVote() - 1);
            hibernate.update(post);

        } catch (Exception e) {
            e.printStackTrace();
            Log.info("Vote does not exist.");
            return Result.error(Result.ErrorCode.NOT_FOUND);
        }
        return Result.ok();
    }

    @Override
    public Result<Void> downVotePost(String postId, String userId, String userPassword) {
        Log.info("downVotePost : postId = " + postId + "; userId = " + userId);
        return votePost(postId, userId, userPassword, DOWNVOTE);
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
        Log.info("getupVotes : postId = " + postId);
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

        return Result.ok(post.getUpVote());
    }

    @Override
    public Result<Integer> getDownVotes(String postId)
    {
        Log.info("getDownVotes : postId = " + postId);
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

        return Result.ok(post.getDownVote());
    }

    private Result<User> authenticateUser(String userId, String password) throws IOException, InterruptedException {
        if (globalClient == null)
            globalClient = new GetUsersClient().getClient();

        return globalClient.getUser(userId, password);
    }
}
