package lm.pkp.com.landmap.area.reporting;

import android.content.Context;
import android.graphics.Bitmap;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import lm.pkp.com.landmap.area.model.AreaElement;
import lm.pkp.com.landmap.area.FileStorageConstants;
import lm.pkp.com.landmap.drive.DriveDBHelper;
import lm.pkp.com.landmap.drive.DriveResource;
import lm.pkp.com.landmap.permission.PermissionsDBHelper;
import lm.pkp.com.landmap.position.PositionElement;
import lm.pkp.com.landmap.position.PositionsDBHelper;
import lm.pkp.com.landmap.sync.LocalFolderStructureManager;

/**
 * Created by USER on 10/24/2017.
 */
public class ReportingContext {

    public static final ReportingContext INSTANCE = new ReportingContext();

    private ReportingContext() {
    }

    private AreaElement currentArea;
    private Context context;
    private Bitmap displayBMap;
    private Boolean generatingReport;
    private List<Bitmap> viewBitmaps = new ArrayList<>();

    public AreaElement getAreaElement() {
        return this.currentArea;
    }

    public void setAreaElement(AreaElement areaElement, Context context) {
        this.currentArea = areaElement;
        this.context = context;

        PositionsDBHelper pdb = new PositionsDBHelper(context);
        this.currentArea.setPositions(pdb.getPositionsForArea(this.currentArea));
        this.reCenter(this.currentArea);

        DriveDBHelper ddh = new DriveDBHelper(context);
        this.currentArea.setMediaResources(ddh.getDriveResourcesByAreaId(this.currentArea.getUniqueId()));

        PermissionsDBHelper pdh = new PermissionsDBHelper(context);
        currentArea.setUserPermissions(pdh.fetchPermissionsByAreaId(currentArea.getUniqueId()));
    }

    public void reCenter(AreaElement areaElement) {
        double latSum = 0.0;
        double longSum = 0.0;
        String positionId = null;

        double latAvg = 0.0;
        double lonAvg = 0.0;

        List<PositionElement> positions = areaElement.getPositions();
        int noOfPositions = positions.size();
        if (noOfPositions != 0) {
            for (int i = 0; i < noOfPositions; i++) {
                PositionElement pe = positions.get(i);
                if (positionId == null) {
                    positionId = pe.getUniqueId();
                }
                latSum += pe.getLat();
                longSum += pe.getLon();
            }
            latAvg = latSum / noOfPositions;
            lonAvg = longSum / noOfPositions;
        }

        PositionElement centerPosition = areaElement.getCenterPosition();
        centerPosition.setLat(latAvg);
        centerPosition.setLon(lonAvg);
        centerPosition.setUniqueId(positionId);
    }

    private DriveResource imagesResourceRoot = null;
    public DriveResource getImagesRootDriveResource() {
        if(imagesResourceRoot == null){
            DriveDBHelper ddh = new DriveDBHelper(context);
            imagesResourceRoot
                    = ddh.getDriveResourceRoot(FileStorageConstants.IMAGES_CONTENT_TYPE, currentArea);
        }
        return imagesResourceRoot;
    }

    private DriveResource videosResourceRoot = null;
    public DriveResource getVideosRootDriveResource() {
        if(videosResourceRoot == null){
            DriveDBHelper ddh = new DriveDBHelper(context);
            videosResourceRoot
                    = ddh.getDriveResourceRoot(FileStorageConstants.VIDEOS_CONTENT_TYPE, currentArea);
        }
        return videosResourceRoot;
    }

    private DriveResource documentsResourceRoot = null;
    public DriveResource getDocumentRootDriveResource() {
        if(documentsResourceRoot == null){
            DriveDBHelper ddh = new DriveDBHelper(context);
            documentsResourceRoot
                    = ddh.getDriveResourceRoot(FileStorageConstants.DOCUMENTS_CONTENT_TYPE, currentArea);
        }
        return documentsResourceRoot;
    }

    public File getAreaLocalImageRoot(String areaId) {
        String areaImageRoot = LocalFolderStructureManager.getImageStorageDir().getAbsolutePath()
                + File.separatorChar + areaId;
        File areaImageFolder = new File(areaImageRoot);
        if (areaImageFolder.exists()) {
            return areaImageFolder;
        } else {
            areaImageFolder.mkdirs();
        }
        return areaImageFolder;
    }

