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

final class VideoDataHolder {

    public static final VideoDataHolder INSTANCE = new VideoDataHolder();

    public ArrayList<VideoDisplayElement> getData() {
        final ArrayList<VideoDisplayElement> videoItems = new ArrayList<>();
        final AreaElement areaElement = AreaContext.INSTANCE.getAreaElement();
        List<DriveResource> driveResources = areaElement.getDriveResources();
        String imgRootPath = AreaContext.INSTANCE.getAreaLocalVideoRoot().getAbsolutePath()
                + File.separatorChar;
        for (int i = 0; i < driveResources.size(); i++) {
            final DriveResource resource = driveResources.get(i);
            if (resource.getType().equals("file")) {
                if (resource.getContentType().equals("Video")) {
                    final VideoDisplayElement videoDisplayElement = new VideoDisplayElement();
                    videoDisplayElement.setName(resource.getName());
                    videoDisplayElement.setAbsPath(imgRootPath + resource.getName());
                    videoItems.add(videoDisplayElement);
                }
            }
        }
        return videoItems;
    }
}
