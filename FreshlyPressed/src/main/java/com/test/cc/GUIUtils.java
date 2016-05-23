/**
 * Created by Leonardo Tomé on 23/05/16.
 *
 */

package com.test.cc;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.os.Looper;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

/**
 * GUI utility methods
 */
public class GUIUtils {

    /**
     * View switch animation.
     * @param showView view to show. Can be null.
     * @param hideView view to hide. Can be null.
     * @param hidden View.GONE o View.INVISIBLE
     */
    public static void alphaSwitch(final View showView, final View hideView, final int hidden){
        if(Looper.myLooper() != Looper.getMainLooper()) {
            View v = showView != null ? showView : hideView;
            v.post(new Runnable() {
                @Override
                public void run() {
                    alphaSwitch(showView, hideView, hidden);
                }
            });
            return;
        }
        int shortAnimTime;

        if(showView != null){
            shortAnimTime = showView.getResources().getInteger(android.R.integer.config_shortAnimTime);
            showView.setAlpha(0);
            showView.setVisibility(View.VISIBLE);
            ObjectAnimator anim1 = ObjectAnimator.ofFloat(showView, "alpha", 0, 1).setDuration(shortAnimTime);
            anim1.setInterpolator(new AccelerateInterpolator());
            anim1.start();

            if(hideView == null)
                return;
        }

        shortAnimTime = hideView.getResources().getInteger(android.R.integer.config_shortAnimTime);
        hideView.setAlpha(1);
        hideView.setVisibility(View.VISIBLE);
        ObjectAnimator anim2 = ObjectAnimator.ofFloat(hideView, "alpha", 1, 0).setDuration(shortAnimTime);
        anim2.setInterpolator(new DecelerateInterpolator());
        anim2.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                hideView.setVisibility(hidden);
            }
        });

        anim2.start();
    }
}
