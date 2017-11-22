package lm.pkp.com.landmap.area.res.disp;

import java.io.File;

/**
 * Created by USER on 11/6/2017.
 */
public class PictureDisplayElement {

    private String name = "";
    private String absPath = "";
    private String resourceId = "";
    private File thumbnailFile = null;
    private File imageFile = null;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAbsPath() {
        return absPath;
    }

    public void setAbsPath(String absPath) {
        this.absPath = absPath;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public File getThumbnailFile() {
        return this.thumbnailFile;
    }

    public void setThumbnailFile(File thumbnailFile) {
        this.thumbnailFile = thumbnailFile;
    }

    public File getImageFile() {
        return this.imageFile;
    }

    public void setImageFile(File imageFile) {
        this.imageFile = imageFile;
    }
}
