package com.example.eyalandmorad;

import android.app.Dialog;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;


public class add_comment_dialog extends AppCompatDialogFragment {

    private EditText commentEditText;
    private Button addButton;
    private Button cancelButton;
    private FireBase fb;
    private static final int DIALOG_WIDTH_DP = 300;
    private String username;
    private String eventId;
    private EventDataBase db;
    private View rootView;

    @NonNull
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_comment_dialog, container, false);
        commentEditText = view.findViewById(R.id.commentEditText);
        addButton = view.findViewById(R.id.addButton);
        cancelButton = view.findViewById(R.id.cancelButton);
        fb = new FireBase();

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //add to firebase the comments and sync
                Comment comment = new Comment(username, commentEditText.getText().toString(), eventId);
                fb.addCommentToFireBase(comment, db, rootView);
                dismiss();
            }
        });
        return view;
    }
 // It sets the width of the dialog to a specific value (DIALOG_WIDTH_DP) measured in density-independent pixels (dp). The code first checks if the dialog exists (getDialog() != null), then calculates the width in pixels using the device's display density. Finally, it updates the dialog's width using the calculated pixel value, ensuring a consistent appearance across different screen densities.
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

    public void setParameters(String username, String eventId, EventDataBase db, View rootView) {
        this.username = username;
        this.eventId = eventId;
        this.db = db;
        this.rootView = rootView;
    }

}