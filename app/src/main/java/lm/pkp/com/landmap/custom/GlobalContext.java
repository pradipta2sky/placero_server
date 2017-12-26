package lm.pkp.com.landmap.custom;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by USER on 11/30/2017.
 */
public class GlobalContext {

    public static final GlobalContext INSTANCE = new GlobalContext();

    public static final String INTERNET_AVAILABLE = "lm.pkp.com.INTERNET_AVAILABLE";
    public static final String APPLICATION_STARTED = "lm.pkp.com.APPLICATION_START";

    private GlobalContext(){
    }

    private Map<String, String> store = new HashMap<>();

    public void put(String key, String value){
        store.put(key, value);
    }

    public String get(String key){
        return store.get(key);
    }

}
