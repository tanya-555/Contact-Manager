package com.example.contact_application;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {
    private Context context;
    private ArrayList<ContactModel> arrayList;
    public int item_position;


    ContactAdapter(Context context, ArrayList<ContactModel> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener{
        TextView contactName, contactNumber, contactEmail, contactOtherDetails;
        ImageView contactImage, expandImage;

        public ViewHolder(View itemView) {
            super(itemView);
            contactName = (TextView) itemView.findViewById(R.id.contactName);
            contactNumber = (TextView) itemView.findViewById(R.id.contactNumber);
            contactEmail = (TextView) itemView.findViewById(R.id.contactEmail);
            contactOtherDetails = (TextView) itemView.findViewById(R.id.contactOtherDetails);
            contactImage = (ImageView) itemView.findViewById(R.id.contactImage);
            expandImage = (ImageView) itemView.findViewById(R.id.expand);


            itemView.setOnCreateContextMenuListener(this);


        }


        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

            menu.setHeaderTitle("Select The Action");
            menu.add(0, 1, 0, "Delete Contact");//groupId, itemId, order, title
            menu.add(0, 2, 0, "Update Contact");

        }


        @Override
        public boolean onContextItemSelected(MenuItem item) {
            Toast toast = null;
            switch(item.getItemId()) {
                case 1:
                    //toast = Toast.makeText(context, "Selected Option1 ", Toast.LENGTH_LONG);
                    //toast.show();
                    boolean result = deleteContact(context, arrayList.get(item_position).getContactNumber(), arrayList.get(item_position).getContactName());
                    if(result == true) {
                        toast = Toast.makeText(context, "Contact deleted successfully", Toast.LENGTH_LONG);
                        toast.show();
                    } else {
                        toast = Toast.makeText(context, "Unable to delete contact!", Toast.LENGTH_LONG);
                        toast.show();
                    }
                    break;
                case 2:
                    break;
            }
            return true;

        }


    }

    @Override
    public int getItemCount() {

        return arrayList.size();
    }


    @Override
    public long getItemId(int position) {

        return position;
    }

    public static boolean deleteContact(Context ctx, String phone, String name) {
        Uri contactUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phone));
        Cursor cur = ctx.getContentResolver().query(contactUri, null, null, null, null);
        try {
            if (cur.moveToFirst()) {
                do {
                    if (cur.getString(cur.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)).equalsIgnoreCase(name)) {
                        String lookupKey = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
                        Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey);
                        ctx.getContentResolver().delete(uri, null, null);
                        return true;
                    }

                } while (cur.moveToNext());
            }

        } catch (Exception e) {
            System.out.println(e.getStackTrace());
        }
        return false;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.custom_view, parent, false);
        ViewHolder contactViewHolder = new ViewHolder(view);
        return contactViewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        ContactModel model = arrayList.get(position);

        holder.expandImage.setImageResource(R.drawable.ic_playlist_add_black_24dp);

        holder.expandImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                v.showContextMenu();
                item_position = holder.getAdapterPosition();

            }
        });

        if (!model.getContactName().equals("") && model.getContactName() != null) {
            holder.contactName.setText(model.getContactName());
        } else {
            holder.contactName.setText(R.string.no_name);
        }

        if (!model.getContactNumber().equals("") && model.getContactNumber() != null) {
            holder.contactNumber.setText(model.getContactNumber());
        } else {
            holder.contactNumber.setText(context.getString(R.string.NO_CONTACT_NO));
        }

        if (!model.getContactEmail().equals("") && model.getContactEmail() != null) {
            holder.contactEmail.setText(model.getContactEmail());
        } else {
            holder.contactEmail.setText(context.getString(R.string.NO_CONTACT_EMAIL));
        }

        if (!model.getContactOtherDetails().equals("") && model.getContactOtherDetails() != null) {
            holder.contactOtherDetails.setText(model.getContactOtherDetails());
        } else {
            holder.contactOtherDetails.setText(context.getString(R.string.NO_CONTACT_OTHER_DETAILS));
        }

        if (!model.getContactImage().equals("") && model.getContactImage() != null) {
            Glide.with(context).load(Uri.parse(model.getContactImage())).apply(new RequestOptions().override(120, 120)).into(holder.contactImage);
        } else {
            Glide.with(context).load(R.drawable.ic_person_black_24dp).apply(new RequestOptions().override(120, 120)).into(holder.contactImage);
        }

    }
}
