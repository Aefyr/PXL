package com.af.pxl.Palettes;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.af.pxl.R;

import java.util.ArrayList;

/**
 * Created by Aefyr on 22.07.2017.
 */

public class PalettePickRecyclerAdapter extends RecyclerView.Adapter<PalettePickRecyclerAdapter.ViewHolder> {

    ArrayList<String> paletteNames;
    LayoutInflater inflater;
    OnPaletteInteractionListener listener;

    PalettePickRecyclerAdapter(Context c, ArrayList<String> paletteNames){
        inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.paletteNames = paletteNames;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.palette_item, null);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Palette2 p = getItem(position);
        holder.paletteName.setText(p.getName());
        holder.previewView.setPalette(p);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(listener!=null)
                    listener.onPaletteClick(getItem(holder.getAdapterPosition()), holder.getAdapterPosition());
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(listener==null)
                    return false;
                listener.onPaletteLongClick(getItem(holder.getAdapterPosition()), holder.getAdapterPosition());
                return true;
            }
        });
    }

    public void setOnPaletteInteractionListener(OnPaletteInteractionListener listener){
        this.listener = listener;
    }

    public Palette2 getItem(int index){
        return PaletteUtils.loadPalette(paletteNames.get(index));
    }

    public void addItem(String paletteName, int index){
        paletteNames.add(index, paletteName);
        notifyItemInserted(index);
    }

    public void removeItem(int index){
        paletteNames.remove(index);
        notifyItemRemoved(index);
    }

    @Override
    public int getItemCount() {
        return paletteNames.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        private PalettePreviewView previewView;
        private TextView paletteName;
        public ViewHolder(View itemView) {
            super(itemView);
            previewView = (PalettePreviewView) itemView.findViewById(R.id.palettePreview);
            paletteName = (TextView) itemView.findViewById(R.id.paletteName);
        }
    }

    interface OnPaletteInteractionListener{
        void onPaletteLongClick(Palette2 palette,int index);
        void onPaletteClick(Palette2 palette, int index);
    }
}
