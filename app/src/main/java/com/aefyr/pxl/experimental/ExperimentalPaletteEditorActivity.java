package com.aefyr.pxl.experimental;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.graphics.Color;
import android.os.Handler;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.aefyr.pxl.R;
import com.aefyr.pxl.custom.ColorCircle;
import com.aefyr.pxl.custom.ColorRect;
import com.aefyr.pxl.util.Utils;

public class ExperimentalPaletteEditorActivity extends AppCompatActivity {
    private ColorCircle[] circles;
    private ColorRect oldRect;
    private ColorRect newRect;
    private ViewGroup notRetardedRoot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_experimental_palette_editor);

        notRetardedRoot = findViewById(R.id.epeaParent);

        circles = new ColorCircle[3];
        circles[0] = findViewById(R.id.epeaTestCircle1);
        circles[0].setColor(Color.RED);
        circles[1] = findViewById(R.id.epeaTestCircle2);
        circles[1].setColor(Color.GREEN);
        circles[2] = findViewById(R.id.epeaTestCircle3);
        circles[2].setColor(Color.BLUE);

        oldRect = findViewById(R.id.epeaColorRectOld);
        newRect = findViewById(R.id.epeaColorRectNew);

        final View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animate(view);
            }
        };

        for(ColorCircle circle: circles)
            circle.setOnClickListener(listener);

    }

    private void animate(final View clickedCircle){
        final ColorCircle newCircle = new ColorCircle(this);
        newCircle.setColor(((ColorCircle)clickedCircle).color());
        final int dp64 = (int) Utils.dpToPx(64, getResources());

        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(dp64, dp64);
        notRetardedRoot.addView(newCircle, params);
        newCircle.setX(getAbsoluteX(clickedCircle));
        newCircle.setY(getAbsoluteY(clickedCircle));

        //Cool looking variant but it relies on ColorRect.setColorWithExplosion implementation
        PropertyValuesHolder x = PropertyValuesHolder.ofFloat(View.TRANSLATION_X, getAbsoluteX(oldRect)+oldRect.getWidth()/2-dp64/2);
        PropertyValuesHolder y = PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, getAbsoluteY(oldRect)+((oldRect.getHeight()-dp64)/2));
        ObjectAnimator positionAnimator = ObjectAnimator.ofPropertyValuesHolder(newCircle, x, y);
        positionAnimator.setDuration(300);
        positionAnimator.setInterpolator(new LinearOutSlowInInterpolator());
        positionAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                notRetardedRoot.removeView(newCircle);
                oldRect.setColorWithExplosion(((ColorCircle) clickedCircle).color(), oldRect.getWidth()/2, oldRect.getHeight()/2, dp64/2);
                Handler temp = new Handler();
                temp.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        newRect.setColorWithExplosion(((ColorCircle) clickedCircle).color(), 0, newRect.getHeight()/2, newRect.getHeight()/2);
                    }
                }, 125);
            }
        });

        //Less cool looking variant but it doesn't rely on ColorRect.setColorWithExplosion implementation
        /*
        PropertyValuesHolder x = PropertyValuesHolder.ofFloat(View.TRANSLATION_X, getAbsoluteX(oldRect)+oldRect.getWidth() - dp64/2);
        PropertyValuesHolder y = PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, getAbsoluteY(oldRect)+((oldRect.getHeight()-dp64)/2));
        ObjectAnimator positionAnimator = ObjectAnimator.ofPropertyValuesHolder(newCircle, x, y);
        positionAnimator.setDuration(300);
        positionAnimator.setInterpolator(new LinearOutSlowInInterpolator());
        positionAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                notRetardedRoot.removeView(newCircle);
                oldRect.setColorWithExplosion(((ColorCircle) clickedCircle).color(), oldRect.getWidth(), oldRect.getHeight()/2, dp64/2);
                newRect.setColorWithExplosion(((ColorCircle) clickedCircle).color(), 0, newRect.getHeight()/2, dp64/2);

            }
        });*/
        positionAnimator.start();
    }

    private float getAbsoluteX(View ofView) {
        if (ofView.getParent() == notRetardedRoot)
            return ofView.getX();
        else
            return ofView.getX() + getAbsoluteX((View) ofView.getParent());
    }

    private float getAbsoluteY(View ofView) {
        if (ofView.getParent() == notRetardedRoot)
            return ofView.getY();
        else
            return ofView.getY() + getAbsoluteY((View) ofView.getParent());
    }
}
