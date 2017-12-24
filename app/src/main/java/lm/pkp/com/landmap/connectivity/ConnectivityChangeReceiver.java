package lm.pkp.com.landmap.connectivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import lm.pkp.com.landmap.connectivity.services.AreaSynchronizationService;
import lm.pkp.com.landmap.connectivity.services.PositionSynchronizationService;
import lm.pkp.com.landmap.custom.GlobalContext;

public class ConnectivityChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        final boolean connected = isConnected(context);
        if(!connected){
            context.sendBroadcast(new Intent("INTERNET_LOST"));
        }else {
            context.sendBroadcast(new Intent("INTERNET_AVAILABLE"));
            context.startService(new Intent(context, AreaSynchronizationService.class));
            context.startService(new Intent(context, PositionSynchronizationService.class));
        }
        GlobalContext.INSTANCE.put(GlobalContext.INTERNET_AVAILABLE,
                new Boolean(connected).toString());
    }

    public boolean isConnected(Context context) {
        ConnectivityManager connectivityManager
                = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected();
    }

}