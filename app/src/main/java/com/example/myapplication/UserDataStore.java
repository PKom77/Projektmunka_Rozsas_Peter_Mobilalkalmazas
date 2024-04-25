package com.example.myapplication;

public class UserDataStore {
    private String nev;
    private String email;
    private String city;

    public UserDataStore() {}

    public UserDataStore(String nev, String email, String city) {
        this.nev = nev;
        this.email = email;
        this.city = city;
    }

    public String getNev() {
        return nev;
    }

    public void setNev(String nev) {
        this.nev = nev;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
}
