package lm.pkp.com.landmap.area;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import lm.pkp.com.landmap.drive.DriveDBHelper;
import lm.pkp.com.landmap.drive.DriveResource;
import lm.pkp.com.landmap.position.PositionElement;
import lm.pkp.com.landmap.position.PositionsDBHelper;

/**
 * Created by USER on 10/24/2017.
 */
public class AreaContext {

    private final String IMAGE_FOLDER_NAME = "LMS_IMAGES";
    private final String VIDEO_FOLDER_NAME = "LMS_VIDEOS";
    private final String DOCUMENT_FOLDER_NAME = "LMS_DOCS";

    private static AreaContext ourInstance = new AreaContext();

    public static AreaContext getInstance() {
        return ourInstance;
    }

    private AreaContext() {
    }

    private AreaElement currArea = null;
    private ArrayList<DriveResource> uploadedDriveResources = new ArrayList<>();
    private ArrayList<PositionElement> positionElements = new ArrayList<>();

    public AreaElement getAreaElement() {
        return currArea;
    }

    public void setAreaElement(AreaElement areaElement, Context context) {
        currArea = areaElement;
        uploadedDriveResources.clear();
        positionElements.clear();

        PositionsDBHelper pdb = new PositionsDBHelper(context);
        currArea.setPositions(pdb.getAllPositionForArea(currArea));

        DriveDBHelper ddh = new DriveDBHelper(context);
        currArea.setDriveResources(ddh.getDriveResourcesByAreaId(currArea.getUniqueId()));

        positionElements.addAll(this.currArea.getPositions());
    }

    // Drive specific resources.
    public void addNewDriveResource(DriveResource dr) {
        uploadedDriveResources.add(dr);
    }

    public void removeUploadedDriveResource(DriveResource dr) {
        uploadedDriveResources.remove(dr);
    }

    public ArrayList<DriveResource> getUploadedDriveResources() {
        return uploadedDriveResources;
    }

    // Position specific resources.
    public void addPosition(PositionElement position) {
        currArea.getPositions().add(position);
        positionElements.add(position);
    }

    public void removePosition(PositionElement position) {
        positionElements.remove(position);
    }

    public ArrayList<PositionElement> getPositions() {
        return positionElements;
    }

    public DriveResource getImagesRootDriveResource() {
        DriveResource imagesDriveRes = null;
        List<DriveResource> existingResouces = currArea.getDriveResources();
        for (int i = 0; i < existingResouces.size(); i++) {
            DriveResource dr = existingResouces.get(i);
            if (dr.getName().equals(IMAGE_FOLDER_NAME)) {
                imagesDriveRes = dr;
                break;
            }
        }
        return imagesDriveRes;
    }

    public DriveResource getVideosRootDriveResource() {
        DriveResource videoDriveRes = null;
        List<DriveResource> existingResouces = currArea.getDriveResources();
        for (int i = 0; i < existingResouces.size(); i++) {
            DriveResource dr = existingResouces.get(i);
            if (dr.getName().equals(VIDEO_FOLDER_NAME)) {
                videoDriveRes = dr;
                break;
            }
        }
        return videoDriveRes;
    }

    public DriveResource getDocumentRootDriveResource() {
        DriveResource docDriveRes = null;
        List<DriveResource> existingResouces = currArea.getDriveResources();
        for (int i = 0; i < existingResouces.size(); i++) {
            DriveResource dr = existingResouces.get(i);
            if (dr.getName().equals(DOCUMENT_FOLDER_NAME)) {
                docDriveRes = dr;
            }
        }
        return docDriveRes;
    }

}
