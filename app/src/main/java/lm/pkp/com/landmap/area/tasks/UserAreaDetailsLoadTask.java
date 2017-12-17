package lm.pkp.com.landmap.area.tasks;

import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import javax.net.ssl.HttpsURLConnection;

import lm.pkp.com.landmap.area.model.AreaAddress;
import lm.pkp.com.landmap.area.model.AreaElement;
import lm.pkp.com.landmap.area.db.AreaDBHelper;
import lm.pkp.com.landmap.area.model.AreaMeasure;
import lm.pkp.com.landmap.custom.AsyncTaskCallback;
import lm.pkp.com.landmap.drive.DriveDBHelper;
import lm.pkp.com.landmap.drive.DriveResource;
import lm.pkp.com.landmap.permission.PermissionElement;
import lm.pkp.com.landmap.permission.PermissionsDBHelper;
import lm.pkp.com.landmap.position.PositionElement;
import lm.pkp.com.landmap.position.PositionsDBHelper;
import lm.pkp.com.landmap.tags.TagsDBHelper;
import lm.pkp.com.landmap.user.UserContext;
import lm.pkp.com.landmap.user.UserElement;

public class UserAreaDetailsLoadTask extends AsyncTask<JSONObject, Void, String> {

    private Context localContext;
    private AreaDBHelper adh;
    private PositionsDBHelper pdh;
    private DriveDBHelper ddh;
    private PermissionsDBHelper pmh;
    private TagsDBHelper tdh;

    private AsyncTaskCallback callback;

    public UserAreaDetailsLoadTask(Context appContext) {
        localContext = appContext;
        adh = new AreaDBHelper(localContext);
        pdh = new PositionsDBHelper(localContext);
        ddh = new DriveDBHelper(localContext);
        pmh = new PermissionsDBHelper(localContext, null);
        tdh = new TagsDBHelper(localContext, null);
    }

    protected void onPreExecute() {
    }

