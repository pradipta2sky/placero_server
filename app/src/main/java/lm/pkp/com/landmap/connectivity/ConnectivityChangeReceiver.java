package lm.pkp.com.landmap.connectivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import lm.pkp.com.landmap.connectivity.services.AreaSynchronizationService;
import lm.pkp.com.landmap.connectivity.services.PositionSynchronizationService;
import lm.pkp.com.landmap.connectivity.services.ResourceSynchronizationService;
import lm.pkp.com.landmap.custom.GlobalContext;

public class ConnectivityChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean connected = isConnected(context);
        // For testing
        //connected = false;
        if(!connected){
            context.sendBroadcast(new Intent("INTERNET_LOST"));
        }else {
            context.sendBroadcast(new Intent("INTERNET_AVAILABLE"));
            if(new Boolean(GlobalContext.INSTANCE.get(GlobalContext.APPLICATION_STARTED))){
                context.startService(new Intent(context, AreaSynchronizationService.class));
                context.startService(new Intent(context, PositionSynchronizationService.class));
                context.startService(new Intent(context, ResourceSynchronizationService.class));
            }
        }
        GlobalContext.INSTANCE.put(GlobalContext.INTERNET_AVAILABLE,
                new Boolean(connected).toString());
    }

    public static boolean isConnected(Context context) {
        ConnectivityManager connectivityManager
                = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected();
    }

}