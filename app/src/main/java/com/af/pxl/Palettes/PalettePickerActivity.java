package com.af.pxl.Palettes;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import com.af.pxl.R;
import com.af.pxl.Utils;

public class PalettePickerActivity extends AppCompatActivity {

    PalettePickRecyclerAdapter adapter;
    public static int REQUEST_CODE_PICK_PALETTE = 932;
    private String currentPaletteName;
    private boolean currentPaletteNameChanged = false;

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
                createPalette();
            }
        });
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
