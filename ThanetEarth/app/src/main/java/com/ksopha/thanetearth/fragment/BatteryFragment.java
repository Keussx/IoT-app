package com.ksopha.thanetearth.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.ksopha.thanetearth.R;
import com.ksopha.thanetearth.ormObject.Sensor;
import com.ksopha.thanetearth.ormObject.SensorBasicData;
import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;

/**
 * Created by Kelvin Sopha on 22/03/18.
 */

public class BatteryFragment extends Fragment {

    private HorizontalBarChart[] charts;
    private TextView[] unavailables;
    private int [] colors;
    private Realm realm;


    /**
     * called at creation of fragment
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // prevent fragment from being re-created when parent Activity is destroyed
        setRetainInstance(true);

    }



    /**
     * called to instantiate fragment ui
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        realm = Realm.getDefaultInstance();

        colors = new int[4];
        colors[0] = Color.rgb(50,196,91);
        colors[1] = Color.rgb(230, 202, 81);
        colors[2] = Color.rgb(229,24,26);

        // Inflate the layout for this fragment
        View root =  inflater.inflate(R.layout.fragment_battery, container, false);

        // save references to ui elements
        setBarCharts(root);
        unavailables = new TextView[4];
        unavailables[0] = root.findViewById(R.id.chart_unavailable1);
        unavailables[1] = root.findViewById(R.id.chart_unavailable2);
        unavailables[2] = root.findViewById(R.id.chart_unavailable3);
        unavailables[3] = root.findViewById(R.id.chart_unavailable4);

        return root;
    }


    /**
     * set the configs for a chart
     * @param chart chart to setup
     */
    private void setBarChartConfig(HorizontalBarChart chart){

        chart.setDrawGridBackground(false);
        chart.getDescription().setEnabled(false);
        chart.setDrawBorders(false);
        chart.getAxisLeft().setEnabled(false);
        chart.getAxisRight().setDrawAxisLine(true);
        chart.getAxisRight().setDrawGridLines(true);
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chart.getXAxis().setDrawAxisLine(true);
        chart.getXAxis().setDrawGridLines(false);
        chart.setDrawBarShadow(false);
        chart.getXAxis().setGridColor(Color.rgb(82,92,104));
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.getXAxis().setTextSize(14f);
        chart.getAxisRight().setTextColor(Color.rgb(255,255,255));
        chart.getAxisRight().setTextSize(12f);
        chart.getXAxis().setTextColor(Color.rgb(255,255,255));
        chart.setExtraTopOffset(5f);
        chart.getLegend().setEnabled(false);

    }

    /**
     * creates and setup the bar charts
     * @param root view containing charts
     */
    private void setBarCharts(View root){

        charts = new HorizontalBarChart[4];
        charts[0] = root.findViewById(R.id.battery_chart_1);
        charts[1] = root.findViewById(R.id.battery_chart_2);
        charts[2] = root.findViewById(R.id.battery_chart_3);
        charts[3] = root.findViewById(R.id.battery_chart_4);

        setBarChartConfig(charts[0]);
        setBarChartConfig(charts[1]);
        setBarChartConfig(charts[2]);
        setBarChartConfig(charts[3]);

    }



