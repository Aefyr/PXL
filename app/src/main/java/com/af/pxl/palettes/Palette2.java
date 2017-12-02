package com.af.pxl.palettes;

import android.graphics.Color;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Aefyr on 10.07.2017.
 */

public class Palette2 {

    private final static int[] defaultPaletteColors = {-43948,-21441,-266496,-3280384,-8388767,-8519737,-7340801,-12081153,-12562991,-6267649,-1937944,-35885,-1,-6513508,-10263709,-16777216,-23716};
    private ArrayList<Integer> colors;
    private String name;
    private File directory;

    public interface OnPaletteChangeListener {
        void onColorSelection(int selectedColor);

        void onPaletteChanged();
    }

    private ArrayList<OnPaletteChangeListener> listeners;

    public Palette2(String name, boolean wasLoaded) {
        this.name = name;
        colors = new ArrayList<>(16);
        directory = new File(PaletteUtils.palettesPath, name + PaletteUtils.EXTENSION);
        if (!wasLoaded) {
            autoSave();
            directory.setLastModified(System.currentTimeMillis());
        }

        listeners = new ArrayList<>(2);
    }

    File directory(){
        return directory;
    }

    void setDirectory(File newDirectory){
        this.directory = newDirectory;
    }

    public Palette2(String name) {
        this.name = name;
        colors = new ArrayList<>(16);
        for (int color : defaultPaletteColors) {
            colors.add(color);
        }
        autoSave();
        listeners = new ArrayList<>(2);
    }


    public void editColor(int index, int newValue) {
        if (index >= colors.size() || newValue == colors.get(index))
            return;
        colors.set(index, newValue);
        listenerEvent();
        selectedColorChangedEvent(newValue);
        autoSave();
    }

    public int getColor(int index) {
        if (index >= colors.size())
            return Color.TRANSPARENT;
        return colors.get(index);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        listenerEvent();
    }

    ArrayList<Integer> getColors() {
        return colors;
    }

    void setColors(ArrayList<Integer> colors){
        this.colors = colors;
        autoSave();
    }

    void fillVoidColorsWithDefault(boolean autoSave){
        for(int i = colors.size()-1;i<16;i++)
            colors.add(defaultPaletteColors[i]);

        if(autoSave)
            autoSave();
    }


    public void addOnPaletteChangeListener(OnPaletteChangeListener listener) {
        listeners.add(listener);
    }

    public void removeOnPaletteChangedListener(OnPaletteChangeListener listener) {
        if (listeners.contains(listener))
            listeners.remove(listener);
    }

    private void listenerEvent() {
        for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).onPaletteChanged();
        }
    }

    private void selectedColorChangedEvent(int newColor) {
        for (OnPaletteChangeListener listener : listeners)
            listener.onColorSelection(newColor);
    }

    private void autoSave() {
        PaletteUtils.savePalette(this);
    }

    void setLastModified(long time){
        directory.setLastModified(time);
    }

    public long lastModified() {
        return directory.lastModified();
    }

}
