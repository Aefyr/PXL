package com.af.pxl.Projects;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.af.pxl.PixelImageView;
import com.af.pxl.R;

import java.util.ArrayList;

/**
 * Created by Aefyr on 19.07.2017.
 */

public class ProjectsRecycleAdapter extends RecyclerView.Adapter<ProjectsRecycleAdapter.ViewHolder> {

    private LayoutInflater inflater;
    private ArrayList<Project> projects;
    private OnProjectClickListener onProjectClickListener;

    public interface OnProjectClickListener{
        void onProjectClick(Project project);
        void onProjectLongClick(int id, Project project);
    }

    public void setOnProjectClickListener(OnProjectClickListener listener){
        onProjectClickListener = listener;
    }

    public ProjectsRecycleAdapter(Context c, ArrayList<Project> projects){
        inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.projects = projects;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = inflater.inflate(R.layout.gallery_item, null, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Project p = getItem(position);
        holder.name.setText(p.name);
        holder.resolution.setText(p.getResolutionString());
        holder.preview.setImageBitmap(p.getBitmap(false));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onProjectClickListener.onProjectClick(getItem(holder.getAdapterPosition()));
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                onProjectClickListener.onProjectLongClick(holder.getAdapterPosition(), getItem(holder.getAdapterPosition()));
                return true;
            }
        });

    }

    @Override
    public int getItemCount() {
        return projects.size();
    }

    public Project getItem(int index){
        return projects.get(index);
    }

    public void addItem(Project project){
        projects.add(0, project);
        notifyItemInserted(0);
    }

    public void removeItem(int index){
        projects.remove(index);
        notifyItemRemoved(index);
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        private PixelImageView preview;
        private TextView name;
        private TextView resolution;
        public ViewHolder(View itemView) {
            super(itemView);
            preview = (PixelImageView) itemView.findViewById(R.id.preview);
            name = (TextView) itemView.findViewById(R.id.name);
            resolution = (TextView) itemView.findViewById(R.id.resolution);
        }
    }


}
