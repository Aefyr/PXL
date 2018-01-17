package com.aefyr.pxl.experimental;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Handler;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.aefyr.pxl.ColorPicker;
import com.aefyr.pxl.R;
import com.aefyr.pxl.common.Ruler;
import com.aefyr.pxl.custom.ColorCircle;
import com.aefyr.pxl.custom.ColorRect;
import com.aefyr.pxl.palettes.ColorSelectionRecyclerAdapter;
import com.aefyr.pxl.palettes.PaletteMakerH;
import com.aefyr.pxl.palettes.PaletteUtils;
import com.aefyr.pxl.util.Utils;

import java.io.FileNotFoundException;

public class ExperimentalPaletteEditorActivity extends AppCompatActivity {
    private ColorCircle[] circles;
    private ColorRect oldRect;
    private ColorRect newRect;
    private ViewGroup notRetardedRoot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_experimental_palette_editor);

        final RecyclerView paletteRecycler = findViewById(R.id.epeaReycler);
        final ColorSelectionRecyclerAdapter adapter = new ColorSelectionRecyclerAdapter(this, PaletteUtils.loadPalette("Default"));
        paletteRecycler.setAdapter(adapter);
        paletteRecycler.setLayoutManager(new GridLayoutManager(this, (int) (Utils.getScreenWidth(getResources()) / (getResources().getDimensionPixelSize(R.dimen.palette_color_circle_size)+Utils.dpToPx(12, getResources())))));


        adapter.setOnColorInteractionListener(new ColorSelectionRecyclerAdapter.OnColorInteractionListener() {
            @Override
            public void onColorClick(int index) {
                animate(paletteRecycler.getChildAt(index).findViewById(R.id.colorCircle));
            }

            @Override
            public void onColorLongClick(int index) {

            }
        });

        notRetardedRoot = findViewById(R.id.epeaParent);

        /*circles = new ColorCircle[3];
        circles[0] = findViewById(R.id.epeaTestCircle1);
        circles[0].setColor(Color.RED);
        circles[1] = findViewById(R.id.epeaTestCircle2);
        circles[1].setColor(Color.GREEN);
        circles[2] = findViewById(R.id.epeaTestCircle3);
        circles[2].setColor(Color.BLUE);*/

        ColorPicker colorPicker = new ColorPicker(getWindow(), Color.RED);
        colorPicker.setOnColorChangeListener(new ColorPicker.OnColorChangeListener() {
            @Override
            public void onColorChanged(int newColor) {
                newRect.setColor(newColor);
                currentCircle.setColor(newColor);
            }
        });

        oldRect = findViewById(R.id.epeaColorRectOld);
        newRect = findViewById(R.id.epeaColorRectNew);

        final View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animate(view);
                lidlTest();
            }
        };

        /*for(ColorCircle circle: circles)
            circle.setOnClickListener(listener);*/

    }

    private void lidlTest(){
        int color = Color.BLUE;
        System.out.println("blue="+color);
        float r = color & 0x00FF0000;
        float g = color & 0x0000FF00;
        float b = color & 0x000000FF;
        System.out.println(String.format("r = %f; g = %f; b = %f", r,g,b));

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), 322);
    }

    private void lidlTest2(Bitmap test){
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 322 && resultCode == Activity.RESULT_OK) {
            final int dLimit = Ruler.getInstance(this).maxDimensionSize();
            final Bitmap importedImage;
            try {
                importedImage = BitmapFactory.decodeStream(getContentResolver().openInputStream(data.getData()));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Utils.toaster(this, getString(R.string.error));
                return;
            }

            lidlTest2(importedImage);
        }
    }

    private ColorCircle currentCircle;
    private void animate(final View clickedCircle){
        if(currentCircle!=null){
            PropertyValuesHolder x = PropertyValuesHolder.ofFloat(View.SCALE_X, 1f);
            PropertyValuesHolder y = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f);
            ObjectAnimator scaleAnimator = ObjectAnimator.ofPropertyValuesHolder(currentCircle, x, y);
            scaleAnimator.setDuration(100);
            scaleAnimator.start();
        }

        currentCircle = (ColorCircle) clickedCircle;

        final int dp64 = (int) Utils.dpToPx(64, getResources());

        PropertyValuesHolder x = PropertyValuesHolder.ofFloat(View.SCALE_X, 1.1f);
        PropertyValuesHolder y = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.1f);
        ObjectAnimator scaleAnimator = ObjectAnimator.ofPropertyValuesHolder(clickedCircle, x, y);
        scaleAnimator.setDuration(100);
        scaleAnimator.start();

        /*final ColorCircle newCircle = new ColorCircle(this);
        newCircle.setColor(((ColorCircle)clickedCircle).color());

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
        });
        positionAnimator.start();*/
        oldRect.setColorWithExplosion(((ColorCircle) clickedCircle).color(), oldRect.getWidth(), oldRect.getHeight()/2, 0);
        newRect.setColorWithExplosion(((ColorCircle) clickedCircle).color(), 0, newRect.getHeight()/2, 0);
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
