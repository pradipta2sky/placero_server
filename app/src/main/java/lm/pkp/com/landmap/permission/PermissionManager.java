package lm.pkp.com.landmap.permission;

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
        if(functionCode.equalsIgnoreCase(PermissionConstants.FULL_CONTROL)){
            return true;
        }

        if(functionCode.equalsIgnoreCase(PermissionConstants.VIEW_ONLY)){
            return false;
        }

        final AreaElement areaElement = AreaContext.getInstance().getAreaElement();
        final PermissionElement permission = areaElement.getPermissions().get(functionCode);
        if(permission != null){
            return true;
        }else {
            return false;
        }
    }
}
