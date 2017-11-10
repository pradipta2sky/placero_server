package lm.pkp.com.landmap.custom;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import lm.pkp.com.landmap.google.signin.SignInActivity;

/**
 * Created by USER on 11/8/2017.
 */
public class GenericActivityExceptionHandler implements Thread.UncaughtExceptionHandler {

    public static final String EXTRA_MY_EXCEPTION_HANDLER = "EXTRA_MY_EXCEPTION_HANDLER";
    private Activity activity;
    private Thread.UncaughtExceptionHandler rootHandler;

    public GenericActivityExceptionHandler(Activity context){
        this.activity = context;
        rootHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        ex.printStackTrace();

        Intent intent = new Intent(activity, SignInActivity.class);
        intent.putExtra("crash", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_CLEAR_TASK
                | Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                LandmapApplication.getInstance().getBaseContext(), 0,
                intent, PendingIntent.FLAG_ONE_SHOT);

        AlarmManager mgr = (AlarmManager) LandmapApplication.getInstance().getBaseContext()
                .getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, pendingIntent);
        activity.finish();
        System.exit(2);
    }
}
