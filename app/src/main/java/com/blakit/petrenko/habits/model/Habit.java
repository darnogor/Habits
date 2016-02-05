package com.blakit.petrenko.habits.model;

import android.support.annotation.NonNull;

import com.blakit.petrenko.habits.model.converters.ActionListParcelConverter;
import com.blakit.petrenko.habits.model.converters.ArticleListParcelConverter;
import com.blakit.petrenko.habits.model.converters.VideoItemParcelConverter;

import org.parceler.Parcel;
import org.parceler.ParcelPropertyConverter;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import io.realm.HabitRealmProxy;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by user_And on 15.07.2015.
 */
@Parcel(implementations = { HabitRealmProxy.class },
        value = Parcel.Serialization.BEAN,
        analyze = { Habit.class })
public class Habit extends RealmObject{

    @PrimaryKey
    private String id;
    private String name;
    private String author;
    private boolean isPublic;
    private String description;
    private Category category;
    private RealmList<Action> actions;
    private RealmList<VideoItem> relatedVideoItems;
    private RealmList<Article> relatedArticles;

    private int addCount;
    private int completeCount;

    private Date creationDate;

    public Habit() {
        this.id = UUID.randomUUID().toString();
        this.relatedVideoItems = new RealmList<>();
        this.relatedArticles = new RealmList<>();
        this.addCount = 0;
        this.completeCount = 0;
        this.creationDate = new Date();
    }

    public Habit(@NonNull String name, String action) {
        this();
        this.name = name;
        this.actions = new RealmList<>();
        for (int i = 0; i < 21; ++i) {
            this.actions.add(new Action(action, i+1));
        }
    }

    public Habit(@NonNull String name, List<Action> actions) {
        this();
        this.name = name;
        this.actions = new RealmList<>();
        for (Action a: actions) {
            this.actions.add(a);
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setIsPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public RealmList<Action> getActions() {
        return actions;
    }

    @ParcelPropertyConverter(ActionListParcelConverter.class)
    public void setActions(RealmList<Action> actions) {
        this.actions = actions;
    }

    public RealmList<VideoItem> getRelatedVideoItems() {
        return relatedVideoItems;
    }

    @ParcelPropertyConverter(VideoItemParcelConverter.class)
    public void setRelatedVideoItems(RealmList<VideoItem> relatedVideoItems) {
        this.relatedVideoItems = relatedVideoItems;
    }

    public RealmList<Article> getRelatedArticles() {
        return relatedArticles;
    }

    @ParcelPropertyConverter(ArticleListParcelConverter.class)
    public void setRelatedArticles(RealmList<Article> relatedArticles) {
        this.relatedArticles = relatedArticles;
    }

    public int getAddCount() {
        return addCount;
    }

    public void setAddCount(int addCount) {
        this.addCount = addCount;
    }

    public int getCompleteCount() {
        return completeCount;
    }

    public void setCompleteCount(int completeCount) {
        this.completeCount = completeCount;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }
}
