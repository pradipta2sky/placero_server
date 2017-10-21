package lm.pkp.com.landmap;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;
import java.util.List;

public class AreaPlotterActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap googleMap;
    private String areaName = null;
    private AreaElement ae = null;
    private List<Marker> areaMarkers = null;
    private Polygon polygon = null;

    private ViewGroup infoWindow;
    private TextView infoTitle;
    private TextView infoSnippet;
    private Button infoButton;
    private OnInfoWindowElemTouchListener infoButtonListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getIntent().getExtras();
        areaName = bundle.getString("area_name");

        setContentView(R.layout.activity_area_plotter);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.googleMap);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.setIndoorEnabled(true);
        googleMap.setMyLocationEnabled(true);
        googleMap.setBuildingsEnabled(true);

        final MapWrapperLayout mapWrapperLayout = (MapWrapperLayout)findViewById(R.id.map_relative_layout);
        // MapWrapperLayout initialization
        // 39 - default marker height
        // 20 - offset between the default InfoWindow bottom edge and it's content bottom edge

        mapWrapperLayout.init(googleMap, getPixelsFromDp(getApplicationContext(), 39 + 20));
        // We want to reuse the info window for all the markers,
        // so let's create only one class member instance
        this.infoWindow = (ViewGroup)getLayoutInflater().inflate(R.layout.info_window, null);
        this.infoTitle = (TextView)infoWindow.findViewById(R.id.title);
        this.infoSnippet = (TextView)infoWindow.findViewById(R.id.snippet);
        this.infoButton = (Button)infoWindow.findViewById(R.id.button);

        // Setting custom OnTouchListener which deals with the pressed state
        // so it shows up
        this.infoButtonListener = new OnInfoWindowElemTouchListener(infoButton,
                getResources().getDrawable(R.drawable.round_but_green_sel), //btn_default_normal_holo_light
                getResources().getDrawable(R.drawable.round_but_red_sel)) //btn_default_pressed_holo_light
        {
            @Override
            protected void onClickConfirmed(View v, Marker marker) {
                doRemovePositionByMarker(marker);
            }

            private void doRemovePositionByMarker(Marker marker) {
                String positionName = marker.getTitle();
                PositionsDBHelper pdh = new PositionsDBHelper(getApplicationContext());
                pdh.deletePositionByName(positionName,ae.getId());
                for (int i = 0; i < areaMarkers.size(); i++) {
                    Marker m = areaMarkers.get(i);
                    m.remove();
                }
                polygon.remove();
                plotPolygonUsingPositions();
            }
        };
        this.infoButton.setOnTouchListener(infoButtonListener);


        googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                // Setting up the infoWindow with current's marker info
                infoTitle.setText(marker.getTitle());
                infoSnippet.setText(marker.getSnippet());
                infoButtonListener.setMarker(marker);

                // We must call this to set the current marker and infoWindow references
                // to the MapWrapperLayout
                mapWrapperLayout.setMarkerWithInfoWindow(marker, infoWindow);
                return infoWindow;
            }
        });

       plotPolygonUsingPositions();
    }

    private void plotPolygonUsingPositions() {
        AreaDBHelper adh = new AreaDBHelper(getApplicationContext());
        ae = adh.getAreaByName(areaName);
        areaMarkers = new ArrayList<Marker>();
        List<PositionElement> positionElements = ae.getPositions();
        if(positionElements.size() == 0){
            return ;
        }
        double latTotal = 0.0;
        double lonTotal = 0.0;
        for (int i = 0; i < positionElements.size(); i++) {
            PositionElement pe = positionElements.get(i);
            Marker m = drawMarkerUsingPosition(pe);
            latTotal += pe.getLat();
            lonTotal += pe.getLon();
            areaMarkers.add(m);
        }
        final double latAvg = latTotal / positionElements.size();
        final double lonAvg = lonTotal / positionElements.size();

        zoomCameraToPosition(latAvg, lonAvg);

        googleMap.setOnPolygonClickListener(new GoogleMap.OnPolygonClickListener() {
            @Override
            public void onPolygonClick(Polygon polygon) {

                final TextView textView = new TextView(getApplicationContext());
                textView.setText("Polygon Test");
                textView.setTextSize(14);

                final Paint paintText = textView.getPaint();

                final Rect boundsText = new Rect();
                paintText.getTextBounds("Polygon Test", 0, textView.length(), boundsText);
                paintText.setTextAlign(Paint.Align.CENTER);

                final Bitmap.Config conf = Bitmap.Config.ARGB_8888;
                final Bitmap bmpText = Bitmap.createBitmap(boundsText.width() + 2
                        * 5, boundsText.height() + 2 * 5, conf);

                final Canvas canvasText = new Canvas(bmpText);
                paintText.setColor(Color.BLACK);

                canvasText.drawText("Polygon Test", canvasText.getWidth() / 2,
                        canvasText.getHeight() - 5 - boundsText.bottom, paintText);

                final MarkerOptions markerOptions = new MarkerOptions()
                        .position(new LatLng(latAvg,lonAvg))
                        .icon(BitmapDescriptorFactory.fromBitmap(bmpText))
                        .anchor(0.5f, 1);

                googleMap.addMarker(markerOptions);
            }
        });

        drawPolygonForMarkers();
    }

    private void drawPolygonForMarkers() {
        PolygonOptions polyOptions = new PolygonOptions();
        for (int i = 0; i < areaMarkers.size(); i++) {
            polyOptions.add(areaMarkers.get(i).getPosition());
        }
        polyOptions = polyOptions.strokeColor(Color.RED).fillColor(Color.CYAN).clickable(true).zIndex(1);
        polygon = googleMap.addPolygon(polyOptions);

        //double polygonArea = PolygonUtil.getPolygonAreaSqFeet(polygon.getPoints());
        double polygonArea = SphericalUtil.computeArea(polygon.getPoints());
        Toast.makeText(getApplicationContext(), "Area : " + polygonArea, Toast.LENGTH_LONG).show();
    }

    public Marker drawMarkerUsingPosition(PositionElement pe) {
        LatLng position = new LatLng(pe.getLat(), pe.getLon());
        Marker marker = googleMap.addMarker((new MarkerOptions().position(position)));
        marker.setSnippet("Position [" + pe.getLat() + "," + pe.getLon() + "]");
        marker.setTitle(pe.getName());
        marker.showInfoWindow();
        return marker;
    }

    private void zoomCameraToPosition(double latAvg, double lonAvg) {
        LatLng position = new LatLng(latAvg, lonAvg);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(position, 21f);
        googleMap.animateCamera(cameraUpdate);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 21f));
    }

    public static int getPixelsFromDp(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int)(dp * scale + 0.5f);
    }

    @Override
    public void onBackPressed() {
        Intent positionMarkerIntent = new Intent(AreaPlotterActivity.this, PositionMarkerActivity.class);
        positionMarkerIntent.putExtra("area_name", ae.getName());
        startActivity(positionMarkerIntent);
    }
}
