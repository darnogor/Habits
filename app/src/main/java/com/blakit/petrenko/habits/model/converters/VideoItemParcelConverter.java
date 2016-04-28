package com.blakit.petrenko.habits.model.converters;

import android.os.Parcel;

import com.blakit.petrenko.habits.model.VideoItem;

import org.parceler.Parcels;

/**
 * Created by user_And on 21.11.2015.
 */
public class VideoItemParcelConverter extends RealmListParcelConverter<VideoItem> {
    @Override
    public void itemToParcel(VideoItem input, Parcel parcel) {
        parcel.writeParcelable(Parcels.wrap(input), 0);
    }

    @Override
    public VideoItem itemFromParcel(Parcel parcel) {
        return Parcels.unwrap(parcel.readParcelable(VideoItem.class.getClassLoader()));
    }
}
