package com.af.pxl;

import java.util.ArrayList;

/**
 * Created by Aefyr on 10.07.2017.
 */

public class Palette2 {

    private ArrayList<Integer> colors;
    private int capacity;
    private int selectedColorIndex;
    private String name;

    interface OnPaletteChangeListener{
        void paletteChanged();
    }

    OnPaletteChangeListener listener;

    Palette2(String name , int capacity, int initialColor){
        this.name = name;
        colors = new ArrayList<>();
        colors.add(initialColor);

        this.capacity = capacity;
    }

    int getSelectedColorIndex(){
        return selectedColorIndex;
    }

    int getSelectedColor(){
        return colors.get(selectedColorIndex);
    }

    void setSelectedColor(int index){
        if(index<colors.size())
            selectedColorIndex = index;
        listenerEvent();
    }

    void editColor(int index, int newValue){
        if(index>=colors.size()||newValue == colors.get(index))
            return;
        colors.set(index, newValue);
        listenerEvent();
    }

    int getColor(int index){
        return colors.get(index);
    }

    void addColor(int color){
        if(!isFull())
            colors.add(color);
        listenerEvent();
    }

    void removeColor(int index){
        if(index>=colors.size())
            return;
        colors.remove(index);
        listenerEvent();
    }

    void colorPickToolWasUsed(int pickedColor){
        int index = colors.indexOf(pickedColor);
        if(index == -1) {
            addColor(pickedColor);
            setSelectedColor(getSelectedColorIndex()+1);
        }else
            setSelectedColor(index);
    }

    String getName(){
        return name;
    }

    ArrayList<Integer> getColors(){
        return colors;
    }

    int getSize(){
        return colors.size();
    }

    boolean isFull(){
        return colors.size()>=capacity;
    }

    void setOnPaletteChangeListener(OnPaletteChangeListener listener){
        this.listener = listener;
    }

    void listenerEvent(){
        if(listener!=null)
            listener.paletteChanged();
    }

}
