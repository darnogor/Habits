package com.blakit.petrenko.habits.model;

import java.util.Date;
import java.util.UUID;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by user_And on 16.01.2016.
 */
public class SearchHistory extends RealmObject {

    @PrimaryKey
    private String id;
    private String word;
    private Date date;

    public SearchHistory() {
        this.id = UUID.randomUUID().toString();
    }

    public SearchHistory(String word) {
        this(word, new Date());
    }

    public SearchHistory(String word, Date date) {
        this();
        this.word = word;
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
