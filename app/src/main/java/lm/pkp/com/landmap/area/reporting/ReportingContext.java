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
    private Boolean generatingReport = false;

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

    public Boolean getGeneratingReport() {
        return this.generatingReport;
    }

    public void setGeneratingReport(Boolean generatingReport) {
        this.generatingReport = generatingReport;
    }

    public Context getActivityContext() {
        return this.context;
    }

}
