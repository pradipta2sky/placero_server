package lm.pkp.com.landmap.drive;

import org.apache.commons.lang3.builder.EqualsBuilder;

import lm.pkp.com.landmap.position.PositionElement;

/**
 * Created by USER on 10/31/2017.
 */
public class DriveResource {

    private String uniqueId = "";
    private String userId = "";
    private String areaId = "";
    private String resourceId = "1"; // dummy for comparison
    private String containerId = "";
    private String name = "";
    private String path = "";
    private String type = "";
    private String size = "";
    private String contentType = "";
    private String mimeType = "";
    private Integer dirty = 0;
    private String dirtyAction = "";
    private String createdOnMillis = System.currentTimeMillis() + "";

    private PositionElement position = null;

    public String getUserId() {
        return this.userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUniqueId() {
        return this.uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getAreaId() {
        return this.areaId;
    }

    public void setAreaId(String areaId) {
        this.areaId = areaId;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSize() {
        return this.size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getContentType() {
        return this.contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getMimeType() {
        return this.mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    @Override
    public boolean equals(Object o) {
        EqualsBuilder builder = new EqualsBuilder().append(getResourceId(), ((DriveResource) o).getResourceId());
        return builder.isEquals();
    }

    public String getCreatedOnMillis() {
        return createdOnMillis;
    }

    public void setCreatedOnMillis(String createdOnMillis) {
        this.createdOnMillis = createdOnMillis;
    }

    public Integer isDirty() {
        return this.dirty;
    }

    public void setDirty(Integer dirty) {
        this.dirty = dirty;
    }

    public String getDirtyAction() {
        return this.dirtyAction;
    }

    public void setDirtyAction(String dirtyAction) {
        this.dirtyAction = dirtyAction;
    }

    public PositionElement getPosition() {
        return this.position;
    }

    public void setPosition(PositionElement position) {
        this.position = position;
    }
}
