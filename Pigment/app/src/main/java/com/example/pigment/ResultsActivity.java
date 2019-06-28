package com.example.pigment;


import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.content.Intent;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class ResultsActivity extends Fragment {
    private ArrayList<String> answers;
    private ArrayList<String> normal;
    private ArrayList<String> protanopia;
    private ArrayList<String> deuteranopia;

    private int normalScore = 0;
    private int protanopiaScore = 0;
    private int deuteranopiaScore = 0;

    private String diag = "";
    private String desc = "";

    private BarChart chart;
    private TextView diagnosis;
    private TextView description;
    private Button retake;
    private Button save;

    public static ResultsActivity newInstance() {
        ResultsActivity fragment = new ResultsActivity();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.activity_results, container, false);

        answers = getArguments().getStringArrayList("answers");
        normal = getArguments().getStringArrayList("normal");
        protanopia = getArguments().getStringArrayList("protanopia");
        deuteranopia = getArguments().getStringArrayList("deuteranopia");

        //plug-in UI elements
        chart = (BarChart) view.findViewById(R.id.chart);
        diagnosis = view.findViewById(R.id.diagnosis);
        description = view.findViewById(R.id.description);
        retake = view.findViewById(R.id.button_retake);
        save = view.findViewById(R.id.button_save);

        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(getActivity());
        desc = sharedPreferences.getString("desc", null);
        if (answers.size() != 0) {
            calculate();
        } else {
            if (desc != null && !desc.isEmpty()) {
                desc = sharedPreferences.getString("desc", null);
                diag = sharedPreferences.getString("diag", null);
                setText(diag, desc);
                normalScore = sharedPreferences.getInt("normalScore", 0);
                protanopiaScore = sharedPreferences.getInt("protanopiaScore", 0);
                deuteranopiaScore = sharedPreferences.getInt("deuteranopiaScore", 0);
            }
        }

        BarData data = new BarData(getDataSet());
        Description label = new Description();
        label.setText("Percent Indicators");
        XAxis xAxis = chart.getXAxis();
        xAxis.setGranularity(1f);
        ValueFormatter custom = new MyValueFormatter("%");
        YAxis leftAxis = chart.getAxisLeft();
        YAxis rightAxis = chart.getAxisRight();
        leftAxis.setValueFormatter(custom);
        leftAxis.setDrawGridLines(false);
        leftAxis.setAxisMinimum(0f);
        rightAxis.setDrawGridLines(false);
        rightAxis.setValueFormatter(custom);
        rightAxis.setAxisMinimum(0f);
        xAxis.setDrawGridLines(false);
        chart.setDrawGridBackground(false);
        chart.setDescription(label);
        chart.setData(data);
        chart.animateXY(2000, 2000);
        chart.invalidate();

        setRetake();
        setSave();

        return view;
    }

    private void setRetake() {
        retake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // start diagnostic activity
                Intent main = new Intent(getActivity(), DiagnosticActivity.class);
                startActivity(main);
            }
        });
    }

    private void setSave() {
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor sharedPreferencesEditor =
                        PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
                sharedPreferencesEditor.putString("desc", desc);
                sharedPreferencesEditor.putString("diag", diag);
                sharedPreferencesEditor.putInt("normalScore", normalScore);
                sharedPreferencesEditor.putInt("protanopiaScore", protanopiaScore);
                sharedPreferencesEditor.putInt("deuteranopiaScore", deuteranopiaScore);
                sharedPreferencesEditor.apply();
                // start main camera activity
                MainActivity.viewPager.setCurrentItem(1);
            }
        });
    }

    private void calculate() {
        int max = 0;
        for (int i = 0; i < answers.size(); i++) {
            String a = answers.get(i);
            String n = normal.get(i);
            String p = protanopia.get(i);
            String d = deuteranopia.get(i);
            if (a.equals(n)) {
                normalScore += 1;
                if (normalScore > max) {
                    max = normalScore;
                    diag = "Normal Vision";
                    desc = "You have normal color vision.";
                }
            }
            if (a.equals(p)) {
                protanopiaScore += 1;
                if (protanopiaScore > max) {
                    max = protanopiaScore;
                    diag = "Protanopia";
                    desc = getResources().getString(R.string.protanopia_description);
                }
            }
            if (a.equals(d)){
                deuteranopiaScore += 1;
                if (deuteranopiaScore > max) {
                    max = deuteranopiaScore;
                    diag = "Deuteranopia";
                    desc = getResources().getString(R.string.deuteranopia_description);
                }
            }
        }
        normalScore  = (int) ((new Double(normalScore)/answers.size()) * 100);
        protanopiaScore  = (int) ((new Double(protanopiaScore)/answers.size()) * 100);
        deuteranopiaScore  = (int) ((new Double(deuteranopiaScore)/answers.size()) * 100);
        setText(diag, desc);
    }

    private void setText(String diag, String desc) {
        diagnosis.setText(diag);
        description.setText(desc);
    }

    private List<IBarDataSet> getDataSet() {
        ArrayList<IBarDataSet> dataSets = new ArrayList<>();

        ArrayList general = new ArrayList();
        ArrayList normal = new ArrayList();
        ArrayList protanopia = new ArrayList();
        ArrayList protanomaly = new ArrayList();
        ArrayList deuteranopia = new ArrayList();
        ArrayList deuteranomaly = new ArrayList();

        BarEntry v1e1 = new BarEntry(1, 0); // General red-green colorblindness
        general.add(v1e1);
        BarEntry v1e2 = new BarEntry(2, normalScore); // normal color vision
        normal.add(v1e2);
        BarEntry v1e3 = new BarEntry(3, protanopiaScore); // protanopia
        protanopia.add(v1e3);
        BarEntry v1e4 = new BarEntry(4, 3); // protanomaly
        protanomaly.add(v1e4);
        BarEntry v1e5 = new BarEntry(4, deuteranopiaScore); // deuteranopia
        deuteranopia.add(v1e5);
        BarEntry v1e6 = new BarEntry(6, 5); // deuteranomaly
        deuteranomaly.add(v1e6);

        BarDataSet generalSet = new BarDataSet(general, "General Red-Green Colorblindness");
        generalSet.setColor(Color.BLUE);
        BarDataSet normalSet = new BarDataSet(normal, "Normal Color Vision");
        normalSet.setColor(Color.CYAN);
        BarDataSet protanopiaSet = new BarDataSet(protanopia, "Protanopia");
        protanopiaSet.setColor(Color.BLUE);
        BarDataSet deuteranopiaSet = new BarDataSet(deuteranopia, "Deuteranopia");
        deuteranopiaSet.setColor(Color.CYAN);


//        dataSets.add(generalSet);
        dataSets.add(normalSet);
        dataSets.add(protanopiaSet);
        dataSets.add(deuteranopiaSet);

        return dataSets;
    }

    private class MyValueFormatter extends ValueFormatter
    {

        private final DecimalFormat mFormat;
        private String suffix;

        public MyValueFormatter(String suffix) {
            mFormat = new DecimalFormat("###,###,###,##0");
            this.suffix = suffix;
        }

        @Override
        public String getFormattedValue(float value) {
            return mFormat.format(value) + suffix;
        }

        @Override
        public String getAxisLabel(float value, AxisBase axis) {
            if (axis instanceof XAxis) {
                return mFormat.format(value);
            } else if (value > 0) {
                return mFormat.format(value) + suffix;
            } else {
                return mFormat.format(value);
            }
        }
    }

}
