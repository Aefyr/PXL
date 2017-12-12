package com.aefyr.pxl.fragments;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;

import com.aefyr.pxl.R;
import com.aefyr.pxl.analytics.FirebaseConstants;
import com.aefyr.pxl.analytics.PalettesAnalyticsHelper;
import com.aefyr.pxl.palettes.DynamicPalettesLoader;
import com.aefyr.pxl.util.PermissionsUtils;
import com.aefyr.pxl.util.Utils;
import com.aefyr.pxl.palettes.Palette2;
import com.aefyr.pxl.palettes.PaletteMakerH;
import com.aefyr.pxl.palettes.PaletteManager;
import com.aefyr.pxl.palettes.PalettePickRecyclerAdapter;
import com.aefyr.pxl.palettes.PaletteUtils;

/**
 * A simple {@link Fragment} subclass.
 */
public class PalettesFragment extends android.app.Fragment {


    PalettePickRecyclerAdapter adapter;
    PaletteManager paletteManager;
    RecyclerView recyclerView;
    private PalettesAnalyticsHelper palettesAnalytics;

    public PalettesFragment() {
        // Required empty public constructor
    }


    int IMPORT_IMAGE = 223;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_palettes, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.palettesRecycler);
        adapter = new PalettePickRecyclerAdapter(getActivity());
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), (int) (Utils.getScreenWidth(getResources()) / Utils.dpToPx(130, getResources()))));
        recyclerView.setItemViewCacheSize(24);
        adapter.setHasStableIds(true);
        recyclerView.setAdapter(adapter);


        initializePaletteItemsInteractions();

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.palettesFAB);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] items = getResources().getStringArray(R.array.palette_creation_options);
                new AlertDialog.Builder(getActivity()).setItems(items, new DialogInterface.OnClickListener() {
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

        DynamicPalettesLoader.getInstance(getActivity()).loadPalettes(new DynamicPalettesLoader.PalettesLoaderCallbackD() {
            @Override
            public void onPaletteLoaded(Palette2 palette) {
                adapter.addPalette(palette);
            }
        });

        palettesAnalytics = PalettesAnalyticsHelper.getInstance(getActivity());

        return view;
    }


    private void importImage() {
        if (!PermissionsUtils.checkStoragePermissions(getActivity())) {
            PermissionsUtils.requestStoragePermissions(this);
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
                PermissionsUtils.showNoStoragePermissionWarning(getActivity());;
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMPORT_IMAGE && resultCode == Activity.RESULT_OK) {

            final ProgressDialog generationDialog = PaletteMakerH.createGenerationProgressDialog(getActivity());
            generationDialog.show();

            PaletteMakerH paletteMakerH = new PaletteMakerH(getActivity());
            paletteMakerH.createPaletteFromImage(data.getData(), new PaletteMakerH.PaletteGeneratonListener() {
                @Override
                public void onPaletteGenerated(Palette2 palette) {
                    generationDialog.dismiss();
                    adapter.addPalette(palette);
                    recyclerView.smoothScrollToPosition(adapter.getItemCount()-1);
                    paletteManager.showPaletteManagerDialog((AppCompatActivity) getActivity(), palette);
                    palettesAnalytics.logPaletteCreated(FirebaseConstants.Projects.VIA_IMPORTING);

                }
            });
            //recyclerView.smoothScrollToPosition(adapter.addPalette(paletteMakerH.extractPalette3(data.getData()).getName(), PalettePickRecyclerAdapter.AUTO_POSITION));


            /*PaletteMaker.getInstance(getActivity()).extractPalette(getActivity(), data.getData(), new PaletteMaker.OnPaletteGenerationListener() {
                @Override
                public void onPaletteGenerated(Palette2 palette) {
                    recyclerView.smoothScrollToPosition(adapter.addPalette(palette.getName(), PalettePickRecyclerAdapter.AUTO_POSITION));
                    generationDialog.dismiss();
                }

                @Override
                public void onGenerationFailed() {
                    generationDialog.dismiss();
                    Utils.toaster(getActivity(), getString(R.string.error));
                }
            });*/


        }
    }

    private int managedPaletteIndex = 0;

    private void initializePaletteItemsInteractions() {
        paletteManager = new PaletteManager();
        paletteManager.hideSelectPaletteOption(true);
        paletteManager.setOnCloseListener(new PaletteManager.OnCloseListener() {
            @Override
            public void onClose() {
                adapter.notifyItemChanged(managedPaletteIndex);
            }
        });
        adapter.setOnPaletteClickListener(new PalettePickRecyclerAdapter.OnPaletteClickListener() {
            @Override
            public void onLongPaletteClick(final Palette2 palette, final int index) {
                createAndShowOptionsDialog(palette, index);
            }

            @Override
            public void onPaletteClick(Palette2 palette, int index) {
                managedPaletteIndex = index;
                paletteManager.showPaletteManagerDialog((AppCompatActivity) getActivity(), palette);
            }
        });
    }

    private void createAndShowOptionsDialog(final Palette2 palette, final int index) {
        String[] options = getResources().getStringArray(R.array.palette_options);
        AlertDialog optionsDialog = new AlertDialog.Builder(getActivity()).setTitle(palette.getName()).setItems(options, new DialogInterface.OnClickListener() {
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

    private void createPalette() {
        final AlertDialog creationDialog = new AlertDialog.Builder(getActivity()).setTitle(R.string.create_new_palette).setView(R.layout.edit_text_dialog_view).setPositiveButton(R.string.ok, null).setNegativeButton(R.string.cancel, null).create();
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
                    Utils.toaster(getActivity(), getString(R.string.incorrect_palette_name));
            }
        });
        ((EditText)creationDialog.findViewById(R.id.dialogEditText)).setHint(R.string.enter_name);
    }

    private void renamePalette(final Palette2 palette, final int id) {
        final AlertDialog renameDialog = new AlertDialog.Builder(getActivity()).setTitle(getString(R.string.rename_palette)).setView(R.layout.edit_text_dialog_view).setPositiveButton(R.string.ok, null).setNegativeButton(R.string.cancel, null).create();
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
                    adapter.renamePalette(id, newName);
                    renameDialog.dismiss();
                } else {
                    Utils.toaster(getActivity(), getString(R.string.incorrect_palette_name));
                }
            }
        });
    }

    private void duplicatePalette(Palette2 palette) {
        adapter.addPalette(PaletteUtils.duplicatePalette(palette));
        recyclerView.smoothScrollToPosition(adapter.getItemCount()-1);
    }

    private void deletePalette(final Palette2 palette, final int id) {
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
