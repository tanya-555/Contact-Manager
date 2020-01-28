package com.example.contact_application;


import android.net.Uri;

public class ContactModel {

    private String contactNumber, contactName, contactEmail, contactOtherDetails, contactImage;

    public ContactModel(String contactName, String contactNumber, String contactEmail, String contactOtherDetails, String contactImage) {
        this.contactNumber = contactNumber;
        this.contactName = contactName;
        this.contactEmail = contactEmail;
        this.contactOtherDetails = contactOtherDetails;
        this.contactImage = contactImage;
    }

    public String getContactName() {
        return contactName;
    }

    public String getContactNumber() { return contactNumber; }

    public String getContactEmail() { return contactEmail;}

    public String getContactOtherDetails() { return contactOtherDetails;}

    public String getContactImage() { return contactImage; }

}