package com.example.contact_application.activity;

import android.os.Bundle;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bluelinelabs.conductor.Conductor;
import com.bluelinelabs.conductor.Router;
import com.bluelinelabs.conductor.RouterTransaction;
import com.example.contact_application.R;
import com.example.contact_application.controller.ContactListController;

public class SecondActivity extends AppCompatActivity {

    private Router router;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.second_activity);
        router = Conductor.attachRouter(SecondActivity.this, (ViewGroup)
                findViewById(R.id.router), savedInstanceState);
        launchController();
    }

    private void launchController() {
        if (!router.hasRootController()) {
            router.setRoot(RouterTransaction.with(new ContactListController()));
        }
    }

    @Override
    public void onBackPressed() {
        if (!router.handleBack()) {
            super.onBackPressed();
        }
    }
}
