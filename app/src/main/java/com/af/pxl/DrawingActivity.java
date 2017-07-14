package com.af.pxl;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class DrawingActivity extends AppCompatActivity implements AdaptivePixelSurface.OnSpecialToolUseListener{

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
        aps.setOnSpecialToolUseListener(this);

        initializeImageButtonsOCL();
        initializeToolPicking();
        initializeCursor();
        tempColorPickInitialize();
        tempInitializeButtons();


    }

    ColorPicker colorPicker;
    void tempColorPickInitialize(){
        colorPickButton = (ColorCircle) findViewById(R.id.color);
        /*palette = new Palette();
        colorPickButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog d = new AlertDialog.Builder(DrawingActivity.this).setView(R.layout.palette).create();
                d.show();
                PaletteView paletteView = ((PaletteView)d.findViewById(R.id.paletteView));
                paletteView.setPalette(palette);
                paletteView.setOnColorChangedListener(new PaletteView.OnColorChangedListener() {
                    @Override
                    public void onColorChanged(int newColor) {
                        //aps.paint.setColor(newColor);
                        //colorPickButton.setColor(newColor);
                        //d.cancel();
                    }
                });
            }
        });*/

        final Palette2 palette2 = new Palette2(16, Color.WHITE);
        aps.setPalette(palette2);
        colorPickButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog d = new AlertDialog.Builder(DrawingActivity.this).setView(R.layout.palette).create();
                d.show();
                PaletteView2 paletteView = ((PaletteView2)d.findViewById(R.id.pv2));
                paletteView.setPalette(palette2);

                d.findViewById(R.id.pvAdd).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(palette2.isFull()) {
                            Utils.toaster(DrawingActivity.this, "Palette is full!");
                            return;
                        }
                        final AlertDialog d = new AlertDialog.Builder(DrawingActivity.this).setView(R.layout.color_picker).create();
                        d.show();
                        colorPicker = new ColorPicker((ColorPickerView) d.findViewById(R.id.colorPickerHue),(SeekBar) d.findViewById(R.id.seekBarHue),
                                (ColorPickerView) d.findViewById(R.id.colorPickerSat), (SeekBar) d.findViewById(R.id.seekBarSat), (ColorPickerView) d.findViewById(R.id.colorPickerVal),
                                (SeekBar) d.findViewById(R.id.seekBarVal), (ColorCircle) d.findViewById(R.id.newColor), Color.RED);
                        (d.findViewById(R.id.colorPickButton)).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                int newColor = Color.HSVToColor(colorPicker.color);
                                palette2.addColor(newColor);

                                d.cancel();
                                colorPicker = null;
                            }
                        });
                    }
                });

                d.findViewById(R.id.pvDone).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        colorPickButton.setColor(palette2.getSelectedColor());
                        aps.paint.setColor(palette2.getSelectedColor());
                        d.cancel();
                    }
                });

                d.findViewById(R.id.pvEdit).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final AlertDialog d = new AlertDialog.Builder(DrawingActivity.this).setView(R.layout.color_picker).create();
                        d.show();
                        colorPicker = new ColorPicker((ColorPickerView) d.findViewById(R.id.colorPickerHue),(SeekBar) d.findViewById(R.id.seekBarHue),
                                (ColorPickerView) d.findViewById(R.id.colorPickerSat), (SeekBar) d.findViewById(R.id.seekBarSat), (ColorPickerView) d.findViewById(R.id.colorPickerVal),
                                (SeekBar) d.findViewById(R.id.seekBarVal), (ColorCircle) d.findViewById(R.id.newColor), palette2.getSelectedColor());
                        (d.findViewById(R.id.colorPickButton)).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                int newColor = Color.HSVToColor(colorPicker.color);
                                palette2.editColor(palette2.getSelectedColorIndex(), newColor);
                                d.cancel();
                                colorPicker = null;
                            }
                        });
                    }
                });

                d.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        colorPickButton.setColor(palette2.getSelectedColor());
                        aps.paint.setColor(palette2.getSelectedColor());
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


    void tempInitializeButtons(){

        //Grid
        final ImageButton grid = (ImageButton) findViewById(R.id.grid);
        grid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(aps.toggleGrid())
                    grid.setImageResource(R.drawable.gridon);
                else
                    grid.setImageResource(R.drawable.gridoff);

            }
        });

        //Symmetry
        final SymmetryModePickView symmetryModePickView = (SymmetryModePickView) findViewById(R.id.emve);
        final ImageButton symmetry = (ImageButton) findViewById(R.id.symmetry);

        symmetryModePickView.setOnItemClickedListener(new SymmetryModePickView.OnItemClickedListener() {
            @Override
            public void onItemClicked(int id) {
                switch (id){
                    case  0:
                        aps.symmetry = false;
                        symmetry.setImageResource(R.drawable.symmetryoff);
                        symmetryModePickView.setVisibility(View.GONE);
                        break;
                    case 1:
                        aps.symmetry = true;
                        aps.symmetryType = AdaptivePixelSurface.SymmetryType.HORIZONTAL;
                        symmetry.setImageResource(R.drawable.symmetryh);
                        symmetryModePickView.setVisibility(View.GONE);
                        break;
                    case 2:
                        aps.symmetry = true;
                        aps.symmetryType = AdaptivePixelSurface.SymmetryType.VERTICAL;
                        symmetry.setImageResource(R.drawable.symmetryv);
                        symmetryModePickView.setVisibility(View.GONE);
                        break;
                }
            }
        });

        symmetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*aps.symmetry = !aps.symmetry;
                if(aps.symmetry)
                    symmetry.setImageResource(R.drawable.symmetryh);
                else
                    symmetry.setImageResource(R.drawable.symmetryoff);
                    */
                if(symmetryModePickView.getVisibility()==View.GONE)
                    symmetryModePickView.setVisibility(View.VISIBLE);
                else
                    symmetryModePickView.setVisibility(View.GONE);
            }
        });

        //History
        ImageButton undo = (ImageButton) findViewById(R.id.undo);
        undo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                aps.canvasHistory.undoHistoricalChange();
            }
        });

        final ImageButton redo = (ImageButton) findViewById(R.id.redo);
        redo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                aps.canvasHistory.redoHistoricalChange();
            }
        });

        //Clear
        findViewById(R.id.clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                aps.clearCanvas();
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Color swap
        if(requestCode==322){
            //Success
            if(resultCode == 1) {
                Bitmap b = BitmapFactory.decodeFile(data.getStringExtra("path"));
                Utils.setBitmapPixelsFromOtherBitmap(aps.pixelBitmap, b);
                b.recycle();
                aps.canvasHistory.completeHistoricalChange();
            } else if(resultCode == 0){
                //Cancel
                aps.canvasHistory.cancelHistoricalChange();
            }
        }
    }

    //Special tools
    @Override
    public void onColorSwapToolUse(int color) {
        System.out.println("Swapping color: "+color);
        startColorSwapActivity(color);
    }

    //TODO Use custom view instead of ImageView in ColorSwapActivity, cuz that scaling algorithm sucks
    private void startColorSwapActivity(int colorToSwap){

        File t = new File(getFilesDir(), "p.pxl");
        try(FileOutputStream stream = new FileOutputStream(t)){
            aps.pixelBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Intent i = new Intent(DrawingActivity.this, ColorSwapActivity.class);
        i.putExtra("path", t.getAbsolutePath());
        i.putExtra("color", colorToSwap);
        aps.canvasHistory.startHistoricalChange();
        startActivityForResult(i, 322);
    }
}
