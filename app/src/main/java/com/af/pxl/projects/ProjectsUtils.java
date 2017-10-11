package com.af.pxl.projects;

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

/**
 * Created by Aefyr on 19.07.2017.
 */

public class ProjectsUtils {
    private static String projectsFolderDirectory;
    private static String projectDuplicatePostfix;
    private static final String PROJECT_NAME_VALIDITY_PATTERN = "\\w+[A-Za-zА-Яа-я_0-9\\s]*";

    public static void initialize(Context c) {
        projectsFolderDirectory = c.getFilesDir() + "/projects";
        projectDuplicatePostfix = " " + c.getString(R.string.project_duplicate_postfix);

        File testDir = new File(projectsFolderDirectory);

        if (!testDir.exists())
            testDir.mkdir();
    }

    public static ArrayList<Project> getProjects() {
        ArrayList<Project> projects = new ArrayList<>();

        File projectsFolder = new File(projectsFolderDirectory);

        File[] projectFolders = projectsFolder.listFiles();

        for (File project : projectFolders) {
            projects.add(new Project(project));
        }

        return projects;
    }

    public static Project loadProject(String projectName) {
        File f = new File(projectsFolderDirectory + "/" + projectName);
        return new Project(f);
    }

    public static boolean isNameAvailable(String name) {
        if (!name.matches(PROJECT_NAME_VALIDITY_PATTERN))
            return false;

        File f = new File(projectsFolderDirectory + "/" + name);
        return (!f.exists());
    }

    public static String getDefaultProjectName(Context c) {
        String name = c.getString(R.string.project);
        if (!isNameAvailable(name)) {
            int a = 1;
            while (!isNameAvailable(name + " " + a))
                a++;
            name += " " + a;
        }
        return name;
    }

    public static Project createNewProject(String name, int pixelWidth, int pixelHeight, String palette, boolean transparentBackground) {
        File newProjectDirectory = new File(projectsFolderDirectory + "/" + name);
        System.out.println("mkdirs=" + newProjectDirectory.mkdir());

        File meta = new File(newProjectDirectory, ".pxlmeta");
        try {
            meta.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (FileWriter fileWrite = new FileWriter(meta)) {
            fileWrite.write(pixelWidth + "," + pixelHeight + "," + palette + "," + transparentBackground);
        } catch (IOException e) {
            e.printStackTrace();
        }
        File bitmapPath = new File(newProjectDirectory, "image.pxl");
        Bitmap b = Bitmap.createBitmap(pixelWidth, pixelHeight, Bitmap.Config.ARGB_8888);
        if (!transparentBackground) {
            Canvas c = new Canvas(b);
            c.drawColor(Color.WHITE);
        }
        Utils.saveBitmap(b, bitmapPath);
        b.recycle();
        return new Project(newProjectDirectory);
    }

    public static Project createProjectFromBitmap(Context c, Bitmap bitmap) {
        String name = c.getString(R.string.name_imported);
        if (!isNameAvailable(name)) {
            int a = 1;
            while (!isNameAvailable(name + " " + a))
                a++;
            name += " " + a;
        }
        Project project = createNewProject(name, bitmap.getWidth(), bitmap.getHeight(), "Default", bitmap.hasAlpha());
        Utils.saveBitmap(bitmap, new File(project.projectDirectory, "image.pxl"));
        return project;
    }

    public static void deleteProject(Project project) {

        if (!project.projectDirectory.exists())
            return;
        File[] children = project.projectDirectory.listFiles();
        for (File c : children)
            c.delete();
        project.projectDirectory.delete();
    }

    public static Project renameProject(Project project, String newName) {
        File newProjectDirectory = new File(projectsFolderDirectory + "/" + newName);
        project.projectDirectory.renameTo(newProjectDirectory);
        project.projectDirectory = newProjectDirectory;
        project.name = newName;
        return project;
    }

    public static Project duplicateProject(Project project) {
        String newProjectName = project.name + projectDuplicatePostfix;

        if (!isNameAvailable(newProjectName)) {
            int a = 1;
            while (!isNameAvailable(newProjectName + " " + a))
                a++;
            newProjectName = newProjectName + " " + a;
        }

        Utils.copyFileOrDirectory(project.projectDirectory, new File(projectsFolderDirectory + "/" + newProjectName));
        return loadProject(newProjectName);
    }
}
