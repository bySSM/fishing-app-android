// app/src/main/java/com/example/fishingapp/model/User.java
package com.example.fishingapp.model;

public class User {
    private Long id;
    private String username;
    private String email;
    private String createdAt;
    private Integer rating;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
}