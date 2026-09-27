package com.example.marketplace.dto;

import com.example.marketplace.entity.User;

public class UserInfoResponce {
    private Long id;
    private String email;

    public UserInfoResponce() {
    }

    public UserInfoResponce(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
