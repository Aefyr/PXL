package com.af.pxl;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.af.pxl.Palettes.PaletteManagerH;
import com.af.pxl.Palettes.PalettePickerActivity;
import com.af.pxl.Palettes.PaletteUtils;
import com.af.pxl.Projects.ProjectsUtils;

import java.io.File;

public class DrawingActivity extends AppCompatActivity implements AdaptivePixelSurfaceH.OnSpecialToolUseListener, PaletteManagerH.OnPaletteChangeRequestListener, CanvasHistoryH.OnHistoryAvailabilityChangeListener{

    AdaptivePixelSurfaceH aps;
    ToolPickView toolButton;
    AlertDialog toolPickDialog;
    ImageButton.OnClickListener onClickListener;
    Button cursorAction;
    ImageButton undoButton;
    ImageButton redoButton;

    PaletteManagerH pm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawing_alt);

        aps = (AdaptivePixelSurfaceH) findViewById(R.id.aps);

        if(getIntent().getStringExtra("projectToLoad")!=null){
            aps.setProject(ProjectsUtils.loadProject(getIntent().getStringExtra("projectToLoad")));
        }


        aps.setOnSpecialToolUseListener(this);
        aps.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(toolButton.shown()) {
                    toolButton.hideTools();
                    return (!aps.cursorMode);
                }
                if(pm.shown()) {
                    pm.hide();
                    return (!aps.cursorMode);
                }

                return false;
            }
        });
        aps.canvasHistory.setOnHistoryAvailabilityChangeListener(this);

        initializeImageButtonsOCL();
        initializeToolPicking();
        initializeCursor();
        tempInitializeButtons();

        findViewById(R.id.TMP).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //startTranslateActivity();
                canvasWiseOptionsDialog();
            }
        });

        BitmapFactory.Options op = new BitmapFactory.Options();
        op.inScaled = false;
        aps.cursor.setCursorPointerImage(BitmapFactory.decodeResource(getResources(), R.drawable.defaultcursor2, op));


        //PALETTES
        PaletteUtils.initialize(this);

        pm = new PaletteManagerH((RelativeLayout)findViewById(R.id.paletteBar), (ColorCircle)findViewById(R.id.currentColor), aps, this);
        aps.setColorManager(pm);
    }

    private void canvasWiseOptionsDialog(){
        AlertDialog canvasWiseOptionsDialog = new AlertDialog.Builder(this).setTitle(getString(R.string.canvas_options)).setItems(getResources().getStringArray(R.array.canvas_options_array), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i){
                    case 0:
                        aps.clearCanvas();
                        break;
                    case 1:
                        startTranslateActivity();
                        break;
                    case 2:
                        mergeTool();
                        break;
                    case 3:
                        aps.scaleAnchorX = 0;
                        aps.scaleAnchorY = 0;
                        aps.centerCanvas();
                        aps.translateChanged = true;
                        aps.invalidate();
                        break;
                }
            }
        }).create();
        canvasWiseOptionsDialog.show();
    }


    /*@Override
    public void onBackPressed() {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        finish();
        super.onBackPressed();
    }*/


    private void initializeToolPicking(){

        toolButton = (ToolPickView) findViewById(R.id.currentTool);
        toolButton.setToolSettingsManager(new ToolSettingsManager(this, aps));

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
                        if(aps.cursorMode) {
                            cursorToggle.setImageResource(R.drawable.cursor3);
                            cursorAction.setVisibility(View.VISIBLE);
                        } else {
                            cursorToggle.setImageResource(R.drawable.normal2);
                            cursorAction.setVisibility(View.GONE);
                        }
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
                        aps.setSymmetryEnabled(false, AdaptivePixelSurfaceH.SymmetryType.HORIZONTAL);
                        symmetry.setImageResource(R.drawable.symmetryoff);
                        symmetryModePickView.setVisibility(View.GONE);
                        break;
                    case 1:
                        aps.setSymmetryEnabled(true, AdaptivePixelSurfaceH.SymmetryType.HORIZONTAL);
                        symmetry.setImageResource(R.drawable.symmetryh);
                        symmetryModePickView.setVisibility(View.GONE);
                        break;
                    case 2:
                        aps.setSymmetryEnabled(true, AdaptivePixelSurfaceH.SymmetryType.VERTICAL);
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
        undoButton = (ImageButton) findViewById(R.id.undo);
        undoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                aps.canvasHistory.undoHistoricalChange();
            }
        });

        redoButton = (ImageButton) findViewById(R.id.redo);
        redoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                aps.canvasHistory.redoHistoricalChange();
            }
        });

    }

    @Override
    public void pastAvailabilityChanged(boolean available) {
        if(!available) {
            undoButton.setAlpha(0.5f);
            undoButton.setBackgroundResource(R.drawable.full_round_rect_bg);
        }else{
            undoButton.setAlpha(1f);
            undoButton.setBackgroundResource(R.drawable.sketchbook_style_bg_selector_2);
        }
    }

    @Override
    public void futureAvailabilityChanged(boolean available) {
        if(!available) {
            redoButton.setAlpha(0.5f);
            redoButton.setBackgroundResource(R.drawable.full_round_rect_bg);
        }else{
            redoButton.setAlpha(1f);
            redoButton.setBackgroundResource(R.drawable.sketchbook_style_bg_selector_2);
        }
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

        //Translate and merge
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
                //paletteManager.setPalette(aps.palette);
                pm.setPalette(aps.palette);
            }else if(resultCode == 0){
            }
        }

        //Image Picker
        if(requestCode == IMPORT_IMAGE){
            if(resultCode == Activity.RESULT_OK){
                Intent mergeIntent = new Intent(DrawingActivity.this, BitmapsMergeActivity.class);
                File t = new File(getFilesDir(), "ti.pxl");
                Utils.saveBitmap(aps.pixelBitmap, t);
                mergeIntent.putExtra("path", t.getAbsolutePath());
                mergeIntent.putExtra("mode", BitmapsMergeActivity.MODE_MERGE);
                mergeIntent.putExtra("uri", data.getDataString());
                aps.canvasHistory.startHistoricalChange();
                startActivityForResult(mergeIntent, 1337);
            }
        }
    }

    @Override
    public void onBackPressed() {
        //aps.cleanupWeReLeaving();
        setResult(1);
        finish();
        super.onBackPressed();
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
        i.putExtra("mode", BitmapsMergeActivity.MODE_TRANSLATE);
        i.putExtra("transparentBackground", aps.project.transparentBackground);
        aps.canvasHistory.startHistoricalChange();
        startActivityForResult(i, 1337);
    }


    //Merge
    static final int IMPORT_IMAGE = 13322;
    private void mergeTool(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), IMPORT_IMAGE);
    }


    @Override
    public void onPaletteChangeRequest() {
        Intent pickerIntent = new Intent(DrawingActivity.this, PalettePickerActivity.class);
        pickerIntent.putExtra("pickerMode", true);
        pickerIntent.putExtra("currentPalette", aps.palette.getName());
        startActivityForResult(pickerIntent, PalettePickerActivity.REQUEST_CODE_PICK_PALETTE);
    }


}
