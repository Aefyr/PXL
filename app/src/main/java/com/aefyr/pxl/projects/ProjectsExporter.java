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
import android.view.View;
import android.widget.SeekBar;
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


    public ProjectsExporter(Context c){
        this.c = c;
    }

    public void prepareDialogFor(final Project project, final boolean shareAfterExport, @Nullable final ExportListener listener){
        String[] resolutionOptions = {project.width + "x" + project.height + " (" + c.getString(R.string.original) + ")", project.width * 2 + "x" + project.height * 2 + " (x2)", project.width * 4 + "x" + project.height * 4 + " (x4)", project.width * 8 + "x" + project.height * 8 + " (x8)", c.getString(R.string.custom)};
        exportResolutionPickDialog = new AlertDialog.Builder(c).setTitle(c.getString(R.string.select_resolution)).setItems(resolutionOptions, new DialogInterface.OnClickListener() {
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
                        final AlertDialog multiplierPickDialog = new AlertDialog.Builder(c).setTitle(R.string.select_multiplier).setView(R.layout.multiplier_picker_dialog).setPositiveButton(R.string.ok, null).setNegativeButton(R.string.cancel, null).create();
                        multiplierPickDialog.show();
                        final SeekBar multiplierBar = multiplierPickDialog.findViewById(R.id.seekBar);
                        final TextView multiplierInfo = multiplierPickDialog.findViewById(R.id.textView);
                        final String infoPattern = c.getString(R.string.multiplier_info_pattern);
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
                                new ExportTask().execute(new Params(project, multiplierBar.getProgress()+1, shareAfterExport, listener));
                                multiplierPickDialog.dismiss();
                            }
                        });
                        break;
                }
                if(launchTask) {
                    new ExportTask().execute(new Params(project, resolutionMultiplier, shareAfterExport, listener));
                }
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
        int resolutionMultiplier;

        Params(Project project, int resolutionMultiplier, boolean shareAfter, ExportListener listener){
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
            Utils.saveBitmap(Bitmap.createScaledBitmap(p.getBitmap(false), p.width * params[0].resolutionMultiplier, p.height * params[0].resolutionMultiplier, false), imagePath);
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
}
