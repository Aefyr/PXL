package com.aefyr.pxl;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.aefyr.pxl.analytics.CanvasAnalyticsHelper;
import com.aefyr.pxl.analytics.FirebaseConstants;
import com.aefyr.pxl.custom.ColorCircle;
import com.aefyr.pxl.custom.ItemWithIcon;
import com.aefyr.pxl.custom.ListAdapterWithIcons;
import com.aefyr.pxl.custom.SymmetryAxisDragger;
import com.aefyr.pxl.experimental.Tutorial;
import com.aefyr.pxl.history.CanvasHistory;
import com.aefyr.pxl.palettes.PaletteManagerH;
import com.aefyr.pxl.palettes.PalettePickerActivity;
import com.aefyr.pxl.palettes.PaletteUtils;
import com.aefyr.pxl.projects.Project;
import com.aefyr.pxl.projects.ProjectsExporter;
import com.aefyr.pxl.projects.ProjectsUtils;
import com.aefyr.pxl.tools.SymmetrySwitcher;
import com.aefyr.pxl.tools.ToolPickRecyclerAdapter;
import com.aefyr.pxl.util.PermissionsUtils;
import com.aefyr.pxl.util.Utils;

import java.io.File;

public class DrawingActivity extends AppCompatActivity implements AdaptivePixelSurfaceH.OnSpecialToolUseListener, PaletteManagerH.OnPaletteChangeRequestListener, CanvasHistory.OnHistoryAvailabilityChangeListener {

    AdaptivePixelSurfaceH aps;
    ImageButton.OnClickListener onClickListener;
    Button cursorAction;
    ImageButton undoButton;
    ImageButton redoButton;

    PaletteManagerH pm;
    ToolPickRecyclerAdapter toolPicker;
    SymmetrySwitcher symmetrySwitcher;

