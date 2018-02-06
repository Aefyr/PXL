package com.aefyr.pxl.fragments;

import android.support.v4.app.Fragment;

import com.aefyr.pxl.history.CanvasHistory;

/**
 * Created by Aefyr on 30.01.2018.
 */

public class HistoryHolderFragment extends Fragment {
    private CanvasHistory history;

    public HistoryHolderFragment(){
        setRetainInstance(true);
    }

    public void holdHistory(CanvasHistory history){
        this.history = history;
    }

    public CanvasHistory history(){
        return history;
    }
}
