package lm.pkp.com.landmap.area.res.disp;

import lm.pkp.com.landmap.area.model.FileItemMarker;

/**
 * Created by USER on 11/7/2017.
 */
public class FileDisplayElement implements FileItemMarker {

    private int icon;
    private String name;
    private String desc;
    private String thumb;
    private String mimeType;
    private String path;
    private String created;
    private String lastModified;
    private String sizeBytes;

    public int getIcon() {
        return this.icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return this.desc;
    }

    public void setDesc(String des) {
        this.desc = des;
    }

    public String getThumb() {
        return this.thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }

    public String getMimeType() {
        return this.mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getCreated() {
        return this.created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getLastModified() {
        return this.lastModified;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

    public String getSizeBytes() {
        return this.sizeBytes;
    }

    public void setSizeBytes(String sizeBytes) {
        this.sizeBytes = sizeBytes;
    }
}
