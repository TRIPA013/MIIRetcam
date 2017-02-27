package com.kalps.patientprofile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import com.kalps.patientprofile.adapter.PatientPagerImageAdapter;
import com.kalps.patientprofile.provider.PatientImages;
import com.kalps.patientprofile.provider.PatientProvider;
import com.kalps.patientprofile.utils.ScaleToFitWidhtHeigthTransform;
import com.kalps.patientprofile.utils.Utils;
import com.kalps.patientprofile.view.CustomViewPager;
import com.kalps.patientprofile.view.TouchImageView;
import com.kalps.patientprofile.view.ZoomImageView;
import com.soundcloud.android.crop.Crop;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import cn.pedant.SweetAlert.SweetAlertDialog;


public class FUllImageActivity extends AppCompatActivity {

    Cursor imageCursor;
    private ArrayList<PatientImages> patientImageList = new ArrayList<>();

    private String mrdNo;
    private int postition;
    CustomViewPager viewPager;
    private ImageView buttonEdit, buttonCrop, buttonDone, buttonDelete;

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.status_bar));
        }
        setContentView(R.layout.activity_full_image);
        if (getIntent() != null) {
            mrdNo = getIntent().getStringExtra(PatientProvider.MRD_NO);
            postition = getIntent().getIntExtra("position", 0);
        }
        Utils.logVerbose("Position : " + postition);
        Utils.logVerbose("mrdNo : " + mrdNo);

        viewPager = (CustomViewPager) findViewById(R.id.view_pager);


        buttonDone = (ImageView) findViewById(R.id.imageViewDone);
        buttonDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        buttonEdit = (ImageView) findViewById(R.id.imageViewEdit);
        buttonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String path = patientImageList.get(viewPager.getCurrentItem()).getImage();
                Intent intent = new Intent(FUllImageActivity.this, ImageFilterEditActivity.class);
                intent.putExtra("image", path);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
            }
        });
        buttonCrop = (ImageView) findViewById(R.id.imageViewCrop);
        buttonCrop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String path = patientImageList.get(viewPager.getCurrentItem()).getImage();
                Intent intent = new Intent(FUllImageActivity.this, ImageCropActivity.class);
                intent.putExtra("url", path);
                startActivity(intent);
                //Crop.of(Uri.fromFile(new File(path)), Uri.fromFile(new File(path))).asSquare().start(FUllImageActivity.this);
            }
        });
        buttonDelete = (ImageView) findViewById(R.id.imageViewDelete);
        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                new SweetAlertDialog(FUllImageActivity.this, SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("Are you sure?")
                        .setContentText("Won't be able to recover this file!")
                        .setConfirmText("Yes,delete it!")
                        .setCancelText("      No     ")
                        .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                sweetAlertDialog.dismissWithAnimation();
                            }
                        })
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sDialog) {
                                sDialog.dismissWithAnimation();

                                String[] projection1 = {String.valueOf(patientImageList.get(viewPager.getCurrentItem()).getId())};

                                int count = getContentResolver().delete(PatientProvider.CONTENT_URI1, " id=? ", projection1);
                                Utils.logVerbose("Deleted Row Count : " + count);
                                boolean isDeleted;

                                if (count > 0) {
                                    Utils.logVerbose("Gonna Delete Pic From Storage: " + patientImageList.get(viewPager.getCurrentItem()).getImage());
                                    isDeleted = Utils.delete(patientImageList.get(viewPager.getCurrentItem()).getImage());
                                } else {
                                    Utils.logVerbose("Gonna Delete Pic From Storage: " + patientImageList.get(viewPager.getCurrentItem()).getImage());
                                    isDeleted = Utils.delete(patientImageList.get(viewPager.getCurrentItem()).getImage());

                                }

                                Utils.logVerbose("Deleted Pic From Storage: " + isDeleted);
                                Intent intent = new Intent();
                                //To Delete The Image
                                setResult(RESULT_OK, intent);

                                finish();
                                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
                            }
                        })
                        .show();


            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        new LoadPatientsImages().execute();

    }

    private void loadImages() {


        String[] projection = new String[]{
                PatientProvider.MRD_NO,
                PatientProvider.IMAGES,
                "id"
        };
        String selection = PatientProvider.MRD_NO + " LIKE " + "'" + mrdNo + "'";
        imageCursor = getContentResolver().query(PatientProvider.CONTENT_URI1, projection, selection, null, null);

        Utils.logVerbose("cursor size : " + imageCursor.getColumnCount());

        if (imageCursor != null) {
            if (imageCursor.moveToFirst()) {
                do {
                    Utils.logVerbose("PatientImages loaded : " + imageCursor.getString(imageCursor.getColumnIndex(PatientProvider.IMAGES)));
                    PatientImages patientImages = new PatientImages();
                    patientImages.setId(imageCursor.getInt(imageCursor.getColumnIndex("id")));
                    patientImages.setMrd(imageCursor.getString(imageCursor.getColumnIndex("mrd")));
                    patientImages.setImage(imageCursor.getString(imageCursor.getColumnIndex("image")));
                    patientImageList.add(patientImages);
                } while (imageCursor.moveToNext());
            }
        }

    }

    private class LoadPatientsImages extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            patientImageList.clear();
        }

        @Override
        protected String doInBackground(String... f_url) {
            loadImages();
            return null;
        }

        @Override
        protected void onPostExecute(String file_url) {

            PatientPagerImageAdapter adapter = new PatientPagerImageAdapter(patientImageList, FUllImageActivity.this);
            viewPager.setAdapter(adapter);
            if (postition > imageCursor.getCount())
                viewPager.setCurrentItem(postition);
            else
                viewPager.setCurrentItem(postition);

            //TODO :
            //pageIndicator.setViewPager(viewPager);
        }
    }


}
