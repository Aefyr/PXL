package com.af.pxl;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;

/**
 * Created by Aefyr on 28.06.2017.
 */

public class CanvasHistory {
    private AdaptivePixelSurface aps;

    private ArrayDeque<int[]> past;
    private Bitmap bitmap;
    private int size;
    private int arraySize;

    private ArrayDeque<int[]> future;

    private ArrayList<OnHistoryAvailabilityChangeListener> listeners;

    private File file;

    public interface OnHistoryAvailabilityChangeListener {
        void pastAvailabilityChanged(boolean available);
        void futureAvailabilityChanged(boolean available);
    }

    public CanvasHistory(AdaptivePixelSurface aps, Bitmap bitmap, int size){
        this.aps = aps;
        this.bitmap = bitmap;
        this.size = size;
        past = new ArrayDeque<>();
        future = new ArrayDeque<>();
        arraySize = bitmap.getWidth()*bitmap.getHeight();
        listeners = new ArrayList<>();

        // FIXME: 14.07.2017
        //Reload autosave file if exists
        file = new File(aps.getContext().getFilesDir(), "autosave.pxl");
        if(file.exists()){
            Utils.setBitmapPixelsFromOtherBitmap(aps.pixelBitmap, BitmapFactory.decodeFile(file.getAbsolutePath()));
        }
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
            aps.pixelDrawThread.update();
        }
        historicalChangeInProgress = false;
    }


    void undoHistoricalChange(){
        if(past.size()==0)
            return;

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

        aps.pixelDrawThread.update();
        saveCanvas();
    }

    void redoHistoricalChange(){
        if(future.size()==0)
            return;

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

        aps.pixelDrawThread.update();
        saveCanvas();
    }

    private void saveCanvas(){


        try {
            try(FileOutputStream out = new FileOutputStream(file, false)){
                aps.pixelBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    int getHistorySize(){
        return future.size()+past.size();
    }



}
