package com.example.eyalandmorad;

import android.app.Dialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialogFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class EditCommentDialog extends AppCompatDialogFragment {

    private EditText commentEditText;
    private Button editButton;
    private Button cancelButton;
    private FireBase fb;
    private static final int DIALOG_WIDTH_DP = 300;
    private String commentText;
    private String commentId;
    private EventDataBase db;
    private View rootView;

    @NonNull
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_comment, container, false);
        commentEditText = view.findViewById(R.id.commentEditText);
        commentEditText.setText(commentText);
        editButton = view.findViewById(R.id.editButton);
        cancelButton = view.findViewById(R.id.cancelButton);
        fb = new FireBase();

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                commentText = commentEditText.getText().toString();
                fb.editComment(commentId, commentText, db, rootView);
                dismiss();
            }
        });
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        Dialog dialog = getDialog();
        if (dialog != null) {
            float density = getResources().getDisplayMetrics().density;
            int widthPx = (int) (DIALOG_WIDTH_DP * density);

            WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
            params.width = widthPx;
            dialog.getWindow().setAttributes(params);
        }
    }

    public void setParameters(String commentText, String commentId, EventDataBase db, View rootView) {
        this.commentText = commentText;
        this.commentId = commentId;
        this.db = db;
        this.rootView = rootView;
    }

}