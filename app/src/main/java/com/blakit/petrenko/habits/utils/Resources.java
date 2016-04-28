package com.blakit.petrenko.habits.utils;

import android.content.Context;
import android.graphics.Typeface;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by user_And on 26.11.2015.
 */
public class Resources {
    private static Resources resources;
    private static boolean isInit = false;

    private Map<String, Typeface> typefaceMap;

    public static Resources getInstance() {
        if (resources == null) {
            resources = new Resources();
        }
        return resources;
    }

    public Resources() {
        typefaceMap = new HashMap<>();
    }

    public void loadResources(Context context) {
        if (isInit) {
            return;
        }
        typefaceMap.put("OpenSans-Bold", Typeface.createFromAsset(context.getAssets(),
                "fonts/OpenSans-Bold.ttf"));
        typefaceMap.put("OpenSans-BoldItalic", Typeface.createFromAsset(context.getAssets(),
                "fonts/OpenSans-BoldItalic.ttf"));
        typefaceMap.put("OpenSans-ExtraBold", Typeface.createFromAsset(context.getAssets(),
                "fonts/OpenSans-ExtraBold.ttf"));
        typefaceMap.put("OpenSans-ExtraBoldItalic", Typeface.createFromAsset(context.getAssets(),
                "fonts/OpenSans-ExtraBoldItalic.ttf"));
        typefaceMap.put("OpenSans-Italic", Typeface.createFromAsset(context.getAssets(),
                "fonts/OpenSans-Italic.ttf"));
        typefaceMap.put("OpenSans-Light", Typeface.createFromAsset(context.getAssets(),
                "fonts/OpenSans-Light.ttf"));
        typefaceMap.put("OpenSans-LightItalic", Typeface.createFromAsset(context.getAssets(),
                "fonts/OpenSans-LightItalic.ttf"));
        typefaceMap.put("OpenSans-Regular", Typeface.createFromAsset(context.getAssets(),
                "fonts/OpenSans-Regular.ttf"));
        typefaceMap.put("OpenSans-Semibold", Typeface.createFromAsset(context.getAssets(),
                "fonts/OpenSans-Semibold.ttf"));
        typefaceMap.put("OpenSans-SemiboldItalic", Typeface.createFromAsset(context.getAssets(),
                "fonts/OpenSans-SemiboldItalic.ttf"));

        isInit = true;
    }

    public Typeface getTypeface(String fontName) {
        return typefaceMap.get(fontName);
    }
}
