package com.ksopha.thanetearth.fragment;

import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.github.lzyzsd.circleprogress.ArcProgress;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.LargeValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.renderer.XAxisRenderer;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.Transformer;
import com.github.mikephil.charting.utils.Utils;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.ksopha.thanetearth.R;
import com.ksopha.thanetearth.ormObject.Sensor;
import com.ksopha.thanetearth.ormObject.SensorBasicData;
import com.ksopha.thanetearth.ormObject.SensorHistory;
import com.ksopha.thanetearth.service.BackgroundWorker;
import com.orm.query.Condition;
import com.orm.query.Select;
import java.util.ArrayList;
import java.util.List;

/**
 * Class representing a fragment for a Site with 4 sensors
 * Created by Kelvin Sopha on 22/03/18.
 */

public class GreenhouseFragment extends Fragment {

    private CardView [] cards;
    private ArcProgress[] averageArc;
    private LineChart [] charts;
    private String [] types = {"temperature", "moisture", "tds", "light"};
    private String[] units = {"°C", "%", "ppm", "lx"};
    private List<String> sites = new ArrayList<>();
    private int[] maxUnits = {100,100,1000,100000};
    private float[] suffixSize = {40f, 40f, 30f, 30f};
    private int ids[] = {R.id.average_temp, R.id.average_moisture, R.id.average_tds, R.id.average_light};
    private TextView[] unavailableViews;
    private String fragmentID;


    /**
     * Create new instance of fragment
     * @param siteID a site the fragment will display data for
     * @return instance
     */
    public static GreenhouseFragment newInstance(String siteID) {
        GreenhouseFragment instance = new GreenhouseFragment();

        Bundle args = new Bundle();
        args.putString("fragmentID", siteID);
        instance.setArguments(args);

        return instance;
    }


    /**
     * called at creation of fragment
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // prevent fragment from being re-created when parent Activity is destroyed
        setRetainInstance(true);

        fragmentID = getArguments().getString("fragmentID", "");
    }


    /**
     * called to instantiate fragment ui
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        sites.add("gh1");
        sites.add("gh2");
        sites.add("gh3");
        sites.add("outside");

        cards = new CardView[4];
        averageArc = new ArcProgress[4];
        charts = new LineChart[4];

        // Inflate the layout for this fragment
        View root =  inflater.inflate(R.layout.fragment_greenhouse, container, false);

        // save references to ui elements
        cards[0] = root.findViewById(R.id.temp_card);
        cards[1] = root.findViewById(R.id.moisture_card);
        cards[2] = root.findViewById(R.id.tds_card);
        cards[3] = root.findViewById(R.id.light_card);


        for(int i=0;i<cards.length;i++){
            averageArc[i] = cards[i].findViewById(ids[i]);
            averageArc[i].setSuffixText(units[i]);
            averageArc[i].setSuffixTextSize(suffixSize[i]);
            averageArc[i].setMax(maxUnits[i]);
        }

        unavailableViews = new TextView[4];
        unavailableViews[0] = cards[0].findViewById(R.id.chart_unavailable1);
        unavailableViews[1] = cards[1].findViewById(R.id.chart_unavailable2);
        unavailableViews[2] = cards[2].findViewById(R.id.chart_unavailable3);
        unavailableViews[3] = cards[3].findViewById(R.id.chart_unavailable4);


        charts[0] = setSectionLineChart("Temperature",  cards[0]);
        charts[1] = setSectionLineChart("Soil Moisture",  cards[1]);
        charts[2] = setSectionLineChart("TDS(Total Dissolved Solids)",  cards[2]);
        charts[3] = setSectionLineChart("LUX(Light Intensity)",  cards[3]);

        return root;
    }



    /**
     * Set section heading and create a LineChart
     * @param sectionName name of section
     * @param root  a view to find chart
     * @return chart instance
     */
    private LineChart setSectionLineChart(String sectionName,  View root){

       ((TextView)root.findViewById(R.id.section_title)).setText(sectionName);

        LineChart chart = (LineChart) root.findViewById(R.id.chart);
        chart.setDrawGridBackground(false);
        chart.getDescription().setEnabled(false);
        chart.setDrawBorders(false);

        chart.getAxisLeft().setEnabled(false);
        chart.getAxisRight().setDrawAxisLine(false);
        chart.getAxisRight().setDrawGridLines(false);
        chart.getXAxis().setDrawAxisLine(true);
        chart.getXAxis().setDrawGridLines(true);
        chart.getXAxis().setGridColor(Color.rgb(82,92,104));
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.getLegend().setTextSize(13f);
        chart.getXAxis().setTextSize(12f);
        chart.getAxisRight().setTextColor(Color.rgb(255,255,255));
        chart.getAxisRight().setTextSize(12f);
        chart.getXAxis().setTextColor(Color.rgb(255,255,255));
        chart.setExtraTopOffset(5f);
        chart.setXAxisRenderer(new CustomXAxisRenderer(chart.getViewPortHandler(),
                chart.getXAxis(), chart.getTransformer(YAxis.AxisDependency.LEFT)));

        return chart;
    }



