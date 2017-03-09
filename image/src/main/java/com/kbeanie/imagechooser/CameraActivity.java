package com.kbeanie.imagechooser;

/**
 * @author Jose Davis Nidhin
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.ZoomControls;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class CameraActivity extends Activity {
    private static final String TAG = "CameraActivity";
    Preview preview;
    Button buttonClick;
    Camera camera;
    Activity act;
    Context ctx;
    private boolean hasFlash = false;
    Camera.Parameters params;
    private ZoomControls zoomControls;
    private Button click;
    int zoom_value = 0;
    int max_zoom;
    int width = 1920, height = 1080, previewWidth = 1920, previewHeight = 1080;
    private float mDist = 0.0f;

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            if (params.isZoomSupported()) {
                zoom_value = zoom_value - 1;
                if (zoom_value > 0) {
                    params.setZoom(zoom_value);
                    /*params.setPictureSize(width, height);
                    params.setPreviewSize(previewWidth, previewHeight);
                    camera.setParameters(params);*/
                }
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            if (params.isZoomSupported() && max_zoom >= zoom_value) {
                zoom_value = zoom_value + 1;
                if (zoom_value > 0 && zoom_value < max_zoom) {
                    params.setZoom(zoom_value);
                    setParameters();
                   /* params.setPictureSize(width, height);
                    params.setPreviewSize(previewWidth, previewHeight);
                    camera.setParameters(params);*/
                }
            }
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("pradeep", " onCreate ");
        ctx = this;
        act = this;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.main);

        hasFlash = getApplicationContext().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
        preview = new Preview(this, (SurfaceView) findViewById(R.id.surfaceView));
        preview.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        ((RelativeLayout) findViewById(R.id.layout)).addView(preview);
        preview.setKeepScreenOn(true);
        zoomControls = (ZoomControls) findViewById(R.id.zoomControls1);
        click = (Button) findViewById(R.id.buttonCLick);
        click.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Log.i("Patient", "preview on click");
                if (jpegCallback != null && rawCallback != null && shutterCallback != null)
                    camera.takePicture(shutterCallback, rawCallback, jpegCallback);


            }
        });

        //		buttonClick = (Button) findViewById(R.id.btnCapture);
        //
        //		buttonClick.setOnClickListener(new OnClickListener() {
        //			public void onClick(View v) {
        ////				preview.camera.takePicture(shutterCallback, rawCallback, jpegCallback);
        //				camera.takePicture(shutterCallback, rawCallback, jpegCallback);
        //			}
        //		});
        //
        //		buttonClick.setOnLongClickListener(new OnLongClickListener(){
        //			@Override
        //			public boolean onLongClick(View arg0) {
        //				camera.autoFocus(new AutoFocusCallback(){
        //					@Override
        //					public void onAutoFocus(boolean arg0, Camera arg1) {
        //						//camera.takePicture(shutterCallback, rawCallback, jpegCallback);
        //					}
        //				});
        //				return true;
        //			}
        //		});
    }

    @Override
    protected void onResume() {
        super.onResume();


        int numCams = Camera.getNumberOfCameras();
        if (numCams > 0) {
            try {
                camera = Camera.open(0);
                setCameraDisplayOrientation(CameraActivity.this, 0, camera);
                //AspectFrameLayout layout = (AspectFrameLayout) findViewById(R.id.aspectFrame);

                //camera.setDisplayOrientation(90);

                params = camera.getParameters();

                max_zoom = params.getMaxZoom();
                zoomControls.setOnZoomInClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (params.isZoomSupported() && max_zoom >= zoom_value) {
                            zoom_value = zoom_value + 1;
                            if (zoom_value > 0 && zoom_value < max_zoom) {
                                params.setZoom(zoom_value);
                                /*params.setPictureSize(width, height);
                                params.setPreviewSize(previewWidth, previewHeight);
                                camera.setParameters(params);*/
                                setParameters();
                            }
                        }
                    }
                });
                zoomControls.setOnZoomOutClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (params.isZoomSupported()) {
                            zoom_value = zoom_value - 1;
                            if (zoom_value > 0) {
                                params.setZoom(zoom_value);
                               /* params.setPictureSize(width, height);
                                params.setPreviewSize(previewWidth, previewHeight);
                                camera.setParameters(params);*/
                                setParameters();
                            }
                        }
                    }
                });

                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
                    // Marshmallow+
                    params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    // handleActionTurnOnFlashLight(getApplicationContext());
                } else {
                    //below Marshmallow
                    params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                }

                //params.setFocusMode(Camera.Parameters.SCENE_MODE_AUTO);
                params.setPictureFormat(PixelFormat.JPEG);

                //Log.i("pradeep", " sizes  :" + sizes.size());

                Display display = getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                /*int width = size.x;
                int height = size.y;*/


                //layout.setAspectRatio((double) width / height);
                /*Log.i("pradeep", "display w : " + width);
                Log.i("pradeep", "display h : " + height);
                Camera.Size optimalSize = getOptimalSize(sizes, width, height);
                Log.i("pradeep", "optimalSize w : " + optimalSize.width);
                Log.i("pradeep", "optimalSize h : " + optimalSize.height);


                Log.i("pradeep", "used w : " + sizes.get(sizes.size() - 1).width);
                Log.i("pradeep", "used h : " + sizes.get(sizes.size() - 1).height);*/


                // params.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);
                // params.setWhiteBalance(Camera.Parameters.FOCUS_MODE_MACRO);
                Log.i("pradeep", "selected width  w : " + width);
                Log.i("pradeep", "selected height h : " + height);

                Log.i("pradeep", "selected previewWidth  w : " + previewWidth);
                Log.i("pradeep", "selected previewHeight h : " + previewHeight);

                /*params.setPictureSize(width, height);
                params.setPreviewSize(previewWidth, previewHeight);
                camera.setParameters(params);*/
                setParameters();
                camera.startPreview();
                preview.setCamera(camera);

            } catch (RuntimeException ex) {
                Toast.makeText(ctx, getString(R.string.camera_not_found), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onStop() {
        if (camera != null) {
            params = camera.getParameters();

            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
                // Marshmallow+
                // handleActionTurnOffFlashLight(getApplicationContext());
                params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                params.setFocusMode(Camera.Parameters.SCENE_MODE_AUTO);
            } else {
                params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                params.setFocusMode(Camera.Parameters.SCENE_MODE_AUTO);
            }

            camera.setParameters(params);
            camera.stopPreview();
            preview.setCamera(null);
            camera.release();
            camera = null;
        }
        super.onStop();
    }

   /* @Override
    protected void onPause() {
        if (camera != null) {
            params = camera.getParameters();
            params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            params.setFocusMode(Camera.Parameters.SCENE_MODE_AUTO);
            camera.setParameters(params);
            camera.stopPreview();
            preview.setCamera(null);
            camera.release();
            camera = null;
        }
        super.onPause();
    }*/

    private void resetCam() {
        setCameraDisplayOrientation(CameraActivity.this, 0, camera);
        params = camera.getParameters();
        params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        /*params.setPictureSize(width, height);
        params.setPreviewSize(previewWidth, previewHeight);
        camera.setParameters(params);*/
        setParameters();
        camera.startPreview();
        preview.setCamera(camera);
    }

    private void refreshGallery(File file) {

        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(Uri.fromFile(file));
        sendBroadcast(mediaScanIntent);
    }

    ShutterCallback shutterCallback = new ShutterCallback() {
        public void onShutter() {
            Log.i("Patient", "onShutter");
            //			 Log.d(TAG, "onShutter'd");
        }
    };

    PictureCallback rawCallback = new PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {

        }
    };

    PictureCallback jpegCallback = new PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.i("Patient", "jpegCallback");
            new SaveImageTask().execute(data);
            //resetCam();
            Log.d(TAG, "onPictureTaken - jpeg");
        }
    };

    private class SaveImageTask extends AsyncTask<byte[], Void, String> {
        @Override
        protected void onPostExecute(String path) {
            super.onPostExecute(path);

            Log.i("Patient", "path : " + path);
            Bundle conData = new Bundle();
            conData.putString("path", path);
            Intent intent = new Intent();
            intent.putExtras(conData);
            setResult(RESULT_OK, intent);
            finish();
        }

        @Override
        protected String doInBackground(byte[]... data) {
            /*FileOutputStream outStream = null;
            // Write to SD Card
            try {
                File sdCard = Environment.getExternalStorageDirectory();
                File dir = new File(sdCard.getAbsolutePath() + "/patientprofile");
                dir.mkdirs();
                String fileName = String.format("%d.jpg", System.currentTimeMillis());
                File outFile = new File(dir, fileName);
                outStream = new FileOutputStream(outFile);
                outStream.write(data[0]);
                outStream.flush();
                outStream.close();
                Log.d(TAG, "onPictureTaken - wrote bytes: " + data.length + " to " + outFile.getAbsolutePath());
                refreshGallery(outFile);
                return outFile.getPath();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
            }*/

            File file = null;
            try {
                file = saveImage(data);
                refreshGallery(file);

                return file.getPath();
            } catch (FileNotFoundException e) {
                Log.i("Patient", "FileNotFoundException : " + e.getLocalizedMessage());
                e.printStackTrace();
            } catch (IOException e) {
                Log.i("Patient", "IOException : " + e.getLocalizedMessage());
                e.printStackTrace();
            }
            return null;
        }
    }

    private File saveImage(byte[]... data) throws FileNotFoundException, IOException {
        FileOutputStream outStream = null;

        File sdCard = Environment.getExternalStorageDirectory();
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" +
                Environment.DIRECTORY_PICTURES + "/" + "patientprofile");
        dir.mkdirs();
        String fileName = String.format("%d.jpg", System.currentTimeMillis());
        File outFile = new File(dir, fileName);

        outStream = new FileOutputStream(outFile);
        Bitmap bm = null;
        if (data != null) {
            int screenWidth = getResources().getDisplayMetrics().widthPixels;
            int screenHeight = getResources().getDisplayMetrics().heightPixels;

            Log.i("ICA", "data legnth :  " + data.length);
            bm = BitmapFactory.decodeByteArray(data[0], 0, data[0].length);
            if (bm == null) {
                Log.i("ICA", "bitmap null ");
            }
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                // Notice that width and height are reversed
                Bitmap scaled = Bitmap.createScaledBitmap(bm, screenHeight, screenWidth, true);
                int w = scaled.getWidth();
                int h = scaled.getHeight();
                // Setting post rotate to 90
                Matrix mtx = new Matrix();

                int CameraEyeValue = setPhotoOrientation(CameraActivity.this, 0); // CameraID = 1 : front 0:back
                mtx.postRotate(CameraEyeValue); // CameraEyeValue is default to Display Rotation

                bm = Bitmap.createBitmap(scaled, 0, 0, w, h, mtx, true);
            } else {// LANDSCAPE MODE
                //No need to reverse width and height
                Bitmap scaled = Bitmap.createScaledBitmap(bm, screenWidth, screenHeight, true);
                bm = scaled;
            }
        }
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        outStream.write(byteArray);
        //fos.write(data);
        outStream.close();
        return outFile;
    }

    public int setPhotoOrientation(Activity activity, int cameraId) {
        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        // do something for phones running an SDK before lollipop
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360; // compensate the mirror
        } else { // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }

        return result;
    }

    public static void setCameraDisplayOrientation(Activity activity, int cameraId, android.hardware.Camera camera) {
        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        //int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        // do something for phones running an SDK before lollipop
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360; // compensate the mirror
        } else { // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }

        camera.setDisplayOrientation(result);
    }

    private Camera.Size getOptimalSize(List<Camera.Size> sizes, int w, int h) {

        final double ASPECT_TOLERANCE = 0.2;
        double targetRatio = (double) w / h;
        if (sizes == null)
            return null;
        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;
        int targetHeight = h;
        // Try to find an size match aspect ratio and size
        for (Camera.Size size : sizes) {
//          Log.d("CameraActivity", "Checking size " + size.width + "w " + size.height + "h");
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }
        // Cannot find the one match the aspect ratio, ignore the requirement

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }

        SharedPreferences previewSizePref;

        previewSizePref = getSharedPreferences("FRONT_PREVIEW_PREF", MODE_PRIVATE);


        SharedPreferences.Editor prefEditor = previewSizePref.edit();
        prefEditor.putInt("width", optimalSize.width);
        prefEditor.putInt("height", optimalSize.height);

        prefEditor.commit();

