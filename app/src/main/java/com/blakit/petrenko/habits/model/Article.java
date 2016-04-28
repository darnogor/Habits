package com.blakit.petrenko.habits.model;

import org.parceler.Parcel;

import java.util.UUID;

import io.realm.ArticleRealmProxy;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by user_And on 07.08.2015.
 */
@Parcel(implementations = {ArticleRealmProxy.class},
        value = Parcel.Serialization.BEAN,
        analyze = {Article.class})
public class Article extends RealmObject {

    @PrimaryKey
    private String id;
    private String uri;
    private String title;

    public Article() {
        this.id = UUID.randomUUID().toString();
    }

    public Article(String uri) {
        this.id = UUID.randomUUID().toString();
        this.uri = uri;
    }


    public String getId() {
        return id;
    }


    public void setId(String id) {
        this.id = id;
    }


    public String getUri() {
        return uri;
    }


    public void setUri(String uri) {
        this.uri = uri;
    }


    public String getTitle() {
        return title;
    }


    public void setTitle(String title) {
        this.title = title;
    }
}
