package com.af.pxl;

import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;

public class MainActivity extends AppCompatActivity {

    ColorPicker colorPicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //CanvasView canvasView = new CanvasView(this);
        setContentView(R.layout.alternate2);

        final AdaptivePixelSurface aps = (AdaptivePixelSurface) findViewById(R.id.aps);

        final Button undo = (Button) findViewById(R.id.undoButton);
        undo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                aps.canvasHistory.undoHistoricalChange();
            }
        });

        final Button redo = (Button) findViewById(R.id.redoButton);
        redo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                aps.canvasHistory.redoHistoricalChange();
            }
        });

        aps.canvasHistory.setOnHistoryAvailabilityChangeListener(new CanvasHistory.OnHistoryAvailabilityChangeListener() {
            @Override
            public void pastAvailabilityChanged(boolean available) {
                undo.setEnabled(available);
            }

            @Override
            public void futureAvailabilityChanged(boolean available) {
                redo.setEnabled(available);
            }
        });

        /*final PixelSurface2 pixelSurface2 = (PixelSurface2) findViewById(R.id.pixelSurface);

        Switch tempSwitch = (Switch) findViewById(R.id.tempSwitch);
        tempSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) pixelSurface2.currentTool = PixelSurface2.Tool.FILL;
                else pixelSurface2.currentTool = PixelSurface2.Tool.PEN;
            }
        });

        Button tempButton = (Button) findViewById(R.id.tempButton);
        tempButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog d = new AlertDialog.Builder(MainActivity.this).setView(R.layout.color_picker).create();
                d.show();
                colorPicker = new ColorPicker((ColorPickerView) d.findViewById(R.id.colorPickerHue),(SeekBar) d.findViewById(R.id.seekBarHue),
                        (ColorPickerView) d.findViewById(R.id.colorPickerSat), (SeekBar) d.findViewById(R.id.seekBarSat), (ColorPickerView) d.findViewById(R.id.colorPickerVal),
                        (SeekBar) d.findViewById(R.id.seekBarVal), (ColorView) d.findViewById(R.id.colorView));
                (d.findViewById(R.id.colorPickButton)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        pixelSurface2.paint.setColor(Color.HSVToColor(colorPicker.color));
                        d.cancel();
                        colorPicker = null;
                    }
                });
            }
        });

        Button tempButton2 = (Button) findViewById(R.id.tempButton2);
        tempButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pixelSurface2.commitHistoryChange();
                pixelSurface2.pixelCanvas.drawColor(Color.WHITE);
                pixelSurface2.drawingThread2.update(false);
            }
        });


        Button tempButton3 = (Button) findViewById(R.id.tempButton3);
        tempButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pixelSurface2.rewindHistory();
            }
        });

        Switch tempSwitch2 = (Switch) findViewById(R.id.tempSwitch2);
        tempSwitch2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                pixelSurface2.showGrid = b;
                pixelSurface2.drawingThread2.update(false);
            }
        });*/

        /*final CanvasView canvasView = (CanvasView) findViewById(R.id.canvasView);
        final ColorPickerView huePicker = (ColorPickerView) findViewById(R.id.colorPicker);


        Switch symmetrySwitch = (Switch) findViewById(R.id.symmetrySwitch);

        symmetrySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                canvasView.symmetry = b;
            }
        });

        Switch gridSwitch = (Switch) findViewById(R.id.gridSwitch);

        gridSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                canvasView.setGridEnabled(b);
            }
        });

        Button colorPick = (Button) findViewById(R.id.colorPick);
        colorPick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog d = new AlertDialog.Builder(MainActivity.this).setView(R.layout.color_picker).create();
                d.show();
                final ColorPicker colorPicker = new ColorPicker((ColorPickerView) d.findViewById(R.id.colorPickerHue),(SeekBar) d.findViewById(R.id.seekBarHue),
                        (ColorPickerView) d.findViewById(R.id.colorPickerSat), (SeekBar) d.findViewById(R.id.seekBarSat), (ColorPickerView) d.findViewById(R.id.colorPickerVal),
                        (SeekBar) d.findViewById(R.id.seekBarVal), (ColorView) d.findViewById(R.id.colorView));
                ((Button)d.findViewById(R.id.colorPickButton)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        canvasView.pixelPaint.setColor(Color.HSVToColor(colorPicker.color));
                        canvasView.drawThread.requestUpdate();
                        d.cancel();
                    }
                });
            }
        });

        Button toolPick = (Button) findViewById(R.id.toolPickButton);

        toolPick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog d = new AlertDialog.Builder(MainActivity.this).setView(R.layout.tool_picker).create();
                d.show();

                ((RadioGroup) d.findViewById(R.id.radioGroupTool)).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
                        if(i == R.id.radioButtonPen){
                            System.out.println("PEN SELECTED");
                        }
                    }
                });
            }
        });
        */
        //AlertDialog dialog = new AlertDialog.Builder(this).setView()
    }
}
