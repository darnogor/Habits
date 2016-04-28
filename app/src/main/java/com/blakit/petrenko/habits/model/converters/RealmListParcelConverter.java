package com.blakit.petrenko.habits.model.converters;

import org.parceler.converter.CollectionParcelConverter;

import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Created by user_And on 21.11.2015.
 */
public abstract class RealmListParcelConverter<T extends RealmObject>
        extends CollectionParcelConverter<T,RealmList<T>> {
    @Override
    public RealmList<T> createCollection() {
        return new RealmList<T>();
    }
}
