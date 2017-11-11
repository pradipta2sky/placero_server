package lm.pkp.com.landmap.custom;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

import lm.pkp.com.landmap.google.signin.SignInActivity;
import lm.pkp.com.landmap.mail.GMailSender;

/**
 * Created by USER on 11/8/2017.
 */
public class GenericActivityExceptionHandler implements Thread.UncaughtExceptionHandler {

    private Activity activity;

    public GenericActivityExceptionHandler(Activity context){
        this.activity = context;
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        final StringBuffer content = new StringBuffer();
        content.append("\n\n Exception trace ! \n");

        System.out.println("\n\n Exception trace ! \n");
        final StackTraceElement[] exceptionTrace = ex.getStackTrace();
        for (int i = 0; i < exceptionTrace.length ; i++) {
            System.out.println(exceptionTrace[i]);
            content.append(exceptionTrace[i].toString());
            content.append("\n");
        }

        content.append("\n\n Thread trace ! \n");
        System.out.println("\n\n Thread trace ! \n");
        final StackTraceElement[] threadStackTrace = thread.getStackTrace();
        for (int i = 0; i < threadStackTrace.length ; i++) {
            System.out.println(threadStackTrace[i]);
            content.append(threadStackTrace[i].toString());
            content.append("\n");
        }

        Runnable r = new Runnable() {
            @Override
            public void run() {
                sendEmail(content.toString());
            }
        };

        if(ex instanceof  UserUnavailableException){
            // Check why the user went off. ?? Do something here.
        }

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

    private void sendEmail(String content){
        try {
            GMailSender sender = new GMailSender("pradhans.prasanna@gmail.com", "baramania");
            sender.sendMail("Landmap crash report",
                    content,
                    "pradhans.prasanna@gmail.com",
                    "pradipta2sky@gmail.com");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
