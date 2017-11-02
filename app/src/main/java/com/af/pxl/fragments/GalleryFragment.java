package com.af.pxl.fragments;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
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
import android.widget.Switch;

import com.af.pxl.DrawingActivity;
import com.af.pxl.R;
import com.af.pxl.common.Ruler;
import com.af.pxl.projects.DynamicProjectsLoader;
import com.af.pxl.projects.ProjectsExporter;
import com.af.pxl.util.Utils;
import com.af.pxl.projects.Project;
import com.af.pxl.projects.ProjectsRecycleAdapter;
import com.af.pxl.projects.ProjectsUtils;

import java.io.File;
import java.io.FileNotFoundException;


/**
 * A simple {@link Fragment} subclass.
 */
public class GalleryFragment extends android.app.Fragment {

    static final int IMPORT_IMAGE = 512;

    ProjectsRecycleAdapter adapter;
    RecyclerView recyclerView;

    public GalleryFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_gallery, container, false);


        ProjectsUtils.initialize(getActivity());

        recyclerView = (RecyclerView) view.findViewById(R.id.galleryRecycler);
        adapter = new ProjectsRecycleAdapter(getActivity());
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), (int) (Utils.getScreenWidth(getResources()) / Utils.dpToPx(180, getResources()))));
        recyclerView.setItemViewCacheSize(16);
        adapter.setHasStableIds(true);
        recyclerView.setAdapter(adapter);

        initializeFABOnClickListener(view);
        initializeOnProjectClickListener();

        DynamicProjectsLoader.getInstance(getActivity()).loadProjects(new DynamicProjectsLoader.ProjectsLoaderCallbackD() {
            @Override
            public void onProjectLoaded(Project project) {
                adapter.addProject(project);
            }
        });

        return view;
    }

    private static final int DRAWING_REQUEST = 1445;
    private int openedProjectIndex = 0;

    private void openProject(String id, int index) {
        Intent i = new Intent(getActivity(), DrawingActivity.class);
        i.putExtra("projectToLoad", id);
        startActivityForResult(i, DRAWING_REQUEST);

        adapter.moveItem(index, 0);
        recyclerView.scrollToPosition(0);
        recyclerView.clearAnimation();
        openedProjectIndex = 0;
        //getActivity().finish();
    }

    private void initializeFABOnClickListener(View view) {
        final FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.floatingActionButton2);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog newProjectOptionPick = new AlertDialog.Builder(getActivity()).setItems(getResources().getStringArray(R.array.new_project_options), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == 0)
                            createNewProject();
                        else if (i == 1)
                            importImage();
                    }
                }).create();
                newProjectOptionPick.show();
            }
        });
    }

    private void createNewProject() {
        final int dLimit = Ruler.getInstance(getActivity()).maxDimensionSize();
        final AlertDialog d = new AlertDialog.Builder(getActivity()).setView(R.layout.project_creation).setPositiveButton(R.string.create, null).setNegativeButton(R.string.cancel, null).create();
        d.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        d.show();

        final EditText nameET = (EditText) d.findViewById(R.id.name);

        TextWatcher resolutionLimiter = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() == 0)
                    return;
                if (Integer.parseInt(editable.toString()) > dLimit)
                    editable.replace(0, editable.length(), dLimit + "");
            }
        };


        final EditText widthET = (EditText) d.findViewById(R.id.width);
        final EditText heightET = (EditText) d.findViewById(R.id.height);

        widthET.addTextChangedListener(resolutionLimiter);
        heightET.addTextChangedListener(resolutionLimiter);

        d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameET.getText().toString();
                /*if (!ProjectsUtils.isNameAvailable(name)) {
                    Utils.toaster(getActivity(), getString(R.string.incorrect_project_name));
                    return;
                }*/

                if (widthET.getText().length() == 0 || heightET.getText().length() == 0) {
                    Utils.toaster(getActivity(), getString(R.string.incorrect_width_or_height));
                    return;
                }

                int width = Integer.parseInt(widthET.getText().toString());
                int height = Integer.parseInt(heightET.getText().toString());
                Project newProject = ProjectsUtils.createNewProject(name, width, height, "Default", ((Switch) d.findViewById(R.id.transparentBackground)).isChecked());
                adapter.addProject(newProject);
                d.dismiss();
                recyclerView.scrollToPosition(0);
                openProject(newProject.id, adapter.getItemCount()-1);
            }
        });
    }

    private void importImage() {
        if (!Utils.checkPermissions(getActivity())) {
            actionAfter = 1;
            requestPermissions();
            return;
        }
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), IMPORT_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMPORT_IMAGE && resultCode == Activity.RESULT_OK) {
            final int dLimit = Ruler.getInstance(getActivity()).maxDimensionSize();
            Bitmap importedImage;
            try {
                importedImage = BitmapFactory.decodeStream(getActivity().getContentResolver().openInputStream(data.getData()));

            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Utils.toaster(getActivity(), getString(R.string.error));
                return;
            }
            if (importedImage.getWidth() > dLimit || importedImage.getHeight() > dLimit) {
                new AlertDialog.Builder(getActivity()).setMessage(String.format(getString(R.string.imported_bitmap_too_big), dLimit, dLimit)).setPositiveButton(R.string.ok, null).show();
                return;
            }
            Project p = ProjectsUtils.createProjectFromBitmap(getActivity(), importedImage);
            importedImage.recycle();
            adapter.addProject(p);
            openProject(p.id, adapter.getItemCount()-1);
        } else if (requestCode == DRAWING_REQUEST && resultCode == 1) {
            adapter.notifyItemChanged(openedProjectIndex);
        }
    }

    private void initializeOnProjectClickListener() {
        adapter.setOnProjectClickListener(new ProjectsRecycleAdapter.OnProjectClickListener() {
            @Override
            public void onProjectClick(Project project, int id) {
                openProject(project.id, id);
            }

            @Override
            public void onLongProjectClick(final int id, final Project project) {
                AlertDialog optionsDialog = new AlertDialog.Builder(getActivity()).setTitle(project.name).setItems(getResources().getStringArray(R.array.project_options), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i) {
                            case 0:
                                exportProject(project, false);
                                break;
                            case 1:
                                exportProject(project, true);
                                break;
                            case 2:
                                renameProject(project, id);
                                break;
                            case 3:
                                duplicateProject(project);
                                break;
                            case 4:
                                deleteProject(project, id);
                                break;
                        }
                    }
                }).create();
                optionsDialog.show();
            }
        });
    }

    private ProjectsExporter projectsExporter;

    private void exportProject(final Project project, final boolean forShare) {
        if(projectsExporter==null)
            projectsExporter = new ProjectsExporter(getActivity());

        projectsExporter.prepareDialogFor(project, forShare, null);

        if (!Utils.checkPermissions(getActivity())) {
            actionAfter = 0;
            requestPermissions();
            return;
        }
        projectsExporter.showDialog();
    }

    final static int STORAGE_PERMISSIONS_REQUEST = 3232;
    int actionAfter = 0;

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= 23) {
            requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSIONS_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSIONS_REQUEST) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                switch (actionAfter) {
                    case 0:
                        projectsExporter.showDialog();
                        break;
                    case 1:
                        importImage();
                        break;
                }

            } else {
                new AlertDialog.Builder(getActivity()).setMessage(getString(R.string.storage_permissions_denied)).setPositiveButton(getString(R.string.ok), null).show();
            }
        }
    }

    private void renameProject(final Project project, final int index) {
        final AlertDialog renameDialog = new AlertDialog.Builder(getActivity()).setTitle(R.string.rename_project).setView(R.layout.edit_text_dialog_view).setPositiveButton(R.string.ok, null).setNegativeButton(R.string.cancel, null).create();
        renameDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        renameDialog.show();
        final EditText nameEditText = renameDialog.findViewById(R.id.dialogEditText);
        nameEditText.setHint(getString(R.string.new_name));
        nameEditText.setText(project.name);
        nameEditText.selectAll();
        renameDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                project.setName(nameEditText.getText().toString());
                adapter.notifyItemChanged(index);
                renameDialog.dismiss();
            }
        });
    }

    private void duplicateProject(Project project) {
        adapter.addProject(ProjectsUtils.duplicateProject(project));
        recyclerView.scrollToPosition(0);
    }

    private void deleteProject(final Project project, final int id) {
        AlertDialog deleteDialog = new AlertDialog.Builder(getActivity()).setTitle(project.name).setMessage(getString(R.string.delete_project)).setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ProjectsUtils.deleteProject(project);
                adapter.removeItem(id);
            }
        }).setNegativeButton(getString(R.string.cancel), null).create();
        deleteDialog.show();
    }

}