    /**
     * Returns a measurement value based on card type
     * @param data sensor data
     * @param cardIndex  index of the card
     * @return measurement value as string
     */
    private String sensorValGetter(SensorBasicData data, int cardIndex){
        return (cardIndex == 0) ?
                 data.getTemperature()+"":
        (cardIndex == 1) ?
                 data.getMoisture()+"":
                (cardIndex == 2) ?
                        data.getTds()+"": data.getLight()+"";

    }



    /**
     * update ui with measurement data
     * @param data sensor data
     * @param sensors  list of sensors
     */
    private void updateUiBasicMeasures(int i, SensorBasicData[] data, List<Sensor> sensors){

        if(data != null && data.length>0 && sensors!=null) {

            String val1 = sensorValGetter(data[0], i);
            String val2 = sensorValGetter(data[1], i);
            String val3 = sensorValGetter(data[2], i);
            String val4 = sensorValGetter(data[3], i);

            // update each sensor current temperature element
            ((TextView) cards[i].findViewById(R.id.sensor1_val))
                    .setText(sensors.get(0).getName() + ": " + val1 + " " + units[i]);
            ((TextView) cards[i].findViewById(R.id.sensor2_val))
                    .setText(sensors.get(1).getName() + ": " + val2 + " " + units[i]);
            ((TextView) cards[i].findViewById(R.id.sensor3_val))
                    .setText(sensors.get(2).getName() + ": " + val3 + " " + units[i]);
            ((TextView) cards[i].findViewById(R.id.sensor4_val))
                    .setText(sensors.get(3).getName() + ": " + val4 + " " + units[i]);


            averageArc[i].setSuffixText(units[i]);
            averageArc[i].setSuffixTextSize(suffixSize[i]);
            averageArc[i].setMax(maxUnits[i]);
            averageArc[i].setProgress(Math.round((Float.parseFloat(val1) + Float.parseFloat(val2)
                    + Float.parseFloat(val3) + Float.parseFloat(val4)) / 4.00f));
        }
    }



    /**
     * start update of current measurements
     */
    public void updateCurrentGreenhouseData(){

        List<Sensor> sensors = Select.from(Sensor.class)
                .where(Condition.prop("site").eq(fragmentID)).list();


        boolean allValid = true;
        SensorBasicData [] data = new SensorBasicData[sensors.size()];

        for(int i=0; i < data.length;i++){
            SensorBasicData s = Select.from(SensorBasicData.class)
                    .where(Condition.prop("sensor").eq(sensors.get(i).getId())).first();

            data[i] = s;

            if(s == null)
                allValid = false;
        }

        if(allValid){
            updateUiBasicMeasures(0, data, sensors);
            updateUiBasicMeasures(1, data, sensors);
            updateUiBasicMeasures(2, data, sensors);
            updateUiBasicMeasures(3, data, sensors);
        }
    }


