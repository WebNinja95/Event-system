package com.example.eyalandmorad;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;
//i create this class to show all the comments in the database in listview exept the comments that the user do
public class CommentsAdapterList extends ArrayAdapter<Comment> {

    private List<Comment> commentsList;

    public CommentsAdapterList(Context context, List<Comment> commentsList) {
        super(context, 0, commentsList);
        this.commentsList = commentsList;
    }
    //its take the postion of the list and show the event with the comment class 1 by 1
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_comment, parent, false);
        }

        Comment comment = commentsList.get(position);

        TextView userTextView = convertView.findViewById(R.id.textViewUserName);
        TextView commentTextView = convertView.findViewById(R.id.textViewCommentText);

        userTextView.setText("User: " + comment.getCreatedUser());
        commentTextView.setText("Comment: " + comment.getCommentText());

        return convertView;
    }
}
