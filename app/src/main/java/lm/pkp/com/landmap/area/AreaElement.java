package lm.pkp.com.landmap.area;

import org.apache.commons.lang3.SerializationUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lm.pkp.com.landmap.drive.DriveResource;
import lm.pkp.com.landmap.position.PositionElement;

/**
 * Created by USER on 10/16/2017.
 */
public class AreaElement implements Serializable{

    private String name;
    private String description;
    private String createdBy;
    private String ownershipType;
    private String currentOwner;

    private double centerLat = 0.0;
    private double centerLon = 0.0;
    private double measureSqFt = 0.0;

    private String uniqueId;
    private String tags;

    private List<PositionElement> positions = new ArrayList<>();

    private List<DriveResource> driveResources = new ArrayList<>();

    public String getOwnershipType() {
        return ownershipType;
    }

    public String getCurrentOwner() {
        return currentOwner;
    }

    public void setCurrentOwner(String currentOwner) {
        this.currentOwner = currentOwner;
    }

    public void setOwnershipType(String ownershipType) {
        this.ownershipType = ownershipType;
    }

    public double getMeasureSqFt() {
        return measureSqFt;
    }

    public void setMeasureSqFt(double measureSqFt) {
        this.measureSqFt = measureSqFt;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getCenterLat() {
        return centerLat;
    }

    public void setCenterLat(double centerLat) {
        this.centerLat = centerLat;
    }

    public double getCenterLon() {
        return centerLon;
    }

    public void setCenterLon(double centerLon) {
        this.centerLon = centerLon;
    }

    public List<PositionElement> getPositions() {
        return positions;
    }

    public void setPositions(List<PositionElement> positions) {
        this.positions = positions;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public AreaElement copy(){
        return SerializationUtils.clone(this);
    }

    public List<DriveResource> getDriveResources() {
        return driveResources;
    }

    public void setDriveResources(List<DriveResource> driveResources) {
        this.driveResources = driveResources;
    }
}
