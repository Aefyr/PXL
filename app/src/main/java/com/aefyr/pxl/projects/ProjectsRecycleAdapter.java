package com.aefyr.pxl.projects;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.aefyr.pxl.R;
import com.aefyr.pxl.custom.PixelImageView;
import com.aefyr.pxl.util.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

/**
 * Created by Aefyr on 19.07.2017.
 */

public class ProjectsRecycleAdapter extends RecyclerView.Adapter<ProjectsRecycleAdapter.ProjectViewHolder> {

    private LayoutInflater inflater;
    private ArrayList<Project> projects;
    private OnProjectClickListener onProjectClickListener;
    private SimpleDateFormat simpleDateFormat;

    private  int maxSizeSide;

    public interface OnProjectClickListener {
        void onProjectClick(Project project, int id);

        void onLongProjectClick(int id, Project project);
    }

    public void setOnProjectClickListener(OnProjectClickListener listener) {
        onProjectClickListener = listener;
    }

    public ProjectsRecycleAdapter(Context c, ArrayList<Project> projects) {
        initialize(c);
        this.projects = projects;
        Collections.sort(projects, new LastModifiedComparator());
    }

    public ProjectsRecycleAdapter(Context c){
        initialize(c);
    }

    private void initialize(Context c){
        inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        simpleDateFormat = new SimpleDateFormat("dd.MM.yy HH:mm", Locale.getDefault());
        maxSizeSide = (int) Utils.dpToPx(144, c.getResources());
    }

    public void setProjects(ArrayList<Project> projects) {
        this.projects = projects;
        notifyDataSetChanged();
    }

    public void addProject(Project project, boolean toFront){
        if(projects == null)
            projects = new ArrayList<>(ProjectsUtils.getProjectsCount());

        if(toFront)
            projects.add(0, project);
        else
            projects.add(project);

        notifyItemInserted(toFront?0:projects.size()-1);
    }

    public void clearProjects(){
        if(projects!=null){
            projects = null;
            notifyDataSetChanged();
        }
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
        holder.lastModified.setText(simpleDateFormat.format(getProject(position).lastModified()));
        holder.preview.setImageBitmap(p.getPreviewBitmap(maxSizeSide));
    }

    @Override
    public int getItemCount() {
        return projects==null?0:projects.size();
    }

    @Override
    public long getItemId(int position) {
        return projects.get(position).lastModified();
    }

    private Project getProject(int index) {
        return projects.get(index);
    }

    public void moveItemToFront(int index) {
        projects.add(0, projects.remove(index));
        notifyItemMoved(index, 0);
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
