package lm.pkp.com.landmap.connectivity.services;

import android.app.IntentService;
import android.content.Intent;

import java.util.ArrayList;

import lm.pkp.com.landmap.CreateAreaFoldersActivity;
import lm.pkp.com.landmap.area.db.AreaDBHelper;
import lm.pkp.com.landmap.custom.GlobalContext;
import lm.pkp.com.landmap.drive.DriveDBHelper;
import lm.pkp.com.landmap.drive.DriveResource;
import lm.pkp.com.landmap.position.PositionElement;
import lm.pkp.com.landmap.position.PositionsDBHelper;

public class ResourceSynchronizationService extends IntentService {

    public ResourceSynchronizationService() {
        super(ResourceSynchronizationService.class.getSimpleName());
    }

    public ResourceSynchronizationService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        DriveDBHelper ddh = new DriveDBHelper(getApplicationContext());
        final ArrayList<DriveResource> dirtyResources = ddh.getDirtyResources();
        for (DriveResource resource : dirtyResources) {
            String dirtyAction = resource.getDirtyAction();
            if (dirtyAction.equalsIgnoreCase("insert")) {
                if (ddh.insertResourceToServer(resource)) {
                    resource.setDirty(0);
                    ddh.updateResourceLocally(resource);
                }
            } else if (dirtyAction.equalsIgnoreCase("update")) {
                if (ddh.updateResourceToServer(resource)) {
                    resource.setDirty(0);
                    ddh.updateResourceLocally(resource);
                }
            } else if (dirtyAction.equalsIgnoreCase("delete")) {
                ddh.deleteResourceByGlobally(resource);
            }
        }

        AreaDBHelper adh = new AreaDBHelper(getApplicationContext());
        PositionsDBHelper pdh = new PositionsDBHelper(getApplicationContext());
        if(adh.getDirtyAreas().size() == 0
                && pdh.getDirtyPositions().size() == 0
                && ddh.getDirtyResources().size() == 0){
            GlobalContext.INSTANCE.put(GlobalContext.SYNCHRONIZING_OFFLINE, new Boolean(false).toString());
        }

    }

}