package com.aefyr.pxl.history;

import com.aefyr.pxl.AdaptivePixelSurfaceH;
import com.aefyr.pxl.projects.Project;

/**
 * Created by Aefyr on 06.02.2018.
 */

public abstract class CanvasHistory {

    //Interface to notify listeners when history availability changes
    public interface OnHistoryAvailabilityChangeListener {
        void pastAvailabilityChanged(boolean available);

        void futureAvailabilityChanged(boolean available);
    }

    //Add listener to notify about history availability changes
    public abstract void addOnHistoryAvailabilityChangeListener(InfiniteCanvasHistory.OnHistoryAvailabilityChangeListener listener);

    //Rebinds this history to the new instances of APS and Project
    public abstract void restore(AdaptivePixelSurfaceH aps, Project project);

    //Notify history that the canvas is about to be edited and it's current state needs to be preserved
    public abstract void startHistoricalChange();

    //Notify history that the canvas has been successfully edited after the startHistoricalChange() call and complete preserving it's state before startHistoricalChange() call
    public abstract void completeHistoricalChange();

    //Notify history to cancel the last call to startHistoricalChange(), canvasWasChanged=true if canvas needs to be restored to it's state before startHistoricalChange() call
    public abstract void cancelHistoricalChange(boolean canvasWasChanged);

    //Undo the last change that was done to canvas
    public abstract void undoHistoricalChange();

    //Redo the last undone change to the canvas
    public abstract void redoHistoricalChange();

    //Are there any elements in the past? Can undo be called?
    public abstract boolean pastAvailable();

    //Are there any elements in the future? Can redo be called?
    public abstract boolean futureAvailable();

    //Save current canvas state to it's real image file
    public abstract void saveCanvas();

    //This history is no longer needed, free the resources
    public abstract void finish();

}
