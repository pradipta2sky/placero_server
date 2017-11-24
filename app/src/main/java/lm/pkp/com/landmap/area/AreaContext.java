package lm.pkp.com.landmap.area;

import android.content.Context;
import android.graphics.Bitmap;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import lm.pkp.com.landmap.drive.DriveDBHelper;
import lm.pkp.com.landmap.drive.DriveResource;
import lm.pkp.com.landmap.permission.PermissionsDBHelper;
import lm.pkp.com.landmap.position.PositionElement;
import lm.pkp.com.landmap.position.PositionsDBHelper;
import lm.pkp.com.landmap.sync.LocalFolderStructureManager;

/**
 * Created by USER on 10/24/2017.
 */
public class AreaContext {

    public static final AreaContext INSTANCE = new AreaContext();

    private AreaContext() {
    }

    private AreaElement currentArea;
    private Context context;
    private Bitmap displayBMap;
    private List<Bitmap> viewBitmaps = new ArrayList<>();
    private final ArrayList<DriveResource> uploadQueue = new ArrayList<>();

    public AreaElement getAreaElement() {
        return this.currentArea;
    }

    public void setAreaElement(AreaElement areaElement, Context context) {
        clearContext();

        this.currentArea = areaElement;
        this.context = context;
        this.uploadQueue.clear();

        PositionsDBHelper pdb = new PositionsDBHelper(context);
        this.currentArea.setPositions(pdb.getPositionsForArea(this.currentArea));
        this.reCenter(this.currentArea);

        DriveDBHelper ddh = new DriveDBHelper(context);
        this.currentArea.setMediaResources(ddh.getDriveResourcesByAreaId(this.currentArea.getUniqueId()));

        PermissionsDBHelper pdh = new PermissionsDBHelper(context);
        currentArea.setUserPermissions(pdh.fetchPermissionsByAreaId(currentArea.getUniqueId()));
    }

    public void clearContext(){
        if(currentArea != null){
            currentArea.getPositions().clear();
            currentArea.getMediaResources().clear();
            currentArea.getUserPermissions().clear();

            currentArea = null;
            context = null;
            uploadQueue.clear();

            if(displayBMap != null){
                displayBMap.recycle();
            }
            Iterator<Bitmap> iterator = viewBitmaps.iterator();
            while (iterator.hasNext()){
                Bitmap bitmap = iterator.next();
                if(bitmap != null){
                    bitmap.recycle();
                }
            }
            displayBMap = null;
            System.gc();
        }
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

    // Drive specific resources.
    public void addResourceToQueue(DriveResource dr) {
        this.uploadQueue.add(dr);
    }

    public void removeResourceFromQueue(DriveResource dr) {
        this.uploadQueue.remove(dr);
    }

    public ArrayList<DriveResource> getUploadedQueue() {
        return this.uploadQueue;
    }

    private DriveResource imagesResourceRoot = null;
    public DriveResource getImagesRootDriveResource() {
        if(imagesResourceRoot == null){
            DriveDBHelper ddh = new DriveDBHelper(context);
            imagesResourceRoot = ddh.getDriveResourceRoot(FileStorageConstants.IMAGE_ROOT_FOLDER_NAME);
        }
        return imagesResourceRoot;
    }

    private DriveResource videosResourceRoot = null;
    public DriveResource getVideosRootDriveResource() {
        if(videosResourceRoot == null){
            DriveDBHelper ddh = new DriveDBHelper(context);
            videosResourceRoot = ddh.getDriveResourceRoot(FileStorageConstants.VIDEO_ROOT_FOLDER_NAME);
        }
        return videosResourceRoot;
    }

    private DriveResource documentsResourceRoot = null;
    public DriveResource getDocumentRootDriveResource() {
        if(documentsResourceRoot == null){
            DriveDBHelper ddh = new DriveDBHelper(context);
            documentsResourceRoot = ddh.getDriveResourceRoot(FileStorageConstants.DOCUMENT_ROOT_FOLDER_NAME);
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
}
