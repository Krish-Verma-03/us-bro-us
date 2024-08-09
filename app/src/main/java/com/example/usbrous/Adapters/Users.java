package com.example.usbrous.Adapters;

public class Users {

    String userName,profilePic,mail,password,status,userId;

    Users(){}

    public Users(String id, String username, String email, String password, String imgurlfordb, String status) {
        this.userId = id;
        this.userName = username;
        this.mail = email;
        this.password = password;
        this.profilePic = imgurlfordb;
        this.status = status;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

}
