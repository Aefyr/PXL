package com.af.pxl.palettes;

import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.af.pxl.AdaptivePixelSurfaceH;
import com.af.pxl.ColorPickerH;
import com.af.pxl.R;
import com.af.pxl.Utils;
import com.af.pxl.views.ColorCircle;

/**
 * Created by Aefyr on 03.08.2017.
 */

public class PaletteManagerH {
    private RelativeLayout layout;
    private AdaptivePixelSurfaceH aps;
    private ColorCircle colorCircleMain;
    private Palette2 palette;
    private ColorSelectionRecyclerAdapter adapter;
    private ColorCircle colorCircleCur;
    private TextView paletteName;
    private ColorPickerH colorPicker;

    private boolean shown = false;

    private int currentColor = Color.RED;

    public interface OnPaletteChangeRequestListener {
        void onPaletteChangeRequest();
    }

    private OnPaletteChangeRequestListener listener;

    private OnVisibilityChangedListener vListener;

    public interface OnVisibilityChangedListener {
        void onVisibilityChanged(boolean visible);
    }

    public PaletteManagerH(RelativeLayout paletteLayoutRoot, ColorCircle mainColorCircle, AdaptivePixelSurfaceH apsH, final OnPaletteChangeRequestListener listener) {
        this.listener = listener;
        layout = paletteLayoutRoot;
        aps = apsH;
        colorCircleMain = mainColorCircle;
        palette = PaletteUtils.loadPalette(aps.getProject().palette);
        colorCircleCur = (ColorCircle) paletteLayoutRoot.findViewById(R.id.colorCircle);
        setCurrentColor(palette.getSelectedColor());
        paletteName = (TextView) paletteLayoutRoot.findViewById(R.id.paletteName);
        paletteName.setText(palette.getName());

        setCurrentColor(palette.getSelectedColor());

        colorCircleMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!shown)
                    show();
                else
                    hide();
            }
        });

        paletteLayoutRoot.findViewById(R.id.changePalette).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onPaletteChangeRequest();
            }
        });

        paletteLayoutRoot.findViewById(R.id.editColor).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog colorEditDialog = new AlertDialog.Builder(aps.getContext()).setView(R.layout.color_picker_2).setPositiveButton(aps.getResources().getString(R.string.done), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        setCurrentColor(colorPicker.getColor());
                        colorPicker = null;
                        hide();
                    }
                }).setNegativeButton(aps.getResources().getString(R.string.cancel), null).setTitle(aps.getResources().getString(R.string.edit_color)).create();
                colorEditDialog.show();
                colorPicker = new ColorPickerH(colorEditDialog.getWindow(), currentColor);
            }
        });

        RecyclerView recycler = (RecyclerView) paletteLayoutRoot.findViewById(R.id.paletteRecycler);
        recycler.setLayoutManager(new GridLayoutManager(paletteLayoutRoot.getContext(), (int) (Utils.getScreenWidth(paletteLayoutRoot.getResources()) / Utils.dpToPx(64, paletteLayoutRoot.getResources()))));
        adapter = new ColorSelectionRecyclerAdapter(paletteLayoutRoot.getContext(), palette);
        recycler.setAdapter(adapter);

        adapter.setOnColorInteractionListener(new ColorSelectionRecyclerAdapter.OnColorInteractionListener() {
            @Override
            public void onColorClick(int index) {
                setCurrentColor(palette.getColor(index));
                hide();
            }

            @Override
            public void onColorLongClick(int index) {
                palette.editColor(index, currentColor);
                adapter.notifyItemChanged(index);
            }
        });
    }

    public void setPalette(Palette2 palette) {
        this.palette = palette;
        aps.getProject().setPalette(palette);
        adapter.setPalette(palette);
        paletteName.setText(palette.getName());
    }

    public Palette2 getPalette() {
        return palette;
    }


    public void setCurrentColor(int color) {
        aps.setColor(color);
        this.currentColor = color;
        colorCircleMain.setColor(color);
        colorCircleCur.setColor(color);
    }

    public void setOnVisibilityChangedListener(OnVisibilityChangedListener listener) {
        vListener = listener;
    }

    private void show() {
        layout.setVisibility(View.VISIBLE);
        shown = true;
        listenerEvent();
    }

    public void hide() {
        if (!shown)
            return;

        layout.setVisibility(View.GONE);
        shown = false;
        listenerEvent();
    }

    private void listenerEvent() {
        if (listener != null)
            vListener.onVisibilityChanged(shown);
    }

    public boolean shown() {
        return shown;
    }

}
