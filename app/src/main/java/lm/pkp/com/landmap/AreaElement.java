package lm.pkp.com.landmap;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by USER on 10/16/2017.
 */
public class AreaElement {

    private Integer id;
    private String name;
    private String description;
    private String createdBy;
    private double centerLat;
    private double centerLon;
    private String unique_id;
    private String tags;
    private List<PositionElement> positions = new ArrayList<PositionElement>();

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

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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
        return unique_id;
    }

    public void setUniqueId(String unique_id) {
        this.unique_id = unique_id;
    }
}
