package com.af.pxl.Fragments;


import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;

import com.af.pxl.Palettes.Palette2;
import com.af.pxl.Palettes.PaletteMaker;
import com.af.pxl.Palettes.PaletteManager;
import com.af.pxl.Palettes.PalettePickRecyclerAdapter;
import com.af.pxl.Palettes.PaletteUtils;
import com.af.pxl.R;
import com.af.pxl.Utils;

import java.io.FileNotFoundException;

/**
 * A simple {@link Fragment} subclass.
 */
public class PalettesFragment extends android.app.Fragment {


    PalettePickRecyclerAdapter adapter;
    PaletteManager paletteManager;

    public PalettesFragment() {
        // Required empty public constructor
    }


    int IMPORT_IMAGE = 223;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_palettes, container, false);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.palettesRecycler);
        adapter = new PalettePickRecyclerAdapter(getActivity(), PaletteUtils.getSavedPalettes());
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), (int)(Utils.getScreenWidth(getResources())/Utils.dpToPx(130, getResources()))));
        recyclerView.setAdapter(adapter);

        initializePaletteItemsInteractions();

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.palettesFAB);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String [] items = getResources().getStringArray(R.array.palette_creation_options);
                new AlertDialog.Builder(getActivity()).setItems(items, new DialogInterface.OnClickListener() {
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
        return view;
    }

    private void importImage(){
        if(!Utils.checkPermissions(getActivity())) {
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
                new AlertDialog.Builder(getActivity()).setMessage(getString(R.string.storage_permissions_denied)).setPositiveButton(getString(R.string.ok), null).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==IMPORT_IMAGE&&resultCode== Activity.RESULT_OK){
            Bitmap importedImage;
            try {
                importedImage = BitmapFactory.decodeStream(getActivity().getContentResolver().openInputStream(data.getData()));

            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Utils.toaster(getActivity(), getString(R.string.error));
                return;
            }

            adapter.addItem(PaletteMaker.heyPaletteMaker(importedImage, getResources()).getName(), PalettePickRecyclerAdapter.AUTO_POSITION);
        }
    }

    private int managedPaletteIndex = 0;
    private void initializePaletteItemsInteractions(){
        paletteManager = new PaletteManager();
        paletteManager.hideSelectPaletteOption(true);
        paletteManager.setOnCloseListener(new PaletteManager.OnCloseListener() {
            @Override
            public void onClose() {
                adapter.notifyItemChanged(managedPaletteIndex);
            }
        });
        adapter.setOnPaletteInteractionListener(new PalettePickRecyclerAdapter.OnPaletteInteractionListener() {
            @Override
            public void onPaletteLongClick(final Palette2 palette, final int index) {
                createAndShowOptionsDialog(palette, index);
            }

            @Override
            public void onPaletteClick(Palette2 palette, int index) {
                managedPaletteIndex = index;
                paletteManager.showPaletteManagerDialog((AppCompatActivity) getActivity(), palette);
            }
        });
    }

    private void createAndShowOptionsDialog(final Palette2 palette,final int index){
        String[] options = getResources().getStringArray(R.array.palette_options);
        AlertDialog optionsDialog = new AlertDialog.Builder(getActivity()).setTitle(palette.getName()).setItems(options, new DialogInterface.OnClickListener() {
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

    private void createPalette(){
        final AlertDialog creationDialog = new AlertDialog.Builder(getActivity()).setTitle(getString(R.string.create_new_palette)).setView(R.layout.edit_text).create();
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
                    Utils.toaster(getActivity(), getString(R.string.incorrect_palette_name));
                }
            }
        });
    }

    private void renamePalette(final Palette2 palette, final int id){
        final AlertDialog renameDialog = new AlertDialog.Builder(getActivity()).setTitle(getString(R.string.rename_palette)).setView(R.layout.edit_text).create();
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
                    PaletteUtils.renamePalette(palette, newName);
                    adapter.paletteNames.set(id, newName);
                    adapter.notifyItemChanged(id);
                    renameDialog.dismiss();
                }else {
                    Utils.toaster(getActivity(), getString(R.string.incorrect_palette_name));
                }
            }
        });
    }

    private void duplicatePalette(Palette2 palette, int id){
        adapter.addItem(PaletteUtils.duplicatePalette(palette), id+1);
    }

    private void deletePalette(final Palette2 palette, final int id){
        AlertDialog deleteDialog = new AlertDialog.Builder(getActivity()).setTitle(palette.getName()).setMessage(getString(R.string.delete_palette)).setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                PaletteUtils.deletePalette(palette);
                adapter.removeItem(id);
            }
        }).setNegativeButton(getString(R.string.cancel), null).create();
        deleteDialog.show();
    }

}
