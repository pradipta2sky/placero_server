package lm.pkp.com.landmap.permission;

/**
 * Created by USER on 11/11/2017.
 */
public class PermissionElement {

    private String areaId;
    private String userId;
    private String functionCode;

    public String getAreaId() {
        return areaId;
    }

    public void setAreaId(String areaId) {
        this.areaId = areaId;
    }

    public String getFunctionCode() {
        return functionCode;
    }

    public void setFunctionCode(String functionCode) {
        this.functionCode = functionCode;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
