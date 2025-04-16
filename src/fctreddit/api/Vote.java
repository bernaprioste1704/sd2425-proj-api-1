package fctreddit.api;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;

@Entity
public class Vote {

    @EmbeddedId
    private VoteId id;

    private boolean voteValue;


    public Vote() {}

    public Vote(String userId, String postId, boolean voteValue) {
        this.id = new VoteId(userId, postId);
        this.voteValue = voteValue;
    }


    public VoteId getId() {
        return id;
    }

    public boolean getVoteValue() {
        return voteValue;
    }




}

