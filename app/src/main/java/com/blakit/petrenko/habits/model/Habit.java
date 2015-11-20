package com.blakit.petrenko.habits.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.blakit.petrenko.habits.HabitApplication;
import com.blakit.petrenko.habits.dao.HabitDao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by user_And on 15.07.2015.
 */
@DatabaseTable(tableName = "habits")
public class Habit implements Parcelable {

    @DatabaseField(generatedId = true)
    private long id;

    @DatabaseField(dataType = DataType.STRING, canBeNull = false)
    private String name;

    @DatabaseField(dataType = DataType.STRING)
    private String author;

    @DatabaseField(dataType = DataType.BOOLEAN)
    private boolean isPublic;

    @DatabaseField(dataType = DataType.STRING, canBeNull = false)
    private String description;

    @ForeignCollectionField(eager = true, columnName = "actions")
    private Collection<Action> actions;

    @ForeignCollectionField(eager = true, columnName = "related_videos")
    private Collection<VideoItem> relatedVideoItems;

    @ForeignCollectionField(eager = true, columnName = "related_articles")
    private Collection<Article> relatedArticles;


    private boolean isRootParcelable = true;

    Habit() {}

    public Habit(String name, String action) {
        try {
            HabitDao dao = HabitApplication.getInstance().getDbOpenHelper().getHabitDao();
            this.name = name;
            this.actions = dao.getDao().getEmptyForeignCollection("actions");
            for (int i = 0; i < 21; ++i) {
                this.actions.add(new Action(action, i));
            }
            this.relatedVideoItems = dao.getDao().getEmptyForeignCollection("related_videos");
            this.relatedArticles = dao.getDao().getEmptyForeignCollection("related_articles");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Habit(String name, List<Action> actions) {
        this.name = name;
        this.actions = actions;
        this.relatedVideoItems = new ArrayList<>();
        this.relatedArticles = new ArrayList<>();
    }

    public Habit(Parcel source) {
        try {
            HabitDao dao = HabitApplication.getInstance().getDbOpenHelper().getHabitDao();
            id = source.readInt();
            name = source.readString();
            author = source.readString();
            description = source.readString();
            isPublic = source.readByte() == 1;

            Collection<Action> newActionList = dao.getDao().getEmptyForeignCollection("actions");
            for (int i = 0; i < source.readInt(); ++i) {
                newActionList.add((Action) source.readSerializable());
            }
            actions = newActionList;

            Collection<VideoItem> newVideoList = dao.getDao().getEmptyForeignCollection("related_videos");
            for (int i = 0; i < source.readInt(); i++) {
                newVideoList.add((VideoItem) source.readParcelable(VideoItem.class.getClassLoader()));
            }
            relatedVideoItems = newVideoList;

            Collection<Article> newArticleList = dao.getDao().getEmptyForeignCollection("related_articles");
            for (int i = 0; i < source.readInt(); ++i) {
                newArticleList.add((Article) source.readSerializable());
            }
            relatedArticles = newArticleList;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
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

    public Collection<Action> getActions() {
        return actions;
    }

    public void setActions(List<Action> actions) {
        this.actions = actions;
    }

    public Collection<VideoItem> getRelatedVideoItems() {
        return relatedVideoItems;
    }

    public void setRelatedVideoItems(List<VideoItem> relatedVideoItems) {
        this.relatedVideoItems = relatedVideoItems;
    }

    public Collection<Article> getRelatedArticles() {
        return relatedArticles;
    }

    public void setRelatedArticles(List<Article> relatedArticles) {
        this.relatedArticles = relatedArticles;
    }

    public Action getAction(int day) {
        for (Action a: actions) {
            if (a.getDay() == day) {
                return a;
            }
        }
        return null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if(!isRootParcelable) {
            return;
        }
        isRootParcelable = false;
        dest.writeLong(id);
        dest.writeString(name);
        dest.writeString(author);
        dest.writeString(description);
        dest.writeByte((byte) (isPublic ? 1 : 0));
        dest.writeInt(actions.size());
        for (Action action : actions) {
            dest.writeSerializable(action);
        }
        dest.writeInt(relatedVideoItems.size());
        for (VideoItem video: relatedVideoItems) {
            dest.writeParcelable(video, flags);
        }
        dest.writeInt(relatedArticles.size());
        for (Article article: relatedArticles) {
            dest.writeSerializable(article);
        }
    }

    public static Creator<Habit> CREATOR = new Creator<Habit>() {
        @Override
        public Habit createFromParcel(Parcel source) {
            return new Habit(source);
        }

        @Override
        public Habit[] newArray(int size) {
            return new Habit[size];
        }
    };
}
