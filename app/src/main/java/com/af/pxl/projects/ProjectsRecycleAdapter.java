package com.af.pxl.projects;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.af.pxl.R;
import com.af.pxl.views.PixelImageView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Aefyr on 19.07.2017.
 */

public class ProjectsRecycleAdapter extends RecyclerView.Adapter<ProjectsRecycleAdapter.ProjectViewHolder> {

    private LayoutInflater inflater;
    private ArrayList<Project> projects;
    private OnProjectClickListener onProjectClickListener;
    private SimpleDateFormat simpleDateFormat;

    public interface OnProjectClickListener {
        void onProjectClick(Project project, int id);

        void onLongProjectClick(int id, Project project);
    }

    public void setOnProjectClickListener(OnProjectClickListener listener) {
        onProjectClickListener = listener;
    }

    public ProjectsRecycleAdapter(Context c, ArrayList<Project> projects) {
        inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        simpleDateFormat = new SimpleDateFormat("dd.MM.yy HH:mm", Locale.getDefault());
        this.projects = projects;
        Collections.sort(projects, new LastModifiedComparator());
    }

    public void setItems(ArrayList<Project> projects) {
        this.projects = projects;
        notifyDataSetChanged();
    }

    @Override
    public ProjectViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = inflater.inflate(R.layout.gallery_item, null, false);
        return new ProjectViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ProjectViewHolder holder, int position) {
        Project p = getProject(position);
        holder.name.setText(p.name);
        holder.resolution.setText(p.getResolutionString());
        holder.preview.setImageBitmap(p.getBitmap(false));
        holder.lastModified.setText(simpleDateFormat.format(new Date(getProject(position).lastModified)));
    }

    @Override
    public int getItemCount() {
        if (projects == null)
            return 0;
        return projects.size();
    }

    @Override
    public long getItemId(int position) {
        return projects.get(position).lastModified;
    }

    private Project getProject(int index) {
        return projects.get(index);
    }

    public void addItem(Project project) {
        projects.add(0, project);
        notifyItemInserted(0);
    }

    public void moveItem(int index, int newIndex) {
        if (index < 0 || index >= projects.size() || newIndex < 0 || newIndex >= projects.size())
            return;
        projects.add(newIndex, projects.remove(index));
        notifyItemMoved(index, newIndex);
    }

    public void removeItem(int index) {
        projects.remove(index);
        notifyItemRemoved(index);
    }

    private class LastModifiedComparator implements Comparator<Project> {
        @Override
        public int compare(Project project, Project t1) {
            if (project.directory.lastModified() < t1.directory.lastModified())
                return 1;
            if (project.directory.lastModified() > t1.directory.lastModified())
                return -1;
            return 0;
        }
    }

    class ProjectViewHolder extends RecyclerView.ViewHolder {
        private PixelImageView preview;
        private TextView name;
        private TextView resolution;
        private TextView lastModified;

        private ProjectViewHolder(View itemView) {
            super(itemView);
            preview = (PixelImageView) itemView.findViewById(R.id.preview);
            name = (TextView) itemView.findViewById(R.id.name);
            resolution = (TextView) itemView.findViewById(R.id.resolution);
            lastModified = (TextView) itemView.findViewById(R.id.lastModified);

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (onProjectClickListener != null) {
                        onProjectClickListener.onLongProjectClick(getAdapterPosition(), getProject(getAdapterPosition()));
                        return true;
                    }
                    return false;
                }
            });
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (onProjectClickListener != null)
                        onProjectClickListener.onProjectClick(getProject(getAdapterPosition()), getAdapterPosition());
                }
            });
        }
    }


}
