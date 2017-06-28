package com.af.pxl;

import android.graphics.Bitmap;

import java.util.ArrayDeque;
import java.util.ArrayList;

/**
 * Created by Aefyr on 28.06.2017.
 */

public class CanvasHistory {
    AdaptivePixelSurface aps;

    private ArrayDeque<int[]> past;
    private Bitmap bitmap;
    private int size;
    private int arraySize;

    private ArrayDeque<int[]> future;

    private ArrayList<OnHistoryAvailabilityChangeListener> listeners;

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
        arraySize = bitmap.getWidth()*bitmap.getWidth();
        listeners = new ArrayList<>();
    }

    public void setOnHistoryAvailabilityChangeListener(OnHistoryAvailabilityChangeListener listener){
        listeners.add(listener);
    }

    void commitHistoricalChange(){

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
    }

    void undoHistoricalChange(){
        if(past.size()==0)
            return;

        int[] pixels = new int[arraySize];

        bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0 , bitmap.getWidth(), bitmap.getHeight());
        future.addFirst(pixels);

        pixels = past.removeFirst();
        bitmap.setPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());



        for(OnHistoryAvailabilityChangeListener listener: listeners){
            listener.futureAvailabilityChanged(true);
        }

        if(past.size()==0){
            for(OnHistoryAvailabilityChangeListener listener: listeners){
                listener.pastAvailabilityChanged(false);
            }
        }

        aps.pixelDrawThread.update();
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
    }



}
