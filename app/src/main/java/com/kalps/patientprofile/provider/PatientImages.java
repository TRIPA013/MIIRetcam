package com.kalps.patientprofile.provider;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by user on 18/1/16.
 */
public class PatientImages implements Serializable {

    private int id ;



    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMrd() {
        return mrd;
    }

    public void setMrd(String mrd) {
        this.mrd = mrd;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    private String image,mrd;


}
