package com.aefyr.pxl.TCA;

import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.aefyr.pxl.R;

/**
 * Created by Aefyr on 05.08.2017.
 */

public class TutorialViewPagerAdapter extends PagerAdapter {

    TutorialFrame[] frames;

    public TutorialViewPagerAdapter(TutorialFrame[] frames){
        this.frames = frames;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        LayoutInflater inflater = LayoutInflater.from(container.getContext());
        View page = inflater.inflate(R.layout.tutorial_page, null);

        ((TextView)page.findViewById(R.id.tutotialTitle)).setText(frames[position].title);
        ((TextView)page.findViewById(R.id.tutorialText)).setText(frames[position].text);
        ((ImageView)page.findViewById(R.id.tutotialImage)).setImageResource(frames[position].image);

        container.addView(page);

        return page;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return frames.length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }
}
