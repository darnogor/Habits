package com.blakit.petrenko.habits.utils;

/**
 * Created by user_And on 07.04.2016.
 */
public interface Callable<Param, Result> {
    Result call(Param... params);
}
