package com.example.contact_application;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;

public class ContactAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<ContactModel> arrayList;

    ContactAdapter(Context context, ArrayList<ContactModel> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }
    private class ViewHolder {
        TextView contactName, contactNumber, contactEmail, contactOtherDetails;
        ImageView contactImage;
    }

    @Override
    public int getCount() {

        return arrayList.size();
    }

    @Override
    public ContactModel getItem(int position) {

        return arrayList.get(position);
    }

    @Override
    public long getItemId(int position) {

        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ContactModel model = arrayList.get(position);
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.custom_view, parent, false);
            holder = new ViewHolder();
            holder.contactName = (TextView) convertView.findViewById(R.id.contactName);
            holder.contactNumber = (TextView) convertView.findViewById(R.id.contactNumber);
            holder.contactEmail = (TextView) convertView.findViewById(R.id.contactEmail);
            holder.contactOtherDetails = (TextView) convertView.findViewById(R.id.contactOtherDetails);
            holder.contactImage = (ImageView) convertView.findViewById(R.id.contactImage);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (!model.getContactName().equals("") && model.getContactName() != null) {
            holder.contactName.setText(model.getContactName());
        } else {
            holder.contactName.setText(R.string.no_name);
        }

        if (!model.getContactNumber().equals("") && model.getContactNumber() != null) {
            holder.contactNumber.setText(model.getContactNumber());
        }  else {
            holder.contactNumber.setText(context.getString(R.string.NO_CONTACT_NO));
        }

        if (!model.getContactEmail().equals("") && model.getContactEmail() != null) {
            holder.contactEmail.setText(model.getContactEmail());
        }  else {
            holder.contactEmail.setText(context.getString(R.string.NO_CONTACT_EMAIL));
        }

        if (!model.getContactOtherDetails().equals("") && model.getContactOtherDetails() != null) {
            holder.contactOtherDetails.setText(model.getContactOtherDetails());
        }  else {
            holder.contactOtherDetails.setText(context.getString(R.string.NO_CONTACT_OTHER_DETAILS));
        }

        if (!model.getContactImage().equals("") && model.getContactImage() != null) {
            Glide.with(context).load(Uri.parse(model.getContactImage())).apply(new RequestOptions().override(120, 120)).into(holder.contactImage);
        } else {
            Glide.with(context).load(R.drawable.ic_person_black_24dp).apply(new RequestOptions().override(120, 120)).into(holder.contactImage);
        }

        return convertView;

    }
}
