package lm.pkp.com.landmap.area.model;

import org.apache.commons.lang3.SerializationUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lm.pkp.com.landmap.drive.DriveResource;
import lm.pkp.com.landmap.permission.PermissionElement;
import lm.pkp.com.landmap.position.PositionElement;

/**
 * Created by USER on 10/16/2017.
 */
public class AreaElement implements Serializable {

    private String name;
    private String description;
    private String createdBy;
    private String type;
    private String uniqueId;
    private AreaAddress address;
    private AreaMeasure measure;

    private PositionElement centerPosition = new PositionElement();
    private List<PositionElement> positions = new ArrayList<>();
    private List<DriveResource> mediaResources = new ArrayList<>();
    private Map<String, PermissionElement> userPermissions = new HashMap<>();

    public String getCreatedBy() {
        return this.createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<PositionElement> getPositions() {
        int size = positions.size();
        for (int i = 0; i < size; i++) {
            positions.get(i).setDisplayName("Position_" + (i + 1));
        }
        return this.positions;
    }

    public void setPositions(List<PositionElement> positions) {
        this.positions = positions;
    }

    public String getUniqueId() {
        return this.uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public AreaElement copy() {
        return SerializationUtils.clone(this);
    }

    public List<DriveResource> getMediaResources() {
        return this.mediaResources;
    }

    public void setMediaResources(List<DriveResource> mediaResources) {
        this.mediaResources = mediaResources;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, PermissionElement> getUserPermissions() {
        return userPermissions;
    }

    public void setUserPermissions(Map<String, PermissionElement> userPermissions) {
        this.userPermissions = userPermissions;
    }

    public PositionElement getCenterPosition() {
        return centerPosition;
    }

    public AreaAddress getAddress() {
        return this.address;
    }

    public void setAddress(AreaAddress address) {
        this.address = address;
    }

    public AreaMeasure getMeasure() {
        return this.measure;
    }

    public void setMeasure(AreaMeasure measure) {
        this.measure = measure;
    }
}
