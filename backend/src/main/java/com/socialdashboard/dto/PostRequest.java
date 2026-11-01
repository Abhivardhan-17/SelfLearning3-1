package com.socialdashboard.dto;
import jakarta.validation.constraints.NotBlank;

public class PostRequest {
    @NotBlank private String caption;
    private String imageUrl;
    public String getCaption() { return caption; }
    public void setCaption(String caption) { this.caption = caption; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
