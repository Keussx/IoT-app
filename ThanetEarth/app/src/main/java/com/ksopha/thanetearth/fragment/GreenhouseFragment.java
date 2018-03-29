package com.ksopha.thanetearth.fragment;

import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import com.ksopha.thanetearth.date.Formatter;
import com.ksopha.thanetearth.ob.Sensor;
import com.ksopha.thanetearth.ob.SensorBasicData;
import com.ksopha.thanetearth.ob.SensorHistory;
import com.ksopha.thanetearth.weather.WeatherApiWorker;
import com.ksopha.thanetearth.weather.WeatherInfo;
import com.orm.query.Condition;
import com.orm.query.Select;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kelvin Sopha on 22/03/18.
 */

public class GreenhouseFragment extends Fragment {

    private WeatherApiWorker weatherApiWorker;
    private CardView weatherCard;
    private CardView [] cards;
    private LineChart [] charts;
    private Formatter formatter;
    private String [] types = {"temperature", "moisture", "tds", "light"};
    private String[] units = {"°C", "%", "ppm", "lx"};
    private int[] maxUnits = {100,100,1000,100000};
    private float[] suffixSize = {40f, 40f, 30f, 30f};


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // prevent fragment from being re-created when parent Activity is destroyed
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        formatter = new Formatter();

        cards = new CardView[4];
        charts = new LineChart[4];

        // Inflate the layout for this fragment
        View root =  inflater.inflate(R.layout.fragment_greenhouse, container, false);

        // save references to ui elements
        //weatherCard = root.findViewById(R.id.weather_today_card);
        cards[0] = root.findViewById(R.id.temp_card);
        cards[1] = root.findViewById(R.id.moisture_card);
        cards[2] = root.findViewById(R.id.tds_card);
        cards[3] = root.findViewById(R.id.light_card);

        charts[0] = setSectionLineChart("Temperature",  cards[0]);
        charts[1] = setSectionLineChart("Moisture",  cards[1]);
        charts[2] = setSectionLineChart("Total Dissolved Solids",  cards[2]);
        charts[3] = setSectionLineChart("Light",  cards[3]);

        //weatherApiWorker = new WeatherApiWorker(this);
        //weatherApiWorker.getTodaysWeather();

        updateCurrentGreenhouseData("gh1");;
        updateHistoryData("gh1");

