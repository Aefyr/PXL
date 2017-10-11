package com.af.pxl.palettes;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.af.pxl.R;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Aefyr on 22.07.2017.
 */

public class PalettePickRecyclerAdapter extends RecyclerView.Adapter<PalettePickRecyclerAdapter.PaletteViewHolder> {

    public ArrayList<String> paletteNames;
    private LayoutInflater inflater;
    private OnPaletteClickListener onPaletteClickListener;
    public final static int AUTO_POSITION = -1;

    public PalettePickRecyclerAdapter(Context c, ArrayList<String> paletteNames) {
        inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.paletteNames = paletteNames;
        Collections.sort(paletteNames);
    }

    @Override
    public PaletteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.palette_item, null);

        return new PaletteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final PaletteViewHolder holder, int position) {
        Palette2 p = getPalette(position);
        holder.paletteName.setText(p.getName());
        holder.previewView.setPalette(p);
    }

    public void setOnPaletteClickListener(OnPaletteClickListener listener) {
        this.onPaletteClickListener = listener;
    }

    private Palette2 getPalette(int index) {
        return PaletteUtils.loadPalette(paletteNames.get(index));
    }

    public int addItem(String paletteName, int index) {
        if (index == AUTO_POSITION) {
            index = 0;
            while (index < paletteNames.size() && paletteName.compareTo(paletteNames.get(index)) > 0)
                index++;
        }
        paletteNames.add(index, paletteName);
        notifyItemInserted(index);
        return index;
    }

    public void removeItem(int index) {
        paletteNames.remove(index);
        notifyItemRemoved(index);
    }

    @Override
    public int getItemCount() {
        return paletteNames.size();
    }

    @Override
    public long getItemId(int position) {
        return paletteNames.get(position).hashCode();
    }

    class PaletteViewHolder extends RecyclerView.ViewHolder {
        private PalettePreviewView previewView;
        private TextView paletteName;

        private PaletteViewHolder(View itemView) {
            super(itemView);
            previewView = (PalettePreviewView) itemView.findViewById(R.id.palettePreview);
            paletteName = (TextView) itemView.findViewById(R.id.paletteName);

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (onPaletteClickListener != null) {
                        onPaletteClickListener.onLongPaletteClick(getPalette(getAdapterPosition()), getAdapterPosition());
                        return true;
                    }
                    return false;
                }
            });
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (onPaletteClickListener != null)
                        onPaletteClickListener.onPaletteClick(getPalette(getAdapterPosition()), getAdapterPosition());
                }
            });
        }
    }

    public interface OnPaletteClickListener {
        void onLongPaletteClick(Palette2 palette, int index);

        void onPaletteClick(Palette2 palette, int index);
    }
}
