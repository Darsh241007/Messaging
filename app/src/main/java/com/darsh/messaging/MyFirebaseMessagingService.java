package com.darsh.messaging;

import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Objects;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    FirebaseFirestore mRoot=FirebaseFirestore.getInstance();

    @Override
    public void onNewToken(@NonNull String token) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null){
            HashMap<String, String> userInfo = new HashMap<>();
            userInfo.put("token",token);

            mRoot.collection("Users").document(Objects.requireNonNull(user.getPhoneNumber())).set(userInfo, SetOptions.merge());

        }
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        if(remoteMessage.getData().size() > 0){

            Intent intent = new Intent(this, Activity_chats.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("value_phone",remoteMessage.getData().get("phone"));
            intent.putExtra("value_name",remoteMessage.getData().get("title"));
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "123");
            if (TextUtils.equals(remoteMessage.getData().get("imageLocation"),"No image")) {

                builder.setContentTitle(remoteMessage.getData().get("title"))
                        .setContentText(remoteMessage.getData().get("body"))
                        .setSmallIcon(R.mipmap.messagingappicon)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true);
            }else {
                String imageLocation = remoteMessage.getData().get("imageLocation");
                StorageReference mStorageRef= FirebaseStorage.getInstance().getReference();

                assert imageLocation != null;
                final long ONE_MEGABYTE = 512 * 512;
                mStorageRef.child(imageLocation).getBytes(ONE_MEGABYTE).addOnSuccessListener(bytes -> {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    builder.setContentTitle(remoteMessage.getData().get("title"))
                            .setContentText(remoteMessage.getData().get("body"))
                            .setSmallIcon(R.mipmap.messagingappicon)
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setContentIntent(pendingIntent)
                            .setLargeIcon(bitmap)
                            .setStyle(new NotificationCompat.BigPictureStyle().bigPicture(bitmap).bigLargeIcon(null))
                            .setAutoCancel(true);
                }).addOnFailureListener(e -> builder.setContentTitle(remoteMessage.getData().get("title"))
                        .setContentText(remoteMessage.getData().get("imageLocation"))
                        .setSmallIcon(R.mipmap.messagingappicon)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true));

            }

                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
                notificationManager.notify(5384, builder.build());

        }
    }
}
