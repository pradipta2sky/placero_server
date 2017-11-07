package lm.pkp.com.landmap.area.res.disp;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import lm.pkp.com.landmap.area.AreaContext;
import lm.pkp.com.landmap.area.AreaElement;
import lm.pkp.com.landmap.drive.DriveResource;
import lm.pkp.com.landmap.sync.LocalFolderStructureManager;

/**
 * Created by USER on 11/6/2017.
 */

final class PictureDataHolder {

    public static final PictureDataHolder INSTANCE = new PictureDataHolder();

    public ArrayList<ImageDisplayElement> getData() {
        final ArrayList<ImageDisplayElement> imageItems = new ArrayList<>();
        final AreaElement areaElement = AreaContext.getInstance().getAreaElement();
        List<DriveResource> driveResources = areaElement.getDriveResources();
        String imgRootPath = LocalFolderStructureManager.getImageStorageDir().getAbsolutePath()
                + File.separatorChar;
        for (int i = 0; i < driveResources.size(); i++) {
            final DriveResource resource = driveResources.get(i);
            if(resource.getType().equals("file")){
                if(resource.getContentType().equals("Image")){
                    final ImageDisplayElement imageDisplayElement = new ImageDisplayElement();
                    imageDisplayElement.setName(resource.getName());
                    imageDisplayElement.setAbsPath(imgRootPath + resource.getName());
                    imageItems.add(imageDisplayElement);
                }
            }
        }
        return imageItems;
    }
}
