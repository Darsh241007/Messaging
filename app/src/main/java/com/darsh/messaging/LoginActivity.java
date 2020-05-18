package com.darsh.messaging;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {
    EditText name_editextbox,number_edittextbox;
    Button next;
    TextInputLayout number_inputlayout;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallback;
    private OtpLayout otpLayout;
    private String resendId;
    private PhoneAuthProvider.ForceResendingToken resendingToken;
    private FirebaseAuth mAuth;
    private MessageDialog mMessageDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Objects.requireNonNull(getSupportActionBar()).setTitle("LOGIN");
        name_editextbox=findViewById(R.id.editText);
        number_edittextbox=findViewById(R.id.editText2);
        next=findViewById(R.id.Next);
        number_inputlayout=findViewById(R.id.textInputLayout2);

        next.setOnClickListener(v -> checkData());

        mAuth = FirebaseAuth.getInstance();

        otpLayout = new OtpLayout(this);
        mMessageDialog = new MessageDialog(this);

        mCallback = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                signin(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {

            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                mMessageDialog.hide();
                otpLayout.show();
                resendId = s;
                resendingToken = forceResendingToken;
            }
        };

        otpLayout.resend.setOnClickListener(v -> {
            mMessageDialog.setMessage("Resending OTP");
            mMessageDialog.show();
            resendOTP();

        });

        otpLayout.verify.setOnClickListener(v -> {
            String otp = otpLayout.getText();
            if (otp.length() ==6 ) {
                mMessageDialog.setMessage("Verifying Otp");
                mMessageDialog.show();

                signin(PhoneAuthProvider.getCredential(resendId, otpLayout.getText()));
            }else {
                otpLayout.setError();
            }
        });

    }

    private void signin(PhoneAuthCredential phoneAuthCredential) {
        otpLayout.cancel();
        mMessageDialog.setMessage("Creating profile");

        mAuth.signInWithCredential(phoneAuthCredential).addOnSuccessListener(authResult -> {
            final FirebaseFirestore mRoot = FirebaseFirestore.getInstance();

            FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(instanceIdResult -> {

                String token = instanceIdResult.getToken();
                HashMap<String, String> userInfo = new HashMap<>();

                assert authResult.getUser()!= null;
                if (TextUtils.isEmpty(authResult.getUser().getDisplayName())) {
                    UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder().setDisplayName(name_editextbox.getText().toString()).build();

                    authResult.getUser().updateProfile(profileChangeRequest);
                    userInfo.put("name",name_editextbox.getText().toString());
                }else {
                    userInfo.put("name",authResult.getUser().getDisplayName());
                }


                String phone = "+91"+number_edittextbox.getText().toString();
                userInfo.put("Phone",phone);
                userInfo.put("token",token);
                userInfo.put("uid",authResult.getUser().getUid());



                mRoot.collection("Users").document(phone).set(userInfo, SetOptions.merge()).addOnSuccessListener(aVoid -> {
                    Intent mainIntent=new Intent(getApplicationContext(),MainActivity.class);
                    startActivity(mainIntent);
                    finish();
                }).addOnFailureListener(e -> Toast.makeText(getApplicationContext(),e.getLocalizedMessage(),Toast.LENGTH_LONG).show()).addOnCompleteListener(task -> Toast.makeText(getApplicationContext(),String.valueOf(task.isSuccessful()),Toast.LENGTH_LONG).show());
            });



        }).addOnFailureListener(e -> Toast.makeText(getApplicationContext(),e.getLocalizedMessage(),Toast.LENGTH_LONG).show());
    }

    private void resendOTP() {
        String number=number_edittextbox.getText().toString();
        PhoneAuthProvider.getInstance().verifyPhoneNumber("+91"+number,60, TimeUnit.SECONDS,this,mCallback,resendingToken);
    }

    private void checkData() {
        mMessageDialog.setMessage("Sending Otp");
        mMessageDialog.setTitle("SignIn");
        mMessageDialog.show();

        String number=number_edittextbox.getText().toString();

        if (number.length() == 10 && (number.startsWith("6") || number.startsWith("7") || number.startsWith("8")||number.startsWith("9"))){
            PhoneAuthProvider.getInstance().verifyPhoneNumber("+91"+number,60, TimeUnit.SECONDS,this,mCallback);
        }else{
            number_inputlayout.setError("Invalid number");
        }
    }
}
