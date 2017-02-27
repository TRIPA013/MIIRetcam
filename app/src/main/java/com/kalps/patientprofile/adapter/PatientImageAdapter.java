package com.kalps.patientprofile.adapter;

import android.app.Activity;
import android.media.Image;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;

import com.kalps.patientprofile.provider.PatientImages;
import com.kalps.patientprofile.provider.Patients;

import java.io.File;
import java.util.List;

import com.kalps.patientprofile.R;
import com.kalps.patientprofile.utils.ScaleToFitWidhtHeigthTransform;
import com.kalps.patientprofile.utils.Utils;
import com.squareup.picasso.Picasso;


public class PatientImageAdapter extends ArrayAdapter<PatientImages> {

    private List<PatientImages> ridesItemLists;
    private Activity _context = null;
    private static LayoutInflater _inflater = null;


    public PatientImageAdapter(Activity context, List<PatientImages> lst) {
        super(context, 0, lst);
        this._context = context;
        this.ridesItemLists = lst;
        _inflater = this._context.getLayoutInflater();
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        /*Holder holder = new Holder();
        view = _inflater.inflate(R.layout.image_row, null);
        holder.img = (ImageView) view.findViewById(R.id.SingleView);
        holder.img.setImageURI(Uri.parse(ridesItemLists.get(position)));*/
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(_context).inflate(R.layout.image_row, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        Uri uri = Uri.fromFile(new File(getItem(position).getImage()));
        Picasso.with(_context).load(uri).
                transform(new ScaleToFitWidhtHeigthTransform(120, true)).skipMemoryCache().into(holder.image);

        return convertView;


    }

    /*public class Holder {
        ImageView img;
    }*/

    static class ViewHolder {
        ImageView image;

        public ViewHolder(View convertView) {
            image = (ImageView) convertView.findViewById(R.id.SingleView);
        }
    }

}
