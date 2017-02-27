package com.kalps.patientprofile;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import com.kalps.patientprofile.utils.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageSharpenFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageView;
import jp.co.cyberagent.android.gpuimage.GPUImageVignetteFilter;

public class ImageFilterEditActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener {
    private String imagePath;
    private GPUImageFilter mFilter;
    private GPUImageFilterTools.FilterAdjuster mFilterAdjuster;
    private GPUImageView mGPUImageView;
    private SeekBar seekBar;
    private LinearLayout linearLayoutBottom;
    private Button buttonVignette, buttonSharpenss;
    private ImageView imageViewCancel, imageViewSave;
    private String fileName, folderName;

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.slide_in_down, R.anim.slide_out_down);
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_image_filter_edit);
        if (getIntent() != null) {
            imagePath = getIntent().getStringExtra("image");
            String[] imagePathAsArray = imagePath.split("/");
            fileName = imagePathAsArray[imagePathAsArray.length - 1];
            folderName = imagePathAsArray[imagePathAsArray.length - 2];
        }

        Utils.logVerbose("imagepath  : " + imagePath);
        Utils.logVerbose("fileName  : " + fileName);
        Utils.logVerbose("folderName  : " + folderName);
        Utils.logVerbose("uri  : " + Uri.fromFile(new File(imagePath)));
        initview();

        mGPUImageView.setImage(new File(imagePath));
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

    private void initview() {
        imageViewCancel = (ImageView) findViewById(R.id.imageViewCancel);
        imageViewCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.slide_in_down, R.anim.slide_out_down);
            }
        });
        imageViewSave = (ImageView) findViewById(R.id.imageViewSave);
        imageViewSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveImage();
            }
        });
        mGPUImageView = (GPUImageView) findViewById(R.id.gpuimage);
        linearLayoutBottom = (LinearLayout) findViewById(R.id.linearlayoutBottom);
        buttonSharpenss = (Button) findViewById(R.id.buttonSharpness);
        buttonVignette = (Button) findViewById(R.id.buttonVignette);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(this);
        buttonVignette.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFilter = null;
                PointF centerPoint = new PointF();
                centerPoint.x = 0.5f;
                centerPoint.y = 0.5f;
                GPUImageVignetteFilter filter = new GPUImageVignetteFilter(centerPoint, new float[]{0.0f, 0.0f, 0.0f}, 0.3f, 0.75f);
                switchFilterTo(filter);
                mGPUImageView.requestRender();
            }
        });
        buttonSharpenss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFilter = null;
                GPUImageSharpenFilter sharpness = new GPUImageSharpenFilter();
                sharpness.setSharpness(2.0f);
                switchFilterTo(sharpness);
                mGPUImageView.requestRender();
            }
        });
    }

    private void saveImage() {
        mGPUImageView.saveToPictures(
                folderName, fileName, new GPUImageView.OnPictureSavedListener() {
                    @Override
                    public void onPictureSaved(Uri uri) {
                        Utils.logVerbose("Picture saved : " + uri.toString());
                        Utils.logVerbose("file saved : " + getRealPathFromURI(uri));
                        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
                       /* try {
                            copyFile(new File(getRealPathFromURI(uri)), new File(imagePath));
                        } catch (IOException e) {

                        }*/
                        finish();
                        overridePendingTransition(R.anim.slide_in_down, R.anim.slide_out_down);
                    }
                });
    }

    private void copyFile(File sourceFile, File destFile) throws IOException {
        if (!sourceFile.exists()) {
            return;
        }
        FileChannel source = null;
        FileChannel destination = null;
        source = new FileInputStream(sourceFile).getChannel();
        destination = new FileOutputStream(destFile).getChannel();
        if (destination != null && source != null) {
            destination.transferFrom(source, 0, source.size());
        }
        if (source != null) {
            source.close();
        }
        if (destination != null) {
            destination.close();
        }

    }

    private String getRealPathFromURI(Uri contentUri) {

        String[] proj = {MediaStore.Video.Media.DATA};
        Cursor cursor = managedQuery(contentUri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    private void switchFilterTo(final GPUImageFilter filter) {

        mFilter = filter;
        mGPUImageView.setFilter(mFilter);
        mFilterAdjuster = new GPUImageFilterTools.FilterAdjuster(mFilter);

        findViewById(R.id.seekBar).setVisibility(
                mFilterAdjuster.canAdjust() ? View.VISIBLE : View.GONE);
    }

}
