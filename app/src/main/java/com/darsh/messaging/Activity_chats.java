package com.darsh.messaging;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

public class Activity_chats extends AppCompatActivity {
    Button send;
    EditText message;
    RecyclerView Rview;

    private FirebaseFirestore mRoot;
    private FirebaseAuth mAuth;

    private StorageReference mStorageRef;
    private CollectionReference mMessageRef;
    private static final int PERMISSION_REQUEST_CODE_CAMERA = 243;
    private static final int PERMISSION_REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 435;
    private String value_phone;
    private Chat_adapter chat_adapter;

    private ListenerRegistration registration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chats);
        mStorageRef = FirebaseStorage.getInstance().getReference();
        ImageButton imageButton = findViewById(R.id.ImageButton);
        send = findViewById(R.id.button3);
        message = findViewById(R.id.message);
        mAuth = FirebaseAuth.getInstance();
        Rview = findViewById(R.id.Recyclerview);

        imageButton.setOnClickListener(v -> selectImage());

        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        assert mAuth.getCurrentUser() != null;
        chat_adapter = new Chat_adapter(getApplicationContext(), mAuth.getCurrentUser().getDisplayName());

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());

        layoutManager.canScrollVertically();

        Rview.setLayoutManager(layoutManager);
        Rview.setAdapter(chat_adapter);


        value_phone = getIntent().getStringExtra("value_phone");
        String value_name = getIntent().getStringExtra("value_name");
        getSupportActionBar().setTitle(value_name);
        mRoot = FirebaseFirestore.getInstance();

        assert value_phone != null;
        assert mAuth.getCurrentUser().getPhoneNumber() != null;
        mMessageRef = mRoot.collection("Message").document(mAuth.getCurrentUser().getPhoneNumber()).collection(value_phone);

        registration = mMessageRef.orderBy("timestamp", Query.Direction.ASCENDING).addSnapshotListener(chat_adapter);

        send.setOnClickListener(v -> {
            String m = message.getText().toString();
            if (m.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Please enter message first", Toast.LENGTH_LONG).show();
            } else {
                HashMap<String, Object> Messages = new HashMap<>();
                Messages.put("message", message.getText().toString());
                Messages.put("by", mAuth.getCurrentUser().getDisplayName());
                Messages.put("timestamp", FieldValue.serverTimestamp());

                DocumentReference mMessageRef1 = mRoot.collection("Message").document(mAuth.getCurrentUser().getPhoneNumber()).collection(value_phone).document();
                mMessageRef1.set(Messages).addOnCompleteListener(task -> message.setText(""));
                mRoot.collection("Message").document(value_phone).collection(mAuth.getCurrentUser().getPhoneNumber()).document(mMessageRef1.getId()).set(Messages).addOnSuccessListener(aVoid -> message.setText(""));

            }

        });

    }

    private void selectImage() {
        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose your profile picture");

        builder.setItems(options, (dialog, item) -> {

            if (options[item].equals("Take Photo")) {
                if (isHavingCameraPermission()) {
                    Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(takePicture, 0);
                } else {
                    requestCameraPermission();
                }


            } else if (options[item].equals("Choose from Gallery")) {
                if (isHavingExternalStoragePermission()) {
                    Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(pickPhoto, 1);
                } else {
                    requestExternalStoragePermission();
                }

            } else if (options[item].equals("Cancel")) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE_CAMERA:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_SHORT).show();

                    Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(takePicture, 0);
                } else {
                    Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                                != PackageManager.PERMISSION_GRANTED) {
                            showMessageOKCancel(
                                    (dialog, which) -> requestCameraPermission(), (dialog, which) -> finish());
                        }
                    }
                }
                break;
            case PERMISSION_REQUEST_CODE_WRITE_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_SHORT).show();

                    Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(pickPhoto, 1);

                } else {
                    Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                != PackageManager.PERMISSION_GRANTED) {
                            showMessageOKCancel(
                                    (dialog, which) -> requestExternalStoragePermission(), (dialog, which) -> finish());
                        }
                    }
                }

        }
    }

    private void showMessageOKCancel(DialogInterface.OnClickListener okListener, DialogInterface.OnClickListener cancelListener) {
        new AlertDialog.Builder(Activity_chats.this)
                .setMessage("You need to allow access permissions")
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", cancelListener)
                .create()
                .show();
    }

    private void requestExternalStoragePermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                PERMISSION_REQUEST_CODE_WRITE_EXTERNAL_STORAGE);
    }

    private boolean isHavingExternalStoragePermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                PERMISSION_REQUEST_CODE_CAMERA);
    }

    private boolean isHavingCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        chat_adapter.clear();
        if (registration != null) {
            registration.remove();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        chat_adapter.clear();
        if (registration != null) {
            registration.remove();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (registration == null){
            registration = mMessageRef.orderBy("timestamp", Query.Direction.ASCENDING).addSnapshotListener(chat_adapter);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        chat_adapter.clear();
        if (registration != null) {
            registration.remove();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_CANCELED) {
            switch (requestCode) {
                case 0:
                    if (data.getExtras() != null) {
                        if (resultCode == RESULT_OK && data.getExtras().containsKey("data")) {

                            Bitmap selectedImage = (Bitmap) data.getExtras().get("data");


                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            assert selectedImage != null;
                            selectedImage.compress(Bitmap.CompressFormat.PNG, 100, stream);
                            byte[] byteArray = stream.toByteArray();
                            FirebaseFirestore mRootRef = FirebaseFirestore.getInstance();
                            DocumentReference mImageRef1 = mRootRef.collection("Images").document();
                            assert mAuth.getCurrentUser() != null;
                            if (!TextUtils.isEmpty(mAuth.getCurrentUser().getPhoneNumber())) {
                                mStorageRef.child("Images").child(mAuth.getCurrentUser().getPhoneNumber()).child(value_phone).child(mImageRef1.getId() + ".jpg").putBytes(byteArray).addOnSuccessListener(taskSnapshot -> {
                                    HashMap<String, Object> hashMap = new HashMap<>();
                                    assert taskSnapshot.getUploadSessionUri() != null;
                                    hashMap.put("imageUrl", taskSnapshot.getUploadSessionUri().toString());
                                    hashMap.put("imageLocation", "Images/" + mAuth.getCurrentUser().getPhoneNumber() + "/" + value_phone + "/" + mImageRef1.getId() + ".jpg");
                                    hashMap.put("by", mAuth.getCurrentUser().getDisplayName());
                                    hashMap.put("message", "");
                                    hashMap.put("timestamp", FieldValue.serverTimestamp());
                                    mRootRef.collection("Message").document(mAuth.getCurrentUser().getPhoneNumber()).collection(value_phone).document(mImageRef1.getId()).set(hashMap).toString();
                                    mRootRef.collection("Message").document(value_phone).collection(mAuth.getCurrentUser().getPhoneNumber()).document(mImageRef1.getId()).set(hashMap);


                                });
                            }
                        }
                    }


                    break;
                case 1:
                    if (resultCode == RESULT_OK && data != null) {
                        Uri selectedImage = data.getData();
                        String[] filePathColumn = {MediaStore.Images.Media.DATA};
                        if (selectedImage != null) {
                            Cursor cursor = getContentResolver().query(selectedImage,
                                    filePathColumn, null, null, null);
                            if (cursor != null) {
                                cursor.moveToFirst();

                                cursor.close();
                            }
                        }

                    }
                    break;
            }
        }
    }
}


