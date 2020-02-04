package com.example.contact_application;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.OperationApplicationException;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.bluelinelabs.conductor.Controller;

import java.util.ArrayList;

public class UpdateContactController extends Controller {

    private EditText mobile_phone, work_phone, home_phone, work_email, home_email, company_name, title;
    private Button update_contact;
    private View view;
    private String contact_id;

    public UpdateContactController(Bundle args) {
        //super(args);
        contact_id = args.getString(ContactListController.CONTACT_ID);
    }

    @NonNull
    @Override
    protected View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {

        view = inflater.inflate(R.layout.update_contact_controller, container, false);
        mobile_phone = (EditText) view.findViewById(R.id.new_mobile_phone);
        work_phone = (EditText) view.findViewById(R.id.new_work_phone);
        home_phone = (EditText) view.findViewById(R.id.new_home_phone);
        work_email = (EditText) view.findViewById(R.id.new_work_email);
        home_email = (EditText) view.findViewById(R.id.new_home_email);
        company_name = (EditText) view.findViewById(R.id.new_company_name);
        title = (EditText) view.findViewById(R.id.new_title);

        update_contact = (Button) view.findViewById(R.id.update_contact);
        update_contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateContact();
            }
        });
        return view;
    }

      private void updateContact()  {

              String[] phoneArgs;
              ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
              String selectPhone = ContactsContract.Data.CONTACT_ID + "=? AND " + ContactsContract.CommonDataKinds.Phone.TYPE + "=?" ;
              if(!("").equals(mobile_phone.getText().toString())) {
                  phoneArgs = new String[]{contact_id, String.valueOf(ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)};
                  ops.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                          .withSelection(selectPhone, phoneArgs)
                          .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, mobile_phone.getText().toString())
                          .build());
              }
              if(!("").equals(home_phone.getText().toString())) {
                  phoneArgs = new String[]{contact_id, String.valueOf(ContactsContract.CommonDataKinds.Phone.TYPE_HOME)};
                  ops.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                          .withSelection(selectPhone, phoneArgs)
                          .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, home_phone.getText().toString())
                          .build());
              }

                  /*
                  phoneArgs = new String[]{contact_id, String.valueOf(ContactsContract.CommonDataKinds.Phone.TYPE_HOME)};
                  ops.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                          .withSelection(selectPhone, phoneArgs)
                          .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, home_phone.getText().toString())
                          .build());

                   if(String.valueOf(ContactsContract.CommonDataKinds.Phone.TYPE_HOME)== null) {

                      phoneArgs = new String[]{contact_id, String.valueOf(ContactsContract.CommonDataKinds.Phone.TYPE_HOME)};
                      ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                              //.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                              .withSelection(selectPhone, phoneArgs)
                              .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                              .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, home_phone.getText().toString())
                              .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_HOME).build());
                  } else {
                      phoneArgs = new String[]{contact_id, String.valueOf(ContactsContract.CommonDataKinds.Phone.TYPE_HOME)};
                      ops.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                              .withSelection(selectPhone, phoneArgs)
                              .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, home_phone.getText().toString())
                              .build());
                  }

                   */
              if(!("").equals(work_phone.getText().toString())) {
                  phoneArgs = new String[]{contact_id, String.valueOf(ContactsContract.CommonDataKinds.Phone.TYPE_WORK)};
                  ops.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                          .withSelection(selectPhone, phoneArgs)
                          .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, work_phone.getText().toString())
                          .build());
              }

              String[] emailArgs;
              String selectEmail = ContactsContract.Data.CONTACT_ID + "=? AND "+ ContactsContract.Data.MIMETYPE + "='" + ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE + "'" + " AND " + ContactsContract.CommonDataKinds.Email.TYPE + "=?";

              if(!("").equals(work_email.getText().toString())) {

                  emailArgs = new String[]{contact_id, String.valueOf(ContactsContract.CommonDataKinds.Email.TYPE_WORK)};
                  ops.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                          .withSelection(selectEmail, emailArgs)
                          .withValue(ContactsContract.CommonDataKinds.Email.DATA, work_email.getText().toString())
                          .build());
              }

              if(!("").equals(home_email.getText().toString())) {
                  emailArgs = new String[]{contact_id, String.valueOf(ContactsContract.CommonDataKinds.Email.TYPE_HOME)};
                  ops.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                          .withSelection(selectEmail, emailArgs)
                          .withValue(ContactsContract.CommonDataKinds.Email.DATA, home_email.getText().toString())
                          .build());
              }

              String[] companyArgs;
              String selectCompanyDetails = ContactsContract.Data.CONTACT_ID + "=? AND " + ContactsContract.Data.MIMETYPE + "='"  + ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE + "'" + " AND " + ContactsContract.CommonDataKinds.Organization.TYPE + "=?";

              if(!("").equals(company_name.getText().toString())) {
                  companyArgs = new String[]{contact_id, String.valueOf(ContactsContract.CommonDataKinds.Organization.COMPANY)};
                  ops.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                          .withSelection(selectCompanyDetails, companyArgs)
                          .withValue(ContactsContract.CommonDataKinds.Organization.DATA, company_name.getText().toString())
                          .build());
              }
              if(!("").equals(title.getText().toString())) {
                  companyArgs = new String[]{contact_id, String.valueOf(ContactsContract.CommonDataKinds.Organization.TITLE)};
                  ops.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                          .withSelection(selectCompanyDetails, companyArgs)
                          .withValue(ContactsContract.CommonDataKinds.Organization.DATA, title.getText().toString())
                          .build());
              }

          try {
              getApplicationContext().getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
              Toast.makeText(getApplicationContext(),"Contact updated successfully", Toast.LENGTH_LONG).show();
          } catch (Exception e) {
              e.printStackTrace();
              Toast.makeText(getApplicationContext(), "Exception: " + e.getMessage(), Toast.LENGTH_SHORT).show();
          }


      }
}
