package com.aefyr.pxl.projects;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.aefyr.pxl.R;
import com.aefyr.pxl.util.Utils;

import java.io.File;

/**
 * Created by Aefyr on 02.11.2017.
 */

public class ProjectsExporter {
    private Context c;
    private AlertDialog exportResolutionPickDialog;
    private float resolutionMultiplier = 1;


    public ProjectsExporter(Context c){
        this.c = c;
    }

    public void prepareDialogFor(final Project project, final boolean shareAfterExport, @Nullable final ExportListener listener){
        final int suggestedMultiplier = calculateMultiplierForSharing(project.width, project.height);
        final String[] resolutionOptions = {project.width + "x" + project.height + " " + c.getString(R.string.original), project.width * suggestedMultiplier + "x" + project.height * suggestedMultiplier + " " + c.getString(R.string.suggested_multiplier), c.getString(R.string.custom)};
        exportResolutionPickDialog = new AlertDialog.Builder(c).setTitle(c.getString(R.string.select_resolution)).setItems(resolutionOptions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                resolutionMultiplier = 1;
                boolean launchTask = true;
                switch (i) {
                    case 0:
                        break;
                    case 1:
                        resolutionMultiplier = suggestedMultiplier;
                        break;
                    case 2:
                        launchTask = false;
                        final AlertDialog multiplierPickDialog = new AlertDialog.Builder(c).setView(R.layout.multiplier_picker_dialog).setPositiveButton(R.string.ok, null).setNegativeButton(R.string.cancel, null).create();
                        multiplierPickDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                        multiplierPickDialog.show();
                        final float greaterSideSize = project.width>project.height?project.width:project.height;
                        final float lesserSideSize = greaterSideSize==project.width?project.height:project.width;
                        final TextView lesserSide = multiplierPickDialog.findViewById(R.id.lesserSide);
                        EditText greaterSide = multiplierPickDialog.findViewById(R.id.greaterSide);
                        greaterSide.setText(String.valueOf((int)greaterSideSize));
                        greaterSide.setSelection(0, greaterSide.length());
                        lesserSide.setText(String.valueOf((int)lesserSideSize));
                        greaterSide.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                            }

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {

                            }

                            @Override
                            public void afterTextChanged(Editable s) {
                                if(s.length()==0)
                                    resolutionMultiplier = 0;
                                else
                                    resolutionMultiplier = ((float)(Integer.parseInt(s.toString()))) / greaterSideSize;

                                lesserSide.setText(String.valueOf((int)(lesserSideSize*resolutionMultiplier)));

                            }
                        });

                        multiplierPickDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if((int)(resolutionMultiplier*project.width)==0||(int)(resolutionMultiplier*project.height)==0){
                                    Utils.easyAlert(c, c.getString(R.string.invalid_resolution), c.getString(R.string.invalid_resolution_desc)).show();
                                }else {
                                    new ExportTask().execute(new Params(project, resolutionMultiplier, shareAfterExport, listener));
                                    multiplierPickDialog.dismiss();
                                }
                            }
                        });
                        break;
                }
                if(launchTask)
                    new ExportTask().execute(new Params(project, resolutionMultiplier, shareAfterExport, listener));
            }
        }).create();
    }

    public void showDialog(){
        if(exportResolutionPickDialog!=null) {
            exportResolutionPickDialog.show();
            showResolutionNoticeIfNeeded();
        }
    }

    public interface ExportListener {
        void onProjectExported(File imagePath);
    }

    private class Params{
        Project project;
        ExportListener listener;
        boolean shareAfter;
        float resolutionMultiplier;

        Params(Project project, float resolutionMultiplier, boolean shareAfter, ExportListener listener){
            this.project = project;
            this.resolutionMultiplier = resolutionMultiplier;
            this.shareAfter = shareAfter;
            this.listener = listener;
        }
    }

    private class ExportTask extends AsyncTask<Params, Void, Void>{
        ProgressDialog progressDialog;
        File imagePath;
        boolean share;
        ExportListener listener;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(c, ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage(c.getString(R.string.exporting) + "...");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Params... params) {
            share = params[0].shareAfter;
            listener = params[0].listener;

            File exportDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/pxl");
            if (!exportDir.exists())
                exportDir.mkdirs();

            Project p = params[0].project;
            imagePath = new File(exportDir, System.currentTimeMillis() + ".png");
            Utils.saveBitmap(Bitmap.createScaledBitmap(p.getBitmap(false), (int)((float)p.width * params[0].resolutionMultiplier), (int)((float)p.height * params[0].resolutionMultiplier), false), imagePath);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressDialog.dismiss();

            if (share) {
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                shareIntent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(c, "com.aefyr.pxl.fileprovider", imagePath));
                shareIntent.setType("image/png");
                c.startActivity(Intent.createChooser(shareIntent, c.getString(R.string.share)));
            } else
                Utils.toaster(c, c.getString(R.string.project_saved));

            Utils.alternativeAddImageToGallery(c, imagePath);

            if(listener!=null)
                listener.onProjectExported(imagePath);
        }
    }


    private void showResolutionNoticeIfNeeded(){
        if(!PreferenceManager.getDefaultSharedPreferences(c).getBoolean("res_notice_shown", false)){
            new AlertDialog.Builder(c).setTitle(R.string.warn).setMessage(R.string.res_notice).setPositiveButton(R.string.ok, null).setNeutralButton(R.string.dont_show_again, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    PreferenceManager.getDefaultSharedPreferences(c).edit().putBoolean("res_notice_shown", true).apply();
                }
            }).create().show();
        }
    }

    private int calculateMultiplierForSharing(int width, int height){
        return width>height?4096/width:4096/height;
    }
}
