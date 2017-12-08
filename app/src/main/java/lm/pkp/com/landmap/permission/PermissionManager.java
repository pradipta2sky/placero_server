package lm.pkp.com.landmap.permission;

import java.util.Map;

import lm.pkp.com.landmap.area.AreaContext;
import lm.pkp.com.landmap.area.model.AreaElement;

/**
 * Created by USER on 11/11/2017.
 */
public class PermissionManager {

    public static final PermissionManager INSTANCE = new PermissionManager();

    private PermissionManager() {
    }

    public boolean hasAccess(String functionCode) {
        AreaElement areaElement = AreaContext.INSTANCE.getAreaElement();
        Map<String, PermissionElement> areaPermissions = areaElement.getUserPermissions();

        PermissionElement fullControl = areaPermissions.get(PermissionConstants.FULL_CONTROL);
        if (fullControl != null) {
            return true;
        }

        PermissionElement viewOnly = areaPermissions.get(PermissionConstants.VIEW_ONLY);
        if (viewOnly != null) {
            return false;
        }

        PermissionElement permission = areaPermissions.get(functionCode);
        return permission != null;
    }
}
