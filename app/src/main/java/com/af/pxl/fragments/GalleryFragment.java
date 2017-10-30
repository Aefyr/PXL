package com.af.pxl.fragments;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.ShareCompat;
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
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.af.pxl.DrawingActivity;
import com.af.pxl.R;
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
        adapter = new ProjectsRecycleAdapter(getActivity(), ProjectsUtils.getProjects());
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), (int) (Utils.getScreenWidth(getResources()) / Utils.dpToPx(180, getResources()))));
        recyclerView.setItemViewCacheSize(16);
        adapter.setHasStableIds(true);
        recyclerView.setAdapter(adapter);

        initializeFABOnClickListener(view);
        initializeOnProjectClickListener();

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
                if (Integer.parseInt(editable.toString()) > 512)
                    editable.replace(0, editable.length(), 512 + "");
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
                adapter.addItem(newProject);
                d.dismiss();
                recyclerView.scrollToPosition(0);
                openProject(newProject.id, 0);
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
            Bitmap importedImage;
            try {
                importedImage = BitmapFactory.decodeStream(getActivity().getContentResolver().openInputStream(data.getData()));

            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Utils.toaster(getActivity(), getString(R.string.error));
                return;
            }
            if (importedImage.getWidth() > 512 || importedImage.getHeight() > 512) {
                new AlertDialog.Builder(getActivity()).setMessage(getString(R.string.imported_bitmap_too_big)).setPositiveButton(getString(R.string.ok), null).show();
                return;
            }
            Project p = ProjectsUtils.createProjectFromBitmap(getActivity(), importedImage);
            importedImage.recycle();
            adapter.addItem(p);
            recyclerView.scrollToPosition(0);
            openProject(p.id, 0);
        } else if (requestCode == DRAWING_REQUEST && resultCode == 1) {
            adapter.notifyItemChanged(openedProjectIndex);
        }
    }

    private void initializeOnProjectClickListener() {
        adapter.setOnProjectClickListener(new ProjectsRecycleAdapter.OnProjectClickListener() {
            @Override
            public void onProjectClick(Project project, int id) {
                System.out.println("Clicked " + project.name);
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

    AlertDialog exportResolutionPickDialog;

    private void exportProject(final Project project, final boolean forShare) {
        String[] resolutionOptions = {project.width + "x" + project.height + " (" + getString(R.string.original) + ")", project.width * 2 + "x" + project.height * 2 + " (x2)", project.width * 4 + "x" + project.height * 4 + " (x4)", project.width * 8 + "x" + project.height * 8 + " (x8)", getString(R.string.custom)};
        exportResolutionPickDialog = new AlertDialog.Builder(getActivity()).setTitle(getString(R.string.select_resolution)).setItems(resolutionOptions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                int resolutionMultiplier = 1;
                boolean launchTask = true;
                switch (i) {
                    case 0:
                        break;
                    case 1:
                        resolutionMultiplier = 2;
                        break;
                    case 2:
                        resolutionMultiplier = 4;
                        break;
                    case 3:
                        resolutionMultiplier = 8;
                        break;
                    case 4:
                        launchTask = false;
                        final AlertDialog multiplierPickDialog = new AlertDialog.Builder(getActivity()).setTitle(R.string.select_multiplier).setView(R.layout.multiplier_picker_dialog).setPositiveButton(R.string.ok, null).setNegativeButton(R.string.cancel, null).create();
                        multiplierPickDialog.show();
                        final SeekBar multiplierBar = multiplierPickDialog.findViewById(R.id.seekBar);
                        final TextView multiplierInfo = multiplierPickDialog.findViewById(R.id.textView);
                        final String infoPattern = getString(R.string.multiplier_info_pattern);
                        multiplierInfo.setText(String.format(infoPattern, 8, project.width*8, project.height*8));
                        multiplierBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            @Override
                            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                int multiplier = progress+1;
                                multiplierInfo.setText(String.format(infoPattern, multiplier, project.width*multiplier, project.height*multiplier));
                            }

                            @Override
                            public void onStartTrackingTouch(SeekBar seekBar) {

                            }

                            @Override
                            public void onStopTrackingTouch(SeekBar seekBar) {

                            }
                        });
                        multiplierPickDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ExportTask exportTask = new ExportTask();
                                exportTask.execute(exportTask.createParams(project, forShare, multiplierBar.getProgress()+1));
                                multiplierPickDialog.dismiss();
                            }
                        });
                        break;
                }
                if(launchTask) {
                    ExportTask exportTask = new ExportTask();
                    exportTask.execute(exportTask.createParams(project, forShare, resolutionMultiplier));
                }
            }
        }).create();
        if (!Utils.checkPermissions(getActivity())) {
            actionAfter = 0;
            requestPermissions();
            return;
        }
        exportResolutionPickDialog.show();

    }


    private class ExportTask extends AsyncTask<ExportTask.Params, Void, ExportTask.Params> {

        class Params {
            Project project;
            boolean share;
            int resolutionMultiplier;
            File imagePath;

            Params(Project p, boolean share, int resolutionMultiplier) {
                this.project = p;
                this.share = share;
                this.resolutionMultiplier = resolutionMultiplier;
            }
        }

        Params createParams(Project p, boolean share, int resolutionMultiplier) {
            return new Params(p, share, resolutionMultiplier);
        }

        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(getActivity(), ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage(getString(R.string.exporting) + "...");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Params doInBackground(Params... params) {
            //TODO Change this path to something more unique?
            File exportDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/pxl");
            if (!exportDir.exists())
                exportDir.mkdirs();

            File imagePath = new File(exportDir, params[0].project.id + ".png");
            Utils.saveBitmap(Bitmap.createScaledBitmap(params[0].project.getBitmap(false), params[0].project.width * params[0].resolutionMultiplier, params[0].project.height * params[0].resolutionMultiplier, false), imagePath);
            params[0].imagePath = imagePath;

            return params[0];
        }

        @Override
        protected void onPostExecute(Params params) {
            super.onPostExecute(params);
            progressDialog.dismiss();
            if (params.share) {
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                shareIntent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(getActivity(), "com.af.pxl.fileprovider", params.imagePath));
                shareIntent.setType("image/png");
                startActivity(Intent.createChooser(shareIntent, getString(R.string.share)));
            } else {
                Utils.alternativeAddImageToGallery(getActivity(), params.imagePath);
            }
        }
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
                        exportResolutionPickDialog.show();
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
        adapter.addItem(ProjectsUtils.duplicateProject(project));
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
