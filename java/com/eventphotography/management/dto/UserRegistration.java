package com.eventphotography.management.dto;

public class UserRegistration {
    private String name;
    private String email;
    private String password;
    private String contactNo;
    private String address;
    private String userType;

    // Constructors
    public UserRegistration() {}

    public UserRegistration(String name, String email, String password, String contactNo, String address, String userType) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.contactNo = contactNo;
        this.address = address;
        this.userType = userType;
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getContactNo() { return contactNo; }
    public void setContactNo(String contactNo) { this.contactNo = contactNo; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }
}