package com.guliver.shammir.guliver;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.TimeUnit;

public class CustomerNumberVerificationActivity extends AppCompatActivity  implements
        View.OnClickListener {

    private static final String TAG = "CustomerVerifyActivity";

    private static final String KEY_VERIFY_IN_PROGRESS = "key_verify_in_progress";

    private static final int STATE_INITIALIZED = 1;
    private static final int STATE_VERIFY_FAILED = 3;
    private static final int STATE_VERIFY_SUCCESS = 4;
    private static final int STATE_SIGNIN_FAILED = 5;
    private static final int STATE_SIGNIN_SUCCESS = 6;

    // [START declare_auth]
    private FirebaseAuth mAuth;
    // [END declare_auth]

//    private FirebaseAuth.AuthStateListener firebaseAuthListener;

    private boolean mVerificationInProgress = false;
    private String mVerificationId;
    private String mPhoneNumber, mCode;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    private EditText mVerificationField;

    private Button mVerifyButton;
    private Button mResendButton;

    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_number_verification);
//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        if (savedInstanceState != null) {
            onRestoreInstanceState(savedInstanceState);
        }

//        collect data from intent
        Intent verificationIntent = getIntent();
        mPhoneNumber = verificationIntent.getStringExtra("PHONE_NUMBER");

        startPhoneNumberVerification( mPhoneNumber );

        mResendToken = (PhoneAuthProvider.ForceResendingToken) verificationIntent.getSerializableExtra("SERIALIZED_RESEND_TOKEN");

        progressBar = (ProgressBar) findViewById(R.id.customer_number_progress_bar);
        progressBar.setVisibility(View.INVISIBLE);


        mVerificationField = (EditText) findViewById(R.id.customer_verification_edit_text);

        mVerifyButton = (Button) findViewById(R.id.confirmation_button);
        mResendButton = (Button) findViewById(R.id.resend_button);


        mVerifyButton.setOnClickListener(this);
        mResendButton.setOnClickListener(this);


        // [START initialize_auth]
        mAuth = FirebaseAuth.getInstance();
        // [END initialize_auth]


    }
    // Initialize phone auth callbacks
    // [START phone_auth_callbacks]
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onVerificationCompleted(PhoneAuthCredential credential) {
            // This callback will be invoked in two situations:
            // 1 - Instant verification. In some cases the phone number can be instantly
            //     verified without needing to send or enter a verification mCode.
            // 2 - Auto-retrieval. On some devices Google Play services can automatically
            //     detect the incoming verification SMS and perform verificaiton without
            //     user action.
            Log.e(TAG, "onVerificationCompleted: Code =" + credential.getSmsCode());
            // [START_EXCLUDE silent]
            mVerificationInProgress = false;
            // [END_EXCLUDE]

            // [START_EXCLUDE silent]
            mCode = credential.getSmsCode();
            if ( mCode != null){
                mVerificationField.setText(mCode);

                //verify the mCode
                verifyVerificationCode(mCode);
            }
            // Update the UI and attempt sign in with the phone credential
             updateUI(STATE_VERIFY_SUCCESS, credential);
            // [END_EXCLUDE]
            signInWithPhoneAuthCredential(credential);
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            // This callback is invoked in an invalid request for verification is made,
            // for instance if the the phone number format is not valid.
            Log.e(TAG, "onVerificationFailed:  "+e.getMessage(), e);
            // [START_EXCLUDE silent]
            mVerificationInProgress = false;
            // [END_EXCLUDE]

            if (e instanceof FirebaseTooManyRequestsException) {
                // The SMS quota for the project has been exceeded
                // [START_EXCLUDE]
                Snackbar.make(findViewById(android.R.id.content), "Quota exceeded.",
                        Snackbar.LENGTH_SHORT).show();
                // [END_EXCLUDE]
            }

            // Show a message and update the UI

        }

        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);

            //storing the verification id that is sent to the user
            Log.i(TAG, "Code sent: "+s);
            mVerificationId = s;
        }

    };
    // [END phone_auth_callbacks]

    // [START on_start_check_user]
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);

        // [START_EXCLUDE]
        if (mVerificationInProgress) {
            startPhoneNumberVerification(mPhoneNumber);
        }
        // [END_EXCLUDE]
    }
    // [END on_start_check_user]

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
    }

    private void verifyVerificationCode(String code) {
        // [START verify_with_code]
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential( mVerificationId, code);
        // [END verify_with_code]
        signInWithPhoneAuthCredential(credential);
    }

    // [START resend_verification]
    private void resendVerificationCode(String phoneNumber,
                                        PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks,         // OnVerificationStateChangedCallbacks
                token);             // ForceResendingToken from callbacks
    }
    // [END resend_verification]

    // [START sign_in_with_phone]
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");

                            //store the creds in db
                            String user_id = mAuth.getCurrentUser().getUid();

                            final DatabaseReference current_user_db = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(user_id).child("phone");
                            current_user_db.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    Log.e(TAG, "Registration Phone "+ dataSnapshot.getValue());
                                    if (dataSnapshot.getValue() != mPhoneNumber){
                                        current_user_db.setValue(mPhoneNumber);

                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }

                            });

                            FirebaseUser user = task.getResult().getUser();
                            // [START_EXCLUDE]
                            updateUI(STATE_SIGNIN_SUCCESS, user);
                            // [END_EXCLUDE]
                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification mCode entered was invalid
                                // [START_EXCLUDE silent]
                                mVerificationField.setError("Invalid mCode.");
                                // [END_EXCLUDE]
                            }
                            // [START_EXCLUDE silent]
                            // Update UI
                            updateUI(STATE_SIGNIN_FAILED);
                            // [END_EXCLUDE]
                        }
                    }
                });
    }
    // [END sign_in_with_phone]

    private void updateUI(int uiState) {
        updateUI(uiState, mAuth.getCurrentUser(), null);
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            updateUI(STATE_SIGNIN_SUCCESS, user);
        } else {
            updateUI(STATE_INITIALIZED);
        }
    }

    private void updateUI(int uiState, PhoneAuthCredential cred) {
        updateUI(uiState, null, cred);
    }

    private void updateUI(int uiState, FirebaseUser user) {
        updateUI(uiState, user, null);
    }


    private void updateUI(int uiState, FirebaseUser user, PhoneAuthCredential cred) {
        switch (uiState) {
            case STATE_INITIALIZED:
                progressBar.setVisibility( View.VISIBLE);
            case STATE_VERIFY_FAILED:
                // Verification has failed, show all options
                enableViews( mVerifyButton, mResendButton,
                        mVerificationField);
                mVerificationField.setError(" Verification failed");
                progressBar.setVisibility(View.INVISIBLE);
                break;
            case STATE_VERIFY_SUCCESS:
                // Verification has succeeded, proceed to firebase sign in
                disableViews( mVerifyButton, mResendButton,
                        mVerificationField);
                progressBar.setVisibility(View.INVISIBLE);

                // Set the verification text based on the credential
                if (cred != null) {
                    if (cred.getSmsCode() != null) {
                        mVerificationField.setText(cred.getSmsCode());
                    } else {
                        mVerificationField.setText(R.string.instant_validation);
                        mVerificationField.setTextColor(Color.parseColor("#4bacb8"));
                    }
                }

                break;
            case STATE_SIGNIN_FAILED:
                // No-op, handled by sign-in check
                progressBar.setVisibility(View.INVISIBLE);
                break;
            case STATE_SIGNIN_SUCCESS:
                // Np-op, handled by sign-in check
                break;
        }

        if (user != null) {
            /*
            mSignedInViews.setVisibility(View.VISIBLE);
            enableViews(mPhoneNumberField, mVerificationField);
            mPhoneNumberField.setText(null);
            mVerificationField.setText(null);
            mStatusText.setText(R.string.signed_in);
            mDetailText.setText(getString(R.string.firebase_status_fmt, user.getUid()));
            */
            Intent intent = new Intent(this, CustomerMapActivity.class);
            startActivity(intent);
            finish();
        }
    }


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


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.confirmation_button:
                String code = mVerificationField.getText().toString();
                if (TextUtils.isEmpty(code)) {
                    mVerificationField.setError("Cannot be empty.");
                    return;
                }

                verifyVerificationCode( code );

                updateUI(STATE_INITIALIZED);
                break;
            case R.id.resend_button:
                updateUI(STATE_INITIALIZED);
                resendVerificationCode(mPhoneNumber, mResendToken);
                break;

        }
    }


}