package com.kalps.patientprofile;

import android.Manifest;

import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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
import com.kalps.patientprofile.utils.Utils;
import com.kalps.patientprofile.view.ExpandableHeightGridView;
import com.kbeanie.imagechooser.api.ChooserType;
import com.kbeanie.imagechooser.api.ChosenImage;
import com.kbeanie.imagechooser.api.ImageChooserListener;
import com.kbeanie.imagechooser.api.ImageChooserManager;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.OnItemClickListener;
import com.orhanobut.dialogplus.ViewHolder;
import de.hdodenhof.circleimageview.CircleImageView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class AddPatientActivity extends AppCompatActivity
	implements ImageChooserListener, DatePickerDialog.OnDateSetListener {
	private Toolbar toolbar;
	private EditText editTextName, editTextRefPhysician, editTextHospital, editextDiagnosis,
		editTextComments, editTextMrd;
	private TextView editTextDob, editTextFollowUpDate, editTextExaminationDate, textViewImagesAttached;
	private RadioButton radioButtonMale, radioButtonFeMale;
	private ImageView buttonAddPatient;
	private CircleImageView imageViewCamera;
	private ImageChooserManager imageChooserManager;
	private String filePath;
	private int chooserType;
	public static final String DATEPICKER_DOB_TAG = "datepickerdob";
	private DatePickerDialog datePickerDialog;
	private boolean isDOb = false;
	private boolean isExaminationDate = false;
	private ArrayList<String> imageList = new ArrayList<>();
	private ExpandableHeightGridView gridView;
	private static final int REQUEST_CODE_FULL_IMAGE = 101;

	private boolean isInserted = false, isGalleryIcon = false;
	private Cursor imageCursor;
	private ArrayList<PatientImages> patientImageList = new ArrayList<>();
	private boolean isSaveButtonPressed = false;
	private static final int CAMERA_PERMISSION = 200;

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		finish();
		overridePendingTransition(R.anim.slide_in_down, R.anim.slide_out_down);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			Window window = getWindow();
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			window.setStatusBarColor(getResources().getColor(R.color.status_bar));
		}
		setContentView(R.layout.activity_add_patient);
		initViews();
		initOperation();
		Utils.hideKeyboard(this.getCurrentFocus(), AddPatientActivity.this);
		initalender(savedInstanceState);
		imageList.clear();
	}

	private void initViews() {
		textViewImagesAttached = (TextView) findViewById(R.id.textViewImagesAttached);
		toolbar = (Toolbar) findViewById(R.id.toolbar);
		editTextName = (EditText) findViewById(R.id.editTextName);
		editTextMrd = (EditText) findViewById(R.id.editTextMrdNo);
		editTextDob = (TextView) findViewById(R.id.editTextDob);
		editTextRefPhysician = (EditText) findViewById(R.id.editTextRefPhysician);
		editTextHospital = (EditText) findViewById(R.id.editTextHospital);
		editextDiagnosis = (EditText) findViewById(R.id.editTextDiagnosis);
		editTextComments = (EditText) findViewById(R.id.editTextComments);
		editTextFollowUpDate = (TextView) findViewById(R.id.editTextFollowUpDate);
		editTextExaminationDate = (TextView) findViewById(R.id.editTextExaminationDate);
		radioButtonMale = (RadioButton) findViewById(R.id.radioButtonMale);
		radioButtonFeMale = (RadioButton) findViewById(R.id.radioButtonFeMale);
		buttonAddPatient = (ImageView) findViewById(R.id.imageViewSave);
		imageViewCamera = (CircleImageView) findViewById(R.id.imageViewCamera);
		gridView = (ExpandableHeightGridView) findViewById(R.id.gridView);
		gridView.setExpanded(true);
	}

	private void initOperation() {
		radioButtonMale.setChecked(true);
		buttonAddPatient.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
                /*if (validate(v)) {
                    Utils.hideKeyboard(v, AddPatientActivity.this);
                    new AddPatientsData().execute();
                }*/
				isGalleryIcon = false;
				isSaveButtonPressed = true;
				Utils.logVerbose("Add patient");
				if (!isInserted) {
					Utils.logVerbose("Add patient if ");
					if (validate(v)) {
						Utils.logVerbose("Add patient if inside ");
						Utils.hideKeyboard(v, AddPatientActivity.this);
						new AddPatientsData().execute();
					}
				}
				else {
					Utils.logVerbose("Add patient else ");
					Utils.hideKeyboard(v, AddPatientActivity.this);
					new UpdatePatientsData().execute();
				}
			}
		});
		imageViewCamera.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				isGalleryIcon = true;
				if (!isInserted) {
					if (validate(v)) {
						Utils.hideKeyboard(v, AddPatientActivity.this);
						new AddPatientsData().execute();
					}
				}
				else {
					Utils.hideKeyboard(v, AddPatientActivity.this);
					new UpdatePatientsData().execute();
				}

			}
		});
		editTextDob.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//datePickerDialog.setYearRange(2014, 2015);
				isDOb = true;//
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
		editTextExaminationDate.setText(getCurrentDate());

		gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


				Intent intent = new Intent(AddPatientActivity.this, FUllImageActivity.class);
				intent.putExtra(PatientProvider.MRD_NO, editTextMrd.getText().toString().trim());
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

	private boolean validate(View view) {
		boolean returnValue = false;
		if (editTextName.getText().toString().isEmpty()) {
			Utils.snackToast("Name is empty", view);
		}
		else if (editTextMrd.getText().toString().isEmpty()) {
			Utils.snackToast("MRD NO is empty", view);
		}
		else {
			returnValue = true;
		}

        /*else if (editTextDob.getText().toString().isEmpty()) {
            Utils.snackToast("Date of Birth is empty", view);
        } else if (editTextRefPhysician.getText().toString().isEmpty()) {
            Utils.snackToast("Reference Physician is empty", view);
        } else if (editTextHospital.getText().toString().isEmpty()) {
            Utils.snackToast("Hospital is empty", view);
        } else if (editextDiagnosis.getText().toString().isEmpty()) {
            Utils.snackToast("Diagnosis is empty", view);
        } else if (editTextFollowUpDate.getText().toString().isEmpty()) {
            Utils.snackToast("FollowUp Date is empty", view);
        } */

		return returnValue;
	}


	private boolean checkIfMrdExists() {
		boolean returnValue = true;

		Cursor c = getContentResolver().query(PatientProvider.CONTENT_URI,
			null, "lower(" + PatientProvider.MRD_NO + ")=lower('" + editTextMrd.getText().toString().trim() + "')",
			null, null);


        /*String selection = "'" + PatientProvider.MRD_NO + "'" + " LIKE " + "'" + editTextMrd.getText().toString() + "'";
        Cursor c = getContentResolver().query(PatientProvider.CONTENT_URI1, null, selection, null, null);*/


		if (c.getCount() == 0) {
			returnValue
				= false;
		}
		return returnValue;

	}

	private boolean addData(View view) {

		if (checkIfMrdExists()) {
			Utils.snackToast("MRD NO Already Exists ! ", view);
			return false;
		}

		String mrdNO;
		ContentValues values = new ContentValues();
		values.put(PatientProvider.NAME, editTextName.getText().toString().trim());
		values.put(PatientProvider.MRD_NO, editTextMrd.getText().toString().trim());
		values.put(PatientProvider.DOB, editTextDob.getText().toString());
		values.put(PatientProvider.DIAGNOSIS, editextDiagnosis.getText().toString().trim());
		values.put(PatientProvider.HOSPITAL, editTextHospital.getText().toString().trim());
		values.put(PatientProvider.FOLLOWUP_DATE, editTextFollowUpDate.getText().toString());
		values.put(PatientProvider.REF_PHYSICIAN, editTextRefPhysician.getText().toString().trim());
		values.put(PatientProvider.COMMENTS, editTextComments.getText().toString().trim());
		values.put(PatientProvider.EXAMINATION_DATE, editTextExaminationDate.getText().toString());
		values.put(PatientProvider.TIMESTAMP, System.currentTimeMillis());


		if (radioButtonMale.isChecked()) {
			values.put(PatientProvider.SEX, "1");
		}
		else {
			values.put(PatientProvider.SEX, "0");
		}

		if (radioButtonFeMale.isChecked()) {
			values.put(PatientProvider.SEX, "0");
		}
		else {
			values.put(PatientProvider.SEX, "1");
		}

		Uri uri = getContentResolver().insert(
			PatientProvider.CONTENT_URI, values);
		if (uri != null) {
			//Utils.snackToast("SuccessFully Added Patient Details", view);

			int size = uri.toString().split("/").length;
			String[] array = uri.toString().split("/");

			mrdNO = editTextMrd.getText().toString().trim();

			if (!imageList.isEmpty()) {
				for (int i = 0; i < imageList.size(); i++) {
					ContentValues contentValues = new ContentValues();
					contentValues.put(PatientProvider.MRD_NO, mrdNO);
					contentValues.put(PatientProvider.IMAGES, imageList.get(i));
					Uri uri1 = getContentResolver().insert(
						PatientProvider.CONTENT_URI1, contentValues);
					Utils.logVerbose("Successfully Added image with uri : :" + uri1);
				}
			}
			return true;
		}
		else {
			Utils.snackToast("Failed To Add Patient Details Due to Invalid Non Unique Mrd No", view);
			return false;

		}

	}

	private void updatePatientImages(String imagePath) {
		ContentValues contentValues = new ContentValues();
		contentValues.put(PatientProvider.MRD_NO, editTextMrd.getText().toString());
		contentValues.put(PatientProvider.IMAGES, imagePath);
		Uri uri = getContentResolver().insert(
			PatientProvider.CONTENT_URI1, contentValues);
		Utils.logVerbose("Successfully Added image uri1:" + uri);
	}

	private void takePicture() {
		chooserType = ChooserType.REQUEST_CAPTURE_PICTURE;
		imageChooserManager = new ImageChooserManager(this,
			ChooserType.REQUEST_CAPTURE_PICTURE, true);
		imageChooserManager.setImageChooserListener(this);
		try {
			filePath = imageChooserManager.choose();
		}
		catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		catch (Exception e) {
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
		}
		catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onImageChosen(ChosenImage chosenImage) {
		filePath = chosenImage.getFilePathOriginal();
		Utils.logVerbose("onImageChosen : filepath :" + filePath);
		Utils.deleteImage(chosenImage.getFileThumbnail(), getApplicationContext());
		Utils.deleteImage(chosenImage.getFileThumbnailSmall(), getApplicationContext());
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
		}
		else if (requestCode == REQUEST_CODE_FULL_IMAGE) {
			new LoadImagesInGrid().execute();

		}
		else {
			Utils.logVerbose(" image not picked properly with result code : " + requestCode);
		}
	}

	@Override
	public void onDateSet(DatePickerDialog datePickerDialog, int year, int month, int day) {
		String resultStartDate = day + "/" + (month + 1) + "/" + year;
		isExaminationDate = false;
		if (!isExaminationDate) {
			if (isDOb) {
				editTextDob.setText(resultStartDate);
			}
			else {
				editTextFollowUpDate.setText(resultStartDate);
			}
		}
		else {
			editTextExaminationDate.setText(resultStartDate);

		}
	}

	private String getCurrentDate() {
		Calendar c = Calendar.getInstance();
		System.out.println("Current time => " + c.getTime());
		SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
		String formattedDate = df.format(c.getTime());
		return formattedDate;
	}


	private void showImagePIckDialog() {
		final DialogPlus dialog;
		dialog = DialogPlus.newDialog(AddPatientActivity.this)
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
				}
				else {
					//below Marshmallow
					takePicture();
				}

				//takePicture();
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

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		switch (requestCode) {
		case 200: {

			boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
			boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

			// If request is cancelled, the result arrays are empty.
			if (cameraAccepted
				&& storageAccepted) {
				// takePicture();

				takePicture();
				// permission was granted, yay! Do the
				// contacts-related task you need to do.

			}
			else {

				Toast.makeText(AddPatientActivity.this,
					" Please enable camera and storage permission from settings to proceed", Toast.LENGTH_SHORT).show();
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

	private class AddPatientsData extends AsyncTask<String, String, Boolean> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(String... f_url) {

			return addData(AddPatientActivity.this.getCurrentFocus());
		}

		@Override
		protected void onPostExecute(Boolean valid) {
			if (valid) {
				isInserted = true;
				editTextMrd.setFocusable(false);
				editTextMrd.setFocusableInTouchMode(false);
				editTextMrd.setClickable(false);
				if (isGalleryIcon) {
					showImagePIckDialog();
				}
				if (isSaveButtonPressed) {
					isSaveButtonPressed = false;
					finish();
					overridePendingTransition(R.anim.slide_in_down, R.anim.slide_out_down);
				}
			}
		}
	}

	private class LoadImagesInGrid extends AsyncTask<String, String, Void> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			patientImageList.clear();
		}

		@Override
		protected Void doInBackground(String... params) {
			loadImages();
			return null;
		}

		@Override
		protected void onPostExecute(Void aVoid) {
			super.onPostExecute(aVoid);

			if (patientImageList.size() == 1) {
				textViewImagesAttached.setText(patientImageList.size() + " image attached");
			}
			else {
				textViewImagesAttached.setText(patientImageList.size() + " image(s) attached");
			}

			gridView.setAdapter(new PatientImageAdapter(AddPatientActivity.this, patientImageList));
			Utils.setGridViewHeightBasedOnChildren(gridView);
		}
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
			new LoadImagesInGrid().execute();
		}
	}

	private void loadImages() {

		String[] projection = new String[] {
			PatientProvider.MRD_NO,
			PatientProvider.IMAGES,
			"id"
		};
		String selection = PatientProvider.MRD_NO + " LIKE " + "'" + editTextMrd.getText().toString().trim() + "'";
		imageCursor = getContentResolver().query(PatientProvider.CONTENT_URI1, projection, selection, null, null);

		Utils.logVerbose("cursor size : " + imageCursor.getColumnCount());

		if (imageCursor != null) {
			if (imageCursor.moveToFirst()) {
				do {
					Utils.logVerbose("PatientImages loaded : " + imageCursor
						.getString(imageCursor.getColumnIndex(PatientProvider.IMAGES)));
					PatientImages patientImages = new PatientImages();
					patientImages.setId(imageCursor.getInt(imageCursor.getColumnIndex("id")));
					patientImages.setMrd(imageCursor.getString(imageCursor.getColumnIndex("mrd")));
					patientImages.setImage(imageCursor.getString(imageCursor.getColumnIndex("image")));
					patientImageList.add(patientImages);
				} while (imageCursor.moveToNext());
			}
		}

	}


	private class UpdatePatientsData extends AsyncTask<String, String, Boolean> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(String... f_url) {
			return updateData(AddPatientActivity.this.getCurrentFocus());
		}

		@Override
		protected void onPostExecute(Boolean valid) {
			if (valid) {
				editTextMrd.setFocusable(false);
				editTextMrd.setFocusableInTouchMode(false);
				editTextMrd.setClickable(false);
				if (isGalleryIcon) {
					showImagePIckDialog();
				}
				if (isSaveButtonPressed) {
					isSaveButtonPressed = false;
					finish();
					overridePendingTransition(R.anim.slide_in_down, R.anim.slide_out_down);
				}
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

		if (radioButtonMale.isChecked()) {
			values.put(PatientProvider.SEX, "1");
		}
		else {
			values.put(PatientProvider.SEX, "0");
		}

		String[] projection1 = { String.valueOf(editTextMrd.getText().toString().trim()) };
		int count = getContentResolver().update(PatientProvider.CONTENT_URI, values, " mrd=? ", projection1);
		if (count > 0) {
			// Utils.snackToast("SuccessFully Updated  Patient Details", view);
			Utils.logVerbose("Successfully updated data count :" + count);

			return true;
		}
		else {
			Utils.snackToast("Failed To Update Patient Details.Need Unique MRD NO", view);
			return false;
		}

	}

}
