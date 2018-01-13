package com.aefyr.pxl;

import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.view.Gravity;
import android.widget.Toast;

import com.aefyr.pxl.projects.Project;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;

/**
 * Created by Aefyr on 28.06.2017.
 */

class CanvasHistoryH {
    private AdaptivePixelSurfaceH aps;

    private ArrayDeque<Bitmap> past;
    private Bitmap bitmap;
    private int size;
    private Paint srcPaint;

    final static int ADAPTIVE_SIZE = 322;

    private ArrayDeque<Bitmap> future;

    private ArrayList<OnHistoryAvailabilityChangeListener> listeners;

    private File projectBitmap;

    interface OnHistoryAvailabilityChangeListener {
        void pastAvailabilityChanged(boolean available);

        void futureAvailabilityChanged(boolean available);
    }

    CanvasHistoryH(AdaptivePixelSurfaceH aps, Bitmap bitmap, int size) {
        this.aps = aps;
        this.bitmap = bitmap;
        this.size = size;
        past = new ArrayDeque<>();
        future = new ArrayDeque<>();
        listeners = new ArrayList<>();
    }

    CanvasHistoryH(AdaptivePixelSurfaceH aps, Project project, int size) {
        this.aps = aps;
        if (size == ADAPTIVE_SIZE) {
            autoSize();
        } else
            this.size = size;

        srcPaint = new Paint();
        srcPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));

        setProject(project);
        past = new ArrayDeque<>(size);
        future = new ArrayDeque<>(size);
        listeners = new ArrayList<>();
    }

    private void autoSize() {
        int pixelCount = aps.pixelHeight * aps.pixelWidth;
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

    void setOnHistoryAvailabilityChangeListener(OnHistoryAvailabilityChangeListener listener) {
        listeners.add(listener);
    }

    private Bitmap temp;
    private boolean historicalChangeInProgress = false;

    void startHistoricalChange() {
        temp = bitmap.copy(Bitmap.Config.ARGB_8888, false);
        historicalChangeInProgress = true;
    }

    void completeHistoricalChange() {
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

            saveCanvas();
            historicalChangeInProgress = false;
        }
    }

    void cancelHistoricalChange(boolean canvasWasChanged) {
        if (historicalChangeInProgress) {
            if (canvasWasChanged) {
                aps.pixelCanvas.drawBitmap(temp, 0, 0, srcPaint);
                aps.invalidate();
            }
            historicalChangeInProgress = false;
        }
    }


    void undoHistoricalChange() {
        if (past.size() == 0)
            return;

        aps.cancelDrawing();

        future.addFirst(bitmap.copy(Bitmap.Config.ARGB_8888, false));

        aps.pixelCanvas.drawBitmap(past.removeFirst(), 0, 0, srcPaint);

        for (OnHistoryAvailabilityChangeListener listener : listeners) {
            listener.futureAvailabilityChanged(true);
        }

        if (past.size() == 0) {
            for (OnHistoryAvailabilityChangeListener listener : listeners) {
                listener.pastAvailabilityChanged(false);
            }
        }

        aps.invalidate();
        saveCanvas();
    }

    void redoHistoricalChange() {
        if (future.size() == 0)
            return;

        if (aps.currentTool == AdaptivePixelSurfaceH.Tool.SELECTOR)
            aps.selector.cancel(0, 0);

        past.addFirst(bitmap.copy(Bitmap.Config.ARGB_8888, false));

        aps.pixelCanvas.drawBitmap(future.removeFirst(), 0, 0, srcPaint);

        for (OnHistoryAvailabilityChangeListener listener : listeners) {
            listener.pastAvailabilityChanged(true);
        }

        if (future.size() == 0) {
            for (OnHistoryAvailabilityChangeListener listener : listeners) {
                listener.futureAvailabilityChanged(false);
            }
        }

        aps.invalidate();
        saveCanvas();
    }


    private Project project;

    void setProject(Project project) {
        this.project = project;
        projectBitmap = new File(project.directory + "/image.pxl");
        bitmap = aps.pixelBitmap;
    }

    private void saveCanvas() {
        if (projectBitmap == null)
            return;
        try (FileOutputStream out = new FileOutputStream(projectBitmap, false)) {
            aps.pixelBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            project.notifyProjectModified();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    int getHistorySize() {
        return future.size() + past.size();
    }


}
