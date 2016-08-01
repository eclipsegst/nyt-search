package com.zhaolongzhong.nytimessearch.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.zhaolongzhong.nytimessearch.R;
import com.zhaolongzhong.nytimessearch.models.Article;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ArticleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = ArticleAdapter.class.getSimpleName();

    private static final int ARTICLE_WITH_PHOTO = 0, ARTICLE_NO_PHOTO = 1;
    private List<Article> articles;
    private Context context;

    public ArticleAdapter(Context context, List<Article> articles) {
        this.context = context;
        this.articles = articles;
    }

    private Context getContext() {
        return context;
    }

    @Override
    public int getItemCount() {
        return this.articles.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (!articles.get(position).getThumbnail().isEmpty()) {
            return ARTICLE_WITH_PHOTO;
        } else {
            return ARTICLE_NO_PHOTO;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case ARTICLE_WITH_PHOTO:
                View v1 = inflater.inflate(R.layout.article_item, parent, false);
                viewHolder = new ViewHolderArticleWithPhoto(v1);
                break;
            case ARTICLE_NO_PHOTO:
                View v2 = inflater.inflate(R.layout.article_item_no_photo, parent, false);
                viewHolder = new ViewHolderArticleNoPhoto(v2);
                break;
            default:
                View v0 = inflater.inflate(R.layout.article_item, parent, false);
                viewHolder = new ViewHolderArticleWithPhoto(v0);
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        switch (viewHolder.getItemViewType()) {
            case ARTICLE_WITH_PHOTO:
                ViewHolderArticleWithPhoto v1 = (ViewHolderArticleWithPhoto) viewHolder;
                configureViewHolderWithPhoto(v1, position);
                break;
            case ARTICLE_NO_PHOTO:
                ViewHolderArticleNoPhoto v2 = (ViewHolderArticleNoPhoto) viewHolder;
                configureViewHolderNoPhoto(v2, position);
                break;
            default:
                break;
        }
    }

    private void configureViewHolderWithPhoto(ViewHolderArticleWithPhoto viewHolder, int position) {
        Article article = articles.get(position);
        Glide.with(getContext())
                .load(article.getThumbnail())
                .crossFade()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(viewHolder.thumbnailImageView);
        viewHolder.headlineTextView.setText(article.getHeadline());
    }

    private void configureViewHolderNoPhoto(ViewHolderArticleNoPhoto viewHolder, int position) {
        Article article = articles.get(position);
        viewHolder.headlineTextView.setText(article.getHeadline());
        viewHolder.snippetTextView.setText(article.getSnippet());
    }

    // Clean all elements of the recycler
    public void clear() {
        articles.clear();
        notifyDataSetChanged();
    }

    // Add a list of items
    public void addAll(List<Article> list) {
        articles.addAll(list);
        notifyDataSetChanged();
    }

    public static class ViewHolderArticleNoPhoto extends RecyclerView.ViewHolder {
        @BindView(R.id.article_item_headline_text_view_id) TextView headlineTextView;
        @BindView(R.id.article_item_snippet_text_view_id) TextView snippetTextView;

        public ViewHolderArticleNoPhoto(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    public static class ViewHolderArticleWithPhoto extends RecyclerView.ViewHolder {
        @BindView(R.id.article_item_thumbnail_image_view_id) ImageView thumbnailImageView;
        @BindView(R.id.article_item_headline_text_view_id) TextView headlineTextView;

        public ViewHolderArticleWithPhoto(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
