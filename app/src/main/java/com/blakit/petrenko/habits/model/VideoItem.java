package com.blakit.petrenko.habits.model;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.api.client.util.DateTime;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;
import java.math.BigInteger;

/**
 * Created by user_And on 07.08.2015.
 */
@DatabaseTable(tableName = "videos")
public class VideoItem implements Parcelable{

    @DatabaseField(generatedId = true)
    private long id;

    @DatabaseField(dataType = DataType.STRING)
    private String videoId;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "habit_id")
    private Habit habit;

    private String title;
    private String chanel;
    private DateTime publishDate;
    private BigInteger viewsCount;

    private String duration;
    private String thumbnailURL;
    private Bitmap thumbnail;

    VideoItem() {}

    public VideoItem(String id, Habit habit) {
        this.videoId = id;
        this.habit = habit;
    }

    public VideoItem(Parcel source) {
        videoId = source.readString();
        habit = source.readParcelable(Habit.class.getClassLoader());
        title = source.readString();
        chanel = source.readString();
        publishDate = (DateTime) source.readSerializable();
        viewsCount = (BigInteger) source.readSerializable();
        duration = source.readString();
        thumbnailURL = source.readString();
        thumbnail = source.readParcelable(Bitmap.class.getClassLoader());
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String id) {
        this.videoId = id;
    }

    public Habit getHabit() {
        return habit;
    }

    public void setHabit(Habit habit) {
        this.habit = habit;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getChanel() {
        return chanel;
    }

    public void setChanel(String chanel) {
        this.chanel = chanel;
    }

    public DateTime getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(DateTime publishDate) {
        this.publishDate = publishDate;
    }

    public BigInteger getViewsCount() {
        return viewsCount;
    }

    public void setViewsCount(BigInteger viewsCount) {
        this.viewsCount = viewsCount;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getThumbnailURL() {
        return thumbnailURL;
    }

    public void setThumbnailURL(String thumbnailURL) {
        this.thumbnailURL = thumbnailURL;
    }

    public Bitmap getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(Bitmap thumbnail) {
        this.thumbnail = thumbnail;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(videoId);
        dest.writeParcelable(habit, flags);
        dest.writeString(title);
        dest.writeString(chanel);
        dest.writeSerializable(publishDate);
        dest.writeSerializable(viewsCount);
        dest.writeString(duration);
        dest.writeString(thumbnailURL);
        dest.writeParcelable(thumbnail, flags);
    }

    public static Creator<VideoItem> CREATOR = new Creator<VideoItem>() {
        @Override
        public VideoItem createFromParcel(Parcel source) {
            return new VideoItem(source);
        }

        @Override
        public VideoItem[] newArray(int size) {
            return new VideoItem[size];
        }
    };
}
