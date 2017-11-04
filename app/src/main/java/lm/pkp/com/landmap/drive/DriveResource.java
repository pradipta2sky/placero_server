package lm.pkp.com.landmap.drive;

/**
 * Created by USER on 10/31/2017.
 */
public class DriveResource {

    private String uniqueId = "";
    private String userId = "";
    private String areaId = "";
    private String driveId = "";
    private String driveResourceId = "";
    private String containerDriveId = "";
    private String name = "";
    private String path = "";
    private String type = "";
    private String size = "";
    private String contentType = "";
    private String mimeType = "";

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getAreaId() {
        return areaId;
    }

    public void setAreaId(String areaId) {
        this.areaId = areaId;
    }

    public String getDriveId() {
        return driveId;
    }

    public void setDriveId(String driveId) {
        this.driveId = driveId;
    }

    public String getDriveResourceId() {
        return driveResourceId;
    }

    public void setDriveResourceId(String driveResourceId) {
        this.driveResourceId = driveResourceId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getContainerDriveId() {
        return containerDriveId;
    }

    public void setContainerDriveId(String containerDriveId) {
        this.containerDriveId = containerDriveId;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }


}
