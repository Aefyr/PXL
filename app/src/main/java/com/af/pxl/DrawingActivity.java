package com.af.pxl;

import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;

public class DrawingActivity extends AppCompatActivity {

    AdaptivePixelSurface aps;
    ToolPickView toolButton;
    String[] tools;
    AlertDialog toolPickDialog;
    ImageButton.OnClickListener onClickListener;
    Button cursorAction;

    ColorCircle colorPickButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawing);

        aps = (AdaptivePixelSurface) findViewById(R.id.aps);
        aps.setColorCircle((ColorCircle)findViewById(R.id.color));

        initializeImageButtonsOCL();
        initializeToolPicking();
        initializeCursor();
        tempColorPickInitialize();

    }

    ColorPicker colorPicker;
    void tempColorPickInitialize(){
        colorPickButton = (ColorCircle) findViewById(R.id.color);
        colorPickButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog d = new AlertDialog.Builder(DrawingActivity.this).setView(R.layout.color_picker).create();
                d.show();
                colorPicker = new ColorPicker((ColorPickerView) d.findViewById(R.id.colorPickerHue),(SeekBar) d.findViewById(R.id.seekBarHue),
                        (ColorPickerView) d.findViewById(R.id.colorPickerSat), (SeekBar) d.findViewById(R.id.seekBarSat), (ColorPickerView) d.findViewById(R.id.colorPickerVal),
                        (SeekBar) d.findViewById(R.id.seekBarVal), (ColorCircle) d.findViewById(R.id.colorView), aps.paint.getColor());
                (d.findViewById(R.id.colorPickButton)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int newColor = Color.HSVToColor(colorPicker.color);
                        aps.paint.setColor(newColor);
                        colorPickButton.setColor(newColor);
                        d.cancel();
                        colorPicker = null;
                    }
                });
            }
        });
    }

    private void initializeToolPicking(){
        final Resources res = getResources();

        tools = new String[]{res.getString(R.string.pencil), res.getString(R.string.fill), res.getString(R.string.colorpick)};

        toolButton = (ToolPickView) findViewById(R.id.currentTool);

        toolButton.setAps(aps);
    }

    private void initializeImageButtonsOCL(){
        onClickListener = new ImageButton.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()){
                    case R.id.currentTool:
                        toolPickDialog.show();
                        break;
                    case R.id.cursorMode:
                        aps.cursor.setEnabled(!aps.cursorMode);
                        break;
                }
            }
        };
    }

    private void initializeCursor(){
        cursorAction = (Button) findViewById(R.id.cursorAction);
        cursorAction.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN)
                    aps.cursor.cursorDown();
                if(motionEvent.getAction() == MotionEvent.ACTION_UP)
                    aps.cursor.cursorUp();
                return false;
            }
        });

        (findViewById(R.id.cursorMode)).setOnClickListener(onClickListener);

        aps.cursor.setOnCursorChangeListener(new Cursor.OnCursorChangeListener() {
            @Override
            public void onCursorEnabled(boolean enabled) {
                if(enabled){
                    cursorAction.setVisibility(View.VISIBLE);
                }else
                    cursorAction.setVisibility(View.GONE);
            }
        });
    }

}
