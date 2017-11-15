package lm.pkp.com.landmap.area.tasks;

import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import lm.pkp.com.landmap.drive.DriveDBHelper;
import lm.pkp.com.landmap.drive.DriveResource;

/**
 * Created by Rinky on 21-10-2017.
 */

public class AreaDriveResourcesLoadTask extends AsyncTask<JSONObject, Void, String> {

    private Context localContext = null;
    private DriveDBHelper ddh = null;

    public AreaDriveResourcesLoadTask(Context appContext) {
        this.localContext = appContext;
        ddh = new DriveDBHelper(this.localContext);
    }

    protected void onPreExecute() {
    }

    protected String doInBackground(JSONObject... postDataParams) {
        try {
            String urlString = "http://35.202.7.223/lm/DriveSearch.php";

            JSONObject postDataParam = postDataParams[0];
            String searchField = postDataParam.getString("sf");
            String sfURL = "?sf=" + searchField;
            String searchStr = postDataParam.getString("ss");
            String ssURL = "&ss=" + searchStr;

            URL url = new URL(urlString + sfURL + ssURL);

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
            JSONArray responseArr = new JSONArray(s);
            for (int i = 0; i < responseArr.length(); i++) {
                JSONObject responseObj = (JSONObject) responseArr.get(i);

                DriveResource dr = new DriveResource();
                dr.setUniqueId((String) responseObj.get("unique_id"));
                dr.setDriveId((String) responseObj.get("drive_id"));
                dr.setUserId((String) responseObj.get("user_id"));
                dr.setName((String) responseObj.get("name"));
                dr.setAreaId((String) responseObj.get("area_id"));
                dr.setType((String) responseObj.get("type"));
                dr.setSize((String) responseObj.get("size"));

                ddh.insertResourceFromServer(dr);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(s);
    }
}
