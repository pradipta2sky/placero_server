package lm.pkp.com.landmap;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

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
        double latTotal = 0.0;
        double lonTotal = 0.0;
        for (int i = 0; i < positionElements.size(); i++) {
            PositionElement pe = positionElements.get(i);
            Marker m = drawMarkerUsingPosition(pe);
            latTotal += pe.getLat();
            lonTotal += pe.getLon();
            areaMarkers.add(m);
        }
        double latAvg = latTotal / positionElements.size();
        double lonAvg = lonTotal / positionElements.size();

        zoomCameraToPosition(latAvg, lonAvg);
        drawPolygonForMarkers();
    }

    private void drawPolygonForMarkers() {
        PolygonOptions polyOptions = new PolygonOptions();
        for (int i = 0; i < areaMarkers.size(); i++) {
            polyOptions.add(areaMarkers.get(i).getPosition());
        }
        polyOptions = polyOptions.strokeColor(Color.RED).fillColor(Color.CYAN).clickable(true).zIndex(1);
        polygon = googleMap.addPolygon(polyOptions);

        double polygonArea = PolygonUtil.getPolygonAreaSqFeet(polygon.getPoints());
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
