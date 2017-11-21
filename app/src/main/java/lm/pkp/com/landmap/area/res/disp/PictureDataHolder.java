package lm.pkp.com.landmap.area.res.disp;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import lm.pkp.com.landmap.area.AreaContext;
import lm.pkp.com.landmap.area.AreaElement;
import lm.pkp.com.landmap.drive.DriveResource;

/**
 * Created by USER on 11/6/2017.
 */

final class PictureDataHolder {

    public static final PictureDataHolder INSTANCE = new PictureDataHolder();

    public ArrayList<PictureDisplayElement> getData() {
        ArrayList<PictureDisplayElement> imageItems = new ArrayList<>();
        AreaContext ac = AreaContext.INSTANCE;

        AreaElement ae = ac.getAreaElement();
        List<DriveResource> driveResources = ae.getMediaResources();
        String imgRootPath = ac.getAreaLocalImageRoot(ae.getUniqueId()).getAbsolutePath() + File.separatorChar;
        for (int i = 0; i < driveResources.size(); i++) {
            DriveResource resource = driveResources.get(i);
            if (resource.getType().equals("file")) {
                if (resource.getContentType().equals("Image")) {
                    PictureDisplayElement imageDisplayElement = new PictureDisplayElement();
                    imageDisplayElement.setName(resource.getName());
                    imageDisplayElement.setAbsPath(imgRootPath + resource.getName());
                    imageDisplayElement.setResourceId(resource.getResourceId());
                    imageItems.add(imageDisplayElement);
                }
            }
        }
        return imageItems;
    }
}
