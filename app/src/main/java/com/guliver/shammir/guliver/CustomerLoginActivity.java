package com.guliver.shammir.guliver;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

public class CustomerLoginActivity  extends AppCompatActivity implements View.OnClickListener, Serializable {

    private static final String TAG = "CustomerLoginActivity";

    private static final String KEY_VERIFY_IN_PROGRESS = "key_verify_in_progress";
    private String mVerificationId;

    // [START declare_auth]
    private FirebaseAuth mAuth;
    // [END declare_auth]
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
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

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verificaiton without
                //     user action.
                Log.d(TAG, "onVerificationCompleted:" + credential);
                // [START_EXCLUDE silent]
                mVerificationInProgress = false;
                // [END_EXCLUDE]

                // [START_EXCLUDE silent]
                // Update the UI and attempt sign in with the phone credential
//                updateUI(STATE_VERIFY_SUCCESS, credential);
                // [END_EXCLUDE]
                enableViews(login_button);


            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.e(TAG, "onVerificationFailed:  "+e.getMessage(), e);
                // [START_EXCLUDE silent]
                mVerificationInProgress = false;
                // [END_EXCLUDE]

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request

                    // [START_EXCLUDE]
                    customer_phone.setError("Invalid phone number.");
                    // [END_EXCLUDE]
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    // [START_EXCLUDE]
                    Snackbar.make(findViewById(android.R.id.content), "Quota exceeded.",
                            Snackbar.LENGTH_SHORT).show();
                    // [END_EXCLUDE]
                }
                enableViews(login_button);

                // Show a message and update the UI
                // [START_EXCLUDE]
//                updateUI(STATE_VERIFY_FAILED);
                // [END_EXCLUDE]

            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d(TAG, "onCodeSent:" + verificationId);

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;

                // [START_EXCLUDE]
                // SEND USER TO CONFIRMATION PAGE
                goToConfirmationPage( customer_phone.getText().toString(), mVerificationId, token );
//                updateUI(STATE_CODE_SENT);
                // [END_EXCLUDE]
            }
        };

    }

    private void startPhoneNumberVerification(String phoneNumber) {
        // [START start_phone_auth]
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
        // [END start_phone_auth]

        mVerificationInProgress = true;
        mStatusText.setVisibility(View.INVISIBLE);
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
    private void goToConfirmationPage(String phone_number, String mVerificationId, PhoneAuthProvider.ForceResendingToken mResendToken){
        Intent login_activity_intent = new Intent(this, CustomerNumberVerificationActivity.class);
        login_activity_intent.putExtra("PHONE_NUMBER", phone_number);
        login_activity_intent.putExtra("VERIFICATION_ID", mVerificationId);
        login_activity_intent.putExtra("SERIALIZED_RESEND_TOKEN", mResendToken);
        startActivity(login_activity_intent);
        finish();
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


                mStatusText.setText("Verifying your number....!");
                startPhoneNumberVerification(customer_phone.getText().toString());
                disableViews(login_button);
        }

    }
}