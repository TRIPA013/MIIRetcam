package com.kalps.patientprofile.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.kalps.patientprofile.R;
import com.kalps.patientprofile.provider.PatientImages;
import com.kalps.patientprofile.utils.ScaleToFitWidhtHeigthTransform;
import com.kalps.patientprofile.utils.Utils;
import com.kalps.patientprofile.view.CustomViewPager;
import com.kalps.patientprofile.view.TouchImageView;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class PatientPagerImageAdapter extends PagerAdapter {

    private ArrayList<PatientImages> patientImagesArrayList;
    private Activity activity;

    public PatientPagerImageAdapter(ArrayList<PatientImages> patientImagesArrayList, Activity activity) {
        this.patientImagesArrayList = patientImagesArrayList;
        this.activity = activity;
    }

    @Override
    public int getCount() {
        return patientImagesArrayList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((ImageView) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        final Context context = activity;
        TouchImageView imageView = new TouchImageView(context, null);

        int padding = context.getResources().getDimensionPixelSize(
                R.dimen.activity_horizontal_margin);
        //imageView.setPadding(padding, padding, padding, padding);
        //imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        String path = patientImagesArrayList.get(position).getImage();

        try {
            Uri uri = Uri.fromFile(new File(path));
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(activity.getContentResolver(),
                    uri);
            imageView.setImageBitmap(bitmap);
            Picasso.with(context).load(uri).
                    skipMemoryCache().into(imageView);
        } catch (IOException e) {
            e.printStackTrace();
            Utils.logVerbose("IO exception" + e.getLocalizedMessage());
        }

        ((CustomViewPager) container).addView(imageView, 0);
        return imageView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ((CustomViewPager) container).removeView((ImageView) object);
    }


    static class ViewHolder {
        ImageView image;

        public ViewHolder(View convertView) {
            image = (ImageView) convertView.findViewById(R.id.SingleView);
        }
    }

}
