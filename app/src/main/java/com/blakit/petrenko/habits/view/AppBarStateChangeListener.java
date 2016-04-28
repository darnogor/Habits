package com.blakit.petrenko.habits.view;

import android.support.design.widget.AppBarLayout;
import android.util.Log;

/**
 * Created by user_And on 30.01.2016.
 */
public abstract class AppBarStateChangeListener implements AppBarLayout.OnOffsetChangedListener {

    private final int toolbarHeight;

    public AppBarStateChangeListener(int toolbarHeight) {
        this.toolbarHeight = toolbarHeight;
    }

    public enum State {
        EXPANDED,
        COLLAPSED,
        IDLE
    }

    private State mCurrentState = State.IDLE;

    @Override
    public final void onOffsetChanged(AppBarLayout appBarLayout, int i) {
        Log.d("!!!!OffsetChanged","totalScrollRange="+appBarLayout.getTotalScrollRange());
        Log.d("!!!!OffsetChanged","offset="+i);
        Log.d("!!!!OffsetChanged","toolbarHeight="+toolbarHeight);
        if (Math.abs(i) <= appBarLayout.getTotalScrollRange()- toolbarHeight) {
            if (mCurrentState != State.EXPANDED) {
                onStateChanged(appBarLayout, State.EXPANDED);
            }
            mCurrentState = State.EXPANDED;
        } else if (Math.abs(i) >= appBarLayout.getTotalScrollRange()) {
            if (mCurrentState != State.COLLAPSED) {
                onStateChanged(appBarLayout, State.COLLAPSED);
            }
            mCurrentState = State.COLLAPSED;
        } else {
            if (mCurrentState != State.IDLE) {
                onStateChanged(appBarLayout, State.IDLE);
            }
            mCurrentState = State.IDLE;
        }
    }

    public abstract void onStateChanged(AppBarLayout appBarLayout, State state);
}
