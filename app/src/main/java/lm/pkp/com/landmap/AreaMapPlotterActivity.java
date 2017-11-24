package lm.pkp.com.landmap;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.format.DateUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnMapLoadedCallback;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.GoogleMap.SnapshotReadyCallback;
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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import lm.pkp.com.landmap.R.drawable;
import lm.pkp.com.landmap.R.id;
import lm.pkp.com.landmap.R.layout;
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
    private LinkedHashMap<Marker, PositionElement> positionMarkers = new LinkedHashMap<>();
    private LinkedHashMap<Marker, DriveResource> resourceMarkers = new LinkedHashMap<>();
    private Polygon polygon;
    private Marker centerMarker;

    private MapWrapperLayout mapWrapperLayout;
    private ViewGroup infoWindow;
    private TextView infoTitle;
    private TextView infoSnippet;
    private ImageView infoImage;
    private OnInfoWindowElemTouchListener infoButtonListener;
    private SupportMapFragment mapFragment;

    private Button infoButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new GenericActivityExceptionHandler(this);

        setContentView(R.layout.activity_area_plotter);
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(id.googleMap);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        this.googleMap = googleMap;
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.setIndoorEnabled(true);
        googleMap.setMyLocationEnabled(true);
        googleMap.setBuildingsEnabled(true);
        googleMap.setTrafficEnabled(true);
        googleMap.setOnMapLoadedCallback(new OnMapLoadedCallback() {
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
        mapWrapperLayout.init(googleMap, getPixelsFromDp(getApplicationContext(), 35));

        infoWindow = (ViewGroup) getLayoutInflater().inflate(layout.info_window, null);
        infoTitle = (TextView) infoWindow.findViewById(id.title);
        infoSnippet = (TextView) infoWindow.findViewById(id.snippet);
        infoImage = (ImageView) infoWindow.findViewById(id.info_element_img);
        infoButton = (Button) infoWindow.findViewById(id.map_info_action);

        final AreaContext ac = AreaContext.INSTANCE;
        infoButtonListener = new OnInfoWindowElemTouchListener(infoButton) {

            @Override
            protected void onClickConfirmed(View v, Marker marker) {
                AreaElement ae = ac.getAreaElement();
                File areaLocalImageRoot = ac.getAreaLocalImageRoot(ae.getUniqueId());
                File areaLocalVideoRoot = ac.getAreaLocalVideoRoot(ae.getUniqueId());
                String imageRootPath = areaLocalImageRoot.getAbsolutePath();
                String videoRootPath = areaLocalVideoRoot.getAbsolutePath();

                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);

                PositionElement position = positionMarkers.get(marker);
                if (position != null) {
                    if(PermissionManager.INSTANCE.hasAccess(PermissionConstants.UPDATE_AREA)){
                        // Do something for non position item.
                        // Implementation for remove button
                        PositionsDBHelper pdh = new PositionsDBHelper(getApplicationContext());
                        PositionElement markedPosition = positionMarkers.get(marker);
                        pdh.deletePositionGlobally(markedPosition);
                        ae.getPositions().remove(markedPosition);
                        ac.reCenter(ae);

                        polygon.remove();
                        plotPolygonUsingPositions();
                    }
                }else {
                    DriveResource resource = resourceMarkers.get(marker);
                    if(resource != null){
                        String contentType = resource.getContentType();
                        if(contentType.equalsIgnoreCase("Image")){
                            File file = new File(imageRootPath + File.separatorChar + resource.getName());
                            intent.setDataAndType(Uri.fromFile(file), "image/*");
                        }else {
                            File file = new File(videoRootPath + File.separatorChar + resource.getName());
                            intent.setDataAndType(Uri.fromFile(file), "video/*");
                        }
                        startActivity(intent);
                    }
                }
            }
        };
        infoButton.setOnTouchListener(infoButtonListener);

        final AreaElement ae = ac.getAreaElement();
        googleMap.setInfoWindowAdapter(new InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {

                PositionElement position = positionMarkers.get(marker);
                DriveResource resource = resourceMarkers.get(marker);

                if(position == null && resource == null){
                    // Center Marker.
                    infoTitle.setText("Center");
                    LatLng markerPosition = marker.getPosition();
                    String snippetText = "Lat: " + markerPosition.latitude + ", Long: " + markerPosition.longitude;
                    infoSnippet.setText(snippetText);
                    infoImage.setImageResource(drawable.marker_image);
                    infoButton.setVisibility(View.GONE);
                }else {
                    infoButton.setVisibility(View.VISIBLE);
                    if (resource != null) {
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
                        CharSequence timeSpan = DateUtils.getRelativeTimeSpanString(new Long(resource.getCreatedOnMillis()),
                                System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS);
                        infoSnippet.setText(timeSpan.toString());
                        infoButton.setText("Open");
                    }else if(position != null){
                        infoImage.setImageResource(drawable.marker_image);
                        infoTitle.setText(position.getDisplayName());
                        CharSequence timeSpan = DateUtils.getRelativeTimeSpanString(new Long(position.getCreatedOnMillis()),
                                System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS);
                        infoSnippet.setText(timeSpan.toString());
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
        List<DriveResource> driveResources = AreaContext.INSTANCE.getAreaElement().getMediaResources();
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

                    MarkerOptions markerOptions = new MarkerOptions();
                    if(resource.getContentType().equalsIgnoreCase("Video")){
                        markerOptions.icon(BitmapDescriptorFactory.fromResource(drawable.video));
                    }else {
                        markerOptions.icon(BitmapDescriptorFactory.fromResource(drawable.camera1));
                    }
                    markerOptions.position(position);

                    Marker marker = googleMap.addMarker(markerOptions);
                    marker.setTitle(resource.getName());
                    marker.setDraggable(false);
                    marker.setVisible(true);
                    resourceMarkers.put(marker, resource);
                }
            }
        }
    }

    private void plotPolygonUsingPositions() {

        AreaElement areaElement = AreaContext.INSTANCE.getAreaElement();
        List<PositionElement> positionElements = areaElement.getPositions();
        int noOfPositions = positionElements.size();

        Set<Marker> markers = positionMarkers.keySet();
        for (Marker m : markers) {
            m.remove();
        }
        positionMarkers.clear();
        if (centerMarker != null) {
            centerMarker.remove();
        }
        for (int i = 0; i < noOfPositions; i++) {
            PositionElement pe = positionElements.get(i);
            positionMarkers.put(buildMarker(pe), pe);
        }

        PositionElement centerPosition = areaElement.getCenterPosition();
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(new LatLng(centerPosition.getLat(), centerPosition.getLon()));
        centerMarker = googleMap.addMarker(markerOptions);
        centerMarker.setVisible(true);
        centerMarker.setAlpha((float) 0.5);
        centerMarker.setTitle("Center");

        zoomCameraToPosition(centerMarker);

        PolygonOptions polyOptions = new PolygonOptions();
        polyOptions = polyOptions.strokeColor(Color.BLUE).fillColor(Color.DKGRAY);
        markers = positionMarkers.keySet();

        List<Marker> markerList = new ArrayList<>(markers);
        for (Marker m : markerList) {
            polyOptions.add(m.getPosition());
        }
        polygon = googleMap.addPolygon(polyOptions);

        double polygonAreaSqMt = SphericalUtil.computeArea(polygon.getPoints());
        double polygonAreaSqFt = polygonAreaSqMt * 10.7639;

        final AreaElement ae = areaElement;
        ae.setMeasureSqFt(polygonAreaSqFt);

        if (PermissionManager.INSTANCE.hasAccess(PermissionConstants.UPDATE_AREA)) {
            AreaDBHelper adh = new AreaDBHelper(getApplicationContext());
            adh.updateAreaLocally(ae);
            adh.updateAreaOnServer(ae);
        }

    }

    public Marker buildMarker(PositionElement pe) {
        LatLng position = new LatLng(pe.getLat(), pe.getLon());
        Marker marker = googleMap.addMarker(new MarkerOptions().position(position));
        marker.setTitle(pe.getUniqueId());
        marker.setDraggable(false);
        if (PermissionManager.INSTANCE.hasAccess(PermissionConstants.UPDATE_AREA)) {
            marker.setDraggable(true);
            googleMap.setOnMarkerDragListener(new OnMarkerDragListener() {
                @Override
                public void onMarkerDragStart(Marker marker) {
                }

                @SuppressWarnings("unchecked")
                @Override
                public void onMarkerDragEnd(Marker marker) {
                    googleMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));

                    PositionElement newPosition = positionMarkers.get(marker);
                    newPosition.setLat(marker.getPosition().latitude);
                    newPosition.setLon(marker.getPosition().longitude);
                    newPosition.setCreatedOnMillis(System.currentTimeMillis() + "");

                    PositionsDBHelper pdh = new PositionsDBHelper(getApplicationContext());
                    pdh.updatePositionLocally(newPosition);
                    pdh.updatePositionToServer(newPosition);

                    AreaElement areaElement = AreaContext.INSTANCE.getAreaElement();
                    areaElement.setPositions(pdh.getPositionsForArea(areaElement));
                    AreaContext.INSTANCE.reCenter(areaElement);

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

    private void zoomCameraToPosition(Marker marker) {
        LatLng position = marker.getPosition();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(position, 21f);
        googleMap.animateCamera(cameraUpdate);
        googleMap.moveCamera(cameraUpdate);
    }

    public static int getPixelsFromDp(Context context, float dp) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    @Override
    public void onBackPressed() {
        googleMap.clear();
        googleMap = null;

        finish();
        Intent positionMarkerIntent = new Intent(this, AreaDetailsActivity.class);
        startActivity(positionMarkerIntent);
    }

    private class MapSnapshotTaker implements SnapshotReadyCallback {

        @Override
        public void onSnapshotReady(Bitmap snapshot) {
            try {
                AreaElement areaElement = AreaContext.INSTANCE.getAreaElement();
                File imageStorageDir = AreaContext.INSTANCE.getAreaLocalImageRoot(areaElement.getUniqueId());
                String dirPath = imageStorageDir.getAbsolutePath();

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
                bmOverlay.compress(CompressFormat.PNG, 90, fos);
                fos.flush();
                fos.close();

                backBitmap.recycle();
                bmOverlay.recycle();

                List<DriveResource> driveResources = areaElement.getMediaResources();
                DriveDBHelper ddh = new DriveDBHelper(getApplicationContext());
                for (int i = 0; i < driveResources.size(); i++) {
                    DriveResource resource = driveResources.get(i);
                    if (resource.getType().equalsIgnoreCase("file")) {
                        String resourceName = resource.getName();
                        if (resourceName.equalsIgnoreCase(screenshotFileName)) {
                            ddh.deleteResourceLocally(resource);
                            ddh.deleteResourceFromServer(resource);
                            driveResources.remove(resource);
                            break;
                        }
                    }
                }


                DriveResource imagesRootResource = AreaContext.INSTANCE.getImagesRootDriveResource();

                DriveResource resource = new DriveResource();
                resource.setUniqueId(UUID.randomUUID().toString());
                resource.setContainerId(imagesRootResource.getResourceId());
                resource.setContentType("Image");
                resource.setMimeType(FileUtil.getMimeType(screenShotFile));
                resource.setType("file");
                resource.setUserId(UserContext.getInstance().getUserElement().getEmail());
                resource.setAreaId(areaElement.getUniqueId());
                resource.setName(screenshotFileName);
                resource.setSize(screenShotFile.length() + "");
                resource.setLatitude(areaElement.getCenterPosition().getLat() + "");
                resource.setLongitude(areaElement.getCenterPosition().getLon() + "");
                resource.setPath(screenShotFilePath);
                resource.setCreatedOnMillis(System.currentTimeMillis() + "");

                ddh.insertResourceLocally(resource);
                ddh.insertResourceToServer(resource);
                driveResources.add(resource);

                ThumbnailCreator creator = new ThumbnailCreator(getApplicationContext());
                creator.createImageThumbnail(screenShotFile, areaElement.getUniqueId());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
