package com.guliver.shammir.guliver;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

public class CustomerLoginActivity  extends AppCompatActivity implements View.OnClickListener, Serializable {

    private static final String TAG = "CustomerLoginActivity";

    private static final String KEY_VERIFY_IN_PROGRESS = "key_verify_in_progress";

    // [START declare_auth]
    private FirebaseAuth mAuth;

    private FirebaseAuth.AuthStateListener firebaseAuthListener;

    private boolean mVerificationInProgress = false;

//    decalare the buttons on welcome screen

    private Button login_button, call_button;

    private EditText customer_phone;

    private TextView mStatusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_login);

//        initialize the buttons
        login_button = (Button) findViewById(R.id.login_login_button);
        call_button = (Button) findViewById(R.id.login_call_button);

//        edit text
        customer_phone = (EditText) findViewById(R.id.customer_login_phone);

        mStatusText = (TextView) findViewById(R.id.status);

//        handle clicks within the class
        login_button.setOnClickListener(this);
        call_button.setOnClickListener(this);

        // [START initialize_auth]
        mAuth = FirebaseAuth.getInstance();
        // [END initialize_auth]

//        check if user is already logged in
        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if(user!=null){
                    Intent intent = new Intent(CustomerLoginActivity.this, CustomerMapActivity.class);
                    startActivity(intent);
                    finish();
                    return;
                }
            }
        };


    }


//    validate phone number
    private boolean validatePhoneNumber() {
        String phoneNumber = customer_phone.getText().toString();
        if (TextUtils.isEmpty(phoneNumber)) {
            customer_phone.setError("Invalid phone number.");
            customer_phone.setTextColor(Color.parseColor("#ff1744"));
            Log.e(TAG, "validatePhoneNumber: empty"  );
            return false;
        }

        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_VERIFY_IN_PROGRESS, mVerificationInProgress);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mVerificationInProgress = savedInstanceState.getBoolean(KEY_VERIFY_IN_PROGRESS);
    }

//    enabling and disabling views to make sure requests are not sent out unwanted
    private void enableViews(View... views) {
        for (View v : views) {
            v.setEnabled(true);
        }
    }

    private void disableViews(View... views) {
        for (View v : views) {
            v.setEnabled(false);
        }
    }

//    goto confirmation page
    private void goToConfirmationPage(String phone_number){
        Intent login_activity_intent = new Intent(this, CustomerNumberVerificationActivity.class);
        login_activity_intent.putExtra("PHONE_NUMBER", phone_number);
        startActivity(login_activity_intent);
    }




    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.login_call_button:
//                handle call intent here
                Intent call_intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + "+252616651266"));
                startActivity(call_intent);
                return;

            case R.id.login_login_button:
                if (!validatePhoneNumber()) {
                    return;
                }

                ///////hide keyboard start
                InputMethodManager inputManager = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);

                inputManager.hideSoftInputFromWindow(customer_phone.getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
                /////////hide keyboard end
                String phoneNumber = customer_phone.getText().toString();

                goToConfirmationPage( phoneNumber );

                mStatusText.setText("Verifying your number....!");

        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(firebaseAuthListener);
    }
    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(firebaseAuthListener);
    }
}