//      Log.d("CameraActivity", "Using size: " + optimalSize.width + "w " + optimalSize.height + "h");
        return optimalSize;
    }

    private void setParameters() {
       /* List<Camera.Size> sizes = params.getSupportedPictureSizes();
        try {
            Camera.Size cameraPreviewSize = params.getPreviewSize();

            for (int i = 0; i < sizes.size(); i++) {

                if (sizes.get(i).width == 1920) {
                    width = 1920;
                    height = 1080;
                    break;
                } else {
                    width = sizes.get(0).width;
                    height = sizes.get(0).height;
                }
            }
            List<Camera.Size> previewSize = params.getSupportedPictureSizes();

            for (int i = 0; i < previewSize.size(); i++) {

                if (sizes.get(i).width == 1920) {
                    previewWidth = 1920;
                    previewHeight = 1080;
                    break;
                } else {
                    previewWidth = sizes.get(0).width;
                    previewHeight = sizes.get(0).height;
                }
            }
            params.setPictureSize(width, height);
            params.setPreviewSize(previewWidth, previewHeight);
            camera.setParameters(params);
            camera.startPreview();
        } catch (Exception e) {*/
        try {
            Camera.Size optimalSize = getBestPreviewSize(1920, 1080);
            params.setPictureSize(optimalSize.width, optimalSize.height);
            params.setPreviewSize(optimalSize.width, optimalSize.height);
            camera.setParameters(params);
            camera.startPreview();
        } catch (Exception e) {

            Log.i("pradeep", "cameara exception : " + e.getLocalizedMessage());
        }

        /*}*/

    }

    private Camera.Size getBestPreviewSize(int width, int height) {
        Camera.Size result = null;
        Camera.Parameters p = camera.getParameters();
        for (Camera.Size size : p.getSupportedPreviewSizes()) {
            if (size.width <= width && size.height <= height) {
                if (result == null) {
                    result = size;
                } else {
                    int resultArea = result.width * result.height;
                    int newArea = size.width * size.height;

                    if (newArea > resultArea) {
                        result = size;
                    }
                }
            }
        }
        return result;

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Get the pointer ID
        Camera.Parameters params = camera.getParameters();
        int action = event.getAction();
        if(event.getY() > zoomControls.getY()){
            return false;
        }

        if (event.getPointerCount() > 1) {
            // handle multi-touch events
            if (action == MotionEvent.ACTION_POINTER_DOWN) {
                mDist = getFingerSpacing(event);
            } else if (action == MotionEvent.ACTION_MOVE && params.isZoomSupported()) {
                camera.cancelAutoFocus();
                handleZoom(event, params);
            }
        } else {
            // handle single touch events
            if (action == MotionEvent.ACTION_UP) {
                handleFocus(event, params);
            }
        }
        return true;
    }

    private void handleZoom(MotionEvent event, Camera.Parameters params) {
        int maxZoom = params.getMaxZoom();
        int zoom = params.getZoom();
        float newDist = getFingerSpacing(event);
        if (newDist > mDist) {
            //zoom in
            if (zoom < maxZoom)
                zoom++;
        } else if (newDist < mDist) {
            //zoom out
            if (zoom > 0)
                zoom--;
        }
        mDist = newDist;
        params.setZoom(zoom);
        camera.setParameters(params);
    }

    public void handleFocus(MotionEvent event, Camera.Parameters params) {
        int pointerId = event.getPointerId(0);
        int pointerIndex = event.findPointerIndex(pointerId);
        // Get the pointer's current position
        float x = event.getX(pointerIndex);
        float y = event.getY(pointerIndex);

        List<String> supportedFocusModes = params.getSupportedFocusModes();
        if (supportedFocusModes != null && supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            camera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean b, Camera camera) {
                    // currently set to auto-focus on single touch
                }
            });
        }
    }

    /** Determine the space between the first two fingers */
    private float getFingerSpacing(MotionEvent event) {
        // ...
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float)Math.sqrt(x * x + y * y);
    }



}