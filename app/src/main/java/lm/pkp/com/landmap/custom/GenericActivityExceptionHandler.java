package lm.pkp.com.landmap.custom;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import lm.pkp.com.landmap.google.signin.SignInActivity;
import lm.pkp.com.landmap.mail.GMailSender;

/**
 * Created by USER on 11/8/2017.
 */
public class GenericActivityExceptionHandler implements Thread.UncaughtExceptionHandler {

    private Activity activity;

    public GenericActivityExceptionHandler(Activity context) {
        this.activity = context;
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        final StringBuffer content = new StringBuffer();
        content.append("\n\n Exception trace ! \n");

        final StackTraceElement[] exceptionTrace = ex.getStackTrace();
        for (int i = 0; i < exceptionTrace.length; i++) {
            content.append(exceptionTrace[i].toString());
            content.append("\n");
        }

        content.append("\n\n Thread trace ! \n");
        final StackTraceElement[] threadStackTrace = thread.getStackTrace();
        for (int i = 0; i < threadStackTrace.length; i++) {
            content.append(threadStackTrace[i].toString());
            content.append("\n");
        }

        if (ex instanceof UserUnavailableException) {
            // Check why the user went off. ?? Do something here.
        }

        sendEmail(content.toString());
        restartApplication();
    }


    private void restartApplication() {
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

    private void sendEmail(String content) {
        try {
            final Date currDate = Calendar.getInstance(TimeZone.getTimeZone("Asia/Calcutta")).getTime();
            GMailSender sender = new GMailSender("pradhans.prasanna@gmail.com", "baramania");
            sender.sendMail("Landmap crash report - " + currDate, content,
                    "pradhans.prasanna@gmail.com", "pradipta2sky@gmail.com");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
