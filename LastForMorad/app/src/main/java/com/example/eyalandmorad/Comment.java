package com.example.eyalandmorad;

public class Comment {
    private String createdUser;
    private String commentText;
    private String commentEventId;
    private String commentId;

    public Comment(String createdUser, String commentText, String commentEventId) {
        this.createdUser = createdUser;
        this.commentText = commentText;
        this.commentEventId = commentEventId;
        this.commentId = "";
    }

    public Comment() {}


    public String getCommentText() {
        return commentText;
    }
    public String getCommentId(){ return commentId; }
    public String getCommentEventId(){ return commentEventId; }
    public String getCreatedUser() { return createdUser; }
    public void setId(String commentId) { this.commentId = commentId; }
}