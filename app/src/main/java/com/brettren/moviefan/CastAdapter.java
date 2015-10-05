package com.brettren.moviefan;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class CastAdapter extends ArrayAdapter<Person> {
    Context context;
    int layoutResourceId;
    List<Person> people = null;

    public CastAdapter(Context context, int layoutResourceId, List<Person> people) {
        super(context, layoutResourceId, people);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.people = people;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        CastHolder holder = null;

        if(view == null) {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            view = inflater.inflate(layoutResourceId, parent, false);

            holder = new CastHolder();
            holder.txtName = (TextView)view.findViewById(R.id.txtName);
            holder.txtCharacter = (TextView)view.findViewById(R.id.txtCharacter);
            holder.imgProfile = (ImageView)view.findViewById(R.id.imageProfile);

            view.setTag(holder);
        }
        else {
            holder = (CastHolder)view.getTag();
        }

        Person person = people.get(position);
        holder.txtCharacter.setText(person.name);
        holder.txtName.setText(person.name);
        Picasso.with(context).load(person.profile_path).into(holder.imgProfile);
        return view;
    }

    public static class CastHolder {
        ImageView imgProfile;
        TextView txtCharacter;
        TextView txtName;
    }
}