package com.blakit.petrenko.habits.model;

import android.graphics.Bitmap;

import com.google.api.client.util.DateTime;

import org.parceler.Parcel;

import java.math.BigInteger;
import java.util.UUID;

import io.realm.RealmObject;
import io.realm.VideoItemRealmProxy;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

/**
 * Created by user_And on 07.08.2015.
 */
@Parcel(implementations = { VideoItemRealmProxy.class },
        value = org.parceler.Parcel.Serialization.BEAN,
        analyze = { VideoItem.class })
public class VideoItem extends RealmObject {

    @PrimaryKey
    private String id;
    private String videoId;

    private String title;
    private String chanel;
    private String publishDate;
    private String viewsCount;
    private String duration;
    private String thumbnailURL;
    @Ignore
    private Bitmap thumbnail;

    public VideoItem() {
        this.id = UUID.randomUUID().toString();
    }

    public VideoItem(String videoId) {
        this.id = UUID.randomUUID().toString();
        this.videoId = videoId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String id) {
        this.videoId = id;
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

    public String getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(String publishDate) {
        this.publishDate = publishDate;
    }

    public String getViewsCount() {
        return viewsCount;
    }

    public void setViewsCount(String viewsCount) {
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

}
