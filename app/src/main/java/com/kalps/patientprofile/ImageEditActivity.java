package com.kalps.patientprofile;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.kalps.patientprofile.utils.Utils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageView;
import jp.co.cyberagent.android.gpuimage.Rotation;

public class ImageEditActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener {
    private String imagePath;
    private Button chooseFilter, redScale, whiteMat, vignette;
    private GPUImageFilter mFilter;
    private GPUImageFilterTools.FilterAdjuster mFilterAdjuster;
    private GPUImageView mGPUImageView;
    private ImageView imageViewLeft, imageViewRight, imageViewSave;
    private ArrayList<String> imageList = new ArrayList<>();
    int x = 0, y = 0;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_image_edit);
        imageList.clear();
        if (getIntent() != null) {
            imagePath = getIntent().getStringExtra("image");
            imageList = getIntent().getStringArrayListExtra("imagelist");
        }
        Utils.logVerbose("imagePath :" + imagePath);
        Utils.logVerbose("Image List Actual Size in EditActivity  :" + imageList.size());
        initViews();
        initOperations();


    }

    private void initViews() {
        ((SeekBar) findViewById(R.id.seekBar)).setOnSeekBarChangeListener(this);
        chooseFilter = (Button) findViewById(R.id.button_choose_filter);
        mGPUImageView = (GPUImageView) findViewById(R.id.gpuimage);
        imageViewLeft = (ImageView) findViewById(R.id.imageViewLeft);
        imageViewRight = (ImageView) findViewById(R.id.imageViewRight);
        imageViewSave = (ImageView) findViewById(R.id.imageViewSave);

    }

    private void initOperations() {
        createDirIfNotExists("PatientProfile");
        chooseFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GPUImageFilterTools.showDialog(ImageEditActivity.this, new GPUImageFilterTools.OnGpuImageFilterChosenListener() {
                    @Override
                    public void onGpuImageFilterChosenListener(final GPUImageFilter filter) {
                        switchFilterTo(filter);
                        mGPUImageView.requestRender();
                    }
                });
            }
        });
        mGPUImageView.setImage(new File(imagePath));
        imageViewLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (x == 0)
                    mGPUImageView.setRotation(Rotation.ROTATION_90);
                else if (x == 1)
                    mGPUImageView.setRotation(Rotation.ROTATION_180);
                else if (x == 2)
                    mGPUImageView.setRotation(Rotation.ROTATION_270);
                else if (x == 3)
                    mGPUImageView.setRotation(Rotation.NORMAL);

                x++;
                if (x > 3)
                    x = 0;
            }
        });
        imageViewRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGPUImageView.setRotation(Rotation.NORMAL);
            }
        });
        imageViewSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HH_mm_ss");
                String currentTimeStamp = dateFormat.format(new Date());
                mGPUImageView.saveToPictures(Environment.getExternalStorageDirectory() + "/PatientProfile/", currentTimeStamp + ".jpg", new GPUImageView.OnPictureSavedListener() {
                    @Override
                    public void onPictureSaved(Uri uri) {
                        Utils.logVerbose("delete picked Image : " + new File(imagePath).delete());
                        Utils.logVerbose("Picture saved : " + uri.toString());
                        imageList.add(uri.toString());
                        Intent returnIntent = new Intent();
                        returnIntent.putStringArrayListExtra("imagelist", imageList);
                        Utils.logVerbose("Edited Image List Size  : " + imageList.size());
                        setResult(Activity.RESULT_OK, returnIntent);
                        finish();
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);

                    }
                });
            }
        });

    }

    public static boolean createDirIfNotExists(String path) {
        boolean ret = true;

        File file = new File(Environment.getExternalStorageDirectory(), path);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                Utils.logVerbose("Problem creating Image folder");
                ret = false;
            }
        }
        return ret;
    }

    private void switchFilterTo(final GPUImageFilter filter) {
        if (mFilter == null
                || (filter != null && !mFilter.getClass().equals(filter.getClass()))) {
            mFilter = filter;
            mGPUImageView.setFilter(mFilter);
            mFilterAdjuster = new GPUImageFilterTools.FilterAdjuster(mFilter);

            findViewById(R.id.seekBar).setVisibility(
                    mFilterAdjuster.canAdjust() ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onProgressChanged(final SeekBar seekBar, final int progress, final boolean fromUser) {
        if (mFilterAdjuster != null) {
            mFilterAdjuster.adjust(progress);
        }
        mGPUImageView.requestRender();
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

}
