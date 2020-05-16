package com.example.contact_application.model;


public class ContactModel {

    private String contactNumber, contactName, contactEmail;
    private byte[] contactImage;

    public ContactModel(String contactName, String contactNumber, String contactEmail, byte[] contactImage) {
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

    public byte[] getContactImage() { return contactImage; }

}