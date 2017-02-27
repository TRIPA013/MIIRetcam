package com.kalps.patientprofile;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;

import com.crashlytics.android.Crashlytics;
import com.kalps.patientprofile.utils.Utils;
import com.kbeanie.imagechooser.api.BChooserPreferences;

import io.fabric.sdk.android.Fabric;
import java.io.File;

/**
 * Created by pradeep on 13/2/16.
 */
public class PatientProfile extends Application implements Application.ActivityLifecycleCallbacks {
    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        registerActivityLifecycleCallbacks(this);
        handler = new Handler(getMainLooper());
        BChooserPreferences pref = new BChooserPreferences(getApplicationContext());
        pref.setFolderName(Environment.DIRECTORY_PICTURES + "/" + Utils.FOLDER);
    }

    @Override
    public void onTerminate() {

        super.onTerminate();

    }

    private Handler handler;
    private Runnable runLogout = new Runnable() {
        @Override
        public void run() {
            //()
            Utils.logVerbose("logoutUser . . . .");
            Utils.logVerbose("Deleting the folder . . . .");
            File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" +
                    Environment.DIRECTORY_PICTURES + "/" + Utils.FOLDER);
            Utils.logVerbose("Deleting the folder . . . ." + dir.getPath());
            if (dir.isDirectory()) {
                Utils.logVerbose("Its a folder");
                String[] children = dir.list();
                if (children != null)
                    for (int i = 0; i < children.length; i++) {
                        //Utils.logVerbose("Deleting the file . . . ." + new File(dir, children[i]).delete());
                        //Utils.logVerbose("List the file . . . ." + new File(dir, children[i]).getAbsolutePath());

                    }
            }
        }
    };

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

        handler.removeCallbacks(runLogout);
    }

    @Override
    public void onActivityStarted(Activity activity) {

        handler.removeCallbacks(runLogout);
    }

    @Override
    public void onActivityResumed(Activity activity) {

        handler.removeCallbacks(runLogout);
    }

    @Override
    public void onActivityPaused(Activity activity) {

        handler.postDelayed(runLogout, 1000);
    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }
}
