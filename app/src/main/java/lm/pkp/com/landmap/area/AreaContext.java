package lm.pkp.com.landmap.area;

import android.content.Context;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import lm.pkp.com.landmap.drive.DriveDBHelper;
import lm.pkp.com.landmap.drive.DriveResource;
import lm.pkp.com.landmap.position.PositionsDBHelper;
import lm.pkp.com.landmap.sync.LocalFolderStructureManager;

/**
 * Created by USER on 10/24/2017.
 */
public class AreaContext {

    public static final AreaContext INSTANCE = new AreaContext();

    private AreaContext() {
    }

    private AreaElement currArea;
    private Context context;
    private ArrayList<DriveResource> uploadQueue = new ArrayList<>();

    public AreaElement getAreaElement() {
        return currArea;
    }

    public void setAreaElement(AreaElement areaElement, Context context) {
        currArea = areaElement;
        this.context = context;
        uploadQueue.clear();

        PositionsDBHelper pdb = new PositionsDBHelper(context);
        currArea.setPositions(pdb.getAllPositionForArea(currArea));

        DriveDBHelper ddh = new DriveDBHelper(context);
        currArea.setDriveResources(ddh.getDriveResourcesByAreaId(currArea.getUniqueId()));
        currArea.setCommonResources(ddh.getCommonResources());
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

    public File getAreaLocalImageRoot(){
        String areaImageRoot = LocalFolderStructureManager.getImageStorageDir().getAbsolutePath()
                + File.separatorChar + currArea.getUniqueId();
        final File areaImageFolder = new File(areaImageRoot);
        if(areaImageFolder.exists()){
            return areaImageFolder;
        }else {
            areaImageFolder.mkdirs();
        }
        return areaImageFolder;
    }

    public File getAreaLocalVideoRoot(){
        String areaVideosRoot = LocalFolderStructureManager.getVideoStorageDir().getAbsolutePath()
                + File.separatorChar + currArea.getUniqueId();
        final File areaVideosFolder = new File(areaVideosRoot);
        if(areaVideosFolder.exists()){
            return areaVideosFolder;
        }else {
            areaVideosFolder.mkdirs();
        }
        return areaVideosFolder;
    }

    public File getAreaLocalDocumentRoot(){
        String areaDocumentsRoot = LocalFolderStructureManager.getDocsStorageDir().getAbsolutePath()
                + File.separatorChar + currArea.getUniqueId();
        final File areaDocumentsFolder = new File(areaDocumentsRoot);
        if(areaDocumentsFolder.exists()){
            return areaDocumentsFolder;
        }else {
            areaDocumentsFolder.mkdirs();
        }
        return areaDocumentsFolder;
    }

    public File getAreaLocalPictureThumbnailRoot(){
        final String localImageRootPath = getAreaLocalImageRoot().getAbsolutePath();
        final String pictureThumbnailRoot = localImageRootPath + File.separatorChar + ".thumb" + File.separatorChar;
        final File pictureThumbnailFolder = new File(pictureThumbnailRoot);
        if(pictureThumbnailFolder.exists()){
            return pictureThumbnailFolder;
        }else {
            pictureThumbnailFolder.mkdirs();
        }
        return pictureThumbnailFolder;
    }

    public File getAreaLocalVideoThumbnailRoot(){
        final String localVideoRootPath = getAreaLocalVideoRoot().getAbsolutePath();
        final String videoThumbnailRoot = localVideoRootPath + File.separatorChar + ".thumb" + File.separatorChar;
        final File videoThumbnailFolder = new File(videoThumbnailRoot);
        if(videoThumbnailFolder.exists()){
            return videoThumbnailFolder;
        }else {
            videoThumbnailFolder.mkdirs();
        }
        return videoThumbnailFolder;
    }

    public File getAreaLocalDocumentThumbnailRoot(){
        final String localDocumentRootPath = getAreaLocalDocumentRoot().getAbsolutePath();
        final String documentThumbnailRoot = localDocumentRootPath + File.separatorChar + ".thumb" + File.separatorChar;
        final File documentThumbnailFolder = new File(documentThumbnailRoot);
        if(documentThumbnailFolder.exists()){
            return documentThumbnailFolder;
        }else {
            documentThumbnailFolder.mkdirs();
        }
        return documentThumbnailFolder;
    }

}
