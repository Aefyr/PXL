package com.af.pxl;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.af.pxl.Palettes.Palette2;
import com.af.pxl.Palettes.PaletteUtils;
import com.af.pxl.Palettes.PaletteView2;
import com.af.pxl.Palettes.PaletteView3;
import com.af.pxl.Projects.ProjectsUtils;

import java.io.File;
import java.util.ArrayList;

public class DrawingActivity extends AppCompatActivity implements AdaptivePixelSurface.OnSpecialToolUseListener{

    AdaptivePixelSurface aps;
    ToolPickView toolButton;
    AlertDialog toolPickDialog;
    ImageButton.OnClickListener onClickListener;
    Button cursorAction;

    ColorCircle colorPickButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawing);

        aps = (AdaptivePixelSurface) findViewById(R.id.aps);

        if(getIntent().getStringExtra("projectToLoad")!=null){
            aps.setProject(ProjectsUtils.loadProject(getIntent().getStringExtra("projectToLoad")));
        }

        aps.setColorCircle((ColorCircle)findViewById(R.id.color));
        aps.setOnSpecialToolUseListener(this);

        initializeImageButtonsOCL();
        initializeToolPicking();
        initializeCursor();
        tempColorPickInitialize();
        tempInitializeButtons();

        findViewById(R.id.TMP).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startTranslateActivity();
            }
        });

        BitmapFactory.Options op = new BitmapFactory.Options();
        op.inScaled = false;
        aps.cursor.setCursorPointerImage(BitmapFactory.decodeResource(getResources(), R.drawable.defaultcursor2, op));


        //PALETTES
        PaletteUtils.initialize(this);



    }

    /*@Override
    public void onBackPressed() {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        finish();
        super.onBackPressed();
    }*/

    private ColorPicker colorPicker;
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

        aps.setPalette(PaletteUtils.loadPalette(aps.project.palette));
        colorPickButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog d = new AlertDialog.Builder(DrawingActivity.this).setView(R.layout.palette).create();
                d.show();
                final PaletteView3 paletteView = ((PaletteView3)d.findViewById(R.id.pv2));
                paletteView.setPalette(aps.palette);
                PaletteUtils.getSavedPalettes();

                ((TextView)d.findViewById(R.id.paletteName)).setText(aps.palette.getName());

                d.findViewById(R.id.pvAdd).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(aps.palette.isFull()) {
                            Utils.toaster(DrawingActivity.this, "Palette is full!");
                            return;
                        }
                        final AlertDialog d = new AlertDialog.Builder(DrawingActivity.this).setView(R.layout.color_picker).setTitle("Add color").create();
                        d.show();
                        colorPicker = new ColorPicker(d.getWindow(), Color.RED);
                        (d.findViewById(R.id.colorPickButton)).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                int newColor = Color.HSVToColor(colorPicker.color);
                                aps.palette.addColor(newColor);

                                d.cancel();
                                colorPicker = null;
                            }
                        });
                    }
                });

                d.findViewById(R.id.pvDone).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        colorPickButton.setColor(aps.palette.getSelectedColor());
                        aps.setColor(aps.palette.getSelectedColor());
                        d.cancel();
                    }
                });

                d.findViewById(R.id.pvEdit).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final AlertDialog d = new AlertDialog.Builder(DrawingActivity.this).setView(R.layout.color_picker).setTitle("Edit color").create();
                        d.show();
                        colorPicker = new ColorPicker(d.getWindow(), aps.palette.getSelectedColor());
                        (d.findViewById(R.id.colorPickButton)).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                int newColor = Color.HSVToColor(colorPicker.color);
                                aps.palette.editColor(aps.palette.getSelectedColorIndex(), newColor);
                                d.cancel();
                                colorPicker = null;
                            }
                        });
                    }
                });

                d.findViewById(R.id.pvSelectPalette).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String[] options = {"Load Palette", "New Palette"};
                        final AlertDialog da = new AlertDialog.Builder(DrawingActivity.this).setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if(i==0){
                                    ArrayList<String> namesAL = PaletteUtils.getSavedPalettes();
                                    final String[] names = namesAL.toArray(new String[namesAL.size()]);

                                    AlertDialog dia = new AlertDialog.Builder(DrawingActivity.this).setItems(names, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            aps.setPalette(PaletteUtils.loadPalette(names[i]));
                                            ((TextView)d.findViewById(R.id.paletteName)).setText(aps.palette.getName());
                                            paletteView.setPalette(aps.palette);
                                        }
                                    }).create();
                                    dia.show();

                                }else if(i==1){
                                    final AlertDialog dialog = new AlertDialog.Builder(DrawingActivity.this).setTitle("Create new palette").setView(R.layout.edit_text).create();
                                    dialog.show();
                                    dialog.findViewById(R.id.okButton).setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            aps.setPalette(new Palette2(((EditText)dialog.findViewById(R.id.editText)).getText().toString(), 16, Color.RED, false));
                                            ((TextView)d.findViewById(R.id.paletteName)).setText(aps.palette.getName());
                                            paletteView.setPalette(aps.palette);
                                            dialog.cancel();
                                        }
                                    });
                                }
                            }
                        }).create();
                        da.show();
                    }
                });

                d.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        colorPickButton.setColor(aps.palette.getSelectedColor());
                        aps.setColor(aps.palette.getSelectedColor());
                    }
                });
            }
        });
    }

    private void initializeToolPicking(){

        toolButton = (ToolPickView) findViewById(R.id.currentTool);

        toolButton.setAps(aps);
    }

    private void initializeImageButtonsOCL(){
        final ImageButton cursorToggle = (ImageButton) findViewById(R.id.cursorMode);
        onClickListener = new ImageButton.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()){
                    case R.id.currentTool:
                        toolPickDialog.show();
                        break;
                    case R.id.cursorMode:
                        aps.cursor.setEnabled(!aps.cursorMode);
                        if(aps.cursorMode)
                            cursorToggle.setImageResource(R.drawable.cursor3);
                        else
                            cursorToggle.setImageResource(R.drawable.normal2);
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
                        aps.setSymmetryEnabled(false, AdaptivePixelSurface.SymmetryType.HORIZONTAL);
                        symmetry.setImageResource(R.drawable.symmetryoff);
                        symmetryModePickView.setVisibility(View.GONE);
                        break;
                    case 1:
                        aps.setSymmetryEnabled(true, AdaptivePixelSurface.SymmetryType.HORIZONTAL);
                        symmetry.setImageResource(R.drawable.symmetryh);
                        symmetryModePickView.setVisibility(View.GONE);
                        break;
                    case 2:
                        aps.setSymmetryEnabled(true, AdaptivePixelSurface.SymmetryType.VERTICAL);
                        symmetry.setImageResource(R.drawable.symmetryv);
                        symmetryModePickView.setVisibility(View.GONE);
                        break;
                }
            }
        });

        symmetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                aps.canvasHistory.cancelHistoricalChange(false);
            }
        }

        //Translate
        if(requestCode==1337){
            if(resultCode==1){
                Bitmap b = BitmapFactory.decodeFile(data.getStringExtra("path"));
                Utils.setBitmapPixelsFromOtherBitmap(aps.pixelBitmap, b);
                b.recycle();
                aps.canvasHistory.completeHistoricalChange();
            }else if(resultCode==0) {
                aps.canvasHistory.cancelHistoricalChange(false);
            }
        }
    }

    //Special tools

    //ColorSwap
    @Override
    public void onColorSwapToolUse(int color) {
        System.out.println("Swapping color: "+color);
        startColorSwapActivity(color);
    }

    private void startColorSwapActivity(int colorToSwap){

        File t = new File(getFilesDir(), "p.pxl");
        Utils.saveBitmap(aps.pixelBitmap, t);

        Intent i = new Intent(DrawingActivity.this, ColorSwapActivity.class);
        i.putExtra("path", t.getAbsolutePath());
        i.putExtra("color", colorToSwap);
        aps.canvasHistory.startHistoricalChange();
        startActivityForResult(i, 322);
    }

    //Translate
    private void startTranslateActivity(){
        File t = new File(getFilesDir(), "ti.pxl");
        Utils.saveBitmap(aps.pixelBitmap, t);
        Intent i = new Intent(DrawingActivity.this, BitmapsMergeActivity.class);
        i.putExtra("path", t.getAbsolutePath());
        i.putExtra("mode", 1);
        i.putExtra("transparentBackground", aps.project.transparentBackground);
        aps.canvasHistory.startHistoricalChange();
        startActivityForResult(i, 1337);
    }
}
