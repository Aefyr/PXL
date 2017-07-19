package com.af.pxl.Projects;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;

import com.af.pxl.Utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Aefyr on 19.07.2017.
 */

public class ProjectsUtils{
    static String projectsFolderDirectory;

    public static void initialize(Context c){
        projectsFolderDirectory = c.getFilesDir()+"/projects";

        File testDir = new File(projectsFolderDirectory);

        if(!testDir.exists())
            testDir.mkdir();
    }

    public static ArrayList<Project> getProjects(){
        ArrayList<Project> projects = new ArrayList<>();

        File projectsFolder = new File(projectsFolderDirectory);

        File[] projectFolders = projectsFolder.listFiles();

        for(File project: projectFolders){
            projects.add(new Project(project.getName(), project.getAbsolutePath()));
        }

        return projects;
    }

    public static Project loadProject(String projectName){
        File f = new File(projectsFolderDirectory+"/"+projectName);
        return new Project(projectName, f.getAbsolutePath());
    }

    public static boolean isNameAvailable(String name){
        File f = new File(projectsFolderDirectory+"/"+name);
        return !f.exists();
    }

    public static void createNewProject(String name, int pixelWidth, int pixelHeight, String palette){
        File newProjectDirectory = new File(projectsFolderDirectory+"/"+name);
        System.out.println("mkdirs="+newProjectDirectory.mkdir());

        File meta = new File(newProjectDirectory, "m.pxlmeta");
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
    }
}
