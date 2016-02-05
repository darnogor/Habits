package com.blakit.petrenko.habits.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.blakit.petrenko.habits.R;

/**
 * Created by user_And on 30.01.2016.
 */
public class DaysItemDecoration extends RecyclerView.ItemDecoration {

    private Paint paint;

    public DaysItemDecoration(Context context) {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(ContextCompat.getColor(context, R.color.md_grey_200));
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(6);
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(c, parent, state);

        final RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();

        for (int i = 0; i < parent.getChildCount(); ++i) {
            final View child = parent.getChildAt(i);

            int left    = layoutManager.getDecoratedLeft(child);
            int top     = layoutManager.getDecoratedTop(child);
            int right   = layoutManager.getDecoratedRight(child);
            int bottom  = layoutManager.getDecoratedBottom(child);

            int cx = left + (right - left) / 2;
            int cy = top + (bottom - top) / 2;
            int radius = (right - left) / 3;

            c.drawCircle(cx, cy, radius, paint);

            if (i == 0) {
                c.drawLine(cx, cy, right, cy, paint);
            } else if (i == parent.getChildCount()-1) {
                c.drawLine(left, cy, cx, cy, paint);
            } else {
                c.drawLine(left, cy, right, cy, paint);
            }
        }
    }
}
