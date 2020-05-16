package com.example.contact_application.model;


public class ContactModel {

    private String contactNumber, contactName, contactEmail, contactImage;

    public ContactModel(String contactName, String contactNumber, String contactEmail, String contactImage) {
        this.contactNumber = contactNumber;
        this.contactName = contactName;
        this.contactEmail = contactEmail;
        this.contactImage = contactImage;
    }

    public String getContactName() {
        return contactName;
    }

    public String getContactNumber() { return contactNumber; }

    public String getContactEmail() { return contactEmail;}

    public String getContactImage() { return contactImage; }

}