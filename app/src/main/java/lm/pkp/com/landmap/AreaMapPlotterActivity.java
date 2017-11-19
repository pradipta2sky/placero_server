package lm.pkp.com.landmap;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.maps.android.SphericalUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import lm.pkp.com.landmap.area.AreaContext;
import lm.pkp.com.landmap.area.AreaElement;
import lm.pkp.com.landmap.area.db.AreaDBHelper;
import lm.pkp.com.landmap.custom.GenericActivityExceptionHandler;
import lm.pkp.com.landmap.custom.MapWrapperLayout;
import lm.pkp.com.landmap.custom.OnInfoWindowElemTouchListener;
import lm.pkp.com.landmap.custom.ThumbnailCreator;
import lm.pkp.com.landmap.drive.DriveDBHelper;
import lm.pkp.com.landmap.drive.DriveResource;
import lm.pkp.com.landmap.permission.PermissionConstants;
import lm.pkp.com.landmap.permission.PermissionManager;
import lm.pkp.com.landmap.position.PositionElement;
import lm.pkp.com.landmap.position.PositionsDBHelper;
import lm.pkp.com.landmap.user.UserContext;
import lm.pkp.com.landmap.util.FileUtil;

public class AreaMapPlotterActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap googleMap;
    private LinkedHashMap<Marker, PositionElement> areaMarkers = new LinkedHashMap<>();
    private LinkedHashMap<Marker, DriveResource> resourceMarkers = new LinkedHashMap<>();
    private Polygon polygon = null;
    private Marker centerMarker = null;
    private String centerLat;
    private String centerLong;

    private MapWrapperLayout mapWrapperLayout = null;
    private ViewGroup infoWindow;
    private TextView infoTitle;
    private TextView infoSnippet;
    private ImageView infoImage;
    private OnInfoWindowElemTouchListener infoButtonListener;
    private SupportMapFragment mapFragment = null;

    private Button infoButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new GenericActivityExceptionHandler(this);

        setContentView(R.layout.activity_area_plotter);
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.googleMap);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        this.googleMap = googleMap;
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.setIndoorEnabled(true);
        googleMap.setMyLocationEnabled(true);
        googleMap.setBuildingsEnabled(true);
        googleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                googleMap.snapshot(new MapSnapshotTaker());
            }
        });

        UiSettings settings = googleMap.getUiSettings();
        settings.setMapToolbarEnabled(true);
        settings.setAllGesturesEnabled(true);
        settings.setCompassEnabled(true);
        settings.setZoomControlsEnabled(true);
        settings.setZoomGesturesEnabled(true);

        mapWrapperLayout = (MapWrapperLayout) findViewById(R.id.map_relative_layout);
        mapWrapperLayout.init(googleMap, getPixelsFromDp(getApplicationContext(), 50));

        infoWindow = (ViewGroup) getLayoutInflater().inflate(R.layout.info_window, null);
        infoTitle = (TextView) infoWindow.findViewById(R.id.title);
        infoSnippet = (TextView) infoWindow.findViewById(R.id.snippet);
        infoImage = (ImageView) infoWindow.findViewById(R.id.info_element_img);
        infoButton = (Button) infoWindow.findViewById(R.id.map_info_action);

        final AreaContext ac = AreaContext.INSTANCE;
        infoButtonListener = new OnInfoWindowElemTouchListener(infoButton) {

            @Override
            protected void onClickConfirmed(View v, Marker marker) {
                PositionElement position = areaMarkers.get(marker);
                AreaContext areaContext = ac;
                AreaElement areaElement = areaContext.getAreaElement();

                File areaLocalImageRoot = areaContext.getAreaLocalImageRoot(areaElement.getUniqueId());
                File areaLocalVideoRoot = areaContext.getAreaLocalVideoRoot(areaElement.getUniqueId());
                String imageRootPath = areaLocalImageRoot.getAbsolutePath();
                String videoRootPath = areaLocalVideoRoot.getAbsolutePath();

                Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_VIEW);

                if (position != null) {
                    // Do something for non position item.
                    // Implementation for remove button
                    PositionsDBHelper pdh = new PositionsDBHelper(getApplicationContext());
                    PositionElement markedPosition = areaMarkers.get(marker);
                    pdh.deletePosition(markedPosition);
                    areaMarkers.remove(markedPosition);
                    polygon.remove();
                    plotPolygonUsingPositions();

                }else {
                    DriveResource resource = resourceMarkers.get(marker);
                    String contentType = resource.getContentType();
                    if(contentType.equalsIgnoreCase("Image")){
                        File file = new File(imageRootPath + File.separatorChar + resource.getName());
                        intent.setDataAndType(Uri.fromFile(file), "image/*");
                        startActivity(intent);
                    }else {
                        File file = new File(videoRootPath + File.separatorChar + resource.getName());
                        intent.setDataAndType(Uri.fromFile(file), "video/*");
                        startActivity(intent);
                    }
                }
            }
        };
        infoButton.setOnTouchListener(infoButtonListener);

        final AreaElement ae = ac.getAreaElement();
        googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {

                PositionElement position = areaMarkers.get(marker);
                DriveResource resource = resourceMarkers.get(marker);

                if(position == null && resource == null){
                    // Center Marker.
                    infoTitle.setText("Center");
                    LatLng markerPosition = marker.getPosition();
                    String snippetText = "Lat: " + markerPosition.latitude + ", Long: " + markerPosition.longitude;
                    infoSnippet.setText(snippetText);
                    infoImage.setImageResource(R.drawable.marker_image);
                    infoButton.setVisibility(View.GONE);
                }else {
                    infoButton.setVisibility(View.VISIBLE);
                    if (position == null) {
                        String thumbRootPath = "";
                        if(resource.getContentType().equalsIgnoreCase("Video")){
                            thumbRootPath = ac.getAreaLocalVideoThumbnailRoot(ae.getUniqueId()).getAbsolutePath();
                        }else {
                            thumbRootPath = ac.getAreaLocalPictureThumbnailRoot(ae.getUniqueId()).getAbsolutePath();
                        }
                        String thumbnailPath = thumbRootPath + File.separatorChar + resource.getName();
                        File thumbFile = new File(thumbnailPath);
                        if(thumbFile.exists()){
                            Bitmap bMap = BitmapFactory.decodeFile(thumbnailPath);
                            infoImage.setImageBitmap(bMap);
                        }
                        infoTitle.setText(resource.getName());
                        infoSnippet.setText(resource.getUserId());
                        infoButton.setText("Open");
                    }else {
                        infoImage.setImageResource(R.drawable.marker_image);
                        infoTitle.setText(position.getName());
                        infoSnippet.setText("");
                        infoButton.setText("Remove");
                    }
                }
                infoButtonListener.setMarker(marker);
                mapWrapperLayout.setMarkerWithInfoWindow(marker, infoWindow);
                return infoWindow;
            }
        });

        plotPolygonUsingPositions();
        plotMediaPoints();
    }

    private void plotMediaPoints() {
        List<DriveResource> driveResources = AreaContext.INSTANCE.getAreaElement().getDriveResources();
        for (int i = 0; i < driveResources.size(); i++) {
            DriveResource resource = driveResources.get(i);
            if (resource.getType().equals("file")) {
                if (!resource.getLatitude().trim().equalsIgnoreCase("")) {
                    // Plot this resource
                    if (resource.getName().equalsIgnoreCase("plot_screenshot.png")) {
                        continue;
                    }
                    Double latitude = new Double(resource.getLatitude());
                    Double longitude = new Double(resource.getLongitude());
                    LatLng position = new LatLng(latitude, longitude);

                    MarkerOptions markerOptions = new MarkerOptions()
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.camera1));
                    markerOptions.position(position);

                    Marker marker = googleMap.addMarker((markerOptions));
                    marker.setTitle(resource.getName());
                    marker.setDraggable(false);
                    marker.setVisible(true);
                    resourceMarkers.put(marker, resource);
                }
            }
        }
    }

    private void plotPolygonUsingPositions() {

        List<PositionElement> positionElements = AreaContext.INSTANCE.getAreaElement().getPositions();
        int noOfPositions = positionElements.size();

        Set<Marker> markerSet = areaMarkers.keySet();
        for (Marker m : markerSet) {
            m.remove();
        }
        areaMarkers = new LinkedHashMap<>();
        if (centerMarker != null) {
            centerMarker.remove();
        }

        PolygonOptions polyOptions = new PolygonOptions();
        polyOptions = polyOptions.strokeColor(Color.RED).fillColor(Color.DKGRAY);

        double latTotal = 0.0;
        double lonTotal = 0.0;
        for (int i = 0; i < noOfPositions; i++) {
            PositionElement pe = positionElements.get(i);
            Marker m = drawMarkerUsingPosition(pe);
            latTotal += pe.getLat();
            lonTotal += pe.getLon();
            areaMarkers.put(m, pe);
            polyOptions.add(m.getPosition());
        }
        polygon = googleMap.addPolygon(polyOptions);

        final double latAvg = latTotal / noOfPositions;
        final double lonAvg = lonTotal / noOfPositions;
        zoomCameraToPosition(latAvg, lonAvg);

        double polygonAreaSqMt = SphericalUtil.computeArea(polygon.getPoints());
        double polygonAreaSqFt = polygonAreaSqMt * 10.7639;

        final AreaElement ae = AreaContext.INSTANCE.getAreaElement();
        ae.setCenterLat(latAvg);
        centerLat = latAvg + "";
        ae.setCenterLon(lonAvg);
        centerLong = latAvg + "";
        ae.setMeasureSqFt(polygonAreaSqFt);

        if (PermissionManager.INSTANCE.hasAccess(PermissionConstants.UPDATE_AREA)) {
            final AreaDBHelper adh = new AreaDBHelper(getApplicationContext());
            adh.updateArea(ae);
            new Thread(new Runnable() {
                public void run() {
                    adh.updateAreaOnServer(ae);
                }
            }).start();
        }

        MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(latAvg, lonAvg));
        centerMarker = googleMap.addMarker(markerOptions);
        centerMarker.setVisible(true);
        centerMarker.setAlpha((float) 0.5);
        centerMarker.setTitle("Center");
    }

    public Marker drawMarkerUsingPosition(final PositionElement pe) {
        LatLng position = new LatLng(pe.getLat(), pe.getLon());
        Marker marker = googleMap.addMarker((new MarkerOptions().position(position)));
        marker.setTitle(pe.getUniqueId());
        marker.setDraggable(false);
        if (PermissionManager.INSTANCE.hasAccess(PermissionConstants.UPDATE_AREA)) {
            marker.setDraggable(true);
            googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                @Override
                public void onMarkerDragStart(Marker marker) {
                }

                @SuppressWarnings("unchecked")
                @Override
                public void onMarkerDragEnd(Marker marker) {
                    googleMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));

                    PositionElement newPositionElem = new PositionElement();
                    String pid = UUID.randomUUID().toString();
                    newPositionElem.setUniqueId(pid);
                    newPositionElem.setUniqueAreaId(AreaContext.INSTANCE.getAreaElement().getUniqueId());
                    newPositionElem.setName("P_" + pid);
                    newPositionElem.setDescription("No Description");
                    newPositionElem.setTags("");
                    newPositionElem.setLat(marker.getPosition().latitude);
                    newPositionElem.setLon(marker.getPosition().longitude);

                    PositionsDBHelper pdh = new PositionsDBHelper(getApplicationContext());
                    pdh.insertPositionLocally(newPositionElem);
                    pdh.insertPositionToServer(newPositionElem);

                    final List<PositionElement> areaPositions
                            = AreaContext.INSTANCE.getAreaElement().getPositions();
                    areaPositions.add(newPositionElem);

                    pdh.deletePosition(areaMarkers.get(marker));
                    areaPositions.remove(areaMarkers.get(marker));

                    polygon.remove();
                    plotPolygonUsingPositions();
                }

                @Override
                public void onMarkerDrag(Marker marker) {
                }
            });

        }
        return marker;
    }

    private void zoomCameraToPosition(double latAvg, double lonAvg) {
        LatLng position = new LatLng(latAvg, lonAvg);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(position, 21f);
        googleMap.animateCamera(cameraUpdate);
        googleMap.moveCamera(cameraUpdate);
    }

    public static int getPixelsFromDp(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    @Override
    public void onBackPressed() {
        googleMap.clear();
        googleMap = null;

        finish();
        Intent positionMarkerIntent = new Intent(AreaMapPlotterActivity.this, AreaDetailsActivity.class);
        startActivity(positionMarkerIntent);
    }

    private class MapSnapshotTaker implements GoogleMap.SnapshotReadyCallback {

        @Override
        public void onSnapshotReady(Bitmap snapshot) {
            try {
                final AreaElement areaElement = AreaContext.INSTANCE.getAreaElement();
                final File imageStorageDir = AreaContext.INSTANCE.getAreaLocalImageRoot(areaElement.getUniqueId());
                final String dirPath = imageStorageDir.getAbsolutePath();

                String screenshotFileName = "plot_screenshot.png";
                String screenShotFilePath = dirPath + File.separatorChar + screenshotFileName;
                File screenShotFile = new File(screenShotFilePath);
                if (screenShotFile.exists()) {
                    screenShotFile.delete();
                }
                screenShotFile.createNewFile();

                View rootView = mapFragment.getView();
                rootView.setDrawingCacheEnabled(true);
                Bitmap backBitmap = rootView.getDrawingCache();
                Bitmap bmOverlay = Bitmap.createBitmap(
                        backBitmap.getWidth(), backBitmap.getHeight(),
                        backBitmap.getConfig());
                Canvas canvas = new Canvas(bmOverlay);
                canvas.drawBitmap(snapshot, new Matrix(), null);
                canvas.drawBitmap(backBitmap, 0, 0, null);

                FileOutputStream fos = new FileOutputStream(screenShotFile);
                bmOverlay.compress(Bitmap.CompressFormat.PNG, 90, fos);
                fos.flush();
                fos.close();

                backBitmap.recycle();
                bmOverlay.recycle();

                List<DriveResource> driveResources = areaElement.getDriveResources();

                DriveResource screenShotResource = null;
                for (int i = 0; i < driveResources.size(); i++) {
                    DriveResource resource = driveResources.get(i);
                    if (resource.getType().equalsIgnoreCase("file")) {
                        String resourceName = resource.getName();
                        if (resourceName.equalsIgnoreCase(screenshotFileName)) {
                            screenShotResource = resource;
                            break;
                        }
                    }
                }

                DriveResource resource = new DriveResource();
                resource.setUniqueId(UUID.randomUUID().toString());
                resource.setContainerId(AreaContext.INSTANCE.getImagesRootDriveResource().getResourceId());
                resource.setContentType("Image");
                resource.setMimeType(FileUtil.getMimeType(screenShotFile));
                resource.setType("file");
                resource.setUserId(UserContext.getInstance().getUserElement().getEmail());
                resource.setAreaId(areaElement.getUniqueId());
                resource.setName(screenshotFileName);
                resource.setSize(screenShotFile.length() + "");
                resource.setLatitude(centerLat);
                resource.setLongitude(centerLong);
                resource.setPath(screenShotFilePath);

                DriveDBHelper ddh = new DriveDBHelper(getApplicationContext());
                if (screenShotResource == null) {
                    ddh.insertResourceLocally(resource);
                    ddh.insertResourceToServer(resource);
                } else {
                    ddh.deleteResource(screenShotResource);
                    ddh.insertResourceLocally(resource);
                    ddh.updateResourceOnServer(resource);
                    driveResources.remove(screenShotResource);
                }
                driveResources.add(resource);

                ThumbnailCreator creator = new ThumbnailCreator(getApplicationContext());
                creator.createImageThumbnail(screenShotFile, areaElement.getUniqueId());

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

}
