package com.af.pxl;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.af.pxl.Palettes.PaletteManager;
import com.af.pxl.Palettes.PalettePickerActivity;
import com.af.pxl.Palettes.PaletteUtils;
import com.af.pxl.Projects.ProjectsUtils;

import java.io.File;

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

    private PaletteManager paletteManager;
    void tempColorPickInitialize(){
        colorPickButton = (ColorCircle) findViewById(R.id.color);
        colorPickButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(paletteManager == null)
                    paletteManager = new PaletteManager();
                paletteManager.showPaletteManagerDialog(DrawingActivity.this, aps.palette);
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

        //Pick palette
        if(requestCode == PalettePickerActivity.REQUEST_CODE_PICK_PALETTE){
            if(resultCode==1){
                aps.setPalette(PaletteUtils.loadPalette(data.getStringExtra("pickedPalette")));
                paletteManager.setPalette(aps.palette);
            }else if(resultCode == 0){

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
