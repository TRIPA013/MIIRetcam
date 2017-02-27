package com.kalps.patientprofile.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TableRow;
import android.widget.TextView;

import com.kalps.patientprofile.provider.Patients;

import java.util.List;

import com.kalps.patientprofile.R;
import com.kalps.patientprofile.utils.Utils;


public class PatientsAdapter extends ArrayAdapter<Patients> {

    private List<Patients> ridesItemLists;
    private Activity _context = null;
    private static LayoutInflater _inflater = null;
    Patients ridesItem;


    public PatientsAdapter(Activity context, List<Patients> lst) {
        super(context, 0, lst);
        this._context = context;
        this.ridesItemLists = lst;
        _inflater = this._context.getLayoutInflater();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (convertView == null) {
            view = _inflater.inflate(R.layout.patients_row, null);
        }

        ridesItem = ridesItemLists.get(position);

        final TextView name = (TextView) view.findViewById(R.id.textViewName);
        final TextView mrdNo = (TextView) view.findViewById(R.id.textViewMrdNo);
        final TextView refernce = (TextView) view.findViewById(R.id.textViewReference);
        final TextView hospital = (TextView) view.findViewById(R.id.textViewHospital);
        final TableRow tableRow = (TableRow) view.findViewById(R.id.tableRow);
        if (position % 2 == 0)
            tableRow.setBackgroundColor(_context.getResources().getColor(R.color.white));
        else
            tableRow.setBackgroundColor(_context.getResources().getColor(R.color.ash_opacity_30));
        name.setText(ridesItem.getName());
        mrdNo.setText(ridesItem.getMrdNo() + "");
        refernce.setText(ridesItem.getRefPhysician());
        hospital.setText(ridesItem.getHospital());
        return view;
    }


}
