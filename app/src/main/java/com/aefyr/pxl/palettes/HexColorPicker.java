package com.aefyr.pxl.palettes;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.aefyr.pxl.R;
import com.aefyr.pxl.custom.ColorRect;
import com.aefyr.pxl.util.Utils;

/**
 * Created by Aefyr on 25.01.2018.
 */

public class HexColorPicker {
    private AlertDialog dialog;

    private TextView hexText;
    private EditText hexEditText;
    private ColorRect hexRect;

    private int color;

    private OnColorPickListener listener;

    public interface OnColorPickListener{
        void onColorPicked(int color);
    }

    public HexColorPicker(Context c, int initialColor, final OnColorPickListener listener){
        this.color = initialColor;
        this.listener = listener;

        dialog = new AlertDialog.Builder(c).setView(R.layout.color_picker_hex).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                done();
            }
        }).setNegativeButton(R.string.cancel, null).create();
    }

    public void show(){
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.show();
        hexText = dialog.findViewById(R.id.hexText);

        hexEditText = dialog.findViewById(R.id.hexEditText);
        hexEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length()!=0)
                    setColor(Utils.hexToColor(s.toString(), false));
                else
                    setColor(Utils.hexToColor("0", false));
            }
        });

        hexRect = dialog.findViewById(R.id.hexRect);

        hexEditText.setText(Utils.colorToHex(color));
        hexEditText.setSelection(0, hexEditText.length());
        hexEditText.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                    done();
                    return true;
                }
                return false;
            }
        });
    }

    private void done(){
        dialog.dismiss();
        listener.onColorPicked(color);
    }

    public void setColor(int color){
        this.color = color;
        hexText.setText(Utils.colorToHex(color));
        hexRect.setColor(color);
    }
}
