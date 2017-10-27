package lm.pkp.com.landmap.user;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;

import javax.net.ssl.HttpsURLConnection;

import lm.pkp.com.landmap.user.UserContext;

/**
 * Created by Rinky on 21-10-2017.
 */

public class UserInfoSearchAsyncTask extends AsyncTask<JSONObject, Void, String>{

    private ArrayAdapter<String> localAdaptor = null;

    public UserInfoSearchAsyncTask(ArrayAdapter<String> adapter) {
        this.localAdaptor = adapter;
    }

    protected void onPreExecute() {
    }

    protected String doInBackground(JSONObject... postDataParams) {
        try {
            String urlString = "http://35.202.7.223/lm/UserSearch.php?us=";
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
            localAdaptor.clear();
            String currUserEmail = UserContext.getInstance().getUserElement().getEmail();
            JSONArray responseArr = new JSONArray(s);
            for (int i = 0; i < responseArr.length(); i++) {
                JSONObject responseObj = (JSONObject) responseArr.get(i);
                String emailStr = (String) responseObj.get("email");
                if(!currUserEmail.equalsIgnoreCase(emailStr)){
                    localAdaptor.add((String) responseObj.get("email"));
                }
            }
            localAdaptor.notifyDataSetChanged();
        }catch (Exception e){
            e.printStackTrace();
        }
        System.out.println(s);
    }
}