        return root;
    }



    private LineChart setSectionLineChart(String sectionName,  View root){

       ((TextView)root.findViewById(R.id.section_title)).setText(sectionName);

        LineChart chart = (LineChart) root.findViewById(R.id.chart);
        chart.setDrawGridBackground(false);
        chart.getDescription().setEnabled(false);
        chart.setDrawBorders(false);

        chart.getAxisLeft().setEnabled(false);
        chart.getAxisRight().setDrawAxisLine(false);
        chart.getAxisRight().setDrawGridLines(false);
        chart.getXAxis().setDrawAxisLine(false);
        chart.getXAxis().setDrawGridLines(false);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.getLegend().setTextSize(13);
        chart.getXAxis().setTextSize(12f);
        chart.getAxisRight().setTextColor(Color.rgb(255,255,255));
        chart.getAxisRight().setTextSize(12f);
        chart.getXAxis().setTextColor(Color.rgb(255,255,255));
        chart.setExtraTopOffset(5f);
        chart.setXAxisRenderer(new CustomXAxisRenderer(chart.getViewPortHandler(),
                chart.getXAxis(), chart.getTransformer(YAxis.AxisDependency.LEFT)));

        return chart;
    }

    public void updateWeatherToday(WeatherInfo weatherInfo){
        if(weatherInfo != null){

            // update weather ui elements
            ((ImageView)weatherCard.findViewById(R.id.weather_icon)).setImageResource(weatherInfo.getIcon());
            ((TextView)weatherCard.findViewById(R.id.weather_temp)).setText(Math.round(weatherInfo.getTemp())+ "° C");
            ((TextView)weatherCard.findViewById(R.id.weather_humidity)).setText(weatherInfo.getHumidity()+"%");
            ((TextView)weatherCard.findViewById(R.id.weather_pressure)).setText(weatherInfo.getPressure()+" hPa");
        }
    }


    private String sensorValGetter(SensorBasicData data, int cardIndex){
        return (cardIndex == 0) ?
                 data.getTemperature()+"":
        (cardIndex == 1) ?
                 data.getMoisture()+"":
                (cardIndex == 2) ?
                        data.getTds()+"": data.getLight()+"";

    }

    private void updateUiBasicMeasures(int i, SensorBasicData[] data, List<Sensor> sensors){

        String val1 = sensorValGetter(data[0], i);
        String val2 = sensorValGetter(data[1], i);
        String val3 = sensorValGetter(data[2], i);
        String val4 = sensorValGetter(data[3], i);

        // update each sensor current temperature element
        ((TextView) cards[i].findViewById(R.id.sensor1_val))
                .setText(sensors.get(0).getSensorID() + ": " + val1 + " " + units[i]);
        ((TextView) cards[i].findViewById(R.id.sensor2_val))
                .setText(sensors.get(1).getSensorID() + ": "  + val2 + " " + units[i]);
        ((TextView) cards[i].findViewById(R.id.sensor3_val))
                .setText(sensors.get(2).getSensorID() + ": "  + val3 + " " + units[i]);
        ((TextView) cards[i].findViewById(R.id.sensor4_val))
                .setText(sensors.get(3).getSensorID() + ": "  + val4 + " " + units[i]);

        ArcProgress average = (ArcProgress)cards[i].findViewById(R.id.average);
        average.setSuffixText(units[i]);
        average.setSuffixTextSize(suffixSize[i]);
        average.setMax(maxUnits[i]);
        average.setProgress(Math.round((Float.parseFloat(val1) + Float.parseFloat(val2)
                + Float.parseFloat(val3) + Float.parseFloat(val4) )/4.00f));
    }

    public void updateCurrentGreenhouseData(String site){

        List<Sensor> sensors = Select.from(Sensor.class)
                .where(Condition.prop("site").eq(site)).list();

        boolean allValid = true;
        SensorBasicData  [] data = new SensorBasicData[sensors.size()];

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


    private void updateSingleChartHistory(int i, List<Sensor> sensors){

        if(sensors.size() == 4){



            List<SensorHistory> sensor1Data = Select.from(SensorHistory.class)
                    .where(Condition.prop("sensor").eq(sensors.get(0).getId()),
                            Condition.prop("type").eq(types[i])).list();

            List<SensorHistory> sensor2Data = Select.from(SensorHistory.class)
                    .where(Condition.prop("sensor").eq(sensors.get(1).getId()),
                            Condition.prop("type").eq(types[i])).list();

            List<SensorHistory>  sensor3Data = Select.from(SensorHistory.class)
                    .where(Condition.prop("sensor").eq(sensors.get(2).getId()),
                            Condition.prop("type").eq(types[i])).list();

            List<SensorHistory> sensor4Data = Select.from(SensorHistory.class)
                    .where(Condition.prop("sensor").eq(sensors.get(3).getId()),
                            Condition.prop("type").eq(types[i])).list();

            if(sensor1Data.size()>0 && sensor2Data.size()>0 && sensor3Data.size()>0 && sensor4Data.size()>0){

                String[] xAxisGroups = new String[sensor1Data.size()];

                List<ILineDataSet> dataSets = new ArrayList<>();

                int[] colors = {
                        Color.rgb(17, 167, 170),
                        Color.rgb(198, 81, 230),
                        Color.rgb(230, 202, 81),
                        Color.rgb(230, 76, 102)
                };

                // save update dates for sensors
                for(int h=0; h< xAxisGroups.length;h++){
                    xAxisGroups[h] = sensor1Data.get(h).getDate();
                }


                dataSets.add(toLineDataSet(sensors.get(0).getSensorID(), colors[0], sensor1Data));
                dataSets.add(toLineDataSet(sensors.get(1).getSensorID(), colors[1], sensor2Data));
                dataSets.add(toLineDataSet(sensors.get(2).getSensorID(), colors[2], sensor3Data));
                dataSets.add(toLineDataSet(sensors.get(3).getSensorID(), colors[3], sensor4Data));

                // setup legend
                Legend legend = charts[i].getLegend();
                legend.setTextSize(14f);
                legend.setXEntrySpace(25f);
                legend.setTextColor(Color.rgb(255, 255, 255));

                LineData data = new LineData(dataSets);

                XAxis xAxis = charts[i].getXAxis();
                xAxis.setGranularity(1.0f);
                xAxis.setValueFormatter(new DateXAxisLabelFormatter(xAxisGroups));

                charts[i].setData(data);
                charts[i].setVisibleXRangeMaximum(2.135f);
                charts[i].invalidate();

            }

        }
    }

    public void updateHistoryData(String site){
        List<Sensor> sensors = Select.from(Sensor.class)
                .where(Condition.prop("site").eq(site)).list();

        updateSingleChartHistory(0, sensors);

    }


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

            if(line.length==1)
                line = new String[2];line[0]="";line[1]="";

            Utils.drawXAxisValue(c, line[0], x+35, y, mAxisLabelPaint, anchor, angleDegrees);
            Utils.drawXAxisValue(c, line[1], x + mAxisLabelPaint.getTextSize()+2,
                    y + mAxisLabelPaint.getTextSize()+2, mAxisLabelPaint, anchor, angleDegrees);
        }
    }


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

}
