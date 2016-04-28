package com.blakit.petrenko.habits.model.converters;

import android.os.Parcel;

import com.blakit.petrenko.habits.model.Article;

import org.parceler.Parcels;

/**
 * Created by user_And on 21.11.2015.
 */
public class ArticleListParcelConverter extends RealmListParcelConverter<Article> {
    @Override
    public void itemToParcel(Article input, Parcel parcel) {
        parcel.writeParcelable(Parcels.wrap(input), 0);
    }

    @Override
    public Article itemFromParcel(Parcel parcel) {
        return Parcels.unwrap(parcel.readParcelable(Article.class.getClassLoader()));
    }
}
