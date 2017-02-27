package com.kalps.patientprofile;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.kalps.patientprofile.adapter.PatientImageAdapter;
import com.kalps.patientprofile.provider.PatientImages;
import com.kalps.patientprofile.provider.PatientProvider;
import com.kalps.patientprofile.provider.Patients;
import com.kalps.patientprofile.utils.Utils;
import com.kalps.patientprofile.view.ExpandableHeightGridView;
import com.kbeanie.imagechooser.api.ChooserType;
import com.kbeanie.imagechooser.api.ChosenImage;
import com.kbeanie.imagechooser.api.ImageChooserListener;
import com.kbeanie.imagechooser.api.ImageChooserManager;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.OnItemClickListener;
import com.orhanobut.dialogplus.ViewHolder;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;

import cn.pedant.SweetAlert.SweetAlertDialog;
import de.hdodenhof.circleimageview.CircleImageView;

public class PatientProfileActivity extends AppCompatActivity implements ImageChooserListener, DatePickerDialog.OnDateSetListener {
    private Toolbar toolbar;
    private EditText editTextName, editTextRefPhysician, editTextHospital, editextDiagnosis,
            editTextComments, editTextMrd;
    private TextView editTextDob, editTextFollowUpDate, editTextExaminationDate, textViewImagesAttached;
    private RadioButton radioButtonMale, radioButtonFeMale;
    private ImageView imageViewSave, imageViewDelete, imageViewExport;
    private Patients patients;
    private ArrayList<String> updateNewImageList = new ArrayList<>();