    /**
     * update the history for a chart
     * @param i  index of card
     * @param sensors slist of sensors
     */
    private void updateSingleChartHistory(int i, List<Sensor> sensors){

        if(sensors.size() == 4) {

            List<SensorHistory> sensor1Data = Select.from(SensorHistory.class)
                    .where(Condition.prop("sensor").eq(sensors.get(0).getId()),
                            Condition.prop("type").eq(types[i])).orderBy("Id desc").list();

            List<SensorHistory> sensor2Data = Select.from(SensorHistory.class)
                    .where(Condition.prop("sensor").eq(sensors.get(1).getId()),
                            Condition.prop("type").eq(types[i])).orderBy("Id desc").list();

            List<SensorHistory> sensor3Data = Select.from(SensorHistory.class)
                    .where(Condition.prop("sensor").eq(sensors.get(2).getId()),
                            Condition.prop("type").eq(types[i])).orderBy("Id desc").list();

            List<SensorHistory> sensor4Data = Select.from(SensorHistory.class)
                    .where(Condition.prop("sensor").eq(sensors.get(3).getId()),
                            Condition.prop("type").eq(types[i])).orderBy("Id desc").list();

            // if there is data available that was stored
            if (sensor1Data.size() > 0 && sensor2Data.size() > 0 && sensor3Data.size() > 0 && sensor4Data.size() > 0) {

                charts[i].setVisibility(View.VISIBLE);
                unavailableViews[i].setVisibility(View.GONE);

                String[] xAxisGroups = new String[sensor1Data.size()];

                List<ILineDataSet> dataSets = new ArrayList<>();

                int[] colors = {
                        Color.rgb(17, 167, 170),
                        Color.rgb(198, 81, 230),
                        Color.rgb(230, 202, 81),
                        Color.rgb(230, 76, 102)
                };

                // save update dates for sensors
                for (int h = 0; h < xAxisGroups.length; h++) {
                    xAxisGroups[h] = sensor1Data.get(h).getDate();
                }


                dataSets.add(toLineDataSet(sensors.get(0).getName(), colors[0], sensor1Data));
                dataSets.add(toLineDataSet(sensors.get(1).getName(), colors[1], sensor2Data));
                dataSets.add(toLineDataSet(sensors.get(2).getName(), colors[2], sensor3Data));
                dataSets.add(toLineDataSet(sensors.get(3).getName(), colors[3], sensor4Data));

                // setup legend
                Legend legend = charts[i].getLegend();
                legend.setTextSize(14f);
                legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
                legend.setXEntrySpace(25f);
                legend.setTextColor(Color.rgb(255, 255, 255));

                LineData data = new LineData(dataSets);

                XAxis xAxis = charts[i].getXAxis();
                xAxis.setGranularity(1.0f);
                xAxis.setValueFormatter(new DateXAxisLabelFormatter(xAxisGroups));

                charts[i].setData(data);
                charts[i].setVisibleXRangeMaximum(2.435f);
                charts[i].invalidate();

            }


            // if there are no data available, hide graph, show message
            else if(!BackgroundWorker.updatedSiteHistory[sites.indexOf(fragmentID)]) {

                charts[i].setVisibility(View.GONE);
                unavailableViews[i].setVisibility(View.VISIBLE);

            }
            else{
                charts[i].setVisibility(View.GONE);
                unavailableViews[i].setVisibility(View.VISIBLE);

                unavailableViews[i].setText(R.string.no_history);

            }
        }
    }



    /**
     * start update of history measurements
     */
    public void updateHistoryData(){
        List<Sensor> sensors = Select.from(Sensor.class)
                .where(Condition.prop("site").eq(fragmentID)).list();

        updateSingleChartHistory(0, sensors);
        updateSingleChartHistory(1, sensors);
        updateSingleChartHistory(2, sensors);
        updateSingleChartHistory(3, sensors);

    }




    /**
     * Class for creating custom X axis value formatter
     */
    public class DateXAxisLabelFormatter implements IAxisValueFormatter{

        String [] labels;

        public DateXAxisLabelFormatter(String[] labels){
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
     * MPAndroid charts allow multiline X axis labels
     * reference: https://stackoverflow.com/questions/32509174/in-mpandroidchart-library-how-to-wrap-x-axis-labels-to-two-lines-when-long
     */
    public class CustomXAxisRenderer extends XAxisRenderer {
        public CustomXAxisRenderer(ViewPortHandler viewPortHandler, XAxis xAxis, Transformer trans) {
            super(viewPortHandler, xAxis, trans);
        }

        @Override
        protected void drawLabel(Canvas c, String formattedLabel, float x, float y, MPPointF anchor, float angleDegrees) {
            String line[] = formattedLabel.split("--");

            if(line[1]==null)
                line[0] = line[1] = "";

            Utils.drawXAxisValue(c, line[0], x+35, y, mAxisLabelPaint, anchor, angleDegrees);
            Utils.drawXAxisValue(c, line[1], x + mAxisLabelPaint.getTextSize()+2,
                    y + mAxisLabelPaint.getTextSize()+2, mAxisLabelPaint, anchor, angleDegrees);
        }
    }


    /**
     * Converts a list of measurement history for a sensor to  a LineDataSet
     * @param deviceName name of sensor
     * @param color color of line
     * @param history list of history
     * @return LineDataSet instance
     */
    private  LineDataSet toLineDataSet(String deviceName, int color, List<SensorHistory> history){

        List<Entry> entries = new ArrayList<Entry>();

        int i = 0;

        for(SensorHistory h: history){
            entries.add(new Entry(i, h.getValue()));
            i++;
        }

        // add the entry to groups
        LineDataSet set = new LineDataSet(entries, deviceName);
        set.setLineWidth(2f);
        set.setDrawValues(false);
        set.setDrawCircles(false);
        set.setCircleRadius(4f);
        set.setColor(color);
        set.setValueFormatter(new LargeValueFormatter());

        return  set;
    }


    /**
     * called when fragment id made visible to user
     */
    public void onResume(){
        super.onResume();

        updateCurrentGreenhouseData();;
        updateHistoryData();
    }

}
