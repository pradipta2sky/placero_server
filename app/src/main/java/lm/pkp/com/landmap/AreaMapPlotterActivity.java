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
import lm.pkp.com.landmap.custom.MarkerSorter;
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
    private final LinkedHashMap<Marker, DriveResource> resourceMarkers = new LinkedHashMap<>();
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

        this.setContentView(layout.activity_area_plotter);
        this.mapFragment = (SupportMapFragment) this.getSupportFragmentManager()
                .findFragmentById(id.googleMap);
        this.mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        this.googleMap = googleMap;
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.setIndoorEnabled(true);
        googleMap.setMyLocationEnabled(true);
        googleMap.setBuildingsEnabled(true);
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

        this.mapWrapperLayout = (MapWrapperLayout) this.findViewById(id.map_relative_layout);
        this.mapWrapperLayout.init(googleMap, AreaMapPlotterActivity.getPixelsFromDp(this.getApplicationContext(), 50));

        this.infoWindow = (ViewGroup) this.getLayoutInflater().inflate(layout.info_window, null);
        this.infoTitle = (TextView) this.infoWindow.findViewById(id.title);
        this.infoSnippet = (TextView) this.infoWindow.findViewById(id.snippet);
        this.infoImage = (ImageView) this.infoWindow.findViewById(id.info_element_img);
        this.infoButton = (Button) this.infoWindow.findViewById(id.map_info_action);

        final AreaContext ac = AreaContext.INSTANCE;
        this.infoButtonListener = new OnInfoWindowElemTouchListener(AreaMapPlotterActivity.this.infoButton) {

            @Override
            protected void onClickConfirmed(View v, Marker marker) {
                AreaElement ae = ac.getAreaElement();
                File areaLocalImageRoot = ac.getAreaLocalImageRoot(ae.getUniqueId());
                File areaLocalVideoRoot = ac.getAreaLocalVideoRoot(ae.getUniqueId());
                String imageRootPath = areaLocalImageRoot.getAbsolutePath();
                String videoRootPath = areaLocalVideoRoot.getAbsolutePath();

                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);

                PositionElement position = AreaMapPlotterActivity.this.areaMarkers.get(marker);
                if (position != null) {
                    // Do something for non position item.
                    // Implementation for remove button
                    PositionsDBHelper pdh = new PositionsDBHelper(AreaMapPlotterActivity.this.getApplicationContext());
                    PositionElement markedPosition = AreaMapPlotterActivity.this.areaMarkers.get(marker);
                    pdh.deletePosition(markedPosition);
                    AreaMapPlotterActivity.this.areaMarkers.remove(markedPosition);
                    AreaMapPlotterActivity.this.polygon.remove();
                    AreaMapPlotterActivity.this.plotPolygonUsingPositions();

                }else {
                    DriveResource resource = AreaMapPlotterActivity.this.resourceMarkers.get(marker);
                    if(resource != null){
                        String contentType = resource.getContentType();
                        if(contentType.equalsIgnoreCase("Image")){
                            File file = new File(imageRootPath + File.separatorChar + resource.getName());
                            intent.setDataAndType(Uri.fromFile(file), "image/*");
                        }else {
                            File file = new File(videoRootPath + File.separatorChar + resource.getName());
                            intent.setDataAndType(Uri.fromFile(file), "video/*");
                        }
                        AreaMapPlotterActivity.this.startActivity(intent);
                    }
                }
            }
        };
        this.infoButton.setOnTouchListener(this.infoButtonListener);

        final AreaElement ae = ac.getAreaElement();
        googleMap.setInfoWindowAdapter(new InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {

                PositionElement position = AreaMapPlotterActivity.this.areaMarkers.get(marker);
                DriveResource resource = AreaMapPlotterActivity.this.resourceMarkers.get(marker);

                if(position == null && resource == null){
                    // Center Marker.
                    AreaMapPlotterActivity.this.infoTitle.setText("Center");
                    LatLng markerPosition = marker.getPosition();
                    String snippetText = "Lat: " + markerPosition.latitude + ", Long: " + markerPosition.longitude;
                    AreaMapPlotterActivity.this.infoSnippet.setText(snippetText);
                    AreaMapPlotterActivity.this.infoImage.setImageResource(drawable.marker_image);
                    AreaMapPlotterActivity.this.infoButton.setVisibility(View.GONE);
                }else {
                    AreaMapPlotterActivity.this.infoButton.setVisibility(View.VISIBLE);
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
                            AreaMapPlotterActivity.this.infoImage.setImageBitmap(bMap);
                        }
                        AreaMapPlotterActivity.this.infoTitle.setText(resource.getName());
                        CharSequence timeSpan = DateUtils.getRelativeTimeSpanString(new Long(resource.getCreatedOnMillis()),
                                System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS);
                        AreaMapPlotterActivity.this.infoSnippet.setText(timeSpan.toString());
                        AreaMapPlotterActivity.this.infoButton.setText("Open");
                    }else if(position != null){
                        AreaMapPlotterActivity.this.infoImage.setImageResource(drawable.marker_image);
                        AreaMapPlotterActivity.this.infoTitle.setText(position.getName());
                        CharSequence timeSpan = DateUtils.getRelativeTimeSpanString(new Long(position.getCreatedOnMillis()),
                                System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS);
                        AreaMapPlotterActivity.this.infoSnippet.setText(timeSpan.toString());
                        AreaMapPlotterActivity.this.infoButton.setText("Remove");
                    }
                }
                AreaMapPlotterActivity.this.infoButtonListener.setMarker(marker);
                AreaMapPlotterActivity.this.mapWrapperLayout.setMarkerWithInfoWindow(marker, AreaMapPlotterActivity.this.infoWindow);
                return AreaMapPlotterActivity.this.infoWindow;
            }
        });

        this.plotPolygonUsingPositions();
        this.plotMediaPoints();
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

                    Marker marker = this.googleMap.addMarker(markerOptions);
                    marker.setTitle(resource.getName());
                    marker.setDraggable(false);
                    marker.setVisible(true);
                    this.resourceMarkers.put(marker, resource);
                }
            }
        }
    }

    private void plotPolygonUsingPositions() {

        AreaElement areaElement = AreaContext.INSTANCE.getAreaElement();
        List<PositionElement> positionElements = areaElement.getPositions();
        int noOfPositions = positionElements.size();

        Set<Marker> markers = this.areaMarkers.keySet();
        for (Marker m : markers) {
            m.remove();
        }
        if (this.centerMarker != null) {
            this.centerMarker.remove();
        }

        this.areaMarkers.clear();
        this.areaMarkers = new LinkedHashMap<>();
        for (int i = 0; i < noOfPositions; i++) {
            PositionElement pe = positionElements.get(i);
            Marker m = this.drawMarkerUsingPosition(pe);
            this.areaMarkers.put(m, pe);
        }
        PositionElement centerPosition = areaElement.getCenterPosition();
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(new LatLng(centerPosition.getLat(), centerPosition.getLon()));
        this.centerMarker = this.googleMap.addMarker(markerOptions);
        this.centerMarker.setVisible(true);
        this.centerMarker.setAlpha((float) 0.5);
        this.centerMarker.setTitle("Center");
        this.zoomCameraToPosition(this.centerMarker);

        PolygonOptions polyOptions = new PolygonOptions();
        polyOptions = polyOptions.strokeColor(Color.RED).fillColor(Color.DKGRAY);
        markers = this.areaMarkers.keySet();

        List<Marker> markerList = new ArrayList<>(markers);
        MarkerSorter.sortMarkers(markerList, this.centerMarker);
        for (Marker m : markerList) {
            polyOptions.add(m.getPosition());
        }
        this.polygon = this.googleMap.addPolygon(polyOptions);

        double polygonAreaSqMt = SphericalUtil.computeArea(this.polygon.getPoints());
        double polygonAreaSqFt = polygonAreaSqMt * 10.7639;

        final AreaElement ae = areaElement;
        ae.setMeasureSqFt(polygonAreaSqFt);

        if (PermissionManager.INSTANCE.hasAccess(PermissionConstants.UPDATE_AREA)) {
            final AreaDBHelper adh = new AreaDBHelper(this.getApplicationContext());
            adh.updateArea(ae);
            new Thread(new Runnable() {
                public void run() {
                    adh.updateAreaOnServer(ae);
                }
            }).start();
        }

    }

    public Marker drawMarkerUsingPosition(PositionElement pe) {
        LatLng position = new LatLng(pe.getLat(), pe.getLon());
        Marker marker = this.googleMap.addMarker(new MarkerOptions().position(position));
        marker.setTitle(pe.getUniqueId());
        marker.setDraggable(false);
        if (PermissionManager.INSTANCE.hasAccess(PermissionConstants.UPDATE_AREA)) {
            marker.setDraggable(true);
            this.googleMap.setOnMarkerDragListener(new OnMarkerDragListener() {
                @Override
                public void onMarkerDragStart(Marker marker) {
                }

                @SuppressWarnings("unchecked")
                @Override
                public void onMarkerDragEnd(Marker marker) {
                    AreaMapPlotterActivity.this.googleMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));

                    PositionElement newPositionElem = new PositionElement();
                    String pid = UUID.randomUUID().toString();
                    newPositionElem.setUniqueId(pid);
                    newPositionElem.setUniqueAreaId(AreaContext.INSTANCE.getAreaElement().getUniqueId());
                    newPositionElem.setName("P_" + pid);
                    newPositionElem.setDescription("No Description");
                    newPositionElem.setTags("");
                    newPositionElem.setLat(marker.getPosition().latitude);
                    newPositionElem.setLon(marker.getPosition().longitude);

                    PositionsDBHelper pdh = new PositionsDBHelper(AreaMapPlotterActivity.this.getApplicationContext());
                    pdh.insertPositionLocally(newPositionElem);
                    pdh.insertPositionToServer(newPositionElem);

                    List<PositionElement> areaPositions
                            = AreaContext.INSTANCE.getAreaElement().getPositions();
                    areaPositions.add(newPositionElem);

                    pdh.deletePosition(AreaMapPlotterActivity.this.areaMarkers.get(marker));
                    areaPositions.remove(AreaMapPlotterActivity.this.areaMarkers.get(marker));

                    AreaMapPlotterActivity.this.polygon.remove();
                    AreaMapPlotterActivity.this.plotPolygonUsingPositions();
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
        this.googleMap.animateCamera(cameraUpdate);
        this.googleMap.moveCamera(cameraUpdate);
    }

    public static int getPixelsFromDp(Context context, float dp) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    @Override
    public void onBackPressed() {
        this.googleMap.clear();
        this.googleMap = null;

        this.finish();
        Intent positionMarkerIntent = new Intent(this, AreaDetailsActivity.class);
        this.startActivity(positionMarkerIntent);
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

                View rootView = AreaMapPlotterActivity.this.mapFragment.getView();
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
                resource.setLatitude(areaElement.getCenterPosition().getLat() + "");
                resource.setLongitude(areaElement.getCenterPosition().getLon() + "");
                resource.setPath(screenShotFilePath);

                DriveDBHelper ddh = new DriveDBHelper(AreaMapPlotterActivity.this.getApplicationContext());
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

                ThumbnailCreator creator = new ThumbnailCreator(AreaMapPlotterActivity.this.getApplicationContext());
                creator.createImageThumbnail(screenShotFile, areaElement.getUniqueId());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
