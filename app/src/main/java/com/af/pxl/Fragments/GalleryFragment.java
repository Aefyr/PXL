package com.af.pxl.Fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.af.pxl.DrawingActivity;
import com.af.pxl.LegacyActivity;
import com.af.pxl.Projects.Project;
import com.af.pxl.Projects.ProjectsRecycleAdapter;
import com.af.pxl.Projects.ProjectsUtils;
import com.af.pxl.R;
import com.af.pxl.Utils;


/**
 * A simple {@link Fragment} subclass.
 */
public class GalleryFragment extends Fragment {


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


        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.floatingActionButton2);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog d = new AlertDialog.Builder(getContext()).setView(R.layout.project_creation).create();
                d.show();
                d.findViewById(R.id.create).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String name = ((EditText)d.findViewById(R.id.name)).getText().toString();
                        if(!ProjectsUtils.isNameAvailable(name)){
                            Utils.toaster(getContext(), "Project with this name already exists!");
                            return;
                        }
                        int width = Integer.parseInt(((EditText)d.findViewById(R.id.width)).getText().toString());
                        int height = Integer.parseInt(((EditText)d.findViewById(R.id.height)).getText().toString());
                        ProjectsUtils.createNewProject(name, width, height, "testPalette");
                        openProject(name);
                    }
                });
            }
        });

        ProjectsUtils.initialize(getContext());

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.galleryRecycler);
        ProjectsRecycleAdapter adapter = new ProjectsRecycleAdapter(getContext(), ProjectsUtils.getProjects());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        adapter.setOnProjectClickListener(new ProjectsRecycleAdapter.OnProjectClickListener() {
            @Override
            public void onProjectClick(Project project) {
                System.out.println("Clicked "+project.name);
                openProject(project.name);
            }
        });

        return view;
    }

    private void openProject(String name){
        Intent i = new Intent(getActivity(), DrawingActivity.class);
        i.putExtra("projectToLoad", name);
        startActivity(i);
        getActivity().finish();
    }

}
