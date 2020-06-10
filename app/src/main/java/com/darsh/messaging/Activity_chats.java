package com.darsh.messaging;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

public class Activity_chats extends AppCompatActivity {
    ImageButton send;
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
                        Uri selectedImageUri = data.getData();

                        Bitmap selectedImage = null;
                        try {
                            selectedImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(),selectedImageUri);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        //int nh = (int) ( selectedImage.getHeight() * (512.0 / selectedImage.getWidth()) );
                        assert selectedImage != null;
                        Bitmap scaled = Bitmap.createScaledBitmap(selectedImage, 256, 256, true);

                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        assert scaled != null;
                        scaled.compress(Bitmap.CompressFormat.PNG, 100, stream);
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
                    break;
            }
        }
    }

    public String compressImage(String imageUri) {

        String filePath = getRealPathFromURI(imageUri);
        Toast.makeText(getApplicationContext(), filePath,Toast.LENGTH_LONG).show();
        Bitmap scaledBitmap = null;

        BitmapFactory.Options options = new BitmapFactory.Options();

//      by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
//      you try the use the bitmap here, you will get null.
        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(filePath, options);

        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;

//      max Height and width values of the compressed image is taken as 816x612

        float maxHeight = 256.0f;
        float maxWidth = 256.0f;
        float imgRatio = actualWidth / actualHeight;
        float maxRatio = maxWidth / maxHeight;

//      width and height values are set maintaining the aspect ratio of the image

        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight;
                actualWidth = (int) (imgRatio * actualWidth);
                actualHeight = (int) maxHeight;
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth;
                actualHeight = (int) (imgRatio * actualHeight);
                actualWidth = (int) maxWidth;
            } else {
                actualHeight = (int) maxHeight;
                actualWidth = (int) maxWidth;

            }
        }

//      setting inSampleSize value allows to load a scaled down version of the original image

        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);

//      inJustDecodeBounds set to false to load the actual bitmap
        options.inJustDecodeBounds = false;

//      this options allow android to claim the bitmap memory if it runs low on memory
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inTempStorage = new byte[16 * 1024];

        try {
//          load the bitmap from its path
            bmp = BitmapFactory.decodeFile(filePath, options);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();

        }
        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }

        float ratioX = actualWidth / (float) options.outWidth;
        float ratioY = actualHeight / (float) options.outHeight;
        float middleX = actualWidth / 2.0f;
        float middleY = actualHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

//      check the rotation of the image and display it properly
        ExifInterface exif;
        try {
            exif = new ExifInterface(filePath);

            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, 0);
            Log.d("EXIF", "Exif: " + orientation);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 3) {
                matrix.postRotate(180);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 8) {
                matrix.postRotate(270);
                Log.d("EXIF", "Exif: " + orientation);
            }
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,
                    scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix,
                    true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        FileOutputStream out = null;
        String filename = getFilename();
        try {
            out = new FileOutputStream(filename);

//          write the compressed bitmap at the destination specified by filename.
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return filename;

    }

    public String getFilename() {
        File file = new File(Environment.getExternalStorageDirectory().getPath(), "MyFolder/Images");
        if (!file.exists()) {
            file.mkdirs();
        }
        String uriSting = (file.getAbsolutePath() + "/" + System.currentTimeMillis() + ".jpg");
        return uriSting;

    }

    private String getRealPathFromURI(String contentURI) {
        Uri contentUri = Uri.parse(contentURI);
        Cursor cursor = getContentResolver().query(contentUri, null, null, null, null);
        if (cursor == null) {
            return contentUri.getPath();
        } else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            return cursor.getString(index);
        }
    }

    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        final float totalPixels = width * height;
        final float totalReqPixelsCap = reqWidth * reqHeight * 2;
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }

        return inSampleSize;
    }

}