    private ArrayList<String> tempImageList = new ArrayList<>();
    private ArrayList<PatientImages> patientImageList = new ArrayList<>();
    private ExpandableHeightGridView gridView;
    private ImageChooserManager imageChooserManager;
    private String filePath;
    private int chooserType;
    public static final String DATEPICKER_DOB_TAG = "datepickerdob";
    private DatePickerDialog datePickerDialog;
    private boolean isDOb = false;
    private boolean isExaminationDate = false;
    private CircleImageView imageViewCamera;
    private PatientImageAdapter patientImageAdapter;
    private boolean fromActivityResult = false;
    private static final int REQUEST_CODE_FULL_IMAGE = 101;

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (updateNewImageList != null)
            for (int i = 0; i < updateNewImageList.size(); i++) {
                // Utils.delete(updateNewImageList.get(i));
            }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.status_bar));
        }
        setContentView(R.layout.activity_patient_profile);
        patients = (Patients) getIntent().getSerializableExtra("patient");
        initViews();
        fillData();
        Utils.hideKeyboard(this.getCurrentFocus(), PatientProfileActivity.this);
        initalender(savedInstanceState);
        new LoadPatientsImages().execute();
    }

    @Override
    protected void onResume() {
        super.onResume();


    }

    private void initViews() {
        textViewImagesAttached = (TextView) findViewById(R.id.textViewImagesAttached);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        editTextName = (EditText) findViewById(R.id.editTextName);
        editTextDob = (TextView) findViewById(R.id.editTextDob);
        editTextMrd = (EditText) findViewById(R.id.editTextMrdNo);
        editTextRefPhysician = (EditText) findViewById(R.id.editTextRefPhysician);
        editTextHospital = (EditText) findViewById(R.id.editTextHospital);
        editextDiagnosis = (EditText) findViewById(R.id.editTextDiagnosis);
        editTextComments = (EditText) findViewById(R.id.editTextComments);
        editTextFollowUpDate = (TextView) findViewById(R.id.editTextFollowUpDate);
        editTextExaminationDate = (TextView) findViewById(R.id.editTextExaminationDate);
        radioButtonMale = (RadioButton) findViewById(R.id.radioButtonMale);
        radioButtonFeMale = (RadioButton) findViewById(R.id.radioButtonFeMale);
        imageViewSave = (ImageView) findViewById(R.id.imageViewSave);
        imageViewDelete = (ImageView) findViewById(R.id.imageViewDelete);
        imageViewExport = (ImageView) findViewById(R.id.imageViewExport);
        gridView = (ExpandableHeightGridView) findViewById(R.id.gridView);
        gridView.setExpanded(true);
        imageViewCamera = (CircleImageView) findViewById(R.id.imageViewCamera);
        imageViewExport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sendEmail();
            }
        });
        imageViewCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showImagePIckDialog();
            }
        });
        imageViewSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                Utils.hideKeyboard(v, PatientProfileActivity.this);
                new UpdatePatientsData().execute();
            }
        });
        imageViewDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SweetAlertDialog(PatientProfileActivity.this, SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("Are you sure?")
                        .setContentText("Won't be able to recover this file!")
                        .setConfirmText("Yes,delete it!")
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sDialog) {
                                sDialog.dismissWithAnimation();
                                deleteOperation();
                            }
                        })
                        .setCancelText("    No    ")
                        .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sDialog) {
                                sDialog.dismissWithAnimation();

                            }
                        })
                        .show();
            }
        });
        editTextDob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //datePickerDialog.setYearRange(2014, 2015);
                isDOb = true;
                datePickerDialog.setCloseOnSingleTapDay(true);
                datePickerDialog.show(getSupportFragmentManager(), DATEPICKER_DOB_TAG);
            }
        });
        editTextFollowUpDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //datePickerDialog.setYearRange(2014, 2015);
                isDOb = false;
                datePickerDialog.setCloseOnSingleTapDay(true);
                datePickerDialog.show(getSupportFragmentManager(), DATEPICKER_DOB_TAG);
            }
        });
        editTextExaminationDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //datePickerDialog.setYearRange(2014, 2015);
                isExaminationDate = true;
                datePickerDialog.setCloseOnSingleTapDay(true);
                datePickerDialog.show(getSupportFragmentManager(), DATEPICKER_DOB_TAG);
            }
        });
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(PatientProfileActivity.this, FUllImageActivity.class);
                intent.putExtra(PatientProvider.MRD_NO, editTextMrd.getText().toString());
                intent.putExtra("position", position);
                startActivityForResult(intent, REQUEST_CODE_FULL_IMAGE);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
            }
        });
    }

    private void initalender(Bundle savedInstanceState) {
        final Calendar calendar = Calendar.getInstance();

        datePickerDialog = DatePickerDialog.newInstance(this, calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), false);

        if (savedInstanceState != null) {
            DatePickerDialog dpd = (DatePickerDialog) getSupportFragmentManager().findFragmentByTag(DATEPICKER_DOB_TAG);
            if (dpd != null) {
                dpd.setOnDateSetListener(this);
            }
        }
    }


    private boolean updateData(View view) {


        ContentValues values = new ContentValues();
        values.put(PatientProvider.NAME, editTextName.getText().toString().trim());
        //values.put(PatientProvider.MRD_NO, editTextMrd.getText().toString().trim());
        values.put(PatientProvider.DOB, editTextDob.getText().toString());
        values.put(PatientProvider.DIAGNOSIS, editextDiagnosis.getText().toString().trim());
        values.put(PatientProvider.HOSPITAL, editTextHospital.getText().toString().trim());
        values.put(PatientProvider.FOLLOWUP_DATE, editTextFollowUpDate.getText().toString());
        values.put(PatientProvider.REF_PHYSICIAN, editTextRefPhysician.getText().toString().trim());
        values.put(PatientProvider.COMMENTS, editTextComments.getText().toString().trim());
        values.put(PatientProvider.EXAMINATION_DATE, editTextExaminationDate.getText().toString());
        values.put(PatientProvider.TIMESTAMP, System.currentTimeMillis());

        if (radioButtonMale.isChecked())
            values.put(PatientProvider.SEX, "1");
        else
            values.put(PatientProvider.SEX, "0");

        String[] projection1 = {String.valueOf(patients.getMrdNo())};
        int count = getContentResolver().update(PatientProvider.CONTENT_URI, values, " mrd=? ", projection1);
        if (count > 0) {
            // Utils.snackToast("SuccessFully Updated  Patient Details", view);
            Utils.logVerbose("Successfully updated data count :" + count);

            return true;
        } else {
            Utils.snackToast("Failed To Update Patient Details.Need Unique MRD NO", view);
            return false;
        }

    }

    private void deleteOperation() {


        String[] projection = new String[]{
                PatientProvider.MRD_NO,
                PatientProvider.IMAGES,
                "id"
        };
        String selection = PatientProvider.MRD_NO + " = " + "'" + patients.getMrdNo() + "'";
        Cursor c = getContentResolver().query(PatientProvider.CONTENT_URI1, null, selection, null, null);
        Utils.logVerbose("patients image cursor size : " + c.getCount());

        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    String path = c.getString(c.getColumnIndex("image"));
                    Utils.logVerbose("image path  " + path);
                    Utils.deleteImage(path, getApplicationContext());
                    getApplicationContext().sendBroadcast(
                            new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                                    Uri.fromFile(new File(path))));

                } while (c.moveToNext());
            }
        }


        String[] projection1 = {String.valueOf(patients.getMrdNo())};
        int count = getContentResolver().delete(PatientProvider.CONTENT_URI, " mrd=? ", projection1);
        Utils.logVerbose("Deleted Row : " + count);

        int count1 = getContentResolver().delete(PatientProvider.CONTENT_URI1, " mrd=? ", projection1);
        Utils.logVerbose("Deleted Row count : " + count1);


        finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);

    }

    private void updatePatientImages(String imagePath) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(PatientProvider.MRD_NO, patients.getMrdNo());
        contentValues.put(PatientProvider.IMAGES, imagePath);
        Uri uri = getContentResolver().insert(
                PatientProvider.CONTENT_URI1, contentValues);
        Utils.logVerbose("Successfully Added image uri1:" + uri);
    }

    private void fillData() {
        editTextMrd.setText(patients.getMrdNo() + "");

        editTextMrd.setFocusable(false);
        editTextMrd.setFocusableInTouchMode(false);
        editTextMrd.setClickable(false);

        editTextName.setText(patients.getName());
        editextDiagnosis.setText(patients.getDiagnosis());
        editTextDob.setText(patients.getDob());
        editTextRefPhysician.setText(patients.getRefPhysician());
        editTextHospital.setText(patients.getHospital());
        editTextComments.setText(patients.getComments());
        editTextFollowUpDate.setText(patients.getFollowupDate());
        editTextExaminationDate.setText(patients.getExaminationDate());
        if (patients.getGender().equalsIgnoreCase("1")) {
            radioButtonMale.setChecked(true);
        } else {
            radioButtonFeMale.setChecked(true);
        }

    }

    private void loadImages() {

        String[] projection = new String[]{
                PatientProvider.MRD_NO,
                PatientProvider.IMAGES,
                "id"
        };
        String selection = PatientProvider.MRD_NO + " = " + "'" + patients.getMrdNo() + "'";
        Cursor c = getContentResolver().query(PatientProvider.CONTENT_URI1, null, selection, null, null);
        Utils.logVerbose("patients image cursor size : " + c.getCount());

        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    Utils.logVerbose("PatientImages loaded mrd: " + c.getString(c.getColumnIndex(PatientProvider.MRD_NO)));
                    Utils.logVerbose("PatientImages loaded : " + c.getString(c.getColumnIndex(PatientProvider.IMAGES)));
                    PatientImages patientImages = new PatientImages();
                    patientImages.setId(c.getInt(c.getColumnIndex("id")));
                    patientImages.setMrd(c.getString(c.getColumnIndex("mrd")));
                    patientImages.setImage(c.getString(c.getColumnIndex("image")));
                    patientImageList.add(patientImages);
                } while (c.moveToNext());
            }
        }


    }


    private void takePicture() {
        chooserType = ChooserType.REQUEST_CAPTURE_PICTURE;
        imageChooserManager = new ImageChooserManager(this,
                ChooserType.REQUEST_CAPTURE_PICTURE, true);
        imageChooserManager.setImageChooserListener(this);
        try {
            filePath = imageChooserManager.choose();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void chooseImage() {
        chooserType = ChooserType.REQUEST_PICK_PICTURE;
        imageChooserManager = new ImageChooserManager(this,
                ChooserType.REQUEST_PICK_PICTURE, true);
        imageChooserManager.setImageChooserListener(this);
        try {

            filePath = imageChooserManager.choose();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onImageChosen(ChosenImage chosenImage) {
        filePath = chosenImage.getFilePathOriginal();
        Utils.deleteImage(chosenImage.getFileThumbnail(), getApplicationContext());
        Utils.deleteImage(chosenImage.getFileThumbnailSmall(), getApplicationContext());
        Utils.logVerbose("filepath :" + filePath);
        new UpdateImages().execute(filePath);
        Utils.callBroadCast(getApplicationContext());
    }

    @Override
    public void onError(String s) {

    }

    private void reinitializeImageChooser() {
        imageChooserManager = new ImageChooserManager(this, chooserType, true);
        imageChooserManager.setImageChooserListener(this);
        imageChooserManager.reinitialize(filePath);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK
                && (requestCode == ChooserType.REQUEST_PICK_PICTURE ||
                requestCode == ChooserType.REQUEST_CAPTURE_PICTURE)) {
            if (imageChooserManager == null) {
                reinitializeImageChooser();
            }
            imageChooserManager.submit(requestCode, data);
        } else if (requestCode == REQUEST_CODE_FULL_IMAGE) {
            new LoadPatientsImages().execute();


        } else {
            Utils.logVerbose(" image not picked properly ");
        }
    }

    @Override
    public void onDateSet(DatePickerDialog datePickerDialog, int year, int month, int day) {
        String resultStartDate = day + "/" + (month + 1) + "/" + year;
        isExaminationDate = false;
        if (!isExaminationDate) {
            if (isDOb)
                editTextDob.setText(resultStartDate);
            else
                editTextFollowUpDate.setText(resultStartDate);
        } else {
            editTextExaminationDate.setText(resultStartDate);

        }
    }

    private class UpdatePatientsData extends AsyncTask<String, String, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... f_url) {

            return updateData(PatientProfileActivity.this.getCurrentFocus());
        }

        @Override
        protected void onPostExecute(Boolean valid) {
            if (valid) {
                finish();
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
            }
        }
    }

    private void showImagePIckDialog() {
        final DialogPlus dialog;
        dialog = DialogPlus.newDialog(PatientProfileActivity.this)
                .setContentHolder(new ViewHolder(R.layout.dialog_camera_gallery))
                .setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(DialogPlus dialog, Object item, View view, int position) {

                    }
                })
                .setExpanded(false)
                .create();
        dialog.show();
        TextView camera = (TextView) dialog.findViewById(R.id.textViewCamera);
        TextView gallery = (TextView) dialog.findViewById(R.id.textViewGallery);
        TextView cancel = (TextView) dialog.findViewById(R.id.textViewCancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
                    // Marshmallow+
                    checkPermissionAndOpenCamera();
                } else {
                    //below Marshmallow
                    takePicture();
                }
            }
        });
        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                chooseImage();
            }
        });
    }


    private class LoadPatientsImages extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            patientImageList.clear();

        }

        @Override
        protected String doInBackground(String... f_url) {
            loadImages();
            return null;
        }

        @Override
        protected void onPostExecute(String file_url) {

            if (patientImageList.size() == 1)
                textViewImagesAttached.setText(patientImageList.size() + " image attached");
            else
                textViewImagesAttached.setText(patientImageList.size() + " image(s) attached");
            patientImageAdapter = new PatientImageAdapter(PatientProfileActivity.this, patientImageList);
            for (int i = 0; i < patientImageList.size(); i++) {

                Utils.logVerbose("path : " + patientImageList.get(i).getImage());


            }
            gridView.setAdapter(patientImageAdapter);
            // Utils.setGridViewHeightBasedOnChildren(gridView);

        }
    }


    private void sendEmail() {


        String email = "", subject = " Patient Profile of  " + patients.getName();

        String firstLine = " <p> Dear Sir/Madam,  <p>";

        String gender;
        if (patients.getGender().equalsIgnoreCase("1"))
            gender = "Female";
        else
            gender = "Male";

        String one = " <p> Name          : " + editTextName.getText().toString() + " <p> \n";
        String two = " <p> MRD NO        : " + editTextMrd.getText().toString() + " <p> \n";
        String three = " <p> SEX           : " + gender + " <p> \n";
        String four = " <p> Date Of Birth : " + editTextDob.getText().toString() + " <p> \n";
        String five = " <p> Hospital      : " + editTextHospital.getText().toString() + " <p> \n";
        String six = " <p> Ref Physician : " + editTextRefPhysician.getText().toString() + " <p> \n";
        String seven = " <p> Diagnosis     : " + editextDiagnosis.getText().toString() + " <p> \n";
        String eight = " <p> Followup Date : " + editTextFollowUpDate.getText().toString() + " <p> \n";
        String nine = " <p> Coments       : " + editTextComments.getText().toString() + " <p> \n";


        Spanned body = Html.fromHtml(new StringBuilder()
                .append(firstLine)
                .append("<p>Please find the below details </p>")
                .append("<small> " + one + "</small>")
                .append("<small> " + two + "</small>")
                .append("<small> " + three + "</small>")
                .append("<small> " + four + "</small>")
                .append("<small> " + five + "</small>")
                .append("<small> " + six + "</small>")
                .append("<small> " + seven + "</small>")
                .append("<small> " + eight + "</small>")
                .append("<small> " + nine + "</small>")

                .toString());

        final Intent ei = new Intent(Intent.ACTION_SEND_MULTIPLE);
        ei.setType("text/html");
        ei.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        ei.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        ei.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
        ei.putExtra(Intent.EXTRA_SUBJECT, subject);
        ArrayList<Uri> uris = new ArrayList<Uri>();
        for (int i = 0; i < patientImageList.size(); i++) {
            File file = new File(patientImageList.get(i).getImage());
            Uri uri = Uri.fromFile(file);
            Utils.logVerbose("image file path : " + patientImageList.get(i).getImage());
            uris.add(uri);
        }
        ei.putExtra(Intent.EXTRA_TEXT, body);
        ei.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        startActivityForResult(Intent.createChooser(ei, "Sending Patient Profile"), 12345);

    }

    private class UpdateImages extends AsyncTask<String, String, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            patientImageList.clear();
        }

        @Override
        protected Void doInBackground(String... params) {
            updatePatientImages(params[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            new LoadPatientsImages().execute();
        }
    }

    private static final int CAMERA_PERMISSION = 1012;

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_PERMISSION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    takePicture();

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    Toast.makeText(PatientProfileActivity.this, " Please enable camera permission to proceed", Toast.LENGTH_SHORT).show();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;

            }
        }
    }

    private void checkPermissionAndOpenCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,
            Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            takePicture();
        }
        else {
            ActivityCompat.requestPermissions(this,
                new String[] { Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE },
                CAMERA_PERMISSION
            );
        }
    }

}