    /**
     * update the battery data for a chart
     * @param index  index of card
     * @param data  sensor data
     */
    public void updateSingleChart(int index, List<SensorBasicData> data){

        index = index-1;

        charts[index].setVisibility(View.VISIBLE);
        unavailables[index].setVisibility(View.GONE);


        String xAxisNames[] = new String[data.size()];

        int barStateColors [] = new int[data.size()];

        List<BarEntry> entries = new ArrayList<>();

        for(int i=0;i<data.size();i++){

            int batteryPercent = data.get(i).getBatteryLevel();

            barStateColors[i] = (batteryPercent >=50) ? colors[0] :
                    (batteryPercent >15) ? colors[1] : colors[2];

            String name = data.get(i).getSensor().getName();

            xAxisNames[i] = name.substring(0, 1).toUpperCase() + name.substring(1) +" sensor";

            entries.add(new BarEntry(i * 1f, batteryPercent *1f ));
        }

        charts[index].getXAxis().setValueFormatter(new LabelFormatter(xAxisNames));

        // set the base data set
        BarDataSet set = new BarDataSet(entries, "sensors");
        set.setColors(barStateColors);
        set.setDrawValues(false);

        // configure the maximum and minimum values for battery levels
        YAxis leftAxis = charts[index].getAxisLeft();
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisMaximum(100f);

        YAxis rightAxis = charts[index].getAxisRight();
        rightAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        rightAxis.setAxisMinimum(0f);
        rightAxis.setAxisMaximum(100f);
        rightAxis.setValueFormatter(new PercentFormatter());
        rightAxis.setTextSize(13f);

        // set granularity
        charts[index].getXAxis().setGranularity(1f);
        charts[index].getXAxis().setGranularityEnabled(true);

        // add data to the chart and refresh
        BarData barData = new BarData(set);
        barData.setBarWidth(0.22f);
        charts[index].setData(barData);
        charts[index].invalidate();
    }




    /**
     * start update of battery measurements
     */
    public void updateUiBatteryMeasures(){

        List<SensorBasicData> data = realm.where(SensorBasicData.class).findAll();

        List<SensorBasicData> site1 = new ArrayList<>();
        List<SensorBasicData> site2 = new ArrayList<>();
        List<SensorBasicData> site3 = new ArrayList<>();
        List<SensorBasicData> site4 = new ArrayList<>();

        for(SensorBasicData basicData: data){

            if(basicData == null )
                return;

            Sensor sensor = basicData.getSensor();

            if(sensor == null )
                return;

            if(sensor.getSite().equals("gh1"))
                site1.add(basicData);
            else if(sensor.getSite().equals("gh2"))
                site2.add(basicData);
            else if(sensor.getSite().equals("gh3"))
                site3.add(basicData);
            else if(sensor.getSite().equals("outside"))
                site4.add(basicData);

        }

        // if there is data available that was stored
        if(site1.size() == 4){
            updateSingleChart(1, site1);
        }
        // if there are no data available, hide graph, show message
        else{
            charts[0].setVisibility(View.GONE);
            unavailables[0].setVisibility(View.VISIBLE);
        }

        // if there is data available that was stored
        if(site2.size() == 4){
            updateSingleChart(2, site2);
        }
        // if there are no data available, hide graph, show message
        else{
            charts[1].setVisibility(View.GONE);
            unavailables[1].setVisibility(View.VISIBLE);
        }

        // if there is data available that was stored
        if(site3.size() == 4){
            updateSingleChart(3, site3);
        }
        // if there are no data available, hide graph, show message
        else{
            charts[2].setVisibility(View.GONE);
            unavailables[2].setVisibility(View.VISIBLE);
        }

        // if there is data available that was stored
        if(site4.size() == 1){
            updateSingleChart(4, site4);
        }
        // if there are no data available, hide graph, show message
        else{
            charts[3].setVisibility(View.GONE);
            unavailables[3].setVisibility(View.VISIBLE);
        }


    }



    /**
     * Class for creating custom X axis value formatter
     */
    public class LabelFormatter implements IAxisValueFormatter {

        String [] labels;

        public LabelFormatter(String[] labels){
            this.labels = labels;
        }

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            int index = (int)value;
            if(index>=0 && index<labels.length){
                return  labels[index];
            }
            return "";
        }
    }

    /**
     * Class for creating custom percent value formatter
     */
    public class PercentFormatter implements IAxisValueFormatter {

        @Override
        public String getFormattedValue(float value, AxisBase axis) {

            return (int)value+" % ";
        }
    }



    /**
     * called when fragment id made visible to user
     */
    public void onResume(){
        super.onResume();

        updateUiBatteryMeasures();
    }

}
