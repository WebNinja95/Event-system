package com.example.eyalandmorad;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
// adapter for comment to show them in the eventdetails 1 by 1
public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {
    private List<Comment> commentsList;
    private FireBase fb;
    private EventDataBase db;
    private Context context;
    private String username;

    public CommentAdapter(List<Comment> commentsList, EventDataBase db, Context context, String username) {
        this.commentsList = commentsList;
        this.db = db;
        this.context = context;
        this.username = username;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = commentsList.get(position);
        holder.userNameCommentTextView.setText(comment.getCreatedUser());
        holder.commentText.setText(comment.getCommentText());
        fb = new FireBase();

        db.openDB();
        if(!comment.getCreatedUser().equals(username)){
            holder.editButton.setVisibility(View.GONE);
            holder.deleteButton.setVisibility(View.GONE);
        } else {
            holder.editButton.setVisibility(View.VISIBLE);
            holder.deleteButton.setVisibility(View.VISIBLE);
        }
        db.closeDB();

        // Set click listeners for the buttons )
        holder.editButton.setOnClickListener(v -> {
            EditCommentDialog dialogFragment = new EditCommentDialog();
            View rootView = ((AppCompatActivity) context).findViewById(android.R.id.content);
            dialogFragment.setParameters(comment.getCommentText(), comment.getCommentId(), db, rootView);
            dialogFragment.show(((AppCompatActivity) context).getSupportFragmentManager(), "EditCommentDialog");
            return;
        });

        holder.deleteButton.setOnClickListener(v -> {
                    // Create an alert dialog to confirm comment deletion
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Deleting comment");
                    builder.setMessage("Are you sure?");
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            View rootView = ((AppCompatActivity) context).findViewById(android.R.id.content);
                            fb.removeCommentFromFireBase(comment.getCommentId(), db, rootView);

                            int position = holder.getAdapterPosition();
                            if (position != RecyclerView.NO_POSITION) {
                                commentsList.remove(comment);
                                notifyItemRemoved(position);
                            }
                        }
                    });
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            return;
                        }
                        // Do nothing if the user cancels the deletion
                    });
                    // Create and display the alert dialog
                    AlertDialog dialog = builder.create();
                    dialog.show();
                    notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return commentsList.size();
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView userNameCommentTextView;
        TextView commentText;
        Button editButton;
        Button deleteButton;

        CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            userNameCommentTextView = itemView.findViewById(R.id.UserNameComment);
            commentText = itemView.findViewById(R.id.commentText);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}