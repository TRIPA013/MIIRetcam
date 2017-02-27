package com.kalps.patientprofile.provider;

import java.io.Serializable;

/**
 * Created by user on 17/1/16.
 */
public class Patients implements Serializable {

    private String mrdNo;

    public String getExaminationDate() {
        return examinationDate;
    }

    public void setExaminationDate(String examinationDate) {
        this.examinationDate = examinationDate;
    }

    private String examinationDate;

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    private String gender;
    private String name, hospital, refPhysician, dob, followupDate, comments, diagnosis;

    public String getMrdNo() {
        return mrdNo;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getFollowupDate() {
        return followupDate;
    }

    public void setFollowupDate(String followupDate) {
        this.followupDate = followupDate;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public void setMrdNo(String mrdNo) {
        this.mrdNo = mrdNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHospital() {
        return hospital;
    }

    public void setHospital(String hospital) {
        this.hospital = hospital;
    }

    public String getRefPhysician() {
        return refPhysician;
    }

    public void setRefPhysician(String refPhysician) {
        this.refPhysician = refPhysician;
    }
}
