package com.brettren.moviefan;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;


public class VideoAdapter extends ArrayAdapter<Video> {
    Context context;
    int layoutResourceId;
    List<Video> videos = null;

    public VideoAdapter(Context context, int layoutResourceId, List<Video> videos) {
        super(context, layoutResourceId, videos);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.videos = videos;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        VideoHolder holder = null;

        if(view == null) {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            view = inflater.inflate(layoutResourceId, parent, false);

            holder = new VideoHolder();
            holder.txtName = (TextView)view.findViewById(R.id.txtName);

            view.setTag(holder);
        }
        else {
            holder = (VideoHolder)view.getTag();
        }

        Video video = videos.get(position);
        holder.txtName.setText(video.name);
        return view;
    }

    public static class VideoHolder {
        TextView txtName;
    }
}
