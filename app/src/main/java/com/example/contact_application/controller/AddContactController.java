package com.example.contact_application.controller;

import android.content.ContentProviderOperation;
import android.content.Intent;
import android.net.Uri;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bluelinelabs.conductor.Controller;
import com.example.contact_application.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;

public class AddContactController extends Controller {

    private EditText contactName, contactNumber, contactEmail;
    private Button addContact;
    private ImageView contactImage;
    private View view;

    @NonNull
    @Override
    protected View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
        view = inflater.inflate(R.layout.add_contact_controller, container, false);
        contactName = view.findViewById(R.id.tv_name);
        contactNumber = view.findViewById(R.id.tv_number);
        contactEmail = view.findViewById(R.id.tv_email);
        contactImage = view.findViewById(R.id.iv_photo);
        addContact = view.findViewById(R.id.add_btn);
        addContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewContact();
            }
        });
        initSelectImageListener();
        return view;
    }

    private void initSelectImageListener() {
        contactImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickPhoto , 1);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Uri selectedImage = data.getData();
            Picasso.with(getActivity()).load(selectedImage).into(contactImage);
        }
    }

    private void addNewContact() {
        ArrayList <ContentProviderOperation> ops = new ArrayList< ContentProviderOperation >();

        ops.add(ContentProviderOperation.newInsert(
                ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build());

        if (contactName.getText().toString() != null && !("").equals(contactName.getText().toString())) {
            ops.add(ContentProviderOperation.newInsert(
                    ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.
                            StructuredName.DISPLAY_NAME, contactName.getText().toString()).build());
        } else {
            contactName.setError("This field cannot be empty!");
            contactName.requestFocus();
            return;
        }

        if (contactNumber.getText().toString() != null && !("").equals(contactNumber.getText().toString())) {
            if(checkNumberValidity(contactNumber.getText().toString())) {
                ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, contactNumber.getText().toString())
                        .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
                                ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE).build());
            } else {
                return;
            }
        } else {
            contactNumber.setError("This field cannot be empty!");
            contactNumber.requestFocus();
            return;
        }

        if (contactEmail.getText().toString() != null && !("").equals(contactEmail.getText().toString())) {
            ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Email.DATA, contactEmail.getText().toString())
                    .withValue(ContactsContract.CommonDataKinds.Email.TYPE,
                            ContactsContract.CommonDataKinds.Email.TYPE_WORK).build());
        }

        try {
            getApplicationContext().getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
            Toast.makeText(getApplicationContext(),"Contact added successfully", Toast.LENGTH_LONG).show();
            getRouter().popController(this);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Exception: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkNumberValidity(String contactNumber) {
        if(contactNumber.length() > 10) {
            Toast.makeText(getActivity(), "Invalid Mobile Number!", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

}
