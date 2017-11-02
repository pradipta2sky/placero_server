package lm.pkp.com.landmap.area;

import java.util.ArrayList;

import lm.pkp.com.landmap.drive.DriveResource;
import lm.pkp.com.landmap.user.UserElement;

/**
 * Created by USER on 10/24/2017.
 */
public class AreaContext {

    private static AreaContext ourInstance = new AreaContext();

    public static AreaContext getInstance() {
        return ourInstance;
    }

    private AreaContext() {
    }

    private AreaElement userElement = null;
    private ArrayList<DriveResource> driveResources = new ArrayList<>();

    public AreaElement getAreaElement() {
        return userElement;
    }

    public void setAreaElement(AreaElement userElement) {
        this.userElement = userElement;
    }

    public void addDriveResource(DriveResource dr){
        driveResources.add(dr);
    }

    public void removeDriveResource(DriveResource dr){
        driveResources.remove(dr);
    }

    public ArrayList<DriveResource> getDriveResources(){
        return driveResources;
    }
}
