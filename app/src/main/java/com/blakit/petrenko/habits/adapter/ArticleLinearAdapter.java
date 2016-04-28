package com.blakit.petrenko.habits.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.blakit.petrenko.habits.R;
import com.blakit.petrenko.habits.model.Article;
import com.blakit.petrenko.habits.utils.ColorGenerator;
import com.blakit.petrenko.habits.utils.Utils;
import com.wnafee.vector.MorphButton;

import java.net.MalformedURLException;
import java.net.URL;

import io.realm.RealmList;

/**
 * Created by user_And on 09.02.2016.
 */
public class ArticleLinearAdapter extends RecyclerView.Adapter<ArticleLinearAdapter.ArticlesViewHolder> {

    private Context context;
    private RecyclerView parent;
    private RealmList<Article> articles;


    public ArticleLinearAdapter(Context context, RecyclerView parent, RealmList<Article> articles) {
        this.context = context;
        this.parent = parent;
        this.articles = articles;

        updateHeight();
    }


    private void updateHeight() {
        if (articles.size() > 0) {
            parent.getLayoutParams().height = Utils.dpToPx(context, 154);
        }
    }

    @Override
    public ArticlesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_article_card_tile, parent, false);

        return new ArticlesViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ArticlesViewHolder holder, int position) {
        final Article article = articles.get(position);

        try {
            holder.resURL.setText(new URL(article.getUri()).getAuthority());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        holder.coloredImg.setForegroundTintList(ColorStateList.valueOf(ColorGenerator.MATERIAL.getRandomColor()));
        holder.title.setText(article.getTitle());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(article.getUri()));
                context.startActivity(i);
            }
        });
        Utils.setMaxLinesByMaxHeight(holder.title, Utils.dpToPx(context, 84));
    }

    @Override
    public int getItemCount() {
        updateHeight();
        return articles.size();
    }


    public class ArticlesViewHolder extends RecyclerView.ViewHolder {

        private View itemView;
        private MorphButton coloredImg;
        private TextView title;
        private TextView resURL;

        public ArticlesViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;

            coloredImg = (MorphButton) itemView.findViewById(R.id.article_item_img_back);
            title      = (TextView)    itemView.findViewById(R.id.article_item_title);
            resURL     = (TextView)    itemView.findViewById(R.id.article_item_resource_url);
        }
    }
}
