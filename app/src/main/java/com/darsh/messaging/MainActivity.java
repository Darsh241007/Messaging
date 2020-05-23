package com.darsh.messaging;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;


public class MainActivity extends AppCompatActivity {

    private FirebaseUser mUser;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseFirestore mRoot;
    FloatingActionButton fab;
    Single_ui Layout;
    private ListenerRegistration registration;
    private CollectionReference mMessageRef;
    private Friend_adapter friend_adapter;
    private MessageDialog mMessageDialog;
    private TextView your_name;
    private View root_view;
    private ImageButton imageButton3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        root_view=findViewById(R.id.root_view);
        your_name=findViewById(R.id.your_name);
        imageButton3=findViewById(R.id.imageButton3);
        mAuth = FirebaseAuth.getInstance();
        authWithFirebase();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("123", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            assert notificationManager != null;
            notificationManager.createNotificationChannel(channel);
        }

        imageButton3.setOnClickListener(v -> mAuth.signOut());

        fab=findViewById(R.id.fab);
        Layout=new Single_ui(this);
        RecyclerView recyclerView = findViewById(R.id.RecyclerView2);
        mMessageDialog = new MessageDialog(this);

        fab.setOnClickListener(v -> Layout.show());

        friend_adapter = new Friend_adapter(getApplicationContext());

        RecyclerView.LayoutManager layoutManager= new LinearLayoutManager(getApplicationContext());
        layoutManager.canScrollVertically();

        mRoot = FirebaseFirestore.getInstance();

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(friend_adapter);

        Layout.cancel.setOnClickListener(v -> Layout.cancel());

        Layout.search.setOnClickListener(v -> {

            final String phoneSearch= Layout.getText();
            if (phoneSearch.length() != 10 ){
                Layout.setError();
            }else {
                mMessageDialog.setTitle("Searching");
                mMessageDialog.setMessage("Searching for friend");
                mMessageDialog.show();
                mRoot.collection("Users").document("+91"+phoneSearch).get().addOnCompleteListener(task -> {
                    assert task.getResult()!= null;
                    if (task.isSuccessful()) if (task.getResult().getData() == null) {
                        mMessageDialog.hide();
                        Layout.cancel();
                        Snackbar snackbar = Snackbar.make(root_view, "Not Having an account", Snackbar.LENGTH_INDEFINITE)
                                .setDuration(8000)
                                .setAction("Invite", v1 -> sendInvite("+91" + phoneSearch));
                        snackbar.show();
                    } else {
                        mMessageDialog.setMessage("Adding Friend");
                        HashMap<String, Object> friendDetails = new HashMap<>();
                        if (task.getResult().getData().containsKey("name")) {
                            assert task.getResult().getData() != null;
                            friendDetails.put("Name", task.getResult().getData().get("name"));
                        }
                        friendDetails.put("phone", "+91" + phoneSearch);
                        assert mUser.getPhoneNumber()!= null;
                        mRoot.collection("FriendList").document(mUser.getPhoneNumber()).collection("Friends").document("+91" + phoneSearch).set(friendDetails, SetOptions.merge()).addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                mMessageDialog.hide();
                                Layout.cancel();
                                Toast.makeText(getApplicationContext(), "Friend has been added successfully", Toast.LENGTH_LONG).show();

                            } else {
                                mMessageDialog.hide();
                                Toast.makeText(getApplicationContext(), "Error occurred please try again", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                    else{
                        Layout.cancel();
                        Toast.makeText(getApplicationContext(),"There is an error try again", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

    }

    public void sendInvite(String phone_number){
        Intent smsIntent = new Intent(Intent.ACTION_VIEW);
        smsIntent.setType("vnd.android-dir/mms-sms");
        smsIntent.putExtra("address", phone_number);
        smsIntent.putExtra("sms_body","Install this app");
        startActivity(smsIntent);

    }

    public void authWithFirebase(){
        mAuthListener = firebaseAuth -> {
            mUser = firebaseAuth.getCurrentUser();
            if (mUser == null) {
                Intent LoginIntent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(LoginIntent);
                finish();
            }else{
                your_name.setText(mAuth.getCurrentUser().getDisplayName());
                assert mUser.getPhoneNumber() != null;
                mMessageRef = mRoot.collection("FriendList").document(mUser.getPhoneNumber()).collection("Friends");
                registration = mMessageRef.addSnapshotListener(friend_adapter);
            }

        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mAuth.removeAuthStateListener(mAuthListener);
        if (registration != null){
            registration.remove();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAuth.removeAuthStateListener(mAuthListener);
    }

}
