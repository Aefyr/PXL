package com.aefyr.pxl.fragments;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Switch;

import com.aefyr.pxl.DrawingActivity;
import com.aefyr.pxl.MainActivity;
import com.aefyr.pxl.R;
import com.aefyr.pxl.analytics.FirebaseConstants;
import com.aefyr.pxl.analytics.ProjectsAnalyticsHelper;
import com.aefyr.pxl.common.Ruler;
import com.aefyr.pxl.projects.DynamicProjectsLoader;
import com.aefyr.pxl.projects.Project;
import com.aefyr.pxl.projects.ProjectsExporter;
import com.aefyr.pxl.projects.ProjectsRecycleAdapter;
import com.aefyr.pxl.projects.ProjectsUtils;
import com.aefyr.pxl.util.PermissionsUtils;
import com.aefyr.pxl.util.Posterizer;
import com.aefyr.pxl.util.Utils;

import java.io.File;
import java.io.FileNotFoundException;


/**
 * A simple {@link Fragment} subclass.
 */
public class GalleryFragment extends Fragment {

    static final int IMPORT_IMAGE = 512;

    ProjectsRecycleAdapter adapter;
    RecyclerView recyclerView;

    private ProjectsAnalyticsHelper projectsAnalytics;

    public GalleryFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_gallery, container, false);


        ProjectsUtils.initialize(getContext());

        recyclerView = (RecyclerView) view.findViewById(R.id.galleryRecycler);
        adapter = new ProjectsRecycleAdapter(getContext());
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), (int) (Utils.getScreenWidth(getResources()) / Utils.dpToPx(180, getResources()))));
        recyclerView.setItemViewCacheSize(16);
        adapter.setHasStableIds(true);
        recyclerView.setAdapter(adapter);

        initializeFABOnClickListener(view);
        initializeOnProjectClickListener();

        DynamicProjectsLoader.getInstance(getContext()).loadProjects(new DynamicProjectsLoader.ProjectsLoaderCallbackD() {
            @Override
            public void onProjectLoaded(Project project) {
                adapter.addProject(project, false);
            }
        });

        projectsAnalytics = ProjectsAnalyticsHelper.getInstance(getContext());

        return view;
    }

    private static final int DRAWING_REQUEST = 1445;

    private void openProject(String id, int index) {
        projectsAnalytics.logProjectOpened();
        Intent i = new Intent(getContext(), DrawingActivity.class);
        i.putExtra("projectToLoad", id);
        startActivityForResult(i, DRAWING_REQUEST);

        adapter.moveItemToFront(index);
        recyclerView.scrollToPosition(0);
        recyclerView.clearAnimation();
        ((MainActivity)getContext()).notifyProjectOpened();
        //getContext().finish();
    }

    private void initializeFABOnClickListener(View view) {
        final FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.floatingActionButton2);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog newProjectOptionPick = new AlertDialog.Builder(getContext()).setItems(getResources().getStringArray(R.array.new_project_options), new DialogInterface.OnClickListener() {
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
        final int dLimit = Ruler.getInstance(getContext()).maxDimensionSize();
        final AlertDialog d = new AlertDialog.Builder(getContext()).setView(R.layout.project_creation).setPositiveButton(R.string.create, null).setNegativeButton(R.string.cancel, null).create();
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
                if(nameET.length()==0){
                    Utils.highlightET(nameET);
                    Utils.toaster(getContext(), getString(R.string.invalid_name));
                    return;
                }

                boolean wrongWidth = widthET.getText().length() == 0 || widthET.getText().toString().equals("0");
                if (wrongWidth || heightET.getText().length() == 0 || heightET.getText().toString().equals("0")) {
                    if(wrongWidth)
                        Utils.highlightET(widthET);
                    else
                        Utils.highlightET(heightET);

                    Utils.toaster(getContext(), getString(R.string.incorrect_width_or_height));
                    return;
                }

                String name = nameET.getText().toString();
                int width = Integer.parseInt(widthET.getText().toString());
                int height = Integer.parseInt(heightET.getText().toString());
                Project newProject = ProjectsUtils.createNewProject(name, width, height, "Default", ((Switch) d.findViewById(R.id.transparentBackground)).isChecked());
                adapter.addProject(newProject, false);
                d.dismiss();
                projectsAnalytics.logProjectCreated(FirebaseConstants.Projects.VIA_NORMAL);
                openProject(newProject.id, adapter.getItemCount()-1);
            }
        });
    }

    private void importImage() {
        if (!PermissionsUtils.checkStoragePermissions(getContext())) {
            actionAfter = 1;
            PermissionsUtils.requestStoragePermissions(this);
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
            final int dLimit = Ruler.getInstance(getContext()).maxDimensionSize();
            final Bitmap importedImage;
            try {
                importedImage = BitmapFactory.decodeStream(getContext().getContentResolver().openInputStream(data.getData()));

            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Utils.toaster(getContext(), getString(R.string.error));
                return;
            }
            if (importedImage.getWidth() > dLimit || importedImage.getHeight() > dLimit) {
                final int suggestedW = importedImage.getWidth()>importedImage.getHeight()?dLimit: (int) ((float)importedImage.getWidth() / ((float) importedImage.getHeight() / (float)dLimit));
                final int suggestedH = importedImage.getHeight()>importedImage.getWidth()?dLimit:(int) ((float)importedImage.getHeight() / ((float) importedImage.getWidth() / (float) dLimit));
                new AlertDialog.Builder(getContext()).setTitle(R.string.warn).setMessage(String.format(getString(R.string.resize_promt), dLimit, dLimit, suggestedW, suggestedH)).setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Bitmap resizedImportedImage = Bitmap.createScaledBitmap(importedImage, suggestedW, suggestedH, true);
                        importedImage.recycle();
                        suggestPosterizing(resizedImportedImage);
                    }
                }).setNegativeButton(R.string.cancel_import, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        importedImage.recycle();
                    }
                }).create().show();
                return;
            }
            suggestPosterizing(importedImage);
        } else if (requestCode == DRAWING_REQUEST && resultCode == 1) {
            adapter.notifyItemChanged(0);
        }
    }

    private void suggestPosterizing(final Bitmap image){
        if(PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("hardware_accelerated", true)){
            new AlertDialog.Builder(getContext()).setTitle(R.string.posterizer_prompt).setMessage(R.string.posterizer_desc).setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    final Posterizer posterizer = new Posterizer(getContext());
                    final ProgressDialog progressDialog = Posterizer.createGenerationProgressDialog(getContext());
                    progressDialog.show();
                    posterizer.posterizeAsync(image,  Ruler.getInstance(getContext()).posterizationColorsCount(), new Posterizer.PosterizationListener() {
                        @Override
                        public void onImagePosterized(Bitmap posterizedImage) {
                            progressDialog.dismiss();
                            createProjectFromImportedImage(posterizedImage);
                            posterizedImage.recycle();
                            image.recycle();
                            posterizer.recycle();
                        }
                    });
                }
            }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    createProjectFromImportedImage(image);
                    image.recycle();
                }
            }).create().show();
        }else {
            createProjectFromImportedImage(image);
            image.recycle();
        }
    }

    private void createProjectFromImportedImage(Bitmap importedImage){
        Project p = ProjectsUtils.createProjectFromBitmap(getContext(), importedImage);
        adapter.addProject(p, true);
        projectsAnalytics.logProjectCreated(FirebaseConstants.Projects.VIA_IMPORTING);
        openProject(p.id, 0);
    }

    private void initializeOnProjectClickListener() {
        adapter.setOnProjectClickListener(new ProjectsRecycleAdapter.OnProjectClickListener() {
            @Override
            public void onProjectClick(Project project, int id) {
                openProject(project.id, id);
            }

            @Override
            public void onLongProjectClick(final int id, final Project project) {
                AlertDialog optionsDialog = new AlertDialog.Builder(getContext()).setTitle(project.name).setItems(getResources().getStringArray(R.array.project_options), new DialogInterface.OnClickListener() {
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
            projectsExporter = new ProjectsExporter(getContext());

        projectsExporter.prepareDialogFor(project, forShare, new ProjectsExporter.ExportListener() {
            @Override
            public void onProjectExported(File imagePath) {
                projectsAnalytics.logProjectExported(forShare);
            }
        });

        if (!PermissionsUtils.checkStoragePermissions(getContext())) {
            actionAfter = 0;
            PermissionsUtils.requestStoragePermissions(this);
            return;
        }
        projectsExporter.showDialog();
    }

    int actionAfter = 0;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d("GF", "Code: "+requestCode);
        if (requestCode == PermissionsUtils.CODE_STORAGE_PERMISSIONS_REQUEST) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                switch (actionAfter) {
                    case 0:
                        projectsExporter.showDialog();
                        break;
                    case 1:
                        importImage();
                        break;
                }

            } else
                PermissionsUtils.showNoStoragePermissionWarning(getContext());;
        }
    }

    private void renameProject(final Project project, final int index) {
        final AlertDialog renameDialog = new AlertDialog.Builder(getContext()).setTitle(R.string.rename_project).setView(R.layout.edit_text_dialog_view).setPositiveButton(R.string.ok, null).setNegativeButton(R.string.cancel, null).create();
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
        adapter.addProject(ProjectsUtils.duplicateProject(project), true);
        recyclerView.scrollToPosition(0);
        projectsAnalytics.logProjectCreated(FirebaseConstants.Projects.VIA_DUPLICATING);
    }

    private void deleteProject(final Project project, final int id) {
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
