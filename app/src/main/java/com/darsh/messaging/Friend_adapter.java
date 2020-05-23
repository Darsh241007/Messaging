package com.darsh.messaging;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class Friend_adapter extends RecyclerView.Adapter<Friend_adapter.SingleFriend> implements EventListener<QuerySnapshot> {

    private LayoutInflater mInflater;
    private List<Friend> mFriendList = new ArrayList<>();
    private Context context;

    Friend_adapter(Context context){
        this.context = context;
        mInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public Friend_adapter.SingleFriend onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = mInflater.inflate(R.layout.single_friendui, parent, false);
        return new SingleFriend(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Friend_adapter.SingleFriend holder, int position) {
        holder.bindToView(mFriendList.get(position),context);
    }

    @Override
    public int getItemCount() {
        return mFriendList.size();
    }


    @Override
    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
        mFriendList.clear();
        if (queryDocumentSnapshots != null){
            for (QueryDocumentSnapshot QueryDocument: queryDocumentSnapshots){
                Log.d("Datass",QueryDocument.getData().toString());
                Friend friend = new Friend();
                friend.setName(QueryDocument.getData().get("Name").toString());
                friend.setPhone(QueryDocument.getData().get("phone").toString());
                mFriendList.add(friend);
                notifyDataSetChanged();
            }
        }
    }


    static class SingleFriend extends RecyclerView.ViewHolder {
        private TextView mName;
        private TextView mphone;
        private View mRoot;
        SingleFriend(@NonNull View itemView) {
            super(itemView);
            mName=itemView.findViewById(R.id.fname);
            mphone=itemView.findViewById(R.id.fnumber);
            mRoot = itemView.findViewById(R.id.friend_Layout);
        }

        void bindToView(Friend friend, final Context context) {
            mName.setText(friend.getName());
            mphone.setText(friend.getPhone());
            mRoot.setOnClickListener(v -> {
                Intent ChatsIntent=new Intent(context,Activity_chats.class);
                ChatsIntent.putExtra("value_phone",mphone.getText());
                ChatsIntent.putExtra("value_name",mName.getText());
                ChatsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(ChatsIntent);
            });
        }
    }
}
