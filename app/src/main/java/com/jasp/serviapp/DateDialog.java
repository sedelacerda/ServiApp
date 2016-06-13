package com.jasp.serviapp;


import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;

import java.util.Calendar;

/**
 * Created by Seba on 6/8/2016.
 */
public class DateDialog extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    AutoCompleteTextView dateView;

    public DateDialog(View view){
        dateView = (AutoCompleteTextView) view;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState){
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    public void onDateSet(DatePicker view, int year, int month, int day){
        String sday = day+"";
        String smonth = (month+1) + "";

        if(sday.length()<2)
            sday = "0" + sday;
        if(smonth.length()<2)
            smonth = "0" + smonth;

        String date = sday + "/" + smonth + "/" + year;
        dateView.setText(date);
    }
}
