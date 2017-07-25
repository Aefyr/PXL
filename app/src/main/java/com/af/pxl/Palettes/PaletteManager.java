package com.af.pxl.Palettes;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.af.pxl.R;
import com.af.pxl.ColorPicker;

/**
 * Created by Aefyr on 25.07.2017.
 */

public class PaletteManager {
    private AlertDialog paletteManagerDialog;
    private TextView paletteName;
    private PaletteView3 paletteView;
    private boolean dialogShown;
    private ColorPicker colorPicker;
    private Palette2 currentPalette;
    private boolean first = true;

    public void showPaletteManagerDialog(final AppCompatActivity a, final Palette2 palette){
        if(paletteManagerDialog==null) {
            paletteManagerDialog = new AlertDialog.Builder(a).setView(R.layout.palette).create();
        }

        paletteManagerDialog.show();
        if(first) {
            paletteName = (TextView) paletteManagerDialog.findViewById(R.id.paletteName);
            paletteView = (PaletteView3) paletteManagerDialog.findViewById(R.id.pv2);
            first = false;
        }
        paletteChanged(palette);
        dialogShown = true;


        paletteManagerDialog.findViewById(R.id.pvSelectPalette).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent pickerIntent = new Intent(a, PalettePickerActivity.class);
                pickerIntent.putExtra("pickerMode", true);
                a.startActivityForResult(pickerIntent, PalettePickerActivity.REQUEST_CODE_PICK_PALETTE);
            }
        });

        paletteManagerDialog.findViewById(R.id.pvEdit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog colorEditDialog = new AlertDialog.Builder(a).setView(R.layout.color_picker).setTitle(a.getString(R.string.edit_color)).create();
                colorEditDialog.show();
                colorPicker = new ColorPicker(colorEditDialog.getWindow(), currentPalette.getSelectedColor());
                (colorEditDialog.findViewById(R.id.colorPickButton)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        currentPalette.editColor(currentPalette.getSelectedColorIndex(), colorPicker.getColor());
                        colorEditDialog.cancel();
                        colorPicker = null;
                    }
                });
            }
        });

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
}
