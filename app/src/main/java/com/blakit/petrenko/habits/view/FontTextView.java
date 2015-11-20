package com.blakit.petrenko.habits.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.TextView;

import com.blakit.petrenko.habits.R;

import java.io.IOException;
import java.util.Arrays;

/**
 * Created by user_And on 22.10.2015.
 */
public class FontTextView extends TextView {
    public FontTextView(Context context) {
        super(context);
        initFont(context, null);
    }

    public FontTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initFont(context, attrs);
    }

    public FontTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initFont(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public FontTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initFont(context, attrs);
    }

    private void initFont(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray array = context.getTheme()
                    .obtainStyledAttributes(attrs, R.styleable.FontTextView, 0, 0);
            String fontString = array.getString(R.styleable.FontTextView_font);
            if (fontString != null) {
                Typeface typeface = Typeface.createFromAsset(context.getAssets(),
                        "fonts/" + fontString + ".ttf");
                if (typeface != null) {
                    setTypeface(typeface);
                }
            }
        }
    }
}
