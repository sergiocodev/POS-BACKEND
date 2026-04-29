package com.sergiocodev.app.repository.projection;

public interface UserProjection {
    Long getId();
    String getUsername();
    String getFullName();
    String getEmail();
    String getProfilePicture();
}