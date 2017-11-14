package lm.pkp.com.landmap.permission;

import java.util.Map;

import lm.pkp.com.landmap.area.AreaContext;
import lm.pkp.com.landmap.area.AreaElement;

/**
 * Created by USER on 11/11/2017.
 */
public class PermissionManager {

    public static final PermissionManager INSTANCE = new PermissionManager();

    private PermissionManager() {
    }

    public boolean hasAccess(String functionCode){
        final AreaElement areaElement = AreaContext.getInstance().getAreaElement();
        final Map<String, PermissionElement> areaPermissions = areaElement.getPermissions();

        final PermissionElement fullControl = areaPermissions.get(PermissionConstants.FULL_CONTROL);
        if(fullControl != null){
            return true;
        }

        final PermissionElement viewOnly = areaPermissions.get(PermissionConstants.VIEW_ONLY);
        if(viewOnly != null){
            return false;
        }

        final PermissionElement permission = areaPermissions.get(functionCode);
        if(permission != null){
            return true;
        }else {
            return false;
        }
    }
}
