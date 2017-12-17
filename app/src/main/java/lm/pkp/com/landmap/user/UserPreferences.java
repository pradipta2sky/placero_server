package lm.pkp.com.landmap.user;

import java.util.ArrayList;
import java.util.List;

import lm.pkp.com.landmap.tags.TagElement;

/**
 * Created by USER on 12/15/2017.
 */
public class UserPreferences {

    private String searchQuery = "";
    private List<TagElement> tags = new ArrayList<>();
    private String dashboardId = "0";
    private boolean filteringEnabled = false;

    public String getSearchQuery() {
        return this.searchQuery;
    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }

    public List<TagElement> getTags() {
        return this.tags;
    }

    public void setTags(List<TagElement> tags) {
        this.tags = tags;
    }

    public String getDashboardId() {
        return this.dashboardId;
    }

    public void setDashboardId(String dashboardId) {
        this.dashboardId = dashboardId;
    }

    public boolean isFilteringEnabled() {
        return this.filteringEnabled;
    }

    public void setFilteringEnabled(boolean filteringEnabled) {
        this.filteringEnabled = filteringEnabled;
    }
}
