package com.aefyr.pxl.fragments;

import android.support.v4.app.Fragment;

import com.aefyr.pxl.CanvasHistoryH;

/**
 * Created by Aefyr on 30.01.2018.
 */

public class HistoryHolderFragment extends Fragment {
    private CanvasHistoryH history;

    public HistoryHolderFragment(){
        setRetainInstance(true);
    }

    public void holdHistory(CanvasHistoryH history){
        this.history = history;
    }

    public CanvasHistoryH history(){
        return history;
    }
}
