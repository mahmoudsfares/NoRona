package com.example.no_rona.models;

import java.util.List;

public class User {

    private String authUid;
    private String email;
    private String password;
    private String name;
    private String idno;
    private String address;
    private String mobile;
    private List<Boolean> answers;
    private double score;
    private int result;

    /*
    This constructor and getAuthUid() method are only for saving SharedPreference
    for automatic login in case the user didn't sign out from the last login
    */
    public User(String authUid, String email, String password){
        this.authUid = authUid;
        this.email = email;
        this.password = password;
    }
    public String getAuthUid() {
        return authUid;
    }

    public User(){}

    public User(String email, String password, String name, String idno, String address, String mobile) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.idno = idno;
        this.address = address;
        this.mobile = mobile;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public String getIdno() {
        return idno;
    }

    public String getAddress() {
        return address;
    }

    public String getMobile() {
        return mobile;
    }

    public List<Boolean> getAnswers() {
        return answers;
    }

    public void setAnswers(List<Boolean> answers) {
        this.answers = answers;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }
}