    private Tutorial tutorial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawing);

        aps = (AdaptivePixelSurfaceH) findViewById(R.id.aps);

        if (getIntent().getStringExtra("projectToLoad") == null) {
            finish();
            return;
        }

        Project project = ProjectsUtils.loadProject(getIntent().getStringExtra("projectToLoad"));
        project.backup();

        aps.setProject(project, savedInstanceState);
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
        aps.canvasHistory.addOnHistoryAvailabilityChangeListener(this);

        initializeCursor();
        initializeButtons(savedInstanceState!=null);
        initializeToolbar(savedInstanceState);
        PaletteUtils.initialize(this);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        instaSwap = preferences.getBoolean("insta_swap", false);

        tutorial = new Tutorial(this);
    }

    private AlertDialog dialog11;
    private void canvasWiseOptionsDialog() {
        if(dialog11 == null) {
            String[] actions = getResources().getStringArray(R.array.canvas_options_array);
            ItemWithIcon[] items = new ItemWithIcon[7];
            items[0] = new ItemWithIcon(actions[0], R.drawable.clear);
            items[1] = new ItemWithIcon(actions[1], R.drawable.move);
            items[2] = new ItemWithIcon(actions[2], R.drawable.overlay);
            items[3] = new ItemWithIcon(actions[3], R.drawable.center);
            items[4] = new ItemWithIcon(actions[4], R.drawable.mirror);
            items[5] = new ItemWithIcon(actions[5], R.drawable.save);
            items[6] = new ItemWithIcon(actions[6], R.drawable.share);
            ListAdapterWithIcons adapterWithIcons = new ListAdapterWithIcons(this, items);

            dialog11 = new AlertDialog.Builder(this).setTitle(getString(R.string.canvas_options)).setAdapter(adapterWithIcons, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    String analyticsAction = "undefined";
                    switch (i) {
                        case 0:
                            aps.clearCanvas();
                            analyticsAction = FirebaseConstants.Canvas.ACTION_CLEAR;
                            break;
                        case 1:
                            startTranslateActivity();
                            analyticsAction = FirebaseConstants.Canvas.ACTION_MOVE;
                            break;
                        case 2:
                            mergeTool();
                            analyticsAction = FirebaseConstants.Canvas.ACTION_OVERLAY;
                            break;
                        case 3:
                            aps.centerCanvas();
                            analyticsAction = FirebaseConstants.Canvas.ACTION_CENTER;
                            aps.invalidate();
                            break;
                        case 4:
                            flipImage();
                            analyticsAction = FirebaseConstants.Canvas.ACTION_MIRROR;
                            break;
                        case 5:
                            exportImage(false);
                            analyticsAction = FirebaseConstants.Canvas.ACTION_SAVE;
                            break;
                        case 6:
                            exportImage(true);
                            analyticsAction = FirebaseConstants.Canvas.ACTION_SHARE;
                            break;

                    }
                    CanvasAnalyticsHelper.getInstance(DrawingActivity.this).logCanvasSpecialAction(analyticsAction);
                }
            }).create();
        }
        dialog11.show();
    }


    private void initializeToolbar(Bundle savedInstanceState) {
        //Add tools to the tool selector
        RecyclerView toolRV = (RecyclerView) findViewById(R.id.toolsRecycler);
        toolRV.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        AdaptivePixelSurfaceH.Tool[] tools = new AdaptivePixelSurfaceH.Tool[7];
        tools[0] = AdaptivePixelSurfaceH.Tool.PENCIL;
        tools[1] = AdaptivePixelSurfaceH.Tool.ERASER;
        tools[2] = AdaptivePixelSurfaceH.Tool.MULTISHAPE;
        tools[3] = AdaptivePixelSurfaceH.Tool.FLOOD_FILL;
        tools[4] = AdaptivePixelSurfaceH.Tool.COLOR_PICK;
        tools[5] = AdaptivePixelSurfaceH.Tool.COLOR_SWAP;
        tools[6] = AdaptivePixelSurfaceH.Tool.SELECTOR;

        toolPicker = new ToolPickRecyclerAdapter(this, tools, aps, (ImageButton) findViewById(R.id.toolButton), toolRV, new ToolSettingsManager(this, aps));
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

        ((SymmetryAxisDragger) findViewById (R.id.symmetryAxisDragger)).bindToAPS(aps);

        if(savedInstanceState!=null){
            toolPicker.restoreState(savedInstanceState);
            pm.restoreState(savedInstanceState);
            symmetrySwitcher.restoreState(savedInstanceState);
        }
    }

    private void initializeButtons(boolean restoreState) {
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
                        tutorial.cursorMode();
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

        if(restoreState){
            if (aps.cursorMode) {
                cursorToggle.setImageResource(R.drawable.cursor3);
                cursorAction.setVisibility(View.VISIBLE);
            } else {
                cursorToggle.setImageResource(R.drawable.normal2);
                cursorAction.setVisibility(View.GONE);
            }

            if (aps.gridEnabled())
                gridToggle.setImageResource(R.drawable.gridon);
            else
                gridToggle.setImageResource(R.drawable.gridoff);

            updateHistoryButton(undoButton, aps.canvasHistory.pastAvailable());
            updateHistoryButton(redoButton, aps.canvasHistory.futureAvailable());
        }
    }

    private void initializeCursor() {
        //Set up cursor getActionName button
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
        updateHistoryButton(undoButton, available);
    }

    @Override
    public void futureAvailabilityChanged(boolean available) {
        updateHistoryButton(redoButton, available);
    }

    private void updateHistoryButton(ImageButton button, boolean available){
        if (!available) {
            button.setAlpha(0.5f);
            button.setBackgroundResource(R.drawable.full_round_rect_bg);
        } else {
            button.setAlpha(1f);
            button.setBackgroundResource(R.drawable.sketchbook_style_bg_selector_2);
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
                aps.cancelDrawing();
                aps.canvasHistory.startHistoricalChange();
                startActivityForResult(mergeIntent, 1337);
            }
        }
    }

    private boolean savedProperly;
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this).setMessage(R.string.exit_prompt).setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                aps.canvasHistory.finish();
                savedProperly = true;
                setResult(1);
                finish();
            }
        }).setNegativeButton(R.string.cancel, null).create().show();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("DrawingActivity", "Stopping");
        if(!savedProperly)
            aps.canvasHistory.saveCanvas();
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
                        exportImage(wasForShare);
                        break;
                }
            }else
                PermissionsUtils.showNoStoragePermissionWarning(this);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        aps.writeStateToBundle(outState);
        toolPicker.writeStateToBundle(outState);
        pm.writeStateToBundle(outState);
        symmetrySwitcher.writeStateToBundle(outState);
    }

    //Special tools

    //ColorSwap
    private boolean instaSwap;
    @Override
    public void onColorSwapToolUse(int color) {
        if(instaSwap){
            aps.canvasHistory.startHistoricalChange();
            ColorSwapperH colorSwapperH = new ColorSwapperH(this, aps.pixelBitmap, color, aps.paint.getColor());
            colorSwapperH.swapTo(aps.paint.getColor());
            colorSwapperH.destroy();
            aps.canvasHistory.completeHistoricalChange();
            aps.invalidate();
        }else {
            System.out.println("Swapping color: " + color);
            startColorSwapActivity(color);
        }
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
        aps.cancelDrawing();
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
    private boolean wasForShare;
    private void exportImage(boolean forShare){
        if(!PermissionsUtils.checkStoragePermissions(this)) {
            wasForShare = forShare;
            actionAfter = 1;
            PermissionsUtils.requestStoragePermissions(this);
            return;
        }
        ProjectsExporter exporter = new ProjectsExporter(this);
        exporter.prepareDialogFor(aps.project, forShare, null);
        exporter.showDialog();
    }

    private void flipImage(){
        new AlertDialog.Builder(this).setTitle(R.string.flip_direction_selection).setItems(R.array.flip_directions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Matrix flippingMatrix = new Matrix();
                flippingMatrix.setScale(which==1?1:-1, which==1?-1:1, ((float)aps.pixelWidth)/2f, ((float)aps.pixelHeight)/2f);
                aps.canvasHistory.startHistoricalChange();
                Bitmap flippedBitmap = Bitmap.createBitmap(aps.pixelBitmap, 0, 0, aps.pixelWidth, aps.pixelHeight, flippingMatrix, false);
                aps.pixelCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                aps.pixelCanvas.drawBitmap(flippedBitmap, 0, 0, null);
                aps.canvasHistory.completeHistoricalChange();
                aps.invalidate();
            }
        }).create().show();
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
