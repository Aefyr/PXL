package com.aefyr.pxl.projects;

import android.content.Context;
import android.os.AsyncTask;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Created by Aefyr on 01.11.2017.
 */

public class DynamicProjectsLoader {
    private static DynamicProjectsLoader instance;
    private File projectDirectory;

    private DynamicProjectsLoader(Context c){
        projectDirectory = new File(c.getFilesDir() + "/projects");
        instance = this;
    }

    public static DynamicProjectsLoader getInstance(Context c){
        return instance==null?new DynamicProjectsLoader(c):instance;
    }

    public void loadProjects(ProjectsLoaderCallbackD listener){
        new ProjectsLoaderTask().execute(new Params(listener));
    }

    public interface ProjectsLoaderCallbackD{
        void onProjectLoaded(Project project);
        void onLoadingFinished();
    }

    private class Params{
        ProjectsLoaderCallbackD listener;
        Params(ProjectsLoaderCallbackD listener){
            this.listener = listener;
        }
    }

    private class ProjectsLoaderTask extends AsyncTask<Params, Project, Void>{
        private ProjectsLoaderCallbackD listener;

        @Override
        protected Void doInBackground(Params... params) {
            this.listener = params[0].listener;

            File[] projectsFiles = projectDirectory.listFiles();
            Arrays.sort(projectsFiles, new Comparator<File>() {
                @Override
                public int compare(File o1, File o2) {
                    return -Long.compare(o1.lastModified(), o2.lastModified());
                }
            });

            for(File projectFile: projectsFiles) {
                if(projectFile.getName().equals("version"))
                    continue;

                publishProgress(new Project(projectFile));
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Project... projects) {
            super.onProgressUpdate(projects);
            listener.onProjectLoaded(projects[0]);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            listener.onLoadingFinished();
        }
    }
}
