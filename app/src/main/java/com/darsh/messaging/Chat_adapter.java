package com.darsh.messaging;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Chat_adapter extends RecyclerView.Adapter<Chat_adapter.SingleMessage> implements EventListener<QuerySnapshot> {

    private LayoutInflater mInflater;
    private List<Message> mMessageList = new ArrayList<>();
    private String UserName;
    private StorageReference mRootRef;


        Chat_adapter(Context applicationContext, String name) {
            mInflater = LayoutInflater.from(applicationContext);
            UserName = name;
            mRootRef = FirebaseStorage.getInstance().getReference();
    }

    @NonNull
    @Override
    public Chat_adapter.SingleMessage onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = mInflater.inflate(R.layout.singlemessage_ui, parent, false);
        return new SingleMessage(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SingleMessage holder, int position) {
        holder.bindToView(mMessageList.get(position),UserName,mRootRef);
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    public void clear(){
            mMessageList.clear();
    }

    @Override
    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
        mMessageList.clear();
        if (queryDocumentSnapshots != null){
            for (QueryDocumentSnapshot QueryDocument: queryDocumentSnapshots){
                Message message = QueryDocument.toObject(Message.class);
                message.setId(QueryDocument.getId());
                boolean dub  = false;
                for(Message message1: mMessageList){
                    if (TextUtils.equals(message1.getId() , QueryDocument.getId())){
                        dub = true;
                        break;
                    }
                }
                if (!dub) {
                    mMessageList.add(mMessageList.size(), message);
                    notifyDataSetChanged();
                }
            }
        }

    }

    static class SingleMessage extends RecyclerView.ViewHolder {

        private TextView mMessage;
        private TextView mTime;
        private TextView mMessage1;
        private TextView mTime1;
        private ImageView mImage;
        private ImageView mImage1;


        SingleMessage(@NonNull View itemView) {
            super(itemView);
            mMessage = itemView.findViewById(R.id.textView2);
            mTime=itemView.findViewById(R.id.textView3);
            mMessage1 = itemView.findViewById(R.id.textView6);
            mTime1 = itemView.findViewById(R.id.textView7);
            mImage = itemView.findViewById(R.id.imageView3);
            mImage1 = itemView.findViewById(R.id.imageView4);
        }

        void bindToView(Message message, String username,StorageReference root){
            if (message.getBy().equals(username)){
                mMessage.setVisibility(View.GONE);
                mTime.setVisibility(View.GONE);
                mImage1.setVisibility(View.GONE);

                if (TextUtils.isEmpty(message.getMessage()) & !TextUtils.isEmpty(message.getImageLocation())){
                    mMessage1.setVisibility(View.GONE);
                    if (message.getTimestamp() != null) {
                        Date currentTime = Calendar.getInstance().getTime();
                        SimpleDateFormat df = new SimpleDateFormat("dd MM yyyy", Locale.getDefault());
                        String currentDate = df.format(currentTime);
                        String messageDate = df.format(message.getTimestamp());

                        if (currentDate.equals(messageDate)) {
                            SimpleDateFormat df1 = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                            String time = df1.format(message.getTimestamp());
                            mTime1.setText(time);
                        } else {
                            mTime1.setText(messageDate);
                        }
                    }

                    StorageReference islandRef = root.child(message.getImageLocation());

                    final long ONE_MEGABYTE = 512 * 512;
                    islandRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            mImage.setVisibility(View.VISIBLE);
                            mImage.setImageBitmap(bitmap);

                        }
                    });

                }else {
                    mImage.setVisibility(View.GONE);
                mMessage1.setText(message.getMessage());
                if (message.getTimestamp() != null) {
                    Date currentTime = Calendar.getInstance().getTime();
                    SimpleDateFormat df = new SimpleDateFormat("dd MM yyyy");
                    String currentDate = df.format(currentTime);
                    String messageDate = df.format(message.getTimestamp());

                    if (currentDate.equals(messageDate)) {
                        SimpleDateFormat df1 = new SimpleDateFormat("HH:mm:ss");
                        String time = df1.format(message.getTimestamp());
                        mTime1.setText(time);
                    } else {
                        mTime1.setText(messageDate);
                    }
                }

                }
            }else {
                mMessage1.setVisibility(View.GONE);
                mTime1.setVisibility(View.GONE);
                mImage.setVisibility(View.GONE);
                if (TextUtils.isEmpty(message.getMessage()) & !TextUtils.isEmpty(message.getImageLocation())){
                    mMessage.setVisibility(View.GONE);
                    StorageReference islandRef = root.child(message.getImageLocation());

                    final long ONE_MEGABYTE = 512 * 512;
                    islandRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            mImage1.setVisibility(View.VISIBLE);
                            mImage1.setImageBitmap(bitmap);

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle any errors
                            Log.d("IMage",exception.getLocalizedMessage());
                        }
                    });

                    if (message.getTimestamp() != null) {
                        Date currentTime = Calendar.getInstance().getTime();
                        SimpleDateFormat df = new SimpleDateFormat("dd MM yyyy");
                        String currentDate = df.format(currentTime);
                        String messageDate = df.format(message.getTimestamp());

                        if (currentDate.equals(messageDate)) {
                            SimpleDateFormat df1 = new SimpleDateFormat("HH:mm:ss");
                            String time = df1.format(message.getTimestamp());
                            mTime.setText(time);
                        } else {
                            mTime.setText(messageDate);
                        }
                    }

                }else {
                mMessage.setText(message.getMessage());
                if (message.getTimestamp() != null) {
                    Date currentTime = Calendar.getInstance().getTime();
                    SimpleDateFormat df = new SimpleDateFormat("dd MM yyyy");
                    String currentDate = df.format(currentTime);
                    String messageDate = df.format(message.getTimestamp());

                    if (currentDate.equals(messageDate)) {
                        SimpleDateFormat df1 = new SimpleDateFormat("HH:mm:ss");
                        String time = df1.format(message.getTimestamp());
                        mTime.setText(time);
                    } else {
                        mTime.setText(messageDate);
                    }
                }

                }
            }


        }
    }
}
