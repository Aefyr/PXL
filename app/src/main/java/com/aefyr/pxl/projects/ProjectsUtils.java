package com.aefyr.pxl.projects;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;

import com.aefyr.pxl.R;
import com.aefyr.pxl.util.Utils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Aefyr on 19.07.2017.
 */

public class ProjectsUtils {
    private static String projectsFolderDirectory;
    private static String DUPLICATE_PREFIX;
    private static final String PROJECT_NAME_VALIDITY_PATTERN = "\\w+[A-Za-zА-Яа-я_0-9\\s]*";
    private static final String META_SEPARATOR = "]|[";
    static final String META_SEPARATOR_REGEX = "]\\|\\[";
    private static File versionFile;


    public static void initialize(Context c) {
        projectsFolderDirectory = c.getFilesDir() + "/projects";
        DUPLICATE_PREFIX = c.getString(R.string.duplicate_prefix)+" ";


        //TODO Create this on first launch
        File projectsDirectory = new File(projectsFolderDirectory);

        if (!projectsDirectory.exists())
            projectsDirectory.mkdir();

        versionFile = new File(projectsFolderDirectory, "version");
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

    public static Project loadProject(String projectId) {
        File f = new File(projectsFolderDirectory + "/" + projectId);
        return new Project(f);
    }

    public static boolean isNameAvailable(String name) {
        if (!name.matches(PROJECT_NAME_VALIDITY_PATTERN))
            return false;

        File f = new File(projectsFolderDirectory + "/" + name);
        return (!f.exists());
    }

    public static int getProjectsCount(){
        return new File(projectsFolderDirectory).listFiles().length;
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
        File newProjectDirectory = new File(projectsFolderDirectory + "/" + generateId());
        System.out.println("mkdirs=" + newProjectDirectory.mkdir());
        name = name.replaceAll(META_SEPARATOR_REGEX, " ");

        Project project = new Project(newProjectDirectory);
        project.name = name;
        project.width = pixelWidth;
        project.height = pixelHeight;
        project.palette = palette;
        project.transparentBackground = transparentBackground;

        writeMeta(project);

        File bitmapPath = new File(newProjectDirectory, "image.pxl");
        Bitmap b = Bitmap.createBitmap(pixelWidth, pixelHeight, Bitmap.Config.ARGB_8888);
        if (!transparentBackground) {
            Canvas c = new Canvas(b);
            c.drawColor(Color.WHITE);
        }
        Utils.saveBitmap(b, bitmapPath);
        b.recycle();

        updateVersion();
        return new Project(newProjectDirectory);
    }

    static void writeMeta(Project project){
        File meta = new File(project.directory, ".pxlmeta");
        if(!meta.exists()) {
            try {
                meta.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try (FileWriter fileWrite = new FileWriter(meta, false)) {
            fileWrite.write(project.width + META_SEPARATOR + project.height + META_SEPARATOR + project.palette + META_SEPARATOR + project.transparentBackground + META_SEPARATOR + project.name);
            updateVersion();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        Utils.saveBitmap(bitmap, new File(project.directory, "image.pxl"));

        updateVersion();
        return project;
    }

    public static void deleteProject(Project project) {
        if (!project.directory.exists())
            return;
        File[] children = project.directory.listFiles();
        for (File c : children)
            c.delete();
        project.directory.delete();

        updateVersion();
    }

    public static Project duplicateProject(Project project) {
        String duplicatedId = generateId();
        String duplicatedName = DUPLICATE_PREFIX + project.name;

        Utils.copyFileOrDirectory(project.directory, new File(projectsFolderDirectory + "/" + duplicatedId));

        Project duplicate = loadProject(duplicatedId);
        duplicate.setName(duplicatedName);
        duplicate.notifyProjectModified();

        updateVersion();
        return duplicate;
    }

    private static String generateId(){
        long id = System.currentTimeMillis();
        while (new File(projectsFolderDirectory + "/" + String.valueOf(System.currentTimeMillis())).exists())
            id++;
        return String.valueOf(id);
    }

    static void updateVersion(){
        try (FileWriter writer = new FileWriter(versionFile, false)){
            writer.write(String.valueOf(System.currentTimeMillis()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static long getVersion(){
        try (FileReader reader = new FileReader(versionFile)){
            int lidlBuffer = reader.read();
            StringBuilder builder = new StringBuilder(11);

            while (lidlBuffer!=-1){
                builder.append((char)lidlBuffer);
                lidlBuffer = reader.read();
            }

            return Long.parseLong(builder.toString());

        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }
}
