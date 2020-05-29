package com.example.contact_application.controller;

import android.Manifest;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.OperationApplicationException;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.RouterTransaction;
import com.example.contact_application.R;
import com.example.contact_application.adapter.ContactAdapter;
import com.example.contact_application.model.ContactModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;

import static com.example.contact_application.adapter.ContactAdapter.getContactID;

public class ContactListController extends Controller {
    private static final String TAG = ContactListController.class.getSimpleName();

    private RecyclerView contact_recyclerview;
    private static ArrayList<ContactModel> arrayList;
    private ContactAdapter adapter;
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
        contact_recyclerview = view.findViewById(R.id.contact_recyclerview);
        progressBar = view.findViewById(R.id.progressbar);
        add_contact = view.findViewById(R.id.add_contact);
        new LoadContacts().execute();

        add_contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchController();
            }
        });
        return view;
    }


    private void launchController() {
        getRouter().pushController(RouterTransaction.with(new AddContactController()));

    }


    ContactAdapter.ContactActionListener listener = new ContactAdapter.ContactActionListener() {

        @Override
        public void onDelete(int itemPosition) {
            boolean result = deleteContact(getApplicationContext().getContentResolver(), arrayList.get(itemPosition).getContactNumber(), adapter, itemPosition);
            if (result == true) {
                toast = Toast.makeText(getApplicationContext(), "Contact deleted successfully", Toast.LENGTH_LONG);
                toast.show();
                adapter.notifyDataSetChanged();

            } else {
                toast = Toast.makeText(getApplicationContext(), "Unable to delete contact!", Toast.LENGTH_LONG);
                toast.show();
            }
        }

    };

    public boolean deleteContact(ContentResolver contactHelper, String number, ContactAdapter adapter, int item_position) {
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        String[] args = new String[]{String.valueOf(getContactID(contactHelper, number))};
        ops.add(ContentProviderOperation.newDelete(ContactsContract.RawContacts.CONTENT_URI).withSelection(ContactsContract.RawContacts.CONTACT_ID + "=?", args).build());
        try {
            contactHelper.applyBatch(ContactsContract.AUTHORITY, ops);
            arrayList.remove(item_position);
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
            while (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_CONTACTS}, REQUEST_CODE);
            }
            while (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_CONTACTS) == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_CONTACTS}, REQUEST_CODE);
            }
            while (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CODE);
            }
            arrayList = readContacts();
            return null;
        }

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            super.onPreExecute();
        }

        @Override
        public void onPostExecute(Void result) {

            if (arrayList != null && arrayList.size() > 0) {
                String msg = String.format("%d contacts fetched", arrayList.size());
                Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
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

    private ArrayList<ContactModel> readContacts() {
        ArrayList<ContactModel> contactList = new ArrayList<>();
        Uri uri = ContactsContract.Contacts.CONTENT_URI;
        Cursor contactsCursor = getActivity().getContentResolver().query(uri, null,
                null, null, ContactsContract.Contacts.DISPLAY_NAME + " ASC ");
        try {
            if (contactsCursor.moveToFirst()) {
                do {
                    long contactId = contactsCursor.getLong(contactsCursor.getColumnIndex("_ID"));
                    Uri dataUri = ContactsContract.Data.CONTENT_URI;
                    Cursor dataCursor = getActivity().getContentResolver().query(dataUri, null, ContactsContract.Data.CONTACT_ID + " = " + contactId, null, null);

                    String displayName = "";
                    String contactNumber = "";
                    String contactEmail = "";
                    byte[] contactImage = new byte[100];

                    if (dataCursor.moveToFirst()) {
                        displayName = dataCursor.getString(dataCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                        do {
                            if (dataCursor.getString(dataCursor.getColumnIndex("mimetype")).
                                    equals(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)) {
                                contactNumber = dataCursor.getString(dataCursor.getColumnIndex("data1"));
                            }

                            if (dataCursor.getString(dataCursor.getColumnIndex("mimetype")).
                                    equals(ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)) {
                                contactEmail = dataCursor.getString(dataCursor.getColumnIndex("data1"));
                            }

                            if (dataCursor.getString(dataCursor.getColumnIndex("mimetype")).
                                    equals(ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)) {
                                contactImage = dataCursor.getBlob(dataCursor.getColumnIndex("data15"));

                            }
                        } while (dataCursor.moveToNext());
                        dataCursor.close();
                        contactList.add(new ContactModel(displayName, contactNumber, contactEmail, contactImage));
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
