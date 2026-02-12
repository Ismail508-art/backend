package com.example.backend.dto;

public class LoginResponse {

    private Long id;
    private String name;
    private String email;
    private String phone;
    private boolean isAdmin;
    private String token;

    public LoginResponse(Long id,
                         String name,
                         String email,
                         String phone,
                         boolean isAdmin,
                         String token) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.isAdmin = isAdmin;
        this.token = token;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public boolean isAdmin() { return isAdmin; }
    public String getToken() { return token; }
}


