package com.aefyr.pxl.palettes;

import android.graphics.Color;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Aefyr on 10.07.2017.
 */

public class Palette2 {

    private final static int[] defaultPaletteColors = {-1808,-465951,-4024696,-8972,-13238282,-9051393,-38808,-4486,-6226046,-10591253,-5153550,-29966,-16777216,-7418960,-11582910,-23716};
    private ArrayList<Integer> colors;
    private int capacity;
    private int selectedColorIndex;
    private String name;
    private File directory;

    public interface OnPaletteChangeListener{
        void onColorSelection(int selectedColor);
        void onPaletteChanged();
    }

    private ArrayList<OnPaletteChangeListener> listeners;

    public Palette2(String name, int capacity, int initialColor, boolean wasLoaded){
        this.name = name;
        colors = new ArrayList<>();
        colors.add(initialColor);
        directory = new File(PaletteUtils.palettesPath, name+PaletteUtils.EXTENSION);
        if(!wasLoaded)
            autoSave();

        this.capacity = capacity;
        listeners = new ArrayList<>(2);
    }

    public Palette2(String name){
        this.name = name;
        this.capacity = 16;
        colors = new ArrayList<>();
        for(int color: defaultPaletteColors){
            colors.add(color);
        }
        autoSave();
        listeners = new ArrayList<>(2);
    }

    public int getSelectedColorIndex(){
        return selectedColorIndex;
    }

    public int getSelectedColor(){
        return colors.get(selectedColorIndex);
    }

    void setSelectedColor(int index){
        if(index>=colors.size()||index == selectedColorIndex)
            return;
        selectedColorIndex = index;
        selectedColorChangedEvent(colors.get(index));

    }

    public void editColor(int index, int newValue){
        if(index>=colors.size()||newValue == colors.get(index))
            return;
        colors.set(index, newValue);
        listenerEvent();
        selectedColorChangedEvent(newValue);
        autoSave();
    }

    public int getColor(int index){
        if(index>=colors.size())
            return Color.TRANSPARENT;
        return colors.get(index);
    }

    public void addColor(int color){
        if(!isFull())
            colors.add(color);
        listenerEvent();
        autoSave();
    }

    void removeColor(int index){
        if(index>=colors.size())
            return;
        colors.remove(index);
        listenerEvent();
    }

    public boolean colorPickToolWasUsed(int pickedColor){
        int index = colors.indexOf(pickedColor);
        if(index != -1) {
            setSelectedColor(index);
            return true;
        }
        editColor(selectedColorIndex, pickedColor);
        return false;
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    ArrayList<Integer> getColors(){
        return colors;
    }

    int getSize(){
        return colors.size();
    }

    public boolean isFull(){
        return colors.size()>=capacity;
    }

    public void addOnPaletteChangeListener(OnPaletteChangeListener listener){
        listeners.add(listener);
    }

    public void removeOnPaletteChangedListener(OnPaletteChangeListener listener){
        if(listeners.contains(listener))
            listeners.remove(listener);
    }

    private void listenerEvent(){
        for(int i = 0; i<listeners.size(); i++) {
            listeners.get(i).onPaletteChanged();
        }
    }

    private void selectedColorChangedEvent(int newColor){
        for(OnPaletteChangeListener listener: listeners)
            listener.onColorSelection(newColor);
    }

    private void autoSave(){
        PaletteUtils.savePalette(this);
    }

    public long lastModified(){
        return directory.lastModified();
    }

}
