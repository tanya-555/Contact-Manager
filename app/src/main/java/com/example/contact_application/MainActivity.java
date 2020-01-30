package com.example.contact_application;


import android.os.Bundle;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;

import com.bluelinelabs.conductor.Conductor;
import com.bluelinelabs.conductor.Router;
import com.bluelinelabs.conductor.RouterTransaction;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Router router = Conductor.attachRouter(MainActivity.this, (ViewGroup) findViewById(R.id.router), savedInstanceState);
        if (!router.hasRootController()) {
            router.setRoot(RouterTransaction.with(new ContactListController()));
        }
    }
}


