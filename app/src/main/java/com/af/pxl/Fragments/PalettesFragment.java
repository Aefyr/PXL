package com.af.pxl.Fragments;


import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
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
import com.af.pxl.Palettes.PaletteManager;
import com.af.pxl.Palettes.PalettePickRecyclerAdapter;
import com.af.pxl.Palettes.PaletteUtils;
import com.af.pxl.R;
import com.af.pxl.Utils;

/**
 * A simple {@link Fragment} subclass.
 */
public class PalettesFragment extends Fragment {


    PalettePickRecyclerAdapter adapter;
    PaletteManager paletteManager;

    public PalettesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_palettes, container, false);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.palettesRecycler);
        adapter = new PalettePickRecyclerAdapter(getContext(), PaletteUtils.getSavedPalettes());
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), (int)(Utils.getScreenWidth(getResources())/Utils.dpToPx(130, getResources()))));
        recyclerView.setAdapter(adapter);

        initializePaletteItemsInteractions();

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.palettesFAB);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createPalette();
            }
        });
        return view;
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
        AlertDialog optionsDialog = new AlertDialog.Builder(getContext()).setTitle(palette.getName()).setItems(options, new DialogInterface.OnClickListener() {
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
        final AlertDialog creationDialog = new AlertDialog.Builder(getContext()).setView(R.layout.edit_text).create();
        creationDialog.show();
        final EditText paletteNameET = (EditText) creationDialog.findViewById(R.id.editText);
        creationDialog.findViewById(R.id.okButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String paletteName = paletteNameET.getText().toString();
                if(PaletteUtils.isNameAvailable(paletteName)) {
                    adapter.addItem(new Palette2(paletteName).getName(), PalettePickRecyclerAdapter.AUTO_POSITION);
                    creationDialog.cancel();
                }else {
                    Utils.toaster(getContext(), getString(R.string.incorrect_palette_name));
                }
            }
        });
    }

    private void renamePalette(final Palette2 palette, final int id){
        final AlertDialog renameDialog = new AlertDialog.Builder(getContext()).setTitle(getString(R.string.rename_palette)).setView(R.layout.edit_text).create();
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
                    Utils.toaster(getContext(), getString(R.string.incorrect_palette_name));
                }
            }
        });
    }

    private void duplicatePalette(Palette2 palette, int id){
        adapter.addItem(PaletteUtils.duplicatePalette(palette), id+1);
    }

    private void deletePalette(final Palette2 palette, final int id){
        AlertDialog deleteDialog = new AlertDialog.Builder(getContext()).setTitle(palette.getName()).setMessage(getString(R.string.delete_palette)).setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                PaletteUtils.deletePalette(palette);
                adapter.removeItem(id);
            }
        }).setNegativeButton(getString(R.string.cancel), null).create();
        deleteDialog.show();
    }

}
