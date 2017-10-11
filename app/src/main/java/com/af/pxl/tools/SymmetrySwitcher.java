package com.af.pxl.tools;

import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.af.pxl.AdaptivePixelSurfaceH;
import com.af.pxl.R;
import com.af.pxl.Utils;

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

    private int toastOffset;

    public interface OnVisibilityChangedListener {
        void onVisibilityChanged(boolean visible);
    }

    public SymmetrySwitcher(ImageButton symmetryButton, LinearLayout layout, AdaptivePixelSurfaceH aps) {
        this.aps = aps;

        s = symmetryButton;

        symmetryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (shown)
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
                switch (view.getId()) {
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

        toastOffset = (int) Utils.dpToPx(58, aps.getResources());
    }


    private void setSymmetry(boolean enabled, AdaptivePixelSurfaceH.SymmetryType type) {
        if (enabled == aps.isSymmetryEnabled() && type == aps.getSymmetryType()) {
            hide();
            return;
        }

        aps.setSymmetryEnabled(enabled, type);

        if (!enabled) {
            dark(no);
            normal(h, v);
            s.setImageResource(R.drawable.symmetryoff);
            gravityDefiedToaster(aps.getResources().getString(R.string.symmetry_none));
            hide();
            return;
        }
        switch (type) {
            case HORIZONTAL:
                dark(h);
                normal(v, no);
                s.setImageResource(R.drawable.symmetryh);
                gravityDefiedToaster(aps.getResources().getString(R.string.symmetry_h));
                break;
            case VERTICAL:
                dark(v);
                normal(h, no);
                s.setImageResource(R.drawable.symmetryv);
                gravityDefiedToaster(aps.getResources().getString(R.string.symmetry_v));
                break;
        }

        hide();
    }

    public void hide() {
        if (!shown)
            return;

        layout.setVisibility(View.GONE);
        shown = false;
        listenerEvent();
    }

    public void show() {
        if (shown)
            return;
        layout.setVisibility(View.VISIBLE);
        shown = true;
        listenerEvent();
    }

    public boolean shown() {
        return shown;
    }

    private void dark(ImageButton... buttons) {
        for (ImageButton b : buttons) {
            b.setBackgroundResource(R.drawable.full_round_rect_bg_dark);

            if (Build.VERSION.SDK_INT >= 21)
                b.setElevation(0);
        }
    }

    private void normal(ImageButton... buttons) {
        for (ImageButton b : buttons) {
            b.setBackgroundResource(R.drawable.sketchbook_style_bg_selector_2);
            if (Build.VERSION.SDK_INT >= 21)
                b.setElevation(Utils.dpToPx(18, aps.getResources()));
        }
    }

    public void setOnVisibilityChangedListener(OnVisibilityChangedListener listener) {
        vListener = listener;
    }

    private void listenerEvent() {
        if (vListener != null)
            vListener.onVisibilityChanged(shown);
    }

    private void gravityDefiedToaster(String message){
        Toast toast = Toast.makeText(aps.getContext(), message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, toastOffset);
        toast.show();
    }
}
