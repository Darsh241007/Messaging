package com.darsh.messaging;

import android.app.Activity;
import android.app.Dialog;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Constraints;

class OtpLayout {

    private Dialog mDialog;
    private EditText Otp;
    Button verify,resend;

    OtpLayout(Activity activity){
        Dialog dialog = new Dialog(activity);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.otp_xml);
        mDialog = dialog;
        Otp= dialog.findViewById(R.id.editText3);
        verify=dialog.findViewById(R.id.button2);
        resend=dialog.findViewById(R.id.button);

    }

    String getText(){
        return Otp.getText().toString();
    }
    void setError(){
        Otp.setError("Enter correct OTP");
    }

    void show(){
        mDialog.show();
        Window window = mDialog.getWindow();
        window.setLayout(Constraints.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
    }

    void cancel(){
        mDialog.cancel();
    }
}
