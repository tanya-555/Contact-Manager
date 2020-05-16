package com.example.contact_application.controller;

import android.Manifest;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.example.contact_application.R;
import com.example.contact_application.adapter.ContactAdapter;
import com.example.contact_application.model.ContactModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;

import static android.app.Activity.RESULT_OK;
import static com.example.contact_application.adapter.ContactAdapter.getContactID;

public class ContactListController extends Controller {
    private static final String TAG = ContactListController.class.getSimpleName();

    public static final String CONTACT_ID = "contact_id";
    private RecyclerView contact_recyclerview;
    private static ArrayList<ContactModel> arrayList;
    public ContactAdapter adapter;
    private ProgressBar progressBar;
    private FloatingActionButton add_contact;
    private boolean result = false;
    View view;
    Toast toast = null;
    byte[] contactImage;
    String contact_id;
    Long raw_contact_id;

    private static final int REQUEST_CODE = 1000;
    private static final int RESULT_LOAD_IMG = 1111;


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



    public void launchController() {
        getRouter().pushController(RouterTransaction.with(new AddContactController()));

    }


    ContactAdapter.ContactActionListener listener = new ContactAdapter.ContactActionListener() {
        @Override
        public void onUpdate(int itemPosition) {

            raw_contact_id = getContactID(getApplicationContext().getContentResolver(), arrayList.get(itemPosition).getContactNumber());
            contact_id = String.valueOf(raw_contact_id);
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
                adapter.notifyDataSetChanged();

            } else {
                toast = Toast.makeText(getApplicationContext(), "Unable to delete contact!", Toast.LENGTH_LONG);
                toast.show();
            }
        }

        @Override
        public void onUploadImage(int itemPosition) {
            raw_contact_id = getContactID(getApplicationContext().getContentResolver(), arrayList.get(itemPosition).getContactNumber());
            contact_id = String.valueOf(raw_contact_id);
            uploadImage(getApplicationContext().getContentResolver(), arrayList.get(itemPosition).getContactNumber(), adapter, itemPosition);
            if(result == true) {
                toast = Toast.makeText(getApplicationContext(), "Image uploaded successfully", Toast.LENGTH_LONG);
                toast.show();
            } else {
                toast = Toast.makeText(getApplicationContext(), "Failed to upload image! Try again later", Toast.LENGTH_LONG);
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

    public void uploadImage(ContentResolver contactHelper, String number, ContactAdapter adapter, int item_position) {
        raw_contact_id = getContactID(contactHelper, number);
        contact_id = String.valueOf(raw_contact_id);
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, RESULT_LOAD_IMG);
    }

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);


        if (resultCode == RESULT_OK && reqCode == RESULT_LOAD_IMG) {
            try {
                result = true;
                Toast.makeText(getApplicationContext(), "successfully uploaded", Toast.LENGTH_LONG);
                final Uri imageUri = data.getData();
                final InputStream imageStream = getApplicationContext().getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                contactImage = toByteArray(selectedImage);
                //Long contactId = Long.parseLong(contact_id);
                updateContactImage(raw_contact_id,contactImage);
                //image_view.setImageBitmap(selectedImage);


            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_LONG).show();
            }

        }else {
            Toast.makeText(getApplicationContext(), "You haven't picked Image",Toast.LENGTH_LONG).show();
        }
    }


    public void updateContactImage(Long rawContactId, byte[] photo) {

/*
        Uri rawContactPhotoUri = Uri.withAppendedPath(
                ContentUris.withAppendedId(ContactsContract.RawContacts.CONTENT_URI, rawContactId),
                ContactsContract.RawContacts.DisplayPhoto.CONTENT_DIRECTORY);
        try {
            AssetFileDescriptor fd = getApplicationContext().getContentResolver().openAssetFileDescriptor(rawContactPhotoUri, "w");
            OutputStream os = fd.createOutputStream();
            os.write(photo);
            os.close();
            fd.close();
            adapter.notifyDataSetChanged();
        } catch (IOException e) {
            // Handle error cases.
        }
        */

         // getApplicationContext().getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);

        //ContentResolver resolver = getActivity().getContentResolver();
        /*
            int photoRow = -1;
            String where = ContactsContract.Data.RAW_CONTACT_ID + " = " + rawContactId
                    + " AND " + ContactsContract.Data.MIMETYPE + " =='" + ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE + "'";
            Cursor cursor = resolver.query(ContactsContract.Data.CONTENT_URI, null, where, null, null);
            int idIdx = cursor.getColumnIndexOrThrow(ContactsContract.Data._ID);
            if (cursor.moveToFirst()) {
                photoRow = cursor.getInt(idIdx);
            }
            cursor.close();

        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        ops.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                .withSelection(ContactsContract.Data._ID + " = ?", new String[] {Integer.toString(photoRow)})
                .withValue(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
                .withValue(ContactsContract.Data.IS_SUPER_PRIMARY, 1)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.Data.DATA15, photo)
                .build());

 */

        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        String selectPhoto = ContactsContract.Data.CONTACT_ID + "=?" ;
            String[] photoArgs = new String[]{contact_id};
            ops.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                    .withSelection(selectPhoto, photoArgs)
                    .withValue(ContactsContract.CommonDataKinds.Photo.DATA15, photo)
                    .build());


        try {
            getApplicationContext().getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (RemoteException e) {

        } catch (OperationApplicationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }


    //function to convert bitmap image to byte array
    public byte[] toByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
        return stream.toByteArray();
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                arrayList = readContacts();
            }
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
                    String contactImage = "";

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
                                Uri contactImageUri = Uri.withAppendedPath(ContentUris.
                                        withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId),
                                        ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
                                contactImage = contactImageUri.toString();
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
