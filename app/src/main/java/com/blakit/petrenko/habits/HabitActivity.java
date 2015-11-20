package com.blakit.petrenko.habits;

import android.animation.AnimatorSet;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LayoutAnimationController;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

/**
 * Created by user_And on 18.07.2015.
 */
public class HabitActivity extends AppCompatActivity {

    private static final String PACKAGE_NAME = "com.blakit.petrenko.habits";

    private int originalOrientation;

    private LinearLayout topLevelLayout;
    private RelativeLayout habitLayout;
    private ImageButton notificationButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.habit_activity);

        topLevelLayout = (LinearLayout) findViewById(R.id.habit_layout_top_level);
        habitLayout = (RelativeLayout) findViewById(R.id.habit_layout);
        notificationButton = (ImageButton) findViewById(R.id.habit_notification);

        Bundle bundle = getIntent().getExtras();
        final int thumbnailTop = bundle.getInt(PACKAGE_NAME + ".top");
        final int thumbnailLeft = bundle.getInt(PACKAGE_NAME + ".left");
        final int thumbnailWidth = bundle.getInt(PACKAGE_NAME + ".width");
        final int thumbnailHeight = bundle.getInt(PACKAGE_NAME + ".height");
        originalOrientation = bundle.getInt(PACKAGE_NAME + ".orientation");

        final ImageView back = (ImageView) findViewById(R.id.habit_back);

        if(savedInstanceState == null) {

            ViewTreeObserver observer = back.getViewTreeObserver();
            observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {

                @Override
                public boolean onPreDraw() {
                    back.getViewTreeObserver().removeOnPreDrawListener(this);

                    int[] screenLocation = new int[2];
                    habitLayout.getLocationOnScreen(screenLocation);


                    float scaleX = (float) thumbnailWidth / back.getWidth();
                    float scaleY = (float) thumbnailHeight / back.getHeight();

                    back.setPivotX((thumbnailLeft - screenLocation[0] + thumbnailWidth) / 2);
                    back.setPivotY((thumbnailTop - screenLocation[1] + thumbnailHeight) / 2);
                    back.setScaleX(scaleX);
                    back.setScaleY(scaleY);
//                    back.setTranslationX(thumbnailLeft - screenLocation[0]);
//                    back.setTranslationY(thumbnailTop - screenLocation[1]);
//
//                    back.animate().setDuration(500).setStartDelay(200)
//                            .translationX(0).translationY(0)
//                            .setInterpolator(new LinearInterpolator())
//                            .start();

                    back.animate().setDuration(500).setStartDelay(520)
                            .scaleX(1).scaleY(1)
                            .setInterpolator(new AccelerateInterpolator())
                            .start();

                    return true;
                }
            });


//            AnimatorSet animatorSet = new AnimatorSet();
//            animatorSet.playTogether(
//                    ObjectAnimator.ofFloat(habitLayout, "scaleX", scaleX, 1.0f),
//                    ObjectAnimator.ofFloat(habitLayout, "scaleY", scaleY, 1.0f),
//                    ObjectAnimator.ofFloat(habitLayout, "translationX", thumbnailLeft, 0),
//                    ObjectAnimator.ofFloat(habitLayout, "translationY", thumbnailTop, 0)
//            );
//            animatorSet.setDuration(600).setInterpolator(new DecelerateInterpolator());
//            animatorSet.start();


//            AnimationSet set = new AnimationSet(true);
//            set.addAnimation(new ScaleAnimation(scaleX, 1, scaleY, 1));
//            set.addAnimation(new TranslateAnimation(thumbnailLeft, 0, thumbnailTop, 0));
//            set.setDuration(500);
//            set.setInterpolator(new DecelerateInterpolator());
//
//            LayoutAnimationController controller = habitLayout.getLayoutAnimation();
//            if (controller == null)
//                controller = new LayoutAnimationController(set);
//            else
//                controller.setAnimation(set);
//            habitLayout.setLayoutAnimation(controller);

//            LayoutTransition layoutTransition = new LayoutTransition();
//            layoutTransition.setAnimator(LayoutTransition.APPEARING, animatorSet);
//            layoutTransition.setDuration(500);
//            layoutTransition.setInterpolator(LayoutTransition.APPEARING, new DecelerateInterpolator());

//            habitLayout.setLayoutTransition(layoutTransition);

//            TransitionSet transitionSet = new TransitionSet();
//            transitionSet.addTransition(new ChangeBounds());
//
//            transitionSet.setOrdering(TransitionSet.ORDERING_TOGETHER);
//            transitionSet.setDuration(400);
        }

    }
}
