package com.aefyr.pxl.palettes;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aefyr.pxl.R;
import com.aefyr.pxl.custom.ColorCircle;

/**
 * Created by Aefyr on 03.08.2017.
 */

public class ColorSelectionRecyclerAdapter extends RecyclerView.Adapter<ColorSelectionRecyclerAdapter.ViewHolder> {

    private Palette2 palette;
    private LayoutInflater inflater;
    private OnColorInteractionListener listener;

    public ColorSelectionRecyclerAdapter(Context c, Palette2 palette) {
        this.palette = palette;
        inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setOnColorInteractionListener(OnColorInteractionListener listener) {
        this.listener = listener;
    }

    @Override
    public ColorSelectionRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(inflater.inflate(R.layout.palette_picker_color_item, null));
    }

    @Override
    public void onBindViewHolder(ColorSelectionRecyclerAdapter.ViewHolder holder, int position) {
        holder.colorCircle.setColor(palette.getColor(position));
    }

    public void setPalette(Palette2 palette) {
        this.palette = palette;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return 16;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private ColorCircle colorCircle;

        public ViewHolder(View itemView) {
            super(itemView);
            colorCircle = (ColorCircle) itemView.findViewById(R.id.colorCircle);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null)
                        listener.onColorClick(getAdapterPosition());
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (listener != null) {
                        listener.onColorLongClick(getAdapterPosition());
                        return true;
                    }
                    return false;
                }
            });
        }
    }

    public interface OnColorInteractionListener {
        void onColorClick(int index);

        void onColorLongClick(int index);
    }
}
