package com.aefyr.pxl.palettes;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import com.aefyr.pxl.R;
import com.aefyr.pxl.analytics.FirebaseConstants;
import com.aefyr.pxl.analytics.PalettesAnalyticsHelper;
import com.aefyr.pxl.util.PermissionsUtils;
import com.aefyr.pxl.util.Utils;

public class PalettePickerActivity extends AppCompatActivity {

    PalettePickRecyclerAdapter adapter;
    RecyclerView recyclerView;
    public static final int REQUEST_CODE_PICK_PALETTE = 932;
    private String currentPaletteName;
    private boolean currentPaletteNameChanged = false;
    private PalettesAnalyticsHelper palettesAnalytics;


    private static final int IMPORT_IMAGE = 223;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_palette_picker);

        recyclerView = (RecyclerView) findViewById(R.id.palettesRecyclerView);
        adapter = new PalettePickRecyclerAdapter(this);
        recyclerView.setLayoutManager(new GridLayoutManager(this, (int) (Utils.getScreenWidth(getResources()) / Utils.dpToPx(130, getResources()))));
        recyclerView.setItemViewCacheSize(24);
        adapter.setHasStableIds(true);
        recyclerView.setAdapter(adapter);
        currentPaletteName = getIntent().getStringExtra("currentPalette");
        initializePaletteItemsInteractions(getIntent().getBooleanExtra("pickerMode", false));


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.palettesFAB);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] items = getResources().getStringArray(R.array.palette_creation_options);
                new AlertDialog.Builder(PalettePickerActivity.this).setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == 0)
                            createPalette();
                        else
                            importImage();
                    }
                }).create().show();
            }
        });

        DynamicPalettesLoader.getInstance(this).loadPalettes(new DynamicPalettesLoader.PalettesLoaderCallbackD() {
            @Override
            public void onPaletteLoaded(Palette2 palette) {
                adapter.addPalette(palette);
            }
        });

        palettesAnalytics = PalettesAnalyticsHelper.getInstance(this);
    }

    private void importImage() {
        if (!PermissionsUtils.checkStoragePermissions(this)) {
            PermissionsUtils.checkStoragePermissions(this);
            return;
        }

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), IMPORT_IMAGE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermissionsUtils.CODE_STORAGE_PERMISSIONS_REQUEST) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                importImage();
            } else {
                new AlertDialog.Builder(this).setMessage(getString(R.string.storage_permissions_denied)).setPositiveButton(getString(R.string.ok), null).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMPORT_IMAGE && resultCode == Activity.RESULT_OK) {

            final ProgressDialog generationDialog = PaletteMakerH.createGenerationProgressDialog(PalettePickerActivity.this);
            generationDialog.show();

            PaletteMakerH paletteMakerH = new PaletteMakerH(PalettePickerActivity.this);
            paletteMakerH.createPaletteFromImage(data.getData(), new PaletteMakerH.PaletteGeneratonListener() {
                @Override
                public void onPaletteGenerated(Palette2 palette) {
                    generationDialog.dismiss();
                    adapter.addPalette(palette);
                    recyclerView.smoothScrollToPosition(adapter.getItemCount()-1);
                    palettesAnalytics.logPaletteCreated(FirebaseConstants.Projects.VIA_IMPORTING);
                }
            });
        }
    }

    private void initializePaletteItemsInteractions(final boolean pickerMode) {
        adapter.setOnPaletteClickListener(new PalettePickRecyclerAdapter.OnPaletteClickListener() {
            @Override
            public void onLongPaletteClick(final Palette2 palette, final int index) {
                String[] options = getResources().getStringArray(R.array.palette_options);
                AlertDialog optionsDialog = new AlertDialog.Builder(PalettePickerActivity.this).setTitle(palette.getName()).setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i) {
                            case 0:
                                renamePalette(palette, index);
                                break;
                            case 1:
                                duplicatePalette(palette);
                                break;
                            case 2:
                                deletePalette(palette, index);
                                break;
                        }
                    }
                }).create();
                optionsDialog.show();
            }

            @Override
            public void onPaletteClick(Palette2 palette, int index) {
                if (pickerMode) {
                    Intent result = new Intent();
                    result.putExtra("pickedPalette", palette.getName());
                    setResult(1, result);
                    finish();
                } else
                    Utils.toaster(PalettePickerActivity.this, "Click!");
            }
        });
    }

    private void createPalette() {
        final AlertDialog creationDialog = new AlertDialog.Builder(this).setTitle(R.string.create_new_palette).setView(R.layout.edit_text_dialog_view).setPositiveButton(R.string.ok, null).setNegativeButton(R.string.cancel, null).create();
        creationDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        creationDialog.show();
        final EditText paletteName = (EditText)creationDialog.findViewById(R.id.dialogEditText);
        paletteName.setFilters(new InputFilter[]{new InputFilter.LengthFilter(48)});
        creationDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = paletteName.getText().toString();
                if (PaletteUtils.isNameAvailable(name)) {
                    adapter.addPalette(new Palette2(name));
                    recyclerView.smoothScrollToPosition(adapter.getItemCount()-1);
                    creationDialog.dismiss();
                    palettesAnalytics.logPaletteCreated(FirebaseConstants.Projects.VIA_NORMAL);
                } else
                    Utils.toaster(PalettePickerActivity.this, getString(R.string.incorrect_palette_name));
            }
        });
        ((EditText)creationDialog.findViewById(R.id.dialogEditText)).setHint(R.string.enter_name);

    }

    private void renamePalette(final Palette2 palette, final int id) {
        final AlertDialog renameDialog = new AlertDialog.Builder(this).setTitle(getString(R.string.rename_palette)).setView(R.layout.edit_text_dialog_view).setPositiveButton(R.string.ok, null).setNegativeButton(R.string.cancel, null).create();
        renameDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        renameDialog.show();

        final EditText nameEditText = renameDialog.findViewById(R.id.dialogEditText);
        nameEditText.setHint(getString(R.string.new_name));
        nameEditText.setText(palette.getName());
        nameEditText.selectAll();

        renameDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newName = nameEditText.getText().toString();
                if (PaletteUtils.isNameAvailable(newName)) {
                    if (palette.getName().equals(currentPaletteName)) {
                        currentPaletteNameChanged = true;
                        currentPaletteName = newName;
                    }
                    adapter.renamePalette(id, newName);
                    renameDialog.dismiss();
                } else {
                    Utils.toaster(PalettePickerActivity.this, getString(R.string.incorrect_palette_name));
                }
            }
        });
    }

    private void duplicatePalette(Palette2 palette) {
        adapter.addPalette(PaletteUtils.duplicatePalette(palette));
        recyclerView.smoothScrollToPosition(adapter.getItemCount()-1);
    }

    private void deletePalette(final Palette2 palette, final int id) {
        AlertDialog deleteDialog = new AlertDialog.Builder(PalettePickerActivity.this).setTitle(palette.getName()).setMessage(getString(R.string.delete_palette)).setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                PaletteUtils.deletePalette(palette);
                adapter.removeItem(id);
            }
        }).setNegativeButton(getString(R.string.cancel), null).create();
        deleteDialog.show();
    }

    @Override
    public void onBackPressed() {
        if (currentPaletteNameChanged) {
            Intent result = new Intent();
            result.putExtra("pickedPalette", currentPaletteName);
            setResult(1, result);
            finish();
        }
        super.onBackPressed();
    }
}
