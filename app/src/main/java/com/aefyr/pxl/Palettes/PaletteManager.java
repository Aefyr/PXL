package com.aefyr.pxl.Palettes;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.aefyr.pxl.ColorPickerH;
import com.aefyr.pxl.R;

/**
 * Created by Aefyr on 25.07.2017.
 */

public class PaletteManager{
    private AlertDialog paletteManagerDialog;
    private TextView paletteName;
    private PaletteView3 paletteView;
    private boolean dialogShown;
    private ColorPickerH colorPicker;
    private Palette2 currentPalette;
    private boolean first = true;

    OnCloseListener listener;
    Context c;

    public interface OnCloseListener{
        void onClose();
    }

    public void showPaletteManagerDialog(final AppCompatActivity a, final Palette2 palette){
        if(paletteManagerDialog==null) {
            paletteManagerDialog = new AlertDialog.Builder(a).setView(R.layout.palette).create();
        }

        paletteManagerDialog.show();
        if(first) {
            paletteName = (TextView) paletteManagerDialog.findViewById(R.id.paletteName);
            paletteView = (PaletteView3) paletteManagerDialog.findViewById(R.id.pv2);

            paletteView.setOnColorClickListener(new PaletteView3.OnColorClickListener() {
                @Override
                public void onColorClick(int positionInPalette) {
                    colorPicker(positionInPalette);
                }
            });
            if(listener!=null){
                paletteManagerDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        listener.onClose();
                    }
                });
            }
            first = false;
        }
        paletteChanged(palette);
        dialogShown = true;

        paletteManagerDialog.findViewById(R.id.pvDone).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                paletteManagerDialog.cancel();
            }
        });

        c = a;

    }

    private void colorPicker(final int position){
        final AlertDialog colorEditDialog = new AlertDialog.Builder(c).setView(R.layout.color_picker_2).setPositiveButton(c.getString(R.string.done), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                currentPalette.editColor(position, colorPicker.getColor());
                colorPicker = null;
            }
        }).setNegativeButton(c.getString(R.string.cancel), null).setTitle(c.getString(R.string.edit_color)).create();
        colorEditDialog.show();
        colorPicker = new ColorPickerH(colorEditDialog.getWindow(), currentPalette.getColor(position));
    }


    public void setPalette(Palette2 palette){
        if(!dialogShown)
            return;
        paletteChanged(palette);

    }


    private void paletteChanged(Palette2 newPalette){
        currentPalette = newPalette;
        paletteName.setText(currentPalette.getName());
        paletteView.setPalette(currentPalette);
    }

    public void closeDialog(){
        if(!dialogShown)
            return;
        paletteManagerDialog.cancel();
    }

    public void setOnCloseListener(OnCloseListener listener){
        this.listener = listener;
    }

    private boolean hideSelectPaletteOption;
    public void hideSelectPaletteOption(boolean a){
        hideSelectPaletteOption = a;
    }
}
