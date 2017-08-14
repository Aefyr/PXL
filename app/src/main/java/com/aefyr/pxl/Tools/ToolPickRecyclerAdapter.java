package com.aefyr.pxl.Tools;

import android.content.Context;
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

public class ToolPickRecyclerAdapter extends RecyclerView.Adapter<ToolPickRecyclerAdapter.ViewHolder>{

    private AdaptivePixelSurfaceH aps;
    private ToolPreview[] tools;
    private LayoutInflater inflater;
    private RecyclerView recyclerView;
    private ImageButton currentTool;
    private ToolSettingsManager manager;
    private boolean shown = false;

    private OnVisibilityChangedListener listener;
    public interface OnVisibilityChangedListener{
        void onVisibilityChanged(boolean visible);
    }

    public ToolPickRecyclerAdapter(Context c, ToolPreview[] tools, AdaptivePixelSurfaceH aps, ImageButton currentTool, final RecyclerView recyclerView, final ToolSettingsManager manager){
        this.aps = aps;
        this.tools = tools;
        inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.recyclerView =recyclerView;
        this.currentTool = currentTool;
        this.manager = manager;
        manager.notifyToolPicked(AdaptivePixelSurfaceH.Tool.PENCIL);
        manager.hide();

        currentTool.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!shown)
                    show();
                else
                    hide();
            }
        });
    }

    public void setOnVisibilityChangedListener(OnVisibilityChangedListener listener){
        this.listener = listener;
    }

    public void show(){
        recyclerView.setVisibility(View.VISIBLE);
        manager.show();
        shown = true;
        listenerEvent();
    }

    public void hide(){
        recyclerView.setVisibility(View.GONE);
        manager.hide();
        shown =false;
        listenerEvent();
    }

    private void listenerEvent(){
        if(listener!=null)
            listener.onVisibilityChanged(shown);
    }

    public boolean shown(){
        return shown;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(inflater.inflate(R.layout.tool_picker_item, null));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.icon.setImageResource(tools[position].toolIconId);
    }

    @Override
    public int getItemCount() {
        return tools.length;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private ImageButton icon;

        public ViewHolder(View itemView) {
            super(itemView);
            icon = (ImageButton) itemView.findViewById(R.id.toolIcon);

            icon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    aps.setTool(tools[getAdapterPosition()].tool);
                    currentTool.setImageResource(tools[getAdapterPosition()].toolIconId);
                    if(manager.notifyToolPicked(tools[getAdapterPosition()].tool))
                        hide();
                }
            });
        }
    }
}
