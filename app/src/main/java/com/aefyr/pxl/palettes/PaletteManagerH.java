package com.aefyr.pxl.palettes;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.aefyr.pxl.AdaptivePixelSurfaceH;
import com.aefyr.pxl.ColorPickerH;
import com.aefyr.pxl.R;
import com.aefyr.pxl.custom.ColorCircle;
import com.aefyr.pxl.experimental.Tutorial;
import com.aefyr.pxl.util.Utils;

/**
 * Created by Aefyr on 03.08.2017.
 */

public class PaletteManagerH {
    private RelativeLayout layout;
    private AdaptivePixelSurfaceH aps;
    private ColorCircle colorCircleMain;
    private Palette2 palette;
    private ColorSelectionRecyclerAdapter adapter;
    //private ColorCircle colorCircleCur;
    private TextView paletteName;
    private ColorPickerH colorPicker;

    private boolean shown = false;

    private int currentColor = Color.RED;

    private Tutorial tutorial;

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
        //colorCircleCur = (ColorCircle) paletteLayoutRoot.findViewById(R.id.colorCircle);
        setCurrentColor(palette.getColor(0));
        paletteName = (TextView) paletteLayoutRoot.findViewById(R.id.paletteName);
        paletteName.setText(palette.getName());

        tutorial = new Tutorial(apsH.getContext());

        colorCircleMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tutorial.palettes();
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
                }).setNegativeButton(aps.getResources().getString(R.string.cancel), null).setTitle(aps.getResources().getString(R.string.edit_color)).setNeutralButton(R.string.hex, null).create();
                colorEditDialog.show();
                colorEditDialog.getButton(DialogInterface.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new HexColorPicker(aps.getContext(), colorPicker.getColor(), new HexColorPicker.OnColorPickListener() {
                            @Override
                            public void onColorPicked(int color) {
                                colorPicker.setColor(color);
                            }
                        }).show();
                    }
                });
                colorPicker = new ColorPickerH(colorEditDialog.getWindow(), currentColor);
            }
        });

        RecyclerView recycler = (RecyclerView) paletteLayoutRoot.findViewById(R.id.paletteRecycler);
        recycler.setLayoutManager(new GridLayoutManager(paletteLayoutRoot.getContext(), (int) (Utils.getScreenWidth(paletteLayoutRoot.getResources()) / (paletteLayoutRoot.getResources().getDimensionPixelSize(R.dimen.palette_color_circle_size)+Utils.dpToPx(12, paletteLayoutRoot.getResources())))));
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
        //colorCircleCur.setColor(color);
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

    public void writeStateToBundle(Bundle outState){
        outState.putBoolean("paletteManager_shown", shown);
    }

    public void restoreState(Bundle savedInstanceState){
        setCurrentColor(savedInstanceState.getInt("currentColor", Color.RED));

        if(savedInstanceState.getBoolean("paletteManager_shown", false))
            show();

    }

}
