package com.aefyr.pxl.palettes;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import com.aefyr.pxl.R;
import com.aefyr.pxl.Utils;

import java.io.FileNotFoundException;

public class PalettePickerActivity extends AppCompatActivity {

    PalettePickRecyclerAdapter adapter;
    public static final int REQUEST_CODE_PICK_PALETTE = 932;
    private String currentPaletteName;
    private boolean currentPaletteNameChanged = false;

    private static final int IMPORT_IMAGE = 223;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_palette_picker);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.palettesRecyclerView);
        adapter = new PalettePickRecyclerAdapter(this, PaletteUtils.getSavedPalettes());
        recyclerView.setLayoutManager(new GridLayoutManager(this, (int)(Utils.getScreenWidth(getResources())/Utils.dpToPx(130, getResources()))));
        recyclerView.setAdapter(adapter);

        currentPaletteName = getIntent().getStringExtra("currentPalette");
        initializePaletteItemsInteractions(getIntent().getBooleanExtra("pickerMode", false));


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.palettesFAB);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String [] items = getResources().getStringArray(R.array.palette_creation_options);
                new AlertDialog.Builder(PalettePickerActivity.this).setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(i==0)
                            createPalette();
                        else
                            importImage();
                    }
                }).create().show();
            }
        });
    }

    private void importImage(){
        if(!Utils.checkPermissions(this)) {
            requestPermissions();
            return;
        }

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), IMPORT_IMAGE);
    }

    final static int STORAGE_PERMISSIONS_REQUEST = 3232;
    private void requestPermissions(){
        if(Build.VERSION.SDK_INT >=23) {
            requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSIONS_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==STORAGE_PERMISSIONS_REQUEST){
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED && grantResults[1]==PackageManager.PERMISSION_GRANTED) {
                importImage();
            }
            else {
                new AlertDialog.Builder(this).setMessage(getString(R.string.storage_permissions_denied)).setPositiveButton(getString(R.string.ok), null).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==IMPORT_IMAGE&&resultCode== Activity.RESULT_OK){
            Bitmap importedImage;
            try {
                importedImage = BitmapFactory.decodeStream(getContentResolver().openInputStream(data.getData()));

            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Utils.toaster(this, getString(R.string.error));
                return;
            }

            adapter.addItem(PaletteMaker.heyPaletteMaker(importedImage, getResources()).getName(), PalettePickRecyclerAdapter.AUTO_POSITION);
        }
    }

    private void initializePaletteItemsInteractions(final boolean pickerMode){
        adapter.setOnPaletteInteractionListener(new PalettePickRecyclerAdapter.OnPaletteInteractionListener() {
            @Override
            public void onPaletteLongClick(final Palette2 palette, final int index) {
                String[] options = getResources().getStringArray(R.array.palette_options);
                AlertDialog optionsDialog = new AlertDialog.Builder(PalettePickerActivity.this).setTitle(palette.getName()).setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i){
                            case 0:
                                renamePalette(palette, index);
                                break;
                            case 1:
                                duplicatePalette(palette, index);
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
                if(pickerMode){
                    Intent result = new Intent();
                    result.putExtra("pickedPalette", palette.getName());
                    setResult(1, result);
                    finish();
                }else
                    Utils.toaster(PalettePickerActivity.this, "Click!");
            }
        });
    }

    private void createPalette(){
        final AlertDialog creationDialog = new AlertDialog.Builder(this).setView(R.layout.edit_text).setTitle(getString(R.string.create_new_palette)).create();
        creationDialog.show();
        final EditText paletteNameET = (EditText) creationDialog.findViewById(R.id.editText);
        paletteNameET.setHint(getString(R.string.enter_name));
        creationDialog.findViewById(R.id.okButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String paletteName = paletteNameET.getText().toString();
                if(PaletteUtils.isNameAvailable(paletteName)) {
                    adapter.addItem(new Palette2(paletteName).getName(), PalettePickRecyclerAdapter.AUTO_POSITION);
                    creationDialog.cancel();
                }else {
                    Utils.toaster(PalettePickerActivity.this, getString(R.string.incorrect_palette_name));
                }
            }
        });
    }

    private void renamePalette(final Palette2 palette, final int id){
        final AlertDialog renameDialog = new AlertDialog.Builder(this).setTitle(getString(R.string.rename_palette)).setView(R.layout.edit_text).create();
        renameDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        renameDialog.show();

        final EditText nameEditText = ((EditText)renameDialog.findViewById(R.id.editText));
        nameEditText.setHint(getString(R.string.new_name));
        nameEditText.setText(palette.getName());
        nameEditText.setSelection(0, palette.getName().length());

        renameDialog.findViewById(R.id.okButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newName = nameEditText.getText().toString();
                if(PaletteUtils.isNameAvailable(newName)){
                    if(palette.getName().equals(currentPaletteName)) {
                        currentPaletteNameChanged = true;
                        currentPaletteName = newName;
                    }
                    PaletteUtils.renamePalette(palette, newName);
                    adapter.paletteNames.set(id, newName);
                    adapter.notifyItemChanged(id);
                    renameDialog.dismiss();
                }else {
                    Utils.toaster(PalettePickerActivity.this, "Name is incorrect or already in use!");
                }
            }
        });
    }

    private void duplicatePalette(Palette2 palette, int id){
        adapter.addItem(PaletteUtils.duplicatePalette(palette), id+1);
    }

    private void deletePalette(final Palette2 palette, final int id){
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
        if(currentPaletteNameChanged){
            System.out.println("HEY!");
            Intent result = new Intent();
            result.putExtra("pickedPalette", currentPaletteName);
            setResult(1, result);
            finish();
        }
        super.onBackPressed();
    }
}
