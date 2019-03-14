package com.guliver.shammir.guliver;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class WelcomeActivity extends AppCompatActivity implements View.OnClickListener {

//    decalare the buttons on welcome screen

    private Button login_button, call_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

//        initialize the buttons
        login_button = (Button) findViewById(R.id.welcome_login_button);
        call_button  = (Button) findViewById(R.id.welcome_call_button);

//        handle clicks within the class
        login_button.setOnClickListener(this);
        call_button.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.welcome_call_button:
//                handle call intent here
                Intent call_intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + "+252616651266"));
                startActivity(call_intent);
                return;

            case R.id.welcome_login_button:
                Intent login_activity_intent = new Intent(this, CustomerLoginActivity.class);
                startActivity(login_activity_intent);
                finish();
                break;
        }

    }
}
