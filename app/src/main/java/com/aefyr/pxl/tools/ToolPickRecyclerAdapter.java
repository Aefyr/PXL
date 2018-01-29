package com.aefyr.pxl.tools;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.aefyr.pxl.AdaptivePixelSurfaceH;
import com.aefyr.pxl.R;
import com.aefyr.pxl.ToolSettingsManager;

/**
 * Created by Aefyr on 05.08.2017.
 */

public class ToolPickRecyclerAdapter extends RecyclerView.Adapter<ToolPickRecyclerAdapter.ViewHolder> implements AdaptivePixelSurfaceH.OnToolChangeListener{

    private AdaptivePixelSurfaceH aps;
    private AdaptivePixelSurfaceH.Tool[] tools;
    private LayoutInflater inflater;
    private RecyclerView recyclerView;
    private ImageButton currentTool;
    private ToolSettingsManager manager;
    private boolean shown = false;

    private OnVisibilityChangedListener listener;

    public interface OnVisibilityChangedListener {
        void onVisibilityChanged(boolean visible);
    }

    public ToolPickRecyclerAdapter(Context c, AdaptivePixelSurfaceH.Tool[] tools, AdaptivePixelSurfaceH aps, ImageButton currentTool, final RecyclerView recyclerView, final ToolSettingsManager manager) {
        this.aps = aps;
        aps.setOnToolChangeListener(this);
        this.tools = tools;
        inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.recyclerView = recyclerView;
        this.currentTool = currentTool;
        this.manager = manager;
        manager.notifyToolPicked(AdaptivePixelSurfaceH.Tool.PENCIL, false);

        currentTool.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!shown)
                    show();
                else
                    hide();
            }
        });
    }

    public void setOnVisibilityChangedListener(OnVisibilityChangedListener listener) {
        this.listener = listener;
    }

    public void show() {
        recyclerView.setVisibility(View.VISIBLE);
        manager.show();
        shown = true;
        listenerEvent();
    }

    public void hide() {
        recyclerView.setVisibility(View.GONE);
        manager.hide();
        shown = false;
        listenerEvent();
    }

    private void listenerEvent() {
        if (listener != null)
            listener.onVisibilityChanged(shown);
    }

    public boolean shown() {
        return shown;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(inflater.inflate(R.layout.tool_picker_item, null));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.icon.setImageResource(ToolPreview.getIconForTool(tools[position]));
    }

    @Override
    public int getItemCount() {
        return tools.length;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private ImageButton icon;

        ViewHolder(View itemView) {
            super(itemView);
            icon = (ImageButton) itemView.findViewById(R.id.toolIcon);

            icon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    aps.setTool(tools[getAdapterPosition()], true);
                }
            });
        }
    }

    @Override
    public void onToolChanged(AdaptivePixelSurfaceH.Tool newTool, boolean showToolSettings) {
        currentTool.setImageResource(ToolPreview.getIconForTool(newTool));
        if(manager.notifyToolPicked(newTool, showToolSettings))
            hide();
    }

    public void writeStateToBundle(Bundle outState){
        outState.putBoolean("toolPicker_shown", shown);
    }

    public void restoreState(Bundle savedInstanceState){
        currentTool.setImageResource(ToolPreview.getIconForTool(aps.currentTool()));
        manager.notifyToolPicked(aps.currentTool(), false);

        if(savedInstanceState.getBoolean("toolPicker_shown", false))
            show();

    }
}
