package lm.pkp.com.landmap.area.res.disp;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import lm.pkp.com.landmap.area.AreaContext;
import lm.pkp.com.landmap.area.model.AreaElement;
import lm.pkp.com.landmap.drive.DriveResource;

/**
 * Created by USER on 11/6/2017.
 */

final class VideoDataHolder {

    public static final VideoDataHolder INSTANCE = new VideoDataHolder();

    public ArrayList<VideoDisplayElement> getData() {
        ArrayList<VideoDisplayElement> videoItems = new ArrayList<>();
        AreaContext ac = AreaContext.INSTANCE;

        AreaElement ae = ac.getAreaElement();
        List<DriveResource> driveResources = ae.getMediaResources();
        String videoRootPath = ac.getAreaLocalVideoRoot(ae.getUniqueId()).getAbsolutePath() + File.separatorChar;
        String thumbnailRoot = ac.getAreaLocalVideoThumbnailRoot(ae.getUniqueId()).getAbsolutePath();

        for (int i = 0; i < driveResources.size(); i++) {
            DriveResource resource = driveResources.get(i);
            if (resource.getType().equals("file")) {
                if (resource.getContentType().equals("Video")) {
                    VideoDisplayElement videoDisplayElement = new VideoDisplayElement();
                    videoDisplayElement.setName(resource.getName());
                    videoDisplayElement.setAbsPath(videoRootPath + resource.getName());
                    videoDisplayElement.setResourceId(resource.getResourceId());
                    videoDisplayElement.setThumbnailFile(new File(thumbnailRoot + File.separatorChar + resource.getName()));
                    videoDisplayElement.setVideoFile(new File(videoRootPath + File.separatorChar + resource.getName()));
                    videoItems.add(videoDisplayElement);
                }
            }
        }
        return videoItems;
    }
}
