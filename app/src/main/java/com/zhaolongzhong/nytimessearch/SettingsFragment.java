package com.zhaolongzhong.nytimessearch;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;

import com.zhaolongzhong.nytimessearch.helpers.DatePickerFragment;
import com.zhaolongzhong.nytimessearch.service.SortOrder;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingsFragment extends DialogFragment {
    private static final String TAG = SettingsFragment.class.getSimpleName();

    // Preference key
    public static final String BEGIN_DATE = "beginDate";
    public static final String SORT_ORDER = "sortOrder";
    public static final String NEWS_DESK_VALUES_ARTS = "arts";
    public static final String NEWS_DESK_VALUES_FASHION = "fashion";
    public static final String NEWS_DESK_VALUES_SPORTS = "sports";

    private SortOrder sortOrder;
    private SettingsFragmentCallback settingsFragmentCallback;

    @BindView(R.id.settings_fragment_begin_date_edit_text_id) TextView beginDateTextView;
    @BindView(R.id.settings_fragment_spinner_id) Spinner sortOrderSpinner;
    @BindView(R.id.settings_fragment_arts_check_box_id) CheckBox artsCheckBox;
    @BindView(R.id.settings_fragment_fashion_check_box_id) CheckBox fashionCheckBox;
    @BindView(R.id.settings_fragment_sports_check_box_id) CheckBox sportsCheckBox;
    @BindView(R.id.settings_fragment_save_button_id) Button saveButton;

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.settings_fragment, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        beginDateTextView.setOnClickListener(beginDateOnClickListener);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.sort_order_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortOrderSpinner.setAdapter(adapter);
        sortOrderSpinner.setOnItemSelectedListener(onItemSelectedListener);
        saveButton.setOnClickListener(v -> saveSearchPreferences());
        setUpViews();
    }

    private void setUpViews() {
        SharedPreferences sharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        String beginDate = sharedPreferences.getString(SettingsFragment.BEGIN_DATE, "");
        beginDateTextView.setText(beginDate);
        String sortOrderString = sharedPreferences.getString(SettingsFragment.SORT_ORDER, "");
        SortOrder sortOrder = SortOrder.instanceFromName(sortOrderString);
        sortOrderSpinner.setSelection(sortOrder.getId());

        String arts = sharedPreferences.getString(SettingsFragment.NEWS_DESK_VALUES_ARTS, "");
        artsCheckBox.setChecked(!arts.isEmpty());

        String fashion = sharedPreferences.getString(SettingsFragment.NEWS_DESK_VALUES_FASHION, "");
        fashionCheckBox.setChecked(!fashion.isEmpty());

        String sports = sharedPreferences.getString(SettingsFragment.NEWS_DESK_VALUES_SPORTS, "");
        sportsCheckBox.setChecked(!sports.isEmpty());
    }

    private void saveSearchPreferences() {
        SharedPreferences sharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(BEGIN_DATE, String.valueOf(beginDateTextView.getText()));
        editor.putString(SORT_ORDER, sortOrder.getName());
        editor.putString(NEWS_DESK_VALUES_ARTS, artsCheckBox.isChecked() ? "Arts" : "");
        editor.putString(NEWS_DESK_VALUES_FASHION, fashionCheckBox.isChecked() ? "Fashion & Style" : "");
        editor.putString(NEWS_DESK_VALUES_SPORTS, sportsCheckBox.isChecked() ? "Sports" : "");
        editor.apply();

        settingsFragmentCallback.onDialogDismiss();
        getDialog().dismiss();
    }

    private View.OnClickListener beginDateOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            DatePickerFragment datePickerFragment = new DatePickerFragment();
            datePickerFragment.setDatePickerCallback(datePickerCallback);
            datePickerFragment.show(getActivity().getFragmentManager(), DatePickerFragment.class.getSimpleName());
        }
    };

    private DatePickerFragment.DatePickerCallback datePickerCallback = new DatePickerFragment.DatePickerCallback() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int day) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, day);

            DateFormat format = new SimpleDateFormat(getString(R.string.begin_date_format), Locale.ENGLISH);
            String beginDate = format.format(calendar.getTime());
            beginDateTextView.setText(beginDate);
        }
    };

    private AdapterView.OnItemSelectedListener onItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            sortOrder = SortOrder.instanceFromId(position);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    public void setCallback(SettingsFragmentCallback settingsFragmentCallback) {
        this.settingsFragmentCallback = settingsFragmentCallback;
    }

    public interface SettingsFragmentCallback {
        void onDialogDismiss();
    }
}