package com.statbike.valtan.statbike;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "MAPS";
    double[] tmp;

    double vmax =0;

    String input;
    String[] tmps;

    double tmpspeed = 0;

    int riga=0;
    boolean EOF=false;

    private String LogName;
    private int NumOfLogs=0; // I use it! AS is stupid!
    private String LogNameFile[];
    private File file_list[];
    boolean wait1 = false;
    boolean wait2 = false;

    private LineChart lineChartGraph;
    LineDataSet set;//ASSE X speed

    //List<LatLng> points = new ArrayList<LatLng>();
    LatLng coord;
    List<ColoredPoint> sourcePoints = new ArrayList<>();
    List<LatLng> currentSegment = new ArrayList<>();


    private static final String LOG_PATH = Environment.getExternalStorageDirectory()
            + File.separator + "StatApp" + File.separator;

    private GoogleMap mMap;

    /*
    * create a fragment with the map and create a graph
    * */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        String text = "<font color='green'> Min </font><font color='cyan'> speed </font><font color='blue'> to </font><font color='magenta'> max </font><font color='red'> speed </font>";
        TextView myAwesomeTextView = (TextView) findViewById(R.id.textView);
//in your OnCreate() method
        myAwesomeTextView.setText(Html.fromHtml(text), TextView.BufferType.SPANNABLE);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        ////////////////////////////////CREO IL GRAFICO VELOCITA'/////////////////////////////////////////////////////
        Log.d(TAG, "1 FINE");

        lineChartGraph = (LineChart) findViewById(R.id.lineGrapfChart);
        //aggiungo al mainLayout

        Log.d(TAG, "2 FINE");
        lineChartGraph.getDescription().setEnabled(false);

        Log.d(TAG, "3 FINE");

        //disabilito pinch zoom
        lineChartGraph.setDragEnabled(false);
        lineChartGraph.setScaleEnabled(false);
        lineChartGraph.setPinchZoom(false);
        Log.d(TAG, "4 FINE");
        //select alternative color background
        lineChartGraph.setBackgroundColor(Color.BLACK);

        LineData data2 = new LineData();
        data2.setValueTextColor(Color.TRANSPARENT);

        Log.d(TAG, "A");
        //aggiungo la data nella barra
        lineChartGraph.setData(data2);

        Log.d(TAG, "B");

        Legend l = lineChartGraph.getLegend();
        l.setForm(Legend.LegendForm.LINE);
        l.setTextColor(Color.RED);

        XAxis x = lineChartGraph.getXAxis();
        x.setTextColor(Color.RED);
        x.setDrawGridLines(true);

        YAxis y = lineChartGraph.getAxisLeft();
        y.setDrawGridLines(true);
        y.setDrawZeroLine(true);
        y.setTextColor(Color.RED);
        Log.d(TAG, "ON CREATE FINE");

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        // Add a marker in Rome and move the camera
        LatLng rome = new LatLng(41.9027835, 12.496365500000024);
        //Log.e(TAG, "POSIZIONE LA MAPPA QUI" + tmp[1] + " " + tmp[2]);
        mMap.addMarker(new MarkerOptions().position(rome).title("Marker in Rome"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(rome));

        LatLng latLng = new LatLng(41.9027835, 12.496365500000024);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                latLng
                , 15));

        readdata();
        Log.d(TAG, "ON MAP READY FINE");

    }

    /*
    * Read the data from the external storage and save some info in some buffer
    * This is done by a thread
    * The thread wait until all data are readed, after write the data on maps
    * The thread wait until all data are writed on maps, after write the data on graph
    * */
    public void readdata() throws IndexOutOfBoundsException{

        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "accedo al percorso");
                String extStorageDirectory = LOG_PATH;
                File file = new File(extStorageDirectory);
                Log.d(TAG, "ho letto lunghezza del file");

                LogNameFile = file.list();
                file_list = file.listFiles();
                NumOfLogs = LogNameFile.length;
                //RECUPERO L'INDICE PER SAPERE QUALE FILE DI LOG APRO
                Intent myIntent = getIntent(); //recuperare l'intent
                int index = myIntent.getIntExtra("chiave", -1); // Ritorna il valore associato a chiave

                LogName = file_list[index].getName();

                //leggo i dati e li metto in un array (di stringhe)
                try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file + File.separator + LogName)))) {
                    Log.d(TAG, "Leggo il file " + LogName);
                    Log.d(TAG, "inizio a leggere le righe");
                    riga = 0;
                    input = br.readLine();
                    riga++;
                    while (input != null && !input.equals("")) {
                        //split il buffer per virgola
                        tmps = input.split(",");
                        tmp = new double[4];

                        //for (String tmp1 : tmps) Log.d(TAG, "Dati letti:" + tmp1);

                        //l'ultima riga potrebbe non contenere tutti i dati, perchè quando interrompo il campionamento perdo dati
                        // se non controllo ciò l'app va in crash causando un eccezione
                        if (tmps.length == 4) {
                            tmp[0] = Double.parseDouble(tmps[0]);
                            tmp[1] = Double.parseDouble(tmps[1]);
                            //l'orario non e' un double!
                            tmp[3] = Double.parseDouble(tmps[3]);  //velocita'
                            if (tmp[3] > vmax) vmax = tmp[3]; // take the max speed
                            coord = new LatLng(tmp[0], tmp[1]);
                            //points.add(coord);
                            sourcePoints.add(new ColoredPoint(coord, colortab.speedToColor(tmp[3], vmax), tmp[3]));
                            //Log.d(TAG, "inizio a leggere la riga: " + riga);
                            //Log.e(TAG, "Velocita' " + tmp[3]);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    //addEntry();
                                    //setPosition();
                                    //Log.d(TAG, "SONO NELLA RUN() DOPO ADDENTRY");
                                }
                            });
                        }
                        input = br.readLine();
                        riga++;
                        //se sono uscito perchè era l'ultima riga oppure la prossima riga è nulla
                        //sono a fine file
                    }
                    if (input == null) {
                        EOF = true;
                        wait1 = true;
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();

                } catch (IOException e) {
                    Log.e(TAG, "Error Reading The File.");
                    e.printStackTrace();
                }

            }
        }).start();
        while (!wait1) ;
        if (EOF && riga > 2)
            showPolyline(sourcePoints);

        else wait2 = true;
        while (!wait2) ;
        if (EOF && riga > 2)
            addEntry(sourcePoints);
        Log.d(TAG, "READ DATA FINE");

    }

    /*
    * @param List<ColoredPoint> points
    * given the list of all point, write it on the maps
    * */
    private void showPolyline(List<ColoredPoint> points) {

        int ix = 0;//0
        ColoredPoint currentPoint = points.get(ix);
        int currentColor = currentPoint.color;
        currentSegment.add(currentPoint.coords);
        ix++;

        while (ix < points.size()) {
            currentPoint = points.get(ix);

            if (currentPoint.color == currentColor) {
                currentSegment.add(currentPoint.coords);
            } else {
                currentSegment.add(currentPoint.coords);
                mMap.addPolyline(new PolylineOptions()
                        .addAll(currentSegment)
                        .color(currentColor)
                        .width(20));
                currentColor = currentPoint.color;
                currentSegment.clear();
                currentSegment.add(currentPoint.coords);
            }

            ix++;
        }

        try {
            ColoredPoint tt = points.get(0);//0

        CameraPosition cameraPosition;
        cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(tt.coords.latitude, tt.coords.longitude))
                .zoom(17)
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }catch(IndexOutOfBoundsException e){
            Log.e(TAG, "ECCEZIONE");
        }
        wait2 = true;

        Log.d(TAG, "SHOW POLY LINE FINE");

    }

    /*
    * structure of the point, with coordinate, color and speed
    * */
    class ColoredPoint {
        public LatLng coords;
        public int color;
        public double speed;

        public ColoredPoint(LatLng coords, int color, double speed) {
            this.coords = coords;
            this.color = color;
            this.speed = speed;
        }

    }

    /*
    * @param List<ColoredPoint> points
     * given the list of all point, write it on the graph
    * */
    private void addEntry(List<ColoredPoint> points){

        LineData data = lineChartGraph.getData();
        if(data != null)
            set = (LineDataSet) data.getDataSetByIndex(0);
        if (set == null) {
            set = createSet();
            data.addDataSet(set);
        }
        int ix = 0;
        ColoredPoint currentPoint = null;

        while (ix < points.size()) {
            try {
                currentPoint = points.get(ix);
            }catch(IndexOutOfBoundsException e){
                Log.e(TAG, "ECCEZIONE");
            }

            if(currentPoint != null) {
                if (String.valueOf(currentPoint.speed).equals("0")) {
                    data.addEntry(new Entry(ix, (float)tmpspeed), 0);
                } else {
                    data.addEntry(new Entry(ix, (float) currentPoint.speed), 0);
                    tmpspeed = currentPoint.speed;
                }
            }
            Log.d(TAG, "Valore inserito "+ ix + " Valore: " + (float)currentPoint.speed);
            ix++;
        }
        lineChartGraph.notifyDataSetChanged();

        Paint paint = lineChartGraph.getRenderer().getPaintRender();
        paint.setShader(new LinearGradient(0, 0, 0, 600,
                        new int[]{
                                0xFFFF0000,
                                0xFFFF0000,
                                0xFFFF00FF,
                                0xFF0000FF,
                                0xFF00FFFF,
                                0xFF00FF00,
                                0xFFFFFF00

                        },
                        null,
                        Shader.TileMode.REPEAT));
        Log.d(TAG, "ADD ENTRY FINE");
    }

    //method for create the set
    private LineDataSet createSet(){
        LineDataSet set1 = new LineDataSet(null, "Speed");
        set1.setCubicIntensity(0.2f);
        set1.setAxisDependency(YAxis.AxisDependency.LEFT);
        //set1.setColor(Color.RED);
        //set1.setCircleColor(Color.RED);
        set1.setCircleColor(Color.TRANSPARENT);
        //set1.setCircleColorHole(Color.RED);
        set1.setCircleColorHole(Color.TRANSPARENT);
        set1.setLineWidth(2f);
        set1.setFillAlpha(65);
        set1.setFillColor(Color.RED);
        set1.setValueTextColor(Color.RED);
        set1.setValueTextSize(10f);
        return set1;


    }

}

// NON CANCELLARE: FUNZIONA!
 /*   public void drawRouteOnMap(GoogleMap map, List<LatLng> positions){
        PolylineOptions options = new PolylineOptions().width(5).color(Color.BLUE).geodesic(true);
        options.addAll(positions);
        Polyline polyline = map.addPolyline(options);
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(positions.get(1).latitude, positions.get(1).longitude))
                .zoom(17)
                .build();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        Log.e(TAG, "drawRouterOnMpa");
    }*/
