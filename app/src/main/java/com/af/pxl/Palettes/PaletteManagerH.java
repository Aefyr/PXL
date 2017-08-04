package com.af.pxl.Palettes;

import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.af.pxl.AdaptivePixelSurfaceH;
import com.af.pxl.ColorCircle;
import com.af.pxl.ColorPickerH;
import com.af.pxl.ColorRect;
import com.af.pxl.R;
import com.af.pxl.Utils;

/**
 * Created by Aefyr on 03.08.2017.
 */

public class PaletteManagerH {
    private RelativeLayout layout;
    private AdaptivePixelSurfaceH aps;
    private ColorCircle colorCircleMain;
    private Palette2 palette;
    private ColorSelectionRecycleAdapter adapter;
    private ColorCircle colorCircleCur;
    private TextView paletteName;
    private ColorPickerH colorPicker;

    private boolean shown = false;

    private int currentColor = Color.RED;

    private boolean addMode = false;

    public interface OnPaletteChangeRequestListener{
        void onPaletteChangeRequest();
    }
    private OnPaletteChangeRequestListener listener;

    public  PaletteManagerH(RelativeLayout paletteLayoutRoot, ColorCircle mainColorCircle, AdaptivePixelSurfaceH apsH, final OnPaletteChangeRequestListener listener){
        this.listener = listener;
        layout = paletteLayoutRoot;
        aps = apsH;
        colorCircleMain = mainColorCircle;
        palette = aps.getPalette();
        colorCircleCur = (ColorCircle) paletteLayoutRoot.findViewById(R.id.colorCircle);
        paletteName = (TextView) paletteLayoutRoot.findViewById(R.id.paletteName);
        paletteName.setText(palette.getName());

        setCurrentColor(palette.getSelectedColor());

        colorCircleMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!shown)
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
        recycler.setLayoutManager(new GridLayoutManager(paletteLayoutRoot.getContext(), (int) (Utils.getScreenWidth(paletteLayoutRoot.getResources())/Utils.dpToPx(64, paletteLayoutRoot.getResources()))));
        adapter = new ColorSelectionRecycleAdapter(paletteLayoutRoot.getContext(), palette);
        recycler.setAdapter(adapter);

        adapter.setOnColorInteractionListener(new ColorSelectionRecycleAdapter.OnColorInteractionListener() {
            @Override
            public void onColorClick(int index) {
                if(!addMode) {
                    setCurrentColor(palette.getColor(index));
                    hide();
                }else {
                    palette.editColor(index, currentColor);
                    adapter.notifyItemChanged(index);
                }
            }

            @Override
            public void onColorLongClick(int index) {
                palette.editColor(index, currentColor);
                adapter.notifyItemChanged(index);
            }
        });
    }

    public void setPalette(Palette2 palette){
        this.palette = palette;
        adapter.setPalette(palette);
        paletteName.setText(palette.getName());
    }


    public void setCurrentColor(int color){
        aps.setColor(color);
        this.currentColor = color;
        colorCircleMain.setColor(color);
        colorCircleCur.setColor(color);
    }

    private void show(){
        layout.setVisibility(View.VISIBLE);
        shown = true;
    }

    public void hide(){
        if(!shown)
            return;

        layout.setVisibility(View.GONE);
        shown = false;
    }

    public boolean shown(){
        return shown;
    }

}
