package fctreddit.api;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class VoteId  {

    private String userId;
    private String postId;

    public VoteId() {}

    public VoteId(String userId, String postId) {
        this.userId = userId;
        this.postId = postId;
    }

    // Getters and setters if needed

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VoteId)) return false;
        VoteId voteId = (VoteId) o;
        return Objects.equals(userId, voteId.userId) &&
                Objects.equals(postId, voteId.postId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, postId);
    }

}
