package com.af.pxl;

import android.graphics.Bitmap;

import com.af.pxl.Projects.Project;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;

/**
 * Created by Aefyr on 28.06.2017.
 */

public class CanvasHistoryH {
    private AdaptivePixelSurfaceH aps;

    private ArrayDeque<int[]> past;
    private Bitmap bitmap;
    private int size;
    private int arraySize;

    final static int ADAPTIVE_SIZE = 322;

    private ArrayDeque<int[]> future;

    private ArrayList<OnHistoryAvailabilityChangeListener> listeners;

    private File projectBitmap;

    public interface OnHistoryAvailabilityChangeListener {
        void pastAvailabilityChanged(boolean available);
        void futureAvailabilityChanged(boolean available);
    }

    public CanvasHistoryH(AdaptivePixelSurfaceH aps, Bitmap bitmap, int size){
        this.aps = aps;
        this.bitmap = bitmap;
        this.size = size;
        past = new ArrayDeque<>();
        future = new ArrayDeque<>();
        arraySize = bitmap.getWidth()*bitmap.getHeight();
        listeners = new ArrayList<>();
    }

    public CanvasHistoryH(AdaptivePixelSurfaceH aps, Project project, int size){
        this.aps = aps;
        if(size==ADAPTIVE_SIZE) {
            autoSize();
        }else
            this.size = size;
        setProject(project);
        past = new ArrayDeque<>();
        future = new ArrayDeque<>();
        listeners = new ArrayList<>();
    }

    private void autoSize(){
        int pixelCount = aps.pixelHeight*aps.pixelWidth;
        if(pixelCount<=4096)
            this.size = 300;
        else if(pixelCount<=16384)
            this.size = 200;
        else if(pixelCount<=65536)
            this.size = 100;
        else
            this.size = 64;

        Utils.toaster(aps.getContext(), String.format(aps.getContext().getString(R.string.hisotory_size), this.size));
    }

    public void setOnHistoryAvailabilityChangeListener(OnHistoryAvailabilityChangeListener listener){
        listeners.add(listener);
    }

    /*void commitHistoricalChange(){

        if(past.size()==size){
            past.removeLast();
        }

        int[] pixels = new int[arraySize];
        bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0 , bitmap.getWidth(), bitmap.getHeight());
        past.addFirst(pixels);

        if(past.size()==1){
            for(OnHistoryAvailabilityChangeListener listener: listeners){
                listener.pastAvailabilityChanged(true);
            }
        }


        future.clear();
        for(OnHistoryAvailabilityChangeListener listener: listeners){
            listener.futureAvailabilityChanged(false);
        }
    }*/


    private int[] temp;
    private boolean historicalChangeInProgress = false;
    void startHistoricalChange(){
        temp = new int[arraySize];
        bitmap.getPixels(temp, 0, bitmap.getWidth(), 0, 0 , bitmap.getWidth(), bitmap.getHeight());
        historicalChangeInProgress = true;
    }

    void completeHistoricalChange(){
        System.out.println("HSIZE="+past.size());
        if(historicalChangeInProgress) {
            if (past.size() == size) {
                past.removeLast();
            }

            past.addFirst(temp);

            if (past.size() == 1) {
                for (OnHistoryAvailabilityChangeListener listener : listeners) {
                    listener.pastAvailabilityChanged(true);
                }
            }


            future.clear();
            for (OnHistoryAvailabilityChangeListener listener : listeners) {
                listener.futureAvailabilityChanged(false);
            }

            saveCanvas();
            historicalChangeInProgress = false;
        }
    }

    void cancelHistoricalChange(boolean canvasWasChanged){
        if(canvasWasChanged) {
            bitmap.setPixels(temp, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
            aps.invalidate();
        }
        historicalChangeInProgress = false;
    }


    void undoHistoricalChange(){
        if(past.size()==0)
            return;

        if(aps.currentTool== AdaptivePixelSurfaceH.Tool.SELECTOR)
            aps.selector.cancel(0, 0);

        int[] pixels = new int[arraySize];

        bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        future.addFirst(pixels);

        pixels = past.removeFirst();
        bitmap.setPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());


        for (OnHistoryAvailabilityChangeListener listener : listeners) {
            listener.futureAvailabilityChanged(true);
        }

        if(past.size()==0){
            for(OnHistoryAvailabilityChangeListener listener: listeners){
                listener.pastAvailabilityChanged(false);
            }
        }

        aps.invalidate();
        saveCanvas();
    }

    void redoHistoricalChange(){
        if(future.size()==0)
            return;

        if(aps.currentTool== AdaptivePixelSurfaceH.Tool.SELECTOR)
            aps.selector.cancel(0, 0);

        int[] pixels = new int[arraySize];
        bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        past.addFirst(pixels);

        pixels = future.removeFirst();
        bitmap.setPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        for(OnHistoryAvailabilityChangeListener listener: listeners){
            listener.pastAvailabilityChanged(true);
        }

        if(future.size()==0){
            for(OnHistoryAvailabilityChangeListener listener: listeners){
                listener.futureAvailabilityChanged(false);
            }
        }

        aps.invalidate();
        saveCanvas();
    }


    private Project project;
    void setProject(Project project){
        this.project = project;
        projectBitmap = new File(project.projectDirectory +"/image.pxl");
        bitmap = aps.pixelBitmap;
        arraySize = bitmap.getWidth()*bitmap.getHeight();
    }

    private void saveCanvas(){
        if(projectBitmap ==null)
            return;
        try(FileOutputStream out = new FileOutputStream(projectBitmap, false)) {
            aps.pixelBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            project.notifyProjectModified();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    int getHistorySize(){
        return future.size()+past.size();
    }



}
