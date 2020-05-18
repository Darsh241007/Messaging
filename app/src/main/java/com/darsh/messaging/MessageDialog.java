package com.darsh.messaging;

import android.app.Activity;
import android.app.Dialog;
import android.widget.TextView;

class MessageDialog {
    private TextView title,message;
    private Dialog mDialog;

    MessageDialog(Activity activity) {
        Dialog dialog = new Dialog(activity);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.loading);
        mDialog = dialog;
        title = dialog.findViewById(R.id.loading_title);
        message = dialog.findViewById(R.id.loading_message);
    }

    void setMessage(String message) {
        this.message.setText(message);
    }

    void setTitle(String title) {
        this.title.setText(title);
    }
    void show(){
        mDialog.show();
    }

    void hide(){
        mDialog.hide();
    }

}
