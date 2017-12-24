package lm.pkp.com.landmap.connectivity.services;

import android.app.IntentService;
import android.content.Intent;

import java.util.ArrayList;

import lm.pkp.com.landmap.area.db.AreaDBHelper;
import lm.pkp.com.landmap.area.model.AreaElement;

public class AreaSynchronizationService extends IntentService {

    public AreaSynchronizationService() {
        super(AreaSynchronizationService.class.getSimpleName());
    }

    public AreaSynchronizationService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        AreaDBHelper adh = new AreaDBHelper(getApplicationContext());
        final ArrayList<AreaElement> dirtyAreas = adh.getDirtyAreas();
        for (AreaElement areaElement : dirtyAreas) {
            String dirtyAction = areaElement.getDirtyAction();
            if (dirtyAction.equalsIgnoreCase("insert")) {
                if (adh.insertAreaToServer(areaElement)) {
                    areaElement.setDirty(0);
                    adh.updateAreaLocally(areaElement);
                }
            } else if (dirtyAction.equalsIgnoreCase("update")) {
                if (adh.updateAreaOnServer(areaElement)) {
                    areaElement.setDirty(0);
                    adh.updateAreaLocally(areaElement);
                }
            } else if (dirtyAction.equalsIgnoreCase("delete")) {
                adh.deleteAreaFromServer(areaElement);
            }
        }
    }

}