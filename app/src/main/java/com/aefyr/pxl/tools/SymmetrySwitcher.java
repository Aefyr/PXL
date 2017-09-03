package com.aefyr.pxl.tools;

import android.os.Build;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.aefyr.pxl.AdaptivePixelSurfaceH;
import com.aefyr.pxl.R;
import com.aefyr.pxl.Utils;

/**
 * Created by Aefyr on 09.08.2017.
 */

public class SymmetrySwitcher {
    private AdaptivePixelSurfaceH aps;

    private LinearLayout layout;
    private ImageButton no;
    private ImageButton h;
    private ImageButton v;
    private ImageButton s;

    private boolean shown;

    private OnVisibilityChangedListener vListener;
    public interface OnVisibilityChangedListener{
        void onVisibilityChanged(boolean visible);
    }

    public SymmetrySwitcher(ImageButton symmetryButton, LinearLayout layout, AdaptivePixelSurfaceH aps){
        this.aps = aps;

        s = symmetryButton;

        symmetryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(shown)
                    hide();
                else
                    show();
            }
        });

        this.layout = layout;
        no = (ImageButton) layout.findViewById(R.id.symmetryN);
        h = (ImageButton) layout.findViewById(R.id.symmetryH);
        v = (ImageButton) layout.findViewById(R.id.symmetryV);

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()){
                    case R.id.symmetryN:
                        setSymmetry(false, AdaptivePixelSurfaceH.SymmetryType.HORIZONTAL);
                        break;
                    case R.id.symmetryH:
                        setSymmetry(true, AdaptivePixelSurfaceH.SymmetryType.HORIZONTAL);
                        break;
                    case R.id.symmetryV:
                        setSymmetry(true, AdaptivePixelSurfaceH.SymmetryType.VERTICAL);
                        break;
                }
            }
        };

        no.setOnClickListener(onClickListener);
        h.setOnClickListener(onClickListener);
        v.setOnClickListener(onClickListener);
        dark(no);
        normal(h, v);
    }



    private void setSymmetry(boolean enabled, AdaptivePixelSurfaceH.SymmetryType type){
        if(enabled==aps.isSymmetryEnabled()&&type==aps.getSymmetryType()) {
            hide();
            return;
        }

        aps.setSymmetryEnabled(enabled, type);

        if(!enabled){
            dark(no);
            normal(h, v);
            s.setImageResource(R.drawable.symmetryoff);
            hide();
            return;
        }
        switch (type){
            case HORIZONTAL:
                dark(h);
                normal(v, no);
                s.setImageResource(R.drawable.symmetryh);
                break;
            case VERTICAL:
                dark(v);
                normal(h, no);
                s.setImageResource(R.drawable.symmetryv);
                break;
        }

        hide();
    }

    public void hide(){
        if(!shown)
            return;

        layout.setVisibility(View.GONE);
        shown = false;
        listenerEvent();
    }

    public void show(){
        if(shown)
            return;
        layout.setVisibility(View.VISIBLE);
        shown = true;
        listenerEvent();
    }

    public boolean shown(){
        return shown;
    }

    private void dark(ImageButton... buttons){
        for(ImageButton b: buttons) {
            b.setBackgroundResource(R.drawable.full_round_rect_bg_dark);

            if(Build.VERSION.SDK_INT>=21)
                b.setElevation(0);
        }
    }

    private void normal(ImageButton... buttons){
        for(ImageButton b: buttons) {
            b.setBackgroundResource(R.drawable.sketchbook_style_bg_selector_2);
            if(Build.VERSION.SDK_INT>=21)
                b.setElevation(Utils.dpToPx(18, aps.getResources()));
        }
    }

    public void setOnVisibilityChangedListener(OnVisibilityChangedListener listener){
        vListener = listener;
    }

    private void listenerEvent(){
        if(vListener!=null)
            vListener.onVisibilityChanged(shown);
    }
}
