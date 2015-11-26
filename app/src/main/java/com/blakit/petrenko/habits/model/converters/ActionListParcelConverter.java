package com.blakit.petrenko.habits.model.converters;

import android.os.Parcel;

import com.blakit.petrenko.habits.model.Action;

import org.parceler.Parcels;

/**
 * Created by user_And on 21.11.2015.
 */
public class ActionListParcelConverter extends RealmListParcelConverter<Action> {
    @Override
    public void itemToParcel(Action input, Parcel parcel) {
        parcel.writeParcelable(Parcels.wrap(input), 0);
    }

    @Override
    public Action itemFromParcel(Parcel parcel) {
        return Parcels.unwrap(parcel.readParcelable(Action.class.getClassLoader()));
    }
}
