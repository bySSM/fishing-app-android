package com.example.fishingapp.model;

import java.util.List;

public class CommentsResponse {

    private List<Comment> comments;

    private long count;


    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }


    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }
}