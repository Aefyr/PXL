package com.af.pxl.Projects;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;

import com.af.pxl.R;
import com.af.pxl.Utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * Created by Aefyr on 19.07.2017.
 */

public class ProjectsUtils{
    static String projectsFolderDirectory;
    static String projectDuplicatePostfix;
    static final String PROJECT_NAME_VALIDITY_PATTERN = "\\w+[A-Za-zА-Яа-я_0-9\\s]*";

    public static void initialize(Context c){
        projectsFolderDirectory = c.getFilesDir()+"/projects";
        projectDuplicatePostfix = " "+c.getString(R.string.project_duplicate_postfix);

        File testDir = new File(projectsFolderDirectory);

        if(!testDir.exists())
            testDir.mkdir();
    }

    public static ArrayList<Project> getProjects(){
        ArrayList<Project> projects = new ArrayList<>();

        File projectsFolder = new File(projectsFolderDirectory);

        File[] projectFolders = projectsFolder.listFiles();

        for(File project: projectFolders){
            projects.add(new Project(project));
        }

        return projects;
    }

    public static Project loadProject(String projectName){
        File f = new File(projectsFolderDirectory+"/"+projectName);
        return new Project(f);
    }

    public static boolean isNameAvailable(String name){
        if(!name.matches(PROJECT_NAME_VALIDITY_PATTERN))
            return false;

        File f = new File(projectsFolderDirectory+"/"+name);
        if(f.exists())
            return false;

        return true;
    }

    public static Project createNewProject(String name, int pixelWidth, int pixelHeight, String palette){
        File newProjectDirectory = new File(projectsFolderDirectory+"/"+name);
        System.out.println("mkdirs="+newProjectDirectory.mkdir());

        File meta = new File(newProjectDirectory, ".pxlmeta");
        try {
            meta.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try(FileWriter fileWrite = new FileWriter(meta)){
            fileWrite.write(pixelWidth+","+pixelHeight+","+palette);
        }catch (IOException e){
            e.printStackTrace();
        }
        File bitmapPath = new File(newProjectDirectory, "image.pxl");
        Bitmap b = Bitmap.createBitmap(pixelWidth, pixelHeight, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        c.drawColor(Color.WHITE);
        Utils.saveBitmap(b, bitmapPath);
        b.recycle();
        return new Project(newProjectDirectory);
    }

    public static void deleteProject(Project project){

        if(!project.projectDirectory.exists())
            return;
        File[] children = project.projectDirectory.listFiles();
        for(File c: children)
            c.delete();
        project.projectDirectory.delete();
    }

    public static Project renameProject(Project project, String newName){
        File newProjectDirectory = new File(projectsFolderDirectory+"/"+newName);
        project.projectDirectory.renameTo(newProjectDirectory);
        project.name = newName;
        return project;
    }

    public static Project duplicateProject(Project project){
        String newProjectName = project.name + projectDuplicatePostfix;

        if(!isNameAvailable(newProjectName)){
            int a = 1;
            while(!isNameAvailable(newProjectName+" "+a))
                a++;
            newProjectName = newProjectName+" "+a;
        }

        Utils.copyFileOrDirectory(project.projectDirectory, new File(projectsFolderDirectory+"/"+newProjectName));
        return loadProject(newProjectName);
    }
}
