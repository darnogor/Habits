package com.blakit.petrenko.habits.model;

import org.parceler.Parcel;

import io.realm.RealmObject;
import io.realm.VideoItemRealmProxy;
import io.realm.annotations.PrimaryKey;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by user_And on 07.08.2015.
 */
@Parcel(implementations = { VideoItemRealmProxy.class },
        value = org.parceler.Parcel.Serialization.BEAN,
        analyze = { VideoItem.class })
public class VideoItem extends RealmObject {

    @PrimaryKey
    private String videoId;

    private String title;
    private String chanel;
    private String publishDate;
    private String viewsCount;
    private String duration;
    private String thumbnailURL;


    public VideoItem() {
    }


    public VideoItem(String videoId) {
        this.videoId = videoId;
    }


    public String getVideoId() {
        return videoId;
    }


    public void setVideoId(String videoId) {
        this.videoId = videoId;
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
}
