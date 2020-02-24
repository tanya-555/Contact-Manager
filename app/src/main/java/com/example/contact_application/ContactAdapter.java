package com.example.contact_application;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {
    private Context context;
    private ArrayList<ContactModel> arrayList;
    public int item_position;
    ContactActionListener actionListener;


    ContactAdapter(Context context, ArrayList<ContactModel> arrayList, ContactActionListener actionListener) {
        this.context = context;
        this.actionListener = actionListener;
        this.arrayList = arrayList;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
        TextView contactName, contactNumber, contactEmail, contactOtherDetails;
        ImageView contactImage, expandImage;

        public ViewHolder(View itemView) {
            super(itemView);
            contactName = itemView.findViewById(R.id.contactName);
            contactNumber = itemView.findViewById(R.id.contactNumber);
            contactEmail = itemView.findViewById(R.id.contactEmail);
            contactOtherDetails = itemView.findViewById(R.id.contactOtherDetails);
            contactImage = itemView.findViewById(R.id.contactImage);
            expandImage = itemView.findViewById(R.id.expand);


            itemView.setOnCreateContextMenuListener(this);
        }


        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.setHeaderTitle("Select The Action");
            MenuItem delete = menu.add(Menu.NONE, 1, 1, "Delete Contact");//groupId, itemId, order, title
            MenuItem edit = menu.add(Menu.NONE, 2, 2, "Update Contact");
            MenuItem upload = menu.add(Menu.NONE, 3, 3, "Upload Image");
            edit.setOnMenuItemClickListener(onEditMenu);
            delete.setOnMenuItemClickListener(onEditMenu);
            upload.setOnMenuItemClickListener(onEditMenu);
        }

        private final MenuItem.OnMenuItemClickListener onEditMenu = new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                switch (item.getItemId()) {
                    case 1:
                        actionListener.onDelete(item_position);
                        break;
                    case 2:
                        actionListener.onUpdate(item_position);
                        break;
                    case 3:
                        actionListener.onUploadImage(item_position);
                }


                return true;
            }
        };


    }

    @Override
    public int getItemCount() {

        return arrayList.size();
    }


    @Override
    public long getItemId(int position) {

        return position;
    }


    public static long getContactID(ContentResolver contactHelper,String number) {
        Uri contactUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
        String[] projection = { ContactsContract.PhoneLookup._ID };
        Cursor cursor = null;
        try {
            cursor = contactHelper.query(contactUri, projection, null, null,null);
            if (cursor.moveToFirst()) {
                int personID = cursor.getColumnIndex(ContactsContract.PhoneLookup._ID);
                return cursor.getLong(personID);
            }
            return -1;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return -1;
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

            if (!("").equals(model.getContactName()) && model.getContactName() != null) {
                holder.contactName.setText(model.getContactName());
            } else {
                holder.contactName.setText(R.string.no_name);
            }

            if (!("").equals(model.getContactNumber()) && model.getContactNumber() != null) {
                holder.contactNumber.setText(model.getContactNumber());
            } else {
                holder.contactNumber.setText(context.getString(R.string.NO_CONTACT_NO));
            }

            if (!("").equals(model.getContactEmail()) && model.getContactEmail() != null) {
                holder.contactEmail.setText(model.getContactEmail());
            } else {
                holder.contactEmail.setText(context.getString(R.string.NO_CONTACT_EMAIL));
            }

            if (!("").equals(model.getContactOtherDetails()) && model.getContactOtherDetails() != null) {
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

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position, List<Object> payloads) {
        if(payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads);
        } else {
            Bundle bundle = (Bundle) payloads.get(0);
            if (bundle.size() != 0) {
                String image = bundle.getString("image");
                if (image != "") {
                    Glide.with(holder.itemView.getContext())
                            .asDrawable()
                            .load(image)
                            //.apply(options)
                            .into(holder.contactImage);
                }

            }
        }

    }

    public interface ContactActionListener{
        void onUpdate(int itemPosition);
        void onDelete(int itemPosition);
        void onUploadImage(int itemPosition);
    }
}
