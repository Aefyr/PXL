package com.aefyr.pxl.TCA;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.aefyr.pxl.R;

public class TutorialActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);

        final TutorialFrame[] frames = new TutorialFrame[3];
        frames[0] = new TutorialFrame("Frame Three!", "Frame One!", R.drawable.cogwheel);
        frames[1] = new TutorialFrame("Frame Three!", "Frame Two!", R.drawable.pencil);
        frames[2] = new TutorialFrame("Frame Three!", "Frame Three!", R.drawable.fill);

        ViewPager pager = (ViewPager) findViewById(R.id.viewPager);

        final Button skip = (Button) findViewById(R.id.skipTutorial);

        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        pager.setAdapter(new TutorialViewPagerAdapter(frames));

        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
