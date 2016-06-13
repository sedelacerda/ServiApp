package com.jasp.serviapp;

/**
 * Created by Seba on 6/7/2016.
 */
public class User {
    private String birthDate;
    private String firstName;
    private String lastName;
    private String mobilePhone;
    private String workPhone;
    private String email;
    private String password;
    private String facebookID;
    private String gender;

    public User() { }

    //region Set methods
    public void setBirthDate(String newBirthDate) {
        birthDate = newBirthDate;
    }
    public void setFirstName(String newFirstName) {
        firstName = newFirstName;
    }
    public void setLastName(String newLastName) {
        lastName = newLastName;
    }
    public void setMobilePhone(String newMobilePhone) {
        mobilePhone = newMobilePhone;
    }
    public void setWorkPhone(String newWorkPhone) {
        workPhone = newWorkPhone;
    }
    public void setEmail(String newEmail) {
        email = newEmail;
    }
    public void setPassword(String newPassword) {
        password = newPassword;
    }
    public void setFacebookID(String newFacebookID) {
        facebookID = newFacebookID;
    }
    public void setGender(String newGender) {
        gender = newGender;
    }
    //endregion

    //region Get methods
    public String getBirthDate() {
        return birthDate;
    }
    public String getFirstName() {
        return firstName;
    }
    public String getLastName() {
        return lastName;
    }
    public String getMobilePhone() {
        return mobilePhone;
    }
    public String getWorkPhone() {
        return workPhone;
    }
    public String getEmail() {
        return email;
    }
    public String getPassword() {
        return password;
    }
    public String getFacebookID() {
        return facebookID;
    }
    public String getGender() {
        return gender;
    }
    //endregion
}
