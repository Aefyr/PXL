package com.aefyr.pxl.custom;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

/**
 * Created by Aefyr on 17.01.2018.
 */

public class ColorExplosionFX {
    private static final String TAG = "ColorExplosionFX";

    private ArrayList<Explosion> currentExplosions;
    private Paint sharedPaint;
    private View boundView;

    public ColorExplosionFX(View bindTo){
        currentExplosions = new ArrayList<>();
        boundView = bindTo;

        sharedPaint = new Paint();
        sharedPaint.setAntiAlias(true);
        sharedPaint.setStyle(Paint.Style.FILL_AND_STROKE);
    }

    public ExplosionTracker spawnExplosion(int atX, int atY, int fromRadius, int toRadius, int forDuration, int color){
        ExplosionTracker explosionTracker = new ExplosionTracker();
        currentExplosions.add(new Explosion(atX, atY, fromRadius, toRadius, forDuration, color, explosionTracker));

        return explosionTracker;
    }

    void renderOn(Canvas c){
        long start = System.currentTimeMillis();

        for(Explosion explosion: currentExplosions){
            sharedPaint.setColor(explosion.color);
            c.drawCircle(explosion.x, explosion.y, explosion.currentRadius, sharedPaint);
        }

        Log.d(TAG, String.format("Rendered %d explosions in %d ms.", currentExplosions.size(), System.currentTimeMillis()-start));
    }

    public class ExplosionTracker{
        private Runnable onEnd;
        private boolean alive = true;

        public void doOnExplosionEnd(Runnable r){
            checkIfAlive();
            onEnd = r;
        }

        void die(){
            alive = false;
        }

        private void checkIfAlive(){
            if(!alive)
                throw new IllegalStateException("The Explosion bound to this tracker has already ended");
        }

    }


    private class Explosion{
        int currentRadius;
        int x;
        int y;

        int color;

        private ValueAnimator animator;

        Explosion(int centerX, int centerY, int fromRadius, int toRadius, int duration, int color, final ExplosionTracker tracker){
            this.currentRadius = fromRadius;
            x = centerX;
            y = centerY;

            this.color = color;

            animator = ValueAnimator.ofInt(fromRadius, toRadius);
            animator.setDuration(duration);

            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    currentRadius = (int)animation.getAnimatedValue();
                    boundView.invalidate();
                }
            });
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);

                    tracker.onEnd.run();
                    tracker.die();

                    currentExplosions.remove(Explosion.this);
                }
            });
            animator.start();
        }
    }
}
