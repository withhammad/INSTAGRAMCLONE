package com.kashdevelopment.development.kashmirstudentportal.adapters.holders;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.kashdevelopment.development.kashmirstudentportal.Constants;
import com.kashdevelopment.development.kashmirstudentportal.R;
import com.kashdevelopment.development.kashmirstudentportal.controllers.LikeController;
import com.kashdevelopment.development.kashmirstudentportal.managers.PostManager;
import com.kashdevelopment.development.kashmirstudentportal.managers.ProfileManager;
import com.kashdevelopment.development.kashmirstudentportal.managers.listeners.OnObjectChangedListener;
import com.kashdevelopment.development.kashmirstudentportal.managers.listeners.OnObjectExistListener;
import com.kashdevelopment.development.kashmirstudentportal.model.Like;
import com.kashdevelopment.development.kashmirstudentportal.model.Post;
import com.kashdevelopment.development.kashmirstudentportal.model.Profile;
import com.kashdevelopment.development.kashmirstudentportal.utils.FormatterUtil;
import com.kashdevelopment.development.kashmirstudentportal.utils.Utils;


public class PostViewHolder extends RecyclerView.ViewHolder {
    public static final String TAG = PostViewHolder.class.getSimpleName();

    private Context context;
    private ImageView postImageView;
    private TextView titleTextView;
    private TextView detailsTextView;
    private TextView likeCounterTextView;
    private ImageView likesImageView;
    private TextView commentsCountTextView;
    private TextView watcherCounterTextView;
    private TextView dateTextView;
    private ImageView authorImageView;
    private ViewGroup likeViewGroup;

    private ProfileManager profileManager;
    private PostManager postManager;

    private LikeController likeController;

    public PostViewHolder(View view, final OnClickListener onClickListener) {
        this(view, onClickListener, true);
    }

    public PostViewHolder(View view, final OnClickListener onClickListener, boolean isAuthorNeeded) {
        super(view);
        this.context = view.getContext();

        postImageView = (ImageView) view.findViewById(R.id.postImageView);
        likeCounterTextView = (TextView) view.findViewById(R.id.likeCounterTextView);
        likesImageView = (ImageView) view.findViewById(R.id.likesImageView);
        commentsCountTextView = (TextView) view.findViewById(R.id.commentsCountTextView);
        watcherCounterTextView = (TextView) view.findViewById(R.id.watcherCounterTextView);
        dateTextView = (TextView) view.findViewById(R.id.dateTextView);
        titleTextView = (TextView) view.findViewById(R.id.titleTextView);
        detailsTextView = (TextView) view.findViewById(R.id.detailsTextView);
        authorImageView = (ImageView) view.findViewById(R.id.authorImageView);
        likeViewGroup = (ViewGroup) view.findViewById(R.id.likesContainer);

        authorImageView.setVisibility(isAuthorNeeded ? View.VISIBLE : View.GONE);

        profileManager = ProfileManager.getInstance(context.getApplicationContext());
        postManager = PostManager.getInstance(context.getApplicationContext());

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = getAdapterPosition();
                if (onClickListener != null && position != RecyclerView.NO_POSITION) {
                    onClickListener.onItemClick(getAdapterPosition(), v);
                }
            }
        });

        likeViewGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = getAdapterPosition();
                if (onClickListener != null && position != RecyclerView.NO_POSITION) {
                    onClickListener.onLikeClick(likeController, position);
                }
            }
        });

        authorImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = getAdapterPosition();
                if (onClickListener != null && position != RecyclerView.NO_POSITION) {
                    onClickListener.onAuthorClick(getAdapterPosition(), v);
                }
            }
        });
    }

    public void bindData(Post post) {

        likeController = new LikeController(context, post, likeCounterTextView, likesImageView, true);

        String title = removeNewLinesDividers(post.getTitle());
        titleTextView.setText(title);
        String description = removeNewLinesDividers(post.getDescription());
        detailsTextView.setText(description);
        likeCounterTextView.setText(String.valueOf(post.getLikesCount()));
        commentsCountTextView.setText(String.valueOf(post.getCommentsCount()));
        watcherCounterTextView.setText(String.valueOf(post.getWatchersCount()));

        CharSequence date = FormatterUtil.getRelativeTimeSpanStringShort(context, post.getCreatedDate());
        dateTextView.setText(date);

        String imageUrl = post.getImagePath();
        int width = Utils.getDisplayWidth(context);
        int height = (int) context.getResources().getDimension(R.dimen.post_detail_image_height);

        // Displayed and saved to cache image, as needs for post detail.
        Glide.with(context)
                .load(imageUrl)
                .centerCrop()
                .override(width, height)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .crossFade()
                .error(R.drawable.ic_stub)
                .into(postImageView);

        if (post.getAuthorId() != null) {
            profileManager.getProfileSingleValue(post.getAuthorId(), createProfileChangeListener(authorImageView));
        }

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            postManager.hasCurrentUserLikeSingleValue(post.getId(), firebaseUser.getUid(), createOnLikeObjectExistListener());
        }
    }

    private String removeNewLinesDividers(String text) {
        int decoratedTextLength = text.length() < Constants.Post.MAX_TEXT_LENGTH_IN_LIST ?
                text.length() : Constants.Post.MAX_TEXT_LENGTH_IN_LIST;
        return text.substring(0, decoratedTextLength).replaceAll("\n", " ").trim();
    }

    private OnObjectChangedListener<Profile> createProfileChangeListener(final ImageView authorImageView) {
        return new OnObjectChangedListener<Profile>() {
            @Override
            public void onObjectChanged(final Profile obj) {
                if (obj.getPhotoUrl() != null) {

                    Glide.with(context)
                            .load(obj.getPhotoUrl())
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .centerCrop()
                            .crossFade()
                            .into(authorImageView);
                }
            }
        };
    }

    private OnObjectExistListener<Like> createOnLikeObjectExistListener() {
        return new OnObjectExistListener<Like>() {
            @Override
            public void onDataChanged(boolean exist) {
                likeController.initLike(exist);
            }
        };
    }

    public interface OnClickListener {
        void onItemClick(int position, View view);

        void onLikeClick(LikeController likeController, int position);

        void onAuthorClick(int position, View view);
    }
}