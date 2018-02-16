package com.aefyr.pxl.history;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.Log;

import com.aefyr.pxl.AdaptivePixelSurfaceH;
import com.aefyr.pxl.R;
import com.aefyr.pxl.projects.Project;
import com.aefyr.pxl.util.Utils;
import com.google.firebase.crash.FirebaseCrash;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Aefyr on 06.02.2018.
 */

public class InfiniteCanvasHistory extends CanvasHistory {
    private static final String TAG = "InfiniteCanvasHistory";

    private AdaptivePixelSurfaceH aps;
    private Project project;

    private ArrayDeque<Bitmap> past;
    private Bitmap bitmap;
    private Paint srcPaint;

    private ArrayDeque<Bitmap> future;

    private ArrayList<OnHistoryAvailabilityChangeListener> listeners;

    private CanvasSaver saver;
    private AtomicInteger changesSinceLastSave;

    private HistoryOffloader offloader;
    private int t = 32;
    private int trailSizeLimit = 1024;

    public InfiniteCanvasHistory(AdaptivePixelSurfaceH aps, Project project) {
        this.aps = aps;
        autoSize();

        srcPaint = new Paint();
        srcPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));

        setProject(project);

        changesSinceLastSave = new AtomicInteger(0);
        saver = new CanvasSaver(2000);
        saver.start();

        offloader = new HistoryOffloader();
        offloader.start();

        past = new ArrayDeque<>(t);
        future = new ArrayDeque<>(t);
        listeners = new ArrayList<>();
    }

    private void setProject(Project project) {
        this.project = project;
        bitmap = aps.pixelBitmap();
    }

    private class CanvasSaver extends Thread {
        private static final String TAG = "CanvasSaver";
        boolean running = true;
        int interval;

        CanvasSaver(int interval) {
            this.interval = interval;
        }

        @Override
        public void run() {
            while (running) {
                if (changesSinceLastSave.intValue() > 0) {
                    changesSinceLastSave.set(0);
                    long start = System.currentTimeMillis();
                    Log.d(TAG, "Canvas has been changed since last save. Saving...");
                    saveCanvas();
                    Log.d(TAG, String.format("Canvas has been saved in %d ms.", System.currentTimeMillis() - start));
                }

                try {
                    sleep(interval);
                } catch (InterruptedException e) {
                    Log.e(TAG, "Unable to sleep\n" + e.getMessage());
                }
            }
            Log.d(TAG, "Finished.");
        }

    }

    private class HistoryOffloader extends Thread {
        private static final String TAG = "HistoryOffloader";
        boolean running = true;

        private File pastFolder;
        private File futureFolder;
        private int offloadedPastLastIndex = 0;
        private int offloadedPastCount = 0;
        private int offloadedFutureLastIndex = 0;
        private int offloadedFutureCount = 0;

        private int pastBreaksAt = -1;

        HistoryOffloader() {
            pastFolder = new File(aps.getContext().getCacheDir(), "past_" + project.directory.getName() + "_" + System.currentTimeMillis());
            pastFolder.mkdirs();
            futureFolder = new File(aps.getContext().getCacheDir(), "future_" + project.directory.getName() + "_" + System.currentTimeMillis());
            futureFolder.mkdirs();
        }

        @Override
        public void run() {
            while (running) {
                if (pastSize() > t) {
                    offloadPast();
                } else if (offloadedPastCount > 0 && pastSize() < t) {
                    reloadPast();
                }

                if (futureSize() > t) {
                    offloadFuture();
                } else if (offloadedFutureCount > 0 && futureSize() < t) {
                    reloadFuture();
                }

                try {
                    synchronized (this) {
                        wait();
                    }
                } catch (InterruptedException e) {
                    Log.e(TAG, "Unable to call wait");
                    e.printStackTrace();
                }
            }
            Log.d(TAG, "Finishing, deleting history trails...");
            Utils.deleteRecursive(pastFolder);
            Utils.deleteRecursive(futureFolder);
            Log.d(TAG, "Finished.");
        }

        private void offloadPast() {
            if(offloadedPastLastIndex==pastBreaksAt+1){
                offloadedPastLastIndex = 0;
                offloadedPastCount = 0;
                pastBreaksAt = -1;
            }
            while (pastSize() > t) {
                offload(removeElementFromPast(true), pastFolder, "p" + offloadedPastLastIndex++);
                offloadedPastCount++;

                if(offloadedPastCount>trailSizeLimit)
                    breakPast();

            }
            Log.d(TAG, String.format("Offloaded past, there are %d elements in past trail now out of total %d elements in past", offloadedPastCount, pastSize() + offloadedPastCount));
        }

        private void reloadPast() {
            while (offloadedPastCount > 0 && pastSize() < t) {
                Bitmap reloadedPast = reload(pastFolder, "p" + --offloadedPastLastIndex);

                if(reloadedPast!=null) {
                    addElementToPast(reloadedPast, true);
                    offloadedPastCount--;
                    Log.d(TAG, String.format("Reloaded past, %d elements remain in past trail", offloadedPastCount));
                }else {
                    historyLostAlert();
                    Log.wtf(TAG, "Where the hell did the offloaded past go? Resetting offloaded past...");
                    FirebaseCrash.report(new FileNotFoundException("Couldn't find next offloaded past file."));
                    offloadedPastCount = 0;
                    offloadedPastLastIndex = 0;
                }

            }
        }

        private void breakPast(){
            while(offloadedPastCount>trailSizeLimit){
                Utils.deleteRecursive(new File(pastFolder, "p"+(++pastBreaksAt)+".ph"));
                offloadedPastCount--;
                Log.d(TAG, "Past breaks at "+pastBreaksAt);
            }
        }

        private void offloadFuture() {
            while (futureSize() > t) {
                offload(removeElementFromFuture(true), futureFolder, "f" + offloadedFutureLastIndex++);
                offloadedFutureCount++;
            }
            Log.d(TAG, String.format("Offloaded future, there are %d elements in future trail now out of total %d elements in past", offloadedFutureCount, futureSize() + offloadedFutureCount));
        }

        private void reloadFuture() {
            while (offloadedFutureCount > 0 && futureSize() < t) {
                Bitmap reloadedFuture = reload(futureFolder, "f" + --offloadedFutureLastIndex);

                if(reloadedFuture!=null) {
                    addElementToFuture(reloadedFuture, true);
                    offloadedFutureCount--;
                    Log.d(TAG, String.format("Reloaded future, %d elements remain in future trail", offloadedFutureCount));
                }else {
                    historyLostAlert();
                    Log.wtf(TAG, "Where the hell did the offloaded future go? Resetting offloaded future...");
                    FirebaseCrash.report(new FileNotFoundException("Couldn't find next offloaded future file."));
                    destroyFuture();
                }

            }
        }

        private void offload(Bitmap bitmap, File where, String name) {
            Utils.saveBitmap(bitmap, new File(where, name + ".ph"));
            bitmap.recycle();
        }

        private Bitmap reload(File fromWhere, String name) {
            File toReload = new File(fromWhere, name + ".ph");
            Bitmap reloaded = BitmapFactory.decodeFile(toReload.getAbsolutePath());
            toReload.delete();

            return reloaded;
        }

        synchronized void futureDestroyed() {
            offloadedFutureCount = 0;
            offloadedFutureLastIndex = 0;
        }

        synchronized void beat() {
            notifyAll();
        }

        private void historyLostAlert(){
            aps.post(new Runnable() {
                @Override
                public void run() {
                    Utils.easyAlert(aps.getContext(), aps.getContext().getString(R.string.history_gone), aps.getContext().getString(R.string.history_gone_desc)).show();
                }
            });
        }

    }

    private void addElementToPast(Bitmap element, boolean last) {
        addElement(past, element, last);

        if (pastSize() == 1) {
            for (final OnHistoryAvailabilityChangeListener listener : listeners) {
                aps.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.pastAvailabilityChanged(true);
                    }
                });

            }
        }
    }

    private void addElementToFuture(Bitmap element, boolean last) {
        addElement(future, element, last);

        if (futureSize() == 1) {
            for (final OnHistoryAvailabilityChangeListener listener : listeners) {
                aps.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.futureAvailabilityChanged(true);
                    }
                });
            }
        }
    }

    private void addElement(ArrayDeque<Bitmap> arrayDeque, Bitmap element, boolean last) {
        synchronized (arrayDeque) {
            if (last)
                arrayDeque.addLast(element);
            else
                arrayDeque.addFirst(element);
        }
        offloader.beat();
    }

    private Bitmap removeElementFromPast(boolean last) {

        if (pastSize() - 1 == 0) {
            for (final OnHistoryAvailabilityChangeListener listener : listeners) {
                aps.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.pastAvailabilityChanged(false);
                    }
                });
            }
        }

        return removeElement(past, last);
    }

    private Bitmap removeElementFromFuture(boolean last) {

        if (futureSize() - 1 == 0) {
            for (final OnHistoryAvailabilityChangeListener listener : listeners) {
                aps.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.futureAvailabilityChanged(false);
                    }
                });
            }
        }

        return removeElement(future, last);
    }

    private Bitmap removeElement(ArrayDeque<Bitmap> arrayDeque, boolean last) {
        Bitmap b;
        synchronized (arrayDeque) {
            if (last)
                b = arrayDeque.removeLast();
            else
                b = arrayDeque.removeFirst();
        }

        offloader.beat();
        return b;
    }

    private int pastSize() {
        synchronized (past) {
            return past.size();
        }
    }

    private int futureSize() {
        synchronized (future) {
            return future.size();
        }
    }

    private void destroyFuture() {
        synchronized (future) {
            future.clear();
            offloader.futureDestroyed();
            for (OnHistoryAvailabilityChangeListener listener : listeners) {
                listener.futureAvailabilityChanged(false);
            }
        }
    }

    private void autoSize() {
        int pixelCount = aps.pixelHeight() * aps.pixelWidth();
        if (pixelCount <= 4096)
            t = 256;
        else if (pixelCount <= 16384)
            t = 128;
        else if (pixelCount <= 65536)
            t = 64;
        else
            t = 32;
    }

    @Override
    public void addOnHistoryAvailabilityChangeListener(OnHistoryAvailabilityChangeListener listener) {
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
            addElementToPast(temp, false);

            destroyFuture();

            historicalChangeInProgress = false;
            changesSinceLastSave.incrementAndGet();
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

        addElementToFuture(bitmap.copy(Bitmap.Config.ARGB_8888, false), false);

        aps.pixelCanvas().drawBitmap(removeElementFromPast(false), 0, 0, srcPaint);

        aps.invalidate();
        changesSinceLastSave.incrementAndGet();
    }

    @Override
    public void redoHistoricalChange() {
        if (future.size() == 0)
            return;

        aps.cancelDrawing();

        addElementToPast(bitmap.copy(Bitmap.Config.ARGB_8888, false), false);

        aps.pixelCanvas().drawBitmap(removeElementFromFuture(false), 0, 0, srcPaint);

        aps.invalidate();
        changesSinceLastSave.incrementAndGet();
    }

    @Override
    public boolean pastAvailable() {
        return pastSize() > 0;
    }

    @Override
    public boolean futureAvailable() {
        return futureSize() > 0;
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
        Log.d(TAG, "Finishing. Stopping CanvasSaver and HistoryOffloader...");
        saver.running = false;
        offloader.running = false;
        offloader.beat();

        Log.d(TAG, "Synchronously saving canvas...");
        saveCanvas();

        Log.d(TAG, String.format("Finished in %d ms.", System.currentTimeMillis() - start));
    }
}
