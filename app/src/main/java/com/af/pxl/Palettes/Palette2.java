package com.af.pxl.Palettes;

import android.graphics.Color;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Aefyr on 10.07.2017.
 */

public class Palette2 {

    private final static int[] defaultPaletteColors = {-16717636,-13959417,-4020990, -58879, -7798531, -16776960, -11583173, -1, -12000284, -4854924, -870305, -38823, -3706428, -16635957, - 15092247, -7901340};
    private ArrayList<Integer> colors;
    private int capacity;
    private int selectedColorIndex;
    private String name;
    File directory;

    public interface OnPaletteChangeListener{
        void onColorSelection(int selectedColor);
        void onPaletteChanged();
    }

    ArrayList<OnPaletteChangeListener> listeners;

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
        if(index>=colors.size())
            return;
        selectedColorIndex = index;
        for(OnPaletteChangeListener listener: listeners)
            listener.onColorSelection(colors.get(index));
    }

    public void editColor(int index, int newValue){
        if(index>=colors.size()||newValue == colors.get(index))
            return;
        colors.set(index, newValue);
        listenerEvent();
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
        System.out.println("LISTENERS COUNT: "+listeners.size());
        for(int i = 0; i<listeners.size(); i++) {
            listeners.get(i).onPaletteChanged();
            System.out.println("LISTENERS COUNT: "+listeners.size()+", i="+i);
        }

    }

    private void autoSave(){
        PaletteUtils.savePalette(this);
    }

    public long lastModified(){
        return directory.lastModified();
    }

}
