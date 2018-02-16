package com.aefyr.pxl.history;

import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.aefyr.pxl.AdaptivePixelSurfaceH;
import com.aefyr.pxl.R;
import com.aefyr.pxl.projects.Project;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Aefyr on 06.02.2018.
 */

public class CanvasHistoryH extends CanvasHistory {
    private static final String TAG = "CanvasHistoryH";

    private AdaptivePixelSurfaceH aps;
    private Project project;

    private ArrayDeque<Bitmap> past;
    private Bitmap bitmap;
    private int size;
    private Paint srcPaint;

    private ArrayDeque<Bitmap> future;

    private ArrayList<OnHistoryAvailabilityChangeListener> listeners;

    private CanvasSaver saver;
    private AtomicInteger changesSinceLastSave;

    public CanvasHistoryH(AdaptivePixelSurfaceH aps, Project project) {
        this.aps = aps;
        autoSize();

        srcPaint = new Paint();
        srcPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));

        setProject(project);

        changesSinceLastSave = new AtomicInteger(0);
        saver = new CanvasSaver(2000);
        saver.start();

        past = new ArrayDeque<>(size);
        future = new ArrayDeque<>(size);
        listeners = new ArrayList<>();
    }


    void setProject(Project project) {
        this.project = project;
        bitmap = aps.pixelBitmap();
    }

    private class CanvasSaver extends Thread{
        private static final String TAG = "CanvasSaver";
        boolean running = true;
        int interval;

        CanvasSaver(int interval){
            this.interval = interval;
        }

        @Override
        public void run() {
            while(running){
                if(changesSinceLastSave.intValue()>0){
                    changesSinceLastSave.set(0);
                    long start = System.currentTimeMillis();
                    Log.d(TAG, "Canvas has been changed since last save. Saving...");
                    saveCanvas();
                    Log.d(TAG, String.format("Canvas has been saved in %d ms.", System.currentTimeMillis()-start));
                }

                try{
                    sleep(interval);
                }catch (InterruptedException e){
                    Log.e(TAG, "Unable to sleep\n" +e.getMessage());
                }
            }
            Log.d(TAG, "Finished.");
        }

    }

    private void autoSize() {
        int pixelCount = aps.pixelHeight() * aps.pixelWidth();
        if (pixelCount <= 4096)
            this.size = 300;
        else if (pixelCount <= 16384)
            this.size = 200;
        else if (pixelCount <= 65536)
            this.size = 100;
        else
            this.size = 64;

        Toast toastInTheAir = Toast.makeText(aps.getContext(), String.format(aps.getContext().getString(R.string.history_size), this.size), Toast.LENGTH_SHORT);
        toastInTheAir.setGravity(Gravity.CENTER, 0, 0);
        toastInTheAir.show();
    }

    @Override
    public void addOnHistoryAvailabilityChangeListener(InfiniteCanvasHistory.OnHistoryAvailabilityChangeListener listener) {
        listeners.add(listener);
    }

    @Override
    public void restore(AdaptivePixelSurfaceH aps, Project project) {
        listeners.clear();
        this.aps = aps;
        setProject(project);
    }

    private Bitmap temp;
    private boolean historicalChangeInProgress = false;

    @Override
    public void startHistoricalChange() {
        temp = bitmap.copy(Bitmap.Config.ARGB_8888, false);
        historicalChangeInProgress = true;
    }

    @Override
    public void completeHistoricalChange() {
        if (historicalChangeInProgress) {
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

            historicalChangeInProgress = false;
            changesSinceLastSave.addAndGet(1);
        }
    }

    @Override
    public void cancelHistoricalChange(boolean canvasWasChanged) {
        if (historicalChangeInProgress) {
            if (canvasWasChanged) {
                aps.pixelCanvas().drawBitmap(temp, 0, 0, srcPaint);
                aps.invalidate();
            }
            historicalChangeInProgress = false;
        }
    }

    @Override
    public void undoHistoricalChange() {
        if (past.size() == 0)
            return;

        aps.cancelDrawing();

        future.addFirst(bitmap.copy(Bitmap.Config.ARGB_8888, false));

        aps.pixelCanvas().drawBitmap(past.removeFirst(), 0, 0, srcPaint);

        for (OnHistoryAvailabilityChangeListener listener : listeners) {
            listener.futureAvailabilityChanged(true);
        }

        if (past.size() == 0) {
            for (OnHistoryAvailabilityChangeListener listener : listeners) {
                listener.pastAvailabilityChanged(false);
            }
        }

        aps.invalidate();
        changesSinceLastSave.addAndGet(1);
    }

    @Override
    public void redoHistoricalChange() {
        if (future.size() == 0)
            return;

        aps.cancelDrawing();

        past.addFirst(bitmap.copy(Bitmap.Config.ARGB_8888, false));

        aps.pixelCanvas().drawBitmap(future.removeFirst(), 0, 0, srcPaint);

        for (OnHistoryAvailabilityChangeListener listener : listeners) {
            listener.pastAvailabilityChanged(true);
        }

        if (future.size() == 0) {
            for (OnHistoryAvailabilityChangeListener listener : listeners) {
                listener.futureAvailabilityChanged(false);
            }
        }

        aps.invalidate();
        changesSinceLastSave.addAndGet(1);
    }

    @Override
    public boolean pastAvailable() {
        return past.size()>0;
    }

    @Override
    public boolean futureAvailable() {
        return future.size()>0;
    }

    @Override
    public void saveCanvas() {
        if (project == null)
            return;
        try (FileOutputStream out = new FileOutputStream(project.imageFile, false)) {
            aps.pixelBitmap().compress(Bitmap.CompressFormat.PNG, 100, out);
            project.notifyProjectModified();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void finish() {
        long start = System.currentTimeMillis();
        Log.d(TAG, "Finishing. Stopping CanvasSaver's internal loop...");
        saver.running = false;

        Log.d(TAG, "Synchronously saving canvas...");
        saveCanvas();

        Log.d(TAG, String.format("Finished in %d ms.", System.currentTimeMillis()-start));
    }
}
