package lm.pkp.com.landmap.area;

import android.content.Context;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lm.pkp.com.landmap.drive.DriveDBHelper;
import lm.pkp.com.landmap.drive.DriveResource;
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
    private ArrayList<DriveResource> uploadQueue = new ArrayList<>();

    public AreaElement getAreaElement() {
        return currentArea;
    }

    public void setAreaElement(AreaElement areaElement, Context context) {
        currentArea = areaElement;
        this.context = context;
        uploadQueue.clear();

        PositionsDBHelper pdb = new PositionsDBHelper(context);
        currentArea.setPositions(pdb.getAllPositionForArea(currentArea));
        loadCenterPosition(currentArea);

        DriveDBHelper ddh = new DriveDBHelper(context);
        currentArea.setMediaResources(ddh.getDriveResourcesByAreaId(currentArea.getUniqueId()));
        currentArea.setCommonResources(ddh.getCommonResources());
    }

    private void loadCenterPosition(AreaElement areaElement) {
        double latSum = 0.0;
        double longSum = 0.0;

        List<PositionElement> positions = areaElement.getPositions();
        int noOfPositions = positions.size();
        for (int i = 0; i < noOfPositions; i++) {
            PositionElement pe = positions.get(i);
            latSum += pe.getLat();
            longSum += pe.getLon();
        }

        final double latAvg = latSum / noOfPositions;
        final double lonAvg = longSum / noOfPositions;

        PositionElement centerPosition = areaElement.getCenterPosition();
        centerPosition.setLat(latAvg);
        centerPosition.setLon(lonAvg);
        centerPosition.setUniqueId(UUID.randomUUID().toString());
    }

    // Drive specific resources.
    public void addResourceToQueue(DriveResource dr) {
        uploadQueue.add(dr);
    }

    public void removeResourceFromQueue(DriveResource dr) {
        uploadQueue.remove(dr);
    }

    public ArrayList<DriveResource> getUploadedQueue() {
        return uploadQueue;
    }

    public DriveResource getImagesRootDriveResource() {
        return new DriveDBHelper(context).getDriveResourceRoot(FileStorageConstants.IMAGE_ROOT_FOLDER_NAME);
    }

    public DriveResource getVideosRootDriveResource() {
        return new DriveDBHelper(context).getDriveResourceRoot(FileStorageConstants.VIDEO_ROOT_FOLDER_NAME);
    }

    public DriveResource getDocumentRootDriveResource() {
        return new DriveDBHelper(context).getDriveResourceRoot(FileStorageConstants.DOCUMENT_ROOT_FOLDER_NAME);
    }

    public File getAreaLocalImageRoot(String areaId){
        String areaImageRoot = LocalFolderStructureManager.getImageStorageDir().getAbsolutePath()
                + File.separatorChar + areaId;
        final File areaImageFolder = new File(areaImageRoot);
        if(areaImageFolder.exists()){
            return areaImageFolder;
        }else {
            areaImageFolder.mkdirs();
        }
        return areaImageFolder;
    }

    public File getAreaLocalVideoRoot(String areaId){
        String areaVideosRoot = LocalFolderStructureManager.getVideoStorageDir().getAbsolutePath()
                + File.separatorChar + areaId;
        final File areaVideosFolder = new File(areaVideosRoot);
        if(areaVideosFolder.exists()){
            return areaVideosFolder;
        }else {
            areaVideosFolder.mkdirs();
        }
        return areaVideosFolder;
    }

    public File getAreaLocalDocumentRoot(String areaId){
        String areaDocumentsRoot = LocalFolderStructureManager.getDocsStorageDir().getAbsolutePath()
                + File.separatorChar + areaId;
        final File areaDocumentsFolder = new File(areaDocumentsRoot);
        if(areaDocumentsFolder.exists()){
            return areaDocumentsFolder;
        }else {
            areaDocumentsFolder.mkdirs();
        }
        return areaDocumentsFolder;
    }

    public File getAreaLocalPictureThumbnailRoot(String areaId){
        final String localImageRootPath = getAreaLocalImageRoot(areaId).getAbsolutePath();
        final String pictureThumbnailRoot = localImageRootPath + File.separatorChar + "thumb" + File.separatorChar;
        final File pictureThumbnailFolder = new File(pictureThumbnailRoot);
        if(pictureThumbnailFolder.exists()){
            return pictureThumbnailFolder;
        }else {
            pictureThumbnailFolder.mkdirs();
        }
        return pictureThumbnailFolder;
    }

    public File getAreaLocalVideoThumbnailRoot(String areaId){
        final String localVideoRootPath = getAreaLocalVideoRoot(areaId).getAbsolutePath();
        final String videoThumbnailRoot = localVideoRootPath + File.separatorChar + "thumb" + File.separatorChar;
        final File videoThumbnailFolder = new File(videoThumbnailRoot);
        if(videoThumbnailFolder.exists()){
            return videoThumbnailFolder;
        }else {
            videoThumbnailFolder.mkdirs();
        }
        return videoThumbnailFolder;
    }

    public File getAreaLocalDocumentThumbnailRoot(String areaId){
        final String localDocumentRootPath = getAreaLocalDocumentRoot(areaId).getAbsolutePath();
        final String documentThumbnailRoot = localDocumentRootPath + File.separatorChar + "thumb" + File.separatorChar;
        final File documentThumbnailFolder = new File(documentThumbnailRoot);
        if(documentThumbnailFolder.exists()){
            return documentThumbnailFolder;
        }else {
            documentThumbnailFolder.mkdirs();
        }
        return documentThumbnailFolder;
    }

    public File getLocalStoreLocationForDriveResource(DriveResource resource){
        // Assuming that folders will not be passed.
        File dumpRoot = null;
        String contentType = resource.getContentType();
        if(contentType.equalsIgnoreCase("Image")){
            dumpRoot = getAreaLocalImageRoot(resource.getAreaId());
        }else if(contentType.equalsIgnoreCase("Video")){
            dumpRoot = getAreaLocalVideoRoot(resource.getAreaId());
        }else if(contentType.equalsIgnoreCase("Document")){
            dumpRoot = getAreaLocalDocumentRoot(resource.getAreaId());
        }
        return dumpRoot;
    }

}
