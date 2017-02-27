package com.kalps.patientprofile;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.kalps.patientprofile.adapter.PatientsAdapter;
import com.kalps.patientprofile.provider.PatientProvider;
import com.kalps.patientprofile.provider.Patients;
import com.kalps.patientprofile.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class PatientsListActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private FloatingActionButton fab;
    private ListView listView;
    private List<Patients> patientsList = new ArrayList<>();
    private EditText editTextSearch;
    private ImageView imageViewAbout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.dark_light));
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patients_list);
        initViews();
        initOperations();


    }

    @Override
    protected void onResume() {
        super.onResume();
        new LoadPatientsData().execute();
        Utils.hideKeyboard(this.getCurrentFocus(), PatientsListActivity.this);
    }

    private void initOperations() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PatientsListActivity.this, AddPatientActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(PatientsListActivity.this, PatientProfileActivity.class);
                intent.putExtra("patient", patientsList.get(position));
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
            }
        });
    }

    private void initViews() {
        imageViewAbout = (ImageView) findViewById(R.id.imageViewAbout);
        imageViewAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PatientsListActivity.this, AboutActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
            }
        });
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        fab = (FloatingActionButton) findViewById(R.id.fabAddPatients);
        listView = (ListView) findViewById(R.id.listView);
        editTextSearch = (EditText) findViewById(R.id.editTextSearch);
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                Utils.logVerbose("afterTextChanged : " + s.toString());
                new LoadPatientsData().execute(s.toString());
            }
        });
    }


    private void initData() {
        //this hides the keyboard if there is no string in edit text .
        Utils.hideKeyboard(this.getCurrentFocus(), PatientsListActivity.this);
        Cursor c = getContentResolver().query(PatientProvider.CONTENT_URI, null, null, null, "timestamp DESC");
        if (c != null) {
            if (c.moveToFirst()) {
                do {

                    Patients patients = new Patients();
                    patients.setHospital(c.getString(c.getColumnIndex(PatientProvider.HOSPITAL)));
                    patients.setName(c.getString(c.getColumnIndex(PatientProvider.NAME)));
                    patients.setRefPhysician(c.getString(c.getColumnIndex(PatientProvider.REF_PHYSICIAN)));
                    patients.setMrdNo(c.getString(c.getColumnIndex(PatientProvider.MRD_NO)));
                    patients.setComments(c.getString(c.getColumnIndex(PatientProvider.COMMENTS)));
                    patients.setDiagnosis(c.getString(c.getColumnIndex(PatientProvider.DIAGNOSIS)));
                    patients.setDob(c.getString(c.getColumnIndex(PatientProvider.DOB)));
                    patients.setFollowupDate(c.getString(c.getColumnIndex(PatientProvider.FOLLOWUP_DATE)));
                    patients.setExaminationDate(c.getString(c.getColumnIndex(PatientProvider.EXAMINATION_DATE)));
                    patients.setGender(c.getString(c.getColumnIndex(PatientProvider.SEX)));

                    Utils.logVerbose("MRD : " + c.getInt(c.getColumnIndex(PatientProvider.MRD_NO)));
                    patientsList.add(patients);

                } while (c.moveToNext());
            }
        }
    }


    private void searchData(String query) {


        String[] projection = new String[]{
                PatientProvider.NAME,
        };
        String selection = PatientProvider.NAME + " LIKE '%" + query + "%' OR " +
                PatientProvider.MRD_NO + " LIKE '%" + query + "%' OR " +
                PatientProvider.REF_PHYSICIAN + " LIKE '%" + query + "%' OR " +
                PatientProvider.HOSPITAL + " LIKE '%" + query + "%'";

        //String[] selectionArgs = new String[]{"%" + query + "%"};

        Cursor c = getContentResolver().query(PatientProvider.CONTENT_URI, null, selection, null, null);
        if (c != null) {
            if (c.moveToFirst()) {
                do {

                    Patients patients = new Patients();
                    patients.setHospital(c.getString(c.getColumnIndex(PatientProvider.HOSPITAL)));
                    patients.setName(c.getString(c.getColumnIndex(PatientProvider.NAME)));
                    patients.setRefPhysician(c.getString(c.getColumnIndex(PatientProvider.REF_PHYSICIAN)));
                    patients.setMrdNo(c.getString(c.getColumnIndex(PatientProvider.MRD_NO)));
                    patients.setComments(c.getString(c.getColumnIndex(PatientProvider.COMMENTS)));
                    patients.setDiagnosis(c.getString(c.getColumnIndex(PatientProvider.DIAGNOSIS)));
                    patients.setDob(c.getString(c.getColumnIndex(PatientProvider.DOB)));
                    patients.setFollowupDate(c.getString(c.getColumnIndex(PatientProvider.FOLLOWUP_DATE)));
                    patients.setExaminationDate(c.getString(c.getColumnIndex(PatientProvider.EXAMINATION_DATE)));
                    patients.setGender(c.getString(c.getColumnIndex(PatientProvider.SEX)));

                    Utils.logVerbose("MRD : " + c.getInt(c.getColumnIndex(PatientProvider.MRD_NO)));
                    patientsList.add(patients);

                } while (c.moveToNext());
            }
        }
    }

    private class LoadPatientsData extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            patientsList.clear();
        }

        @Override
        protected String doInBackground(String... query) {
            if (query != null && query.length > 0) {
                if (query[0].toString().length() > 0)
                    searchData(query[0]);
                else
                    initData();
            } else {
                initData();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String file_url) {
            listView.setAdapter(new PatientsAdapter(PatientsListActivity.this, patientsList));
        }
    }

    private static final int WRITE_PERMISSION = 1012;

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case WRITE_PERMISSION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // takePicture();
                    checkPermissionAndOpenCamera();
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    Toast.makeText(PatientsListActivity.this, " Please enable storage permission to proceed", Toast.LENGTH_SHORT).show();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;

            }
        }
    }

    private void checkPermissionAndOpenCamera() {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, WRITE_PERMISSION
                );
            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, WRITE_PERMISSION
                );

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }
}
