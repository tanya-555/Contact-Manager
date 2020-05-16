package com.example.contact_application.controller;

import android.content.ContentProviderOperation;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.bluelinelabs.conductor.Controller;
import com.example.contact_application.R;

import java.util.ArrayList;

public class UpdateContactController extends Controller {

    private EditText contactName, contactNumber, contactEmail;
    private ImageView contactImage;
    private Button updateContact;
    private View view;
    private String contactId;
    private String iContactName;
    private String iContactNumber;
    private String iContactEmail;
    private byte[] iContactImage;

    public UpdateContactController(Bundle args) {
        super(args);
        contactId = args.getString(ContactListController.CONTACT_ID);
        iContactName = args.getString(ContactListController.CONTACT_NAME);
        iContactNumber = args.getString(ContactListController.CONTACT_NUMBER);
        iContactEmail = args.getString(ContactListController.CONTACT_EMAIL);
        iContactImage = args.getByteArray(ContactListController.CONTACT_IMAGE);
    }

    @NonNull
    @Override
    protected View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {

        view = inflater.inflate(R.layout.update_contact_controller, container, false);
        contactName = view.findViewById(R.id.tv_name);
        contactNumber = view.findViewById(R.id.tv_number);
        contactEmail = view.findViewById(R.id.tv_email);
        contactImage = view.findViewById(R.id.iv_photo);
        updateContact = view.findViewById(R.id.update_btn);
        loadInitialData();
        updateContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateContact();
            }
        });
        return view;
    }

    private void loadInitialData() {
        contactNumber.setText(iContactNumber);
        contactName.setText(iContactName);
        contactEmail.setText(iContactEmail);
        Bitmap bitmap = BitmapFactory.decodeByteArray(iContactImage, 0, iContactImage.length);
        contactImage.setImageBitmap(bitmap);
    }

    private void updateContact()  {

        String[] phoneArgs;
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        String selectPhone = ContactsContract.Data.CONTACT_ID + "=? AND " + ContactsContract.CommonDataKinds.Phone.TYPE + "=?" ;
        if(!("").equals(contactNumber.getText().toString())) {
            phoneArgs = new String[]{contactId, String.valueOf(ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)};
            ops.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                    .withSelection(selectPhone, phoneArgs)
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, contactNumber.getText().toString())
                    .build());
        }

        String[] emailArgs;
        String selectEmail = ContactsContract.Data.CONTACT_ID + "=? AND "+ ContactsContract.Data.MIMETYPE + "='"
                + ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE + "'" + " AND " +
                ContactsContract.CommonDataKinds.Email.TYPE + "=?";

        if(!("").equals(contactEmail.getText().toString())) {

            emailArgs = new String[]{contactId, String.valueOf(ContactsContract.
                    CommonDataKinds.Email.TYPE_WORK)};
            ops.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                    .withSelection(selectEmail, emailArgs)
                    .withValue(ContactsContract.CommonDataKinds.Email.DATA,
                            contactEmail.getText().toString())
                    .build());
        }

        try {
            getApplicationContext().getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
            Toast.makeText(getApplicationContext(),"Contact updated successfully", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Exception: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        getRouter().popController(this);
      }
}
