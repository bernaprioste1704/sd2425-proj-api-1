package fctreddit.api;

/**
 * Represents a Post and a Reply in the system
 */

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;

import java.util.UUID;

@Entity
public class Post {

	@Id
	private String postId;
	private String authorId;
	private long creationTimestamp;
	@Column(length = 2000)
	private String content;
	private String mediaUrl;
	private String parentUrl; //This should be null when this is a top level post.
	private int upVote;
	private int downVote;
	@JsonIgnore
	private int directReplies;


	public Post() {

	}

	public Post(String authorId, String content) {
		this.postId = UUID.randomUUID().toString();
		this.authorId = authorId;
		this.creationTimestamp = System.currentTimeMillis();
		this.content = content;
		this.mediaUrl = null;
		this.parentUrl = null;
		this.upVote = 0;
		this.downVote = 0;

		this.directReplies = 0;
	}

	public Post(String authorId, String content, String parentUrl) {
		this(authorId, content);
		this.parentUrl = parentUrl;
	}

	public Post(String authorId, String content, String parentUrl, String mediaUrl) {
		this(authorId, content, parentUrl);
		this.mediaUrl = mediaUrl;
	}

	public Post(String postId, String authorId, long creationTime, String content, String mediaUrl, String parentUrl, int upVote, int downVote) {
		if (postId == null || postId.equals(""))
			this.postId = UUID.randomUUID().toString();
		else
			this.postId = postId;
		this.authorId = authorId;
		this.creationTimestamp = creationTime;
		this.content = content;
		this.mediaUrl = mediaUrl;
		this.parentUrl = parentUrl;
		this.upVote = upVote;
		this.downVote = downVote;

		this.directReplies = 0;
	}

	public Post(String postId, String authorId, long creationTime, String content, String mediaUrl, String parentUrl, int upVote, int downVote, int directReplies) {
		if (postId == null || postId.equals(""))
			this.postId = UUID.randomUUID().toString();
		else
			this.postId = postId;
		this.authorId = authorId;
		this.creationTimestamp = creationTime;
		this.content = content;
		this.mediaUrl = mediaUrl;
		this.parentUrl = parentUrl;
		this.upVote = upVote;
		this.downVote = downVote;

		this.directReplies = directReplies;
	}


	public String getPostId() {
		return postId;
	}

	public void setPostId(String postId) {
		this.postId = postId;
	}

	public String getAuthorId() {
		return authorId;
	}

	public void setAuthorId(String authorId) {
		this.authorId = authorId;
	}

	public long getCreationTimestamp() {
		return creationTimestamp;
	}

	public void setCreationTimestamp(long creationTimestamp) {
		this.creationTimestamp = creationTimestamp;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getMediaUrl() {
		return mediaUrl;
	}

	public void setMediaUrl(String mediaUrl) {
		this.mediaUrl = mediaUrl;
	}

	public String getParentUrl() {
		return parentUrl;
	}

	public void setParentUrl(String parentUrl) {
		this.parentUrl = parentUrl;
	}

	public int getUpVote() {
		return upVote;
	}

	public void setUpVote(int upVote) {
		this.upVote = upVote;
	}

	public int getDownVote() {
		return downVote;
	}

	public void setDownVote(int downVote) {
		this.downVote = downVote;
	}

	public void increaseDirectReplies() {
		this.directReplies++;
	}

	public void decreaseDirectReplies() {
		this.directReplies--;
	}

	// Only for test purposes
	public void setDirectReplies(int directReplies) {
		this.directReplies = directReplies;
	}

	public int getDirectReplies() {
		return directReplies;
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((postId == null) ? 0 : postId.hashCode());
		result = prime * result + ((authorId == null) ? 0 : authorId.hashCode());
		result = prime * result + ((content == null) ? 0 : content.hashCode());
		result = prime * result + ((parentUrl == null) ? 0 : parentUrl.hashCode());
		return result;
	}
	
	@Override
	public String toString() {
		return "Post [postId=" + postId + ", authorId=" + authorId + ", content=" + content + ", mediaUrl=" + mediaUrl
				+ ", parentUrl=" + parentUrl + ", creationTimestamp=" + creationTimestamp + ", upVote=" + upVote + ", downVote=" + downVote + "]";
	}
}