    public File getAreaLocalVideoRoot(String areaId) {
        String areaVideosRoot = LocalFolderStructureManager.getVideoStorageDir().getAbsolutePath()
                + File.separatorChar + areaId;
        File areaVideosFolder = new File(areaVideosRoot);
        if (areaVideosFolder.exists()) {
            return areaVideosFolder;
        } else {
            areaVideosFolder.mkdirs();
        }
        return areaVideosFolder;
    }

    public File getAreaLocalDocumentRoot(String areaId) {
        String areaDocumentsRoot = LocalFolderStructureManager.getDocsStorageDir().getAbsolutePath()
                + File.separatorChar + areaId;
        File areaDocumentsFolder = new File(areaDocumentsRoot);
        if (areaDocumentsFolder.exists()) {
            return areaDocumentsFolder;
        } else {
            areaDocumentsFolder.mkdirs();
        }
        return areaDocumentsFolder;
    }

    public File getAreaLocalPictureThumbnailRoot(String areaId) {
        String localImageRootPath = this.getAreaLocalImageRoot(areaId).getAbsolutePath();
        String pictureThumbnailRoot = localImageRootPath + File.separatorChar + "thumb" + File.separatorChar;
        File pictureThumbnailFolder = new File(pictureThumbnailRoot);
        if (pictureThumbnailFolder.exists()) {
            return pictureThumbnailFolder;
        } else {
            pictureThumbnailFolder.mkdirs();
        }
        return pictureThumbnailFolder;
    }

    public File getAreaLocalVideoThumbnailRoot(String areaId) {
        String localVideoRootPath = this.getAreaLocalVideoRoot(areaId).getAbsolutePath();
        String videoThumbnailRoot = localVideoRootPath + File.separatorChar + "thumb" + File.separatorChar;
        File videoThumbnailFolder = new File(videoThumbnailRoot);
        if (videoThumbnailFolder.exists()) {
            return videoThumbnailFolder;
        } else {
            videoThumbnailFolder.mkdirs();
        }
        return videoThumbnailFolder;
    }

    public File getAreaLocalDocumentThumbnailRoot(String areaId) {
        String localDocumentRootPath = this.getAreaLocalDocumentRoot(areaId).getAbsolutePath();
        String documentThumbnailRoot = localDocumentRootPath + File.separatorChar + "thumb" + File.separatorChar;
        File documentThumbnailFolder = new File(documentThumbnailRoot);
        if (documentThumbnailFolder.exists()) {
            return documentThumbnailFolder;
        } else {
            documentThumbnailFolder.mkdirs();
        }
        return documentThumbnailFolder;
    }

    public File getLocalStoreLocationForDriveResource(DriveResource resource) {
        // Assuming that folders will not be passed.
        File dumpRoot = null;
        String contentType = resource.getContentType();
        if (contentType.equalsIgnoreCase("Image")) {
            dumpRoot = this.getAreaLocalImageRoot(resource.getAreaId());
        } else if (contentType.equalsIgnoreCase("Video")) {
            dumpRoot = this.getAreaLocalVideoRoot(resource.getAreaId());
        } else if (contentType.equalsIgnoreCase("Document")) {
            dumpRoot = this.getAreaLocalDocumentRoot(resource.getAreaId());
        }
        return dumpRoot;
    }

    public Bitmap getDisplayBMap() {
        return this.displayBMap;
    }

    public void setDisplayBMap(Bitmap displayBMap) {
        this.displayBMap = displayBMap;
    }

    public List<Bitmap> getViewBitmaps() {
        return this.viewBitmaps;
    }

    public void setViewBitmaps(List<Bitmap> viewBitmaps) {
        this.viewBitmaps = viewBitmaps;
    }

    public Boolean getGeneratingReport() {
        return this.generatingReport;
    }

    public void setGeneratingReport(Boolean generatingReport) {
        this.generatingReport = generatingReport;
    }

    public Context getActivityContext() {
        return this.context;
    }


    public void setActivityContext(Context context) {
        this.context = context;
    }
}
