package lm.pkp.com.landmap.area;

import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import lm.pkp.com.landmap.SplashActivity;
import lm.pkp.com.landmap.custom.AsyncTaskCallback;

/**
 * Created by Rinky on 21-10-2017.
 */

public class UserAreaLoadTask extends AsyncTask<JSONObject, Void, String>{

    private Context localContext = null;
    private AreaDBHelper adh = null;
    private AsyncTaskCallback callback = null;

    public UserAreaLoadTask(Context appContext) {
        this.localContext = appContext;
        adh = new AreaDBHelper(this.localContext);
    }

    protected void onPreExecute() {
    }

    protected String doInBackground(JSONObject... postDataParams) {
        try {
            String urlString = "http://35.202.7.223/lm/AreaSearch.php?us=";
            JSONObject postDataParam = postDataParams[0];
            String searchKey = postDataParam.getString("us");
            URL url = new URL(urlString+searchKey);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new
                        InputStreamReader(
                        conn.getInputStream()));

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
            AreaPositionsLoadTask apt = new AreaPositionsLoadTask(localContext);

            for (int i = 0; i < responseArr.length(); i++) {
                JSONObject responseObj = (JSONObject) responseArr.get(i);

                AreaElement ae = new AreaElement();
                ae.setName(responseObj.getString("name"));
                ae.setCreatedBy(responseObj.getString("created_by"));
                ae.setDescription(responseObj.getString("description"));
                ae.setCenterLat(new Double((String) responseObj.get("center_lat")));
                ae.setCenterLon(new Double((String) responseObj.get("center_lon")));
                ae.setUniqueId(responseObj.getString("unique_id"));
                ae.setMeasureSqFt(new Double((String) responseObj.get("measure_sqft")));
                ae.setOwnershipType("self");
                ae.setCurrentOwner(responseObj.getString("curr_own"));
                ae.setTags(responseObj.getString("tags"));
                adh.insertAreaFromServer(ae);

                JSONObject searchObj = new JSONObject();
                searchObj.put("aid", ae.getUniqueId());
                apt.execute(searchObj);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        finalizeTaskCompletion();
        System.out.println(s);
    }

    public void setCompletionCallback(AsyncTaskCallback callback) {
        this.callback = callback;
    }

    public void finalizeTaskCompletion(){
        callback.taskCompleted();
    }
}
