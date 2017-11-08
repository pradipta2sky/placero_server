package lm.pkp.com.landmap.custom;

import android.app.Activity;

/**
 * Created by USER on 11/8/2017.
 */
public class GenericActivityExceptionHandler implements Thread.UncaughtExceptionHandler {

    public static final String EXTRA_MY_EXCEPTION_HANDLER = "EXTRA_MY_EXCEPTION_HANDLER";
    private Activity context;
    private Thread.UncaughtExceptionHandler rootHandler;

    public GenericActivityExceptionHandler(Activity context){
        this.context = context;
        // we should store the current exception handler -- to invoke it for all not handled exceptions ...
        rootHandler = Thread.getDefaultUncaughtExceptionHandler();
        // we replace the exception handler now with us -- we will properly dispatch the exceptions ...
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        ex.printStackTrace();
    }
}
