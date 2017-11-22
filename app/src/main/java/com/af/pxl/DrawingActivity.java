package com.af.pxl;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.af.pxl.palettes.PaletteManagerH;
import com.af.pxl.palettes.PalettePickerActivity;
import com.af.pxl.palettes.PaletteUtils;
import com.af.pxl.projects.ProjectsExporter;
import com.af.pxl.projects.ProjectsUtils;
import com.af.pxl.tools.SymmetrySwitcher;
import com.af.pxl.tools.ToolPickRecyclerAdapter;
import com.af.pxl.tools.ToolPreview;
import com.af.pxl.util.PermissionsUtils;
import com.af.pxl.util.Utils;
import com.af.pxl.views.ColorCircle;

import java.io.File;

public class DrawingActivity extends AppCompatActivity implements AdaptivePixelSurfaceH.OnSpecialToolUseListener, PaletteManagerH.OnPaletteChangeRequestListener, CanvasHistoryH.OnHistoryAvailabilityChangeListener {

    AdaptivePixelSurfaceH aps;
    ImageButton.OnClickListener onClickListener;
    Button cursorAction;
    ImageButton undoButton;
    ImageButton redoButton;

    PaletteManagerH pm;
    ToolPickRecyclerAdapter toolPicker;
    SymmetrySwitcher symmetrySwitcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawing);

        aps = (AdaptivePixelSurfaceH) findViewById(R.id.aps);

        if (getIntent().getStringExtra("projectToLoad") != null)
            aps.setProject(ProjectsUtils.loadProject(getIntent().getStringExtra("projectToLoad")));
        aps.project.notifyProjectModified();


        aps.setOnSpecialToolUseListener(this);
        aps.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (pm.shown()) {
                    pm.hide();
                    return (!aps.cursorMode);
                }
                if (toolPicker.shown()) {
                    toolPicker.hide();
                    return (!aps.cursorMode);
                }
                if (symmetrySwitcher.shown()) {
                    symmetrySwitcher.hide();
                    return (!aps.cursorMode);
                }

                return false;
            }
        });
        aps.canvasHistory.setOnHistoryAvailabilityChangeListener(this);

        initializeButtons();
        initializeToolbar();
        initializeCursor();
        PaletteUtils.initialize(this);
    }

    private void canvasWiseOptionsDialog() {
        AlertDialog canvasWiseOptionsDialog = new AlertDialog.Builder(this).setTitle(getString(R.string.canvas_options)).setItems(getResources().getStringArray(R.array.canvas_options_array), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i) {
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
                    case 4:
                        exportImage();
                        break;

                }
            }
        }).create();
        canvasWiseOptionsDialog.show();
    }


    private void initializeToolbar() {
        //Add tools to the tool selector
        RecyclerView toolRV = (RecyclerView) findViewById(R.id.toolsRecycler);
        toolRV.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        ToolPreview[] previews = new ToolPreview[7];
        previews[0] = new ToolPreview(R.drawable.pencil, AdaptivePixelSurfaceH.Tool.PENCIL);
        previews[1] = new ToolPreview(R.drawable.eraser, AdaptivePixelSurfaceH.Tool.ERASER);
        previews[2] = new ToolPreview(R.drawable.shapes, AdaptivePixelSurfaceH.Tool.MULTISHAPE);
        previews[3] = new ToolPreview(R.drawable.fill, AdaptivePixelSurfaceH.Tool.FLOOD_FILL);
        previews[4] = new ToolPreview(R.drawable.colorpick, AdaptivePixelSurfaceH.Tool.COLOR_PICK);
        previews[5] = new ToolPreview(R.drawable.colorswap, AdaptivePixelSurfaceH.Tool.COLOR_SWAP);
        previews[6] = new ToolPreview(R.drawable.selection, AdaptivePixelSurfaceH.Tool.SELECTOR);

        toolPicker = new ToolPickRecyclerAdapter(this, previews, aps, (ImageButton) findViewById(R.id.toolButton), toolRV, new ToolSettingsManager(this, aps));
        toolRV.setAdapter(toolPicker);

        toolPicker.setOnVisibilityChangedListener(new ToolPickRecyclerAdapter.OnVisibilityChangedListener() {
            @Override
            public void onVisibilityChanged(boolean visible) {
                if (visible) {
                    pm.hide();
                    symmetrySwitcher.hide();
                }
            }
        });


        //Set up palette manager
        pm = new PaletteManagerH((RelativeLayout) findViewById(R.id.paletteBar), (ColorCircle) findViewById(R.id.currentColor), aps, this);
        aps.setColorManager(pm);

        pm.setOnVisibilityChangedListener(new PaletteManagerH.OnVisibilityChangedListener() {
            @Override
            public void onVisibilityChanged(boolean visible) {
                if (visible) {
                    toolPicker.hide();
                    symmetrySwitcher.hide();
                }
            }
        });


        //Setup selection options
        selectionOptions = (LinearLayout) findViewById(R.id.selectionOptions);

        findViewById(R.id.cloneSelection).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                aps.selector.copy();
            }
        });

        findViewById(R.id.deleteSelection).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                aps.selector.delete();
            }
        });


        //Set up symmetry
        symmetrySwitcher = new SymmetrySwitcher((ImageButton) findViewById(R.id.symmetry), (LinearLayout) findViewById(R.id.symmetrySwitcher), aps);

        symmetrySwitcher.setOnVisibilityChangedListener(new SymmetrySwitcher.OnVisibilityChangedListener() {
            @Override
            public void onVisibilityChanged(boolean visible) {
                if (visible) {
                    pm.hide();
                    toolPicker.hide();
                }
            }
        });
    }

    private void initializeButtons() {
        final ImageButton cursorToggle = (ImageButton) findViewById(R.id.cursorMode);
        final ImageButton canvasOptions = (ImageButton) findViewById(R.id.canvasOptions);
        final ImageButton gridToggle = (ImageButton) findViewById(R.id.grid);
        undoButton = (ImageButton) findViewById(R.id.undo);
        redoButton = (ImageButton) findViewById(R.id.redo);

        onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.cursorMode:
                        aps.setCursorModeEnabled(!aps.cursorMode);
                        if (aps.cursorMode) {
                            cursorToggle.setImageResource(R.drawable.cursor3);
                            cursorAction.setVisibility(View.VISIBLE);
                        } else {
                            cursorToggle.setImageResource(R.drawable.normal2);
                            cursorAction.setVisibility(View.GONE);
                        }
                        break;
                    case R.id.canvasOptions:
                        if (aps.currentTool == AdaptivePixelSurfaceH.Tool.SELECTOR)
                            aps.selector.cancel(0, 0);
                        canvasWiseOptionsDialog();
                        break;
                    case R.id.grid:
                        if (aps.toggleGrid())
                            gridToggle.setImageResource(R.drawable.gridon);
                        else
                            gridToggle.setImageResource(R.drawable.gridoff);
                        break;
                    case R.id.undo:
                        aps.canvasHistory.undoHistoricalChange();
                        break;
                    case R.id.redo:
                        aps.canvasHistory.redoHistoricalChange();
                }
            }
        };

        cursorToggle.setOnClickListener(onClickListener);
        canvasOptions.setOnClickListener(onClickListener);
        gridToggle.setOnClickListener(onClickListener);
        undoButton.setOnClickListener(onClickListener);
        redoButton.setOnClickListener(onClickListener);
    }

    private void initializeCursor() {
        //Set up cursor action button
        cursorAction = (Button) findViewById(R.id.cursorAction);
        cursorAction.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN)
                    aps.cursor.cursorDown();
                if (motionEvent.getAction() == MotionEvent.ACTION_UP)
                    aps.cursor.cursorUp();
                return false;
            }
        });

        //Load cursor pointer image
        BitmapFactory.Options op = new BitmapFactory.Options();
        op.inScaled = false;
        aps.cursor.setCursorPointerImage(BitmapFactory.decodeResource(getResources(), R.drawable.defaultcursor2, op));
    }

    @Override
    public void pastAvailabilityChanged(boolean available) {
        if (!available) {
            undoButton.setAlpha(0.5f);
            undoButton.setBackgroundResource(R.drawable.full_round_rect_bg);
        } else {
            undoButton.setAlpha(1f);
            undoButton.setBackgroundResource(R.drawable.sketchbook_style_bg_selector_2);
        }
    }

    @Override
    public void futureAvailabilityChanged(boolean available) {
        if (!available) {
            redoButton.setAlpha(0.5f);
            redoButton.setBackgroundResource(R.drawable.full_round_rect_bg);
        } else {
            redoButton.setAlpha(1f);
            redoButton.setBackgroundResource(R.drawable.sketchbook_style_bg_selector_2);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Color swapTo
        if (requestCode == 322) {
            //Success
            if (resultCode == 1) {
                Bitmap b = BitmapFactory.decodeFile(data.getStringExtra("path"));
                Utils.setBitmapPixelsFromOtherBitmap(aps.pixelBitmap, b);
                b.recycle();
                aps.canvasHistory.completeHistoricalChange();
            } else if (resultCode == 0) {
                //Cancel
                aps.canvasHistory.cancelHistoricalChange(false);
            }
        }

        //Translate and merge
        if (requestCode == 1337) {
            if (resultCode == 1) {
                Bitmap b = BitmapFactory.decodeFile(data.getStringExtra("path"));
                Utils.setBitmapPixelsFromOtherBitmap(aps.pixelBitmap, b);
                b.recycle();
                aps.canvasHistory.completeHistoricalChange();
            } else if (resultCode == 0) {
                aps.canvasHistory.cancelHistoricalChange(false);
            }
        }

        //Pick palette
        if (requestCode == PalettePickerActivity.REQUEST_CODE_PICK_PALETTE) {
            if (resultCode == 1)
                pm.setPalette(PaletteUtils.loadPalette(data.getStringExtra("pickedPalette")));
        }

        //Image Picker
        if (requestCode == IMPORT_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
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
        new AlertDialog.Builder(this).setMessage(R.string.exit_prompt).setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                setResult(1);
                finish();
            }
        }).setNegativeButton(R.string.cancel, null).create().show();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermissionsUtils.CODE_STORAGE_PERMISSIONS_REQUEST) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                switch (actionAfter){
                    case 0:
                        mergeTool();
                        break;
                    case 1:
                        exportImage();
                        break;
                }
            }else
                PermissionsUtils.showNoStoragePermissionWarning(this);
        }
    }


    //Special tools

    //ColorSwap
    @Override
    public void onColorSwapToolUse(int color) {
        System.out.println("Swapping color: " + color);
        startColorSwapActivity(color);
    }

    private void startColorSwapActivity(int colorToSwap) {

        File t = new File(getFilesDir(), "p.pxl");
        Utils.saveBitmap(aps.pixelBitmap, t);

        Intent i = new Intent(DrawingActivity.this, ColorSwapActivity.class);
        i.putExtra("path", t.getAbsolutePath());
        i.putExtra("color", colorToSwap);
        aps.canvasHistory.startHistoricalChange();
        startActivityForResult(i, 322);
    }


    //Selection assist
    LinearLayout selectionOptions;

    @Override
    public void onSelectionOptionsVisibilityChanged(boolean visible) {
        if (visible)
            selectionOptions.setVisibility(View.VISIBLE);
        else
            selectionOptions.setVisibility(View.GONE);
    }


    //Translate
    private void startTranslateActivity() {
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

    private void mergeTool() {
        if (!PermissionsUtils.checkStoragePermissions(this)) {
            actionAfter = 0;
            PermissionsUtils.requestStoragePermissions(this);
            return;
        }
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), IMPORT_IMAGE);
    }

    private int actionAfter = 0; // 0=merge, 1=export
    private void exportImage(){
        if(!PermissionsUtils.checkStoragePermissions(this)) {
            actionAfter = 1;
            PermissionsUtils.requestStoragePermissions(this);
            return;
        }
        ProjectsExporter exporter = new ProjectsExporter(this);
        exporter.prepareDialogFor(aps.project, false, null);
        exporter.showDialog();
    }

    //OnPaletteChangeRequestListener
    @Override
    public void onPaletteChangeRequest() {
        Intent pickerIntent = new Intent(DrawingActivity.this, PalettePickerActivity.class);
        pickerIntent.putExtra("pickerMode", true);
        pickerIntent.putExtra("currentPalette", pm.getPalette().getName());
        startActivityForResult(pickerIntent, PalettePickerActivity.REQUEST_CODE_PICK_PALETTE);
    }




}
