package com.af.pxl.Fragments;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;

import com.af.pxl.DrawingActivity;
import com.af.pxl.Palettes.PalettePickerActivity;
import com.af.pxl.Projects.Project;
import com.af.pxl.Projects.ProjectsRecycleAdapter;
import com.af.pxl.Projects.ProjectsUtils;
import com.af.pxl.R;
import com.af.pxl.TestActivity;
import com.af.pxl.Utils;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class GalleryFragment extends Fragment {

    ProjectsRecycleAdapter adapter;

    public GalleryFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_gallery, container, false);


        view.findViewById(R.id.saveme).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), DrawingActivity.class);
                startActivity(i);
                getActivity().finish();
            }
        });




        ProjectsUtils.initialize(getContext());

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.galleryRecycler);
        adapter = new ProjectsRecycleAdapter(getContext(), ProjectsUtils.getProjects());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));


        view.findViewById(R.id.test1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), PalettePickerActivity.class);
                startActivity(i);
            }
        });

        initializeFABOnClickListener(view);
        initializeOnProjectClickListener();

        return view;
    }

    private void openProject(String name){
        Intent i = new Intent(getActivity(), DrawingActivity.class);
        i.putExtra("projectToLoad", name);
        startActivity(i);
        //getActivity().finish();
    }

    private void initializeFABOnClickListener(View view){
        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.floatingActionButton2);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog d = new AlertDialog.Builder(getContext()).setView(R.layout.project_creation).create();
                d.show();

                TextWatcher resolutionLimiter = new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        if(editable.length()==0)
                            return;
                        if(Integer.parseInt(editable.toString())>512)
                            editable.replace(0, editable.length(), 512+"");
                    }
                };

                final EditText widthET = (EditText)d.findViewById(R.id.width);
                final EditText heightET = (EditText)d.findViewById(R.id.height);

                widthET.addTextChangedListener(resolutionLimiter);
                heightET.addTextChangedListener(resolutionLimiter);

                d.findViewById(R.id.create).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String name = ((EditText)d.findViewById(R.id.name)).getText().toString();
                        if(!ProjectsUtils.isNameAvailable(name)){
                            Utils.toaster(getContext(), "Project with this name already exists!");
                            return;
                        }


                        int width = Integer.parseInt(widthET.getText().toString());
                        int height = Integer.parseInt(heightET.getText().toString());
                        adapter.addItem(ProjectsUtils.createNewProject(name, width, height, "Default"));
                        d.dismiss();
                        openProject(name);
                    }
                });
            }
        });
    }

    private void initializeOnProjectClickListener(){
        adapter.setOnProjectClickListener(new ProjectsRecycleAdapter.OnProjectClickListener() {
            @Override
            public void onProjectClick(Project project) {
                System.out.println("Clicked "+project.name);
                openProject(project.name);
            }

            @Override
            public void onProjectLongClick(final int id, final Project project) {
                AlertDialog optionsDialog = new AlertDialog.Builder(getContext()).setTitle(project.name).setItems(getResources().getStringArray(R.array.project_options), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i){
                            case 0:
                                renameProject(project, id);
                                break;
                            case 1:
                                duplicateProject(project);
                                break;
                            case 2:
                                deleteProject(project, id);
                                break;
                        }
                    }
                }).create();
                optionsDialog.show();
            }
        });
    }

    private void renameProject(final Project project, final int id){
        final AlertDialog renameDialog = new AlertDialog.Builder(getContext()).setTitle(getString(R.string.rename_project)).setView(R.layout.edit_text).create();
        renameDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        renameDialog.show();

        final EditText nameEditText = ((EditText)renameDialog.findViewById(R.id.editText));
        nameEditText.setHint(getString(R.string.new_name));
        nameEditText.setText(project.name);
        nameEditText.setSelection(0, project.name.length());

        renameDialog.findViewById(R.id.okButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newName = nameEditText.getText().toString();
                if(ProjectsUtils.isNameAvailable(newName)){
                    ProjectsUtils.renameProject(project, newName);
                    adapter.notifyItemChanged(id);
                    renameDialog.dismiss();
                }else {
                    Utils.toaster(getContext(), "Name is incorrect or already in use!");
                }
            }
        });

    }

    private void duplicateProject(Project project){
        adapter.addItem(ProjectsUtils.duplicateProject(project));
    }

    private void deleteProject(final Project project, final int id){
        AlertDialog deleteDialog = new AlertDialog.Builder(getContext()).setTitle(project.name).setMessage(getString(R.string.delete_project)).setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ProjectsUtils.deleteProject(project);
                adapter.removeItem(id);
            }
        }).setNegativeButton(getString(R.string.cancel), null).create();
        deleteDialog.show();
    }

}
