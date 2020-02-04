package com.example.contact_application;

import android.Manifest;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bluelinelabs.conductor.Controller;

import com.bluelinelabs.conductor.RouterTransaction;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import static com.example.contact_application.ContactAdapter.getContactID;

public class ContactListController extends Controller {
    private static final String TAG = ContactListController.class.getSimpleName();

    public static final String CONTACT_ID = "contact_id";
    private RecyclerView contact_recyclerview;
    private static ArrayList<ContactModel> arrayList;
    public ContactAdapter adapter;
    private ProgressBar progressBar;
    private FloatingActionButton add_contact;
    View view;
    Toast toast = null;

    private static final int REQUEST_CODE = 1000;


    public ContactListController() {

    }

    @NonNull
    @Override
    protected View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
        view = inflater.inflate(R.layout.main_controller, container, false);
        contact_recyclerview = (RecyclerView) view.findViewById(R.id.contact_recyclerview);
        progressBar = (ProgressBar) view.findViewById(R.id.progressbar);
        add_contact = (FloatingActionButton) view.findViewById(R.id.add_contact);
        new LoadContacts().execute();

        add_contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runThis(v);
            }
        });
        return view;
    }



    public void runThis(View v) {
        getRouter().pushController(RouterTransaction.with(new AddContactController()));

    }


    ContactAdapter.ContactActionListener listener = new ContactAdapter.ContactActionListener() {
        @Override
        public void onUpdate(int itemPosition) {

            String contact_id = String.valueOf(getContactID(getApplicationContext().getContentResolver(), arrayList.get(itemPosition).getContactNumber()));
            Bundle args = new Bundle();
            args.putString(CONTACT_ID, contact_id);
            getRouter().pushController(RouterTransaction.with(new UpdateContactController(args)));
        }

        @Override
        public void onDelete(int itemPosition) {
            boolean result = deleteContact(getApplicationContext().getContentResolver(),  arrayList.get(itemPosition).getContactNumber(), adapter, itemPosition);
            if (result == true) {
                toast = Toast.makeText(getApplicationContext(), "Contact deleted successfully", Toast.LENGTH_LONG);
                toast.show();

            } else {
                toast = Toast.makeText(getApplicationContext(), "Unable to delete contact!", Toast.LENGTH_LONG);
                toast.show();
            }
        }
    };

    public boolean deleteContact(ContentResolver contactHelper, String number, ContactAdapter adapter, int item_position) {
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        String[] args = new String[] { String.valueOf(getContactID(contactHelper, number))};
        ops.add(ContentProviderOperation.newDelete(ContactsContract.RawContacts.CONTENT_URI).withSelection(ContactsContract.RawContacts.CONTACT_ID + "=?", args).build());
        try {
            contactHelper.applyBatch(ContactsContract.AUTHORITY, ops);
            //this.notifyDataSetChanged();
            adapter.notifyItemRemoved(item_position);
            return true;
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }
        return false;
    }


    //Nested class

    class LoadContacts extends AsyncTask<Void, Integer, Void> {


        @Override
        protected Void doInBackground(Void... params) {
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_CONTACTS}, REQUEST_CODE);
            } else {
                arrayList = readContacts();
            }
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_CONTACTS) == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_CONTACTS}, REQUEST_CODE);
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            super.onPreExecute();
        }

        @Override
        public void onPostExecute(Void result) {
            //super.onPostExecute(result);
            if (arrayList != null && arrayList.size() > 0) {

                ((AppCompatActivity)getActivity()).getSupportActionBar().setSubtitle(arrayList.size() + " Contacts");
                adapter = null;
                if (adapter == null) {
                    adapter = new ContactAdapter(getActivity(), arrayList, listener);
                    LinearLayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
                    contact_recyclerview.setLayoutManager(mLayoutManager);
                    contact_recyclerview.setAdapter(adapter);
                }
                adapter.notifyDataSetChanged();
            } else {
                Log.d(TAG, "");
                Toast.makeText(getActivity(), "There are no contacts.", Toast.LENGTH_LONG).show();
            }
            progressBar.setVisibility(View.GONE);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                new LoadContacts().execute();
            }
        }
    }


    private ArrayList<ContactModel> readContacts() {
        ArrayList<ContactModel> contactList = new ArrayList<ContactModel>();
        Uri uri = ContactsContract.Contacts.CONTENT_URI;
        Cursor contactsCursor = getActivity().getContentResolver().query(uri, null, null, null, ContactsContract.Contacts.DISPLAY_NAME + " ASC ");
        try {
            if (contactsCursor.moveToFirst()) {
                do {
                    long contactId = contactsCursor.getLong(contactsCursor.getColumnIndex("_ID"));
                    Uri dataUri = ContactsContract.Data.CONTENT_URI;
                    Cursor dataCursor = getActivity().getContentResolver().query(dataUri, null, ContactsContract.Data.CONTACT_ID + " = " + contactId, null, null);

                    String displayName = "";
                    String contactNumber = "";
                    String contactEmail = "";
                    String contactOtherDetails = "";
                    String homePhone;
                    String workPhone;
                    String mobilePhone;
                    String homeEmail;
                    String workEmail;
                    String companyName;
                    String title;
                    String contactImage = "";

                    if (dataCursor.moveToFirst()) {
                        displayName = dataCursor.getString(dataCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                        do {
                            if (dataCursor.getString(dataCursor.getColumnIndex("mimetype")).equals(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)) {
                                switch (dataCursor.getInt(dataCursor.getColumnIndex("data2"))) {
                                    case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
                                        homePhone = dataCursor.getString(dataCursor.getColumnIndex("data1"));
                                        contactNumber += homePhone + "   ";
                                        break;

                                    case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
                                        workPhone = dataCursor.getString(dataCursor.getColumnIndex("data1"));
                                        contactNumber += workPhone + "   ";
                                        break;

                                    case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
                                        mobilePhone = dataCursor.getString(dataCursor.getColumnIndex("data1"));
                                        contactNumber += mobilePhone + "   ";
                                        break;

                                }
                            }

                            if (dataCursor.getString(dataCursor.getColumnIndex("mimetype")).equals(ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)) {

                                switch (dataCursor.getInt(dataCursor.getColumnIndex("data2"))) {
                                    case ContactsContract.CommonDataKinds.Email.TYPE_HOME:
                                        homeEmail = dataCursor.getString(dataCursor.getColumnIndex("data1"));
                                        contactEmail += homeEmail + "   ";
                                        break;
                                    case ContactsContract.CommonDataKinds.Email.TYPE_WORK:
                                        workEmail = dataCursor.getString(dataCursor.getColumnIndex("data1"));
                                        contactEmail += workEmail + "   ";
                                        break;

                                }
                            }

                            if (dataCursor.getString(dataCursor.getColumnIndex("mimetype")).equals(ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE)) {
                                companyName = dataCursor.getString(dataCursor.getColumnIndex("data1"));
                                contactOtherDetails += "Company Name : " + companyName + "   ";
                                title = dataCursor.getString(dataCursor.getColumnIndex("data4"));
                                contactOtherDetails += "Title : " + title + "   ";
                            }

                            if (dataCursor.getString(dataCursor.getColumnIndex("mimetype")).equals(ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)) {
                                Uri contactImageUri = Uri.withAppendedPath(ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId), ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
                                contactImage = contactImageUri.toString();
                            }

                        } while (dataCursor.moveToNext());
                        dataCursor.close();
                        contactList.add(new ContactModel(displayName, contactNumber, contactEmail, contactOtherDetails, contactImage));
                    }
                } while (contactsCursor.moveToNext());
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        contactsCursor.close();
        return contactList;
    }



}
