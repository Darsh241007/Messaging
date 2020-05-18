package com.darsh.messaging;

import android.app.Activity;
import android.app.Dialog;
import android.widget.Button;
import android.widget.EditText;

class Single_ui {
    private Dialog Dialog;
    private EditText number;
    Button search,cancel;

    Single_ui(Activity activity){
        Dialog dialog = new Dialog(activity);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.search_ui);
        Dialog = dialog;
        number= dialog.findViewById(R.id.search_editText);
        search=dialog.findViewById(R.id.search);
        cancel=dialog.findViewById(R.id.Cancel);
    }

    String getText(){
        return number.getText().toString();
    }
    void setError(){
        number.setError("Enter a valid number");
    }

    void show(){
        Dialog.show();
    }

    void cancel(){
        Dialog.cancel();
    }
}

