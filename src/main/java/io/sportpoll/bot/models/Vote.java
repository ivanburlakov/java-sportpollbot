package io.sportpoll.bot.models;

import java.io.Serializable;
import java.util.UUID;

public class Vote implements Serializable {
    private final String voteId;
    private final long requesterId;
    private final String requesterName;
    private final String voterName;
    private final boolean isAnonymous;

    public Vote(long requesterId, String requesterName, String voterName) {
        this.voteId = UUID.randomUUID().toString();
        this.requesterId = requesterId;
        this.requesterName = requesterName;
        this.voterName = voterName;
        this.isAnonymous = (voterName == null || voterName.trim().isEmpty());
    }

    public Vote(long requesterId, String requesterName) {
        this(requesterId, requesterName, null);
    }

    public String getVoteId() {
        return voteId;
    }

    public long getRequesterId() {
        return requesterId;
    }

    public String getRequesterName() {
        return requesterName;
    }

    public String getVoterName() {
        return voterName;
    }

    public boolean isAnonymous() {
        return isAnonymous;
    }
}