    protected String doInBackground(JSONObject... postDataParams) {
        try {
            String urlString = "http://35.202.7.223/lm/AreaSearch.php?us=";
            JSONObject postDataParam = postDataParams[0];
            String searchKey = postDataParam.getString("us");
            URL url = new URL(urlString + URLEncoder.encode(searchKey, "utf-8"));

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                StringBuffer sb = new StringBuffer("");
                String line = "";
                while ((line = in.readLine()) != null) {
                    sb.append(line);
                    break;
                }
                in.close();
                return sb.toString();
            } else {
                return new String("false : " + responseCode);
            }
        } catch (Exception e) {
            return new String("Exception: " + e.getMessage());
        }
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        try {
            JSONObject responseObj = new JSONObject(s);
            JSONArray areaResponse = responseObj.getJSONArray("area_response");
            for (int i = 0; i < areaResponse.length(); i++) {
                JSONObject areaResponseObj = (JSONObject) areaResponse.get(i);
                JSONObject areaObj = (JSONObject) areaResponseObj.get("area");
                AreaElement ae = new AreaElement();
                ae.setName(areaObj.getString("name"));
                ae.setCreatedBy(areaObj.getString("created_by"));
                ae.setDescription(areaObj.getString("description"));
                ae.getCenterPosition().setLat(areaObj.getDouble("center_lat"));
                ae.getCenterPosition().setLon(areaObj.getDouble("center_lon"));
                ae.setUniqueId(areaObj.getString("unique_id"));

                double msqFt = areaObj.getDouble("measure_sqft");
                AreaMeasure measure = new AreaMeasure(msqFt);
                ae.setMeasure(measure);

                String addressText = areaObj.getString("address");
                AreaAddress areaAddress = AreaAddress.fromStoredAddress(addressText);
                if(areaAddress != null){
                    ae.setAddress(areaAddress);
                    tdh.insertTagsLocally(areaAddress.getTags(), "area", ae.getUniqueId());
                }
                ae.setType(areaObj.getString("type"));

                adh.insertAreaFromServer(ae);

                JSONArray positionsArr = (JSONArray) areaResponseObj.get("positions");
                for (int p = 0; p < positionsArr.length(); p++) {
                    JSONObject positionObj = (JSONObject) positionsArr.get(p);
                    PositionElement pe = new PositionElement();
                    pe.setUniqueId((String) positionObj.get("unique_id"));
                    pe.setUniqueAreaId((String) positionObj.get("unique_area_id"));
                    pe.setName((String) positionObj.get("name"));
                    pe.setDescription((String) positionObj.get("description"));
                    pe.setLat(positionObj.getDouble("lat"));
                    pe.setLon(positionObj.getDouble("lon"));
                    pe.setTags((String) positionObj.get("tags"));
                    pe.setCreatedOnMillis(positionObj.getString("created_on"));

                    pdh.insertPositionFromServer(pe);
                }

                JSONArray driveArr = (JSONArray) areaResponseObj.get("drs");
                for (int d = 0; d < driveArr.length(); d++) {
                    JSONObject driveObj = (JSONObject) driveArr.get(d);
                    DriveResource dr = new DriveResource();
                    dr.setUniqueId(driveObj.getString("unique_id"));
                    dr.setAreaId(driveObj.getString("area_id"));
                    dr.setUserId(driveObj.getString("user_id"));
                    dr.setContainerId(driveObj.getString("container_id"));
                    dr.setResourceId(driveObj.getString("resource_id"));
                    dr.setName(driveObj.getString("name"));
                    dr.setType(driveObj.getString("type"));
                    dr.setSize(driveObj.getString("size"));
                    dr.setMimeType(driveObj.getString("mime_type"));
                    dr.setContentType(driveObj.getString("content_type"));
                    dr.setLatitude(driveObj.getString("latitude"));
                    dr.setLongitude(driveObj.getString("longitude"));
                    dr.setCreatedOnMillis(driveObj.getString("created_on"));

                    ddh.insertResourceFromServer(dr);
                }

                JSONArray permissionsArr = (JSONArray) areaResponseObj.get("permissions");
                for (int e = 0; e < permissionsArr.length(); e++) {
                    JSONObject permissionObj = (JSONObject) permissionsArr.get(e);
                    PermissionElement pe = new PermissionElement();
                    pe.setUserId(permissionObj.getString("user_id"));
                    pe.setAreaId(permissionObj.getString("area_id"));
                    pe.setFunctionCode(permissionObj.getString("function_code"));

                    pmh.insertPermissionLocally(pe);
                }
            }

            JSONArray commonResponse = responseObj.getJSONArray("common_response");
            for (int i = 0; i < commonResponse.length(); i++) {
                JSONObject driveObj = commonResponse.getJSONObject(i);
                DriveResource resource = new DriveResource();
                resource.setUniqueId(driveObj.getString("unique_id"));
                resource.setAreaId(driveObj.getString("area_id"));
                resource.setUserId(driveObj.getString("user_id"));
                resource.setContainerId(driveObj.getString("container_id"));
                resource.setResourceId(driveObj.getString("resource_id"));
                resource.setName(driveObj.getString("name"));
                resource.setType(driveObj.getString("type"));
                resource.setSize(driveObj.getString("size"));
                resource.setMimeType(driveObj.getString("mime_type"));
                resource.setContentType(driveObj.getString("content_type"));
                resource.setCreatedOnMillis(driveObj.getString("created_on"));

                ddh.insertResourceFromServer(resource);
            }

            UserElement userElement = UserContext.getInstance().getUserElement();
            tdh.insertTagsLocally(userElement.getPreferences().getTags(), "user", userElement.getEmail());
        } catch (Exception e) {
            e.printStackTrace();
        }
        finalizeTaskCompletion();
    }

    public void setCompletionCallback(AsyncTaskCallback callback) {
        this.callback = callback;
    }

    public void finalizeTaskCompletion() {
        if(callback != null){
            callback.taskCompleted("");
        }
    }
}
