

package com.kashdevelopment.development.kashmirstudentportal.managers;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ValueEventListener;
import com.kashdevelopment.development.kashmirstudentportal.ApplicationHelper;
import com.kashdevelopment.development.kashmirstudentportal.managers.listeners.OnDataChangedListener;
import com.kashdevelopment.development.kashmirstudentportal.managers.listeners.OnTaskCompleteListener;
import com.kashdevelopment.development.kashmirstudentportal.model.Comment;
import com.kashdevelopment.development.kashmirstudentportal.utils.LogUtil;

public class CommentManager extends FirebaseListenersManager {

    private static final String TAG = CommentManager.class.getSimpleName();
    private static CommentManager instance;

    private Context context;

    public static CommentManager getInstance(Context context) {
        if (instance == null) {
            instance = new CommentManager(context);
        }

        return instance;
    }

    private CommentManager(Context context) {
        this.context = context;
    }

    public void createOrUpdateComment(String commentText, String postId, OnTaskCompleteListener onTaskCompleteListener) {
        ApplicationHelper.getDatabaseHelper().createComment(commentText, postId, onTaskCompleteListener);
    }

    public void decrementCommentsCount(String postId, OnTaskCompleteListener onTaskCompleteListener) {
        ApplicationHelper.getDatabaseHelper().decrementCommentsCount(postId, onTaskCompleteListener);
    }

    public void getCommentsList(Context activityContext, String postId, OnDataChangedListener<Comment> onDataChangedListener) {
        ValueEventListener valueEventListener = ApplicationHelper.getDatabaseHelper().getCommentsList(postId, onDataChangedListener);
        addListenerToMap(activityContext, valueEventListener);
    }

    public void removeComment(String commentId, final String postId, final OnTaskCompleteListener onTaskCompleteListener) {
        final DatabaseHelper databaseHelper = ApplicationHelper.getDatabaseHelper();

        databaseHelper.removeComment(commentId, postId).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                decrementCommentsCount(postId, onTaskCompleteListener);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                onTaskCompleteListener.onTaskComplete(false);
                LogUtil.logError(TAG, "removeComment()", e);
            }
        });
    }

    public void updateComment(String commentId, String commentText, String postId, OnTaskCompleteListener onTaskCompleteListener) {
        ApplicationHelper.getDatabaseHelper().updateComment(commentId, commentText, postId, onTaskCompleteListener);
    }
}
