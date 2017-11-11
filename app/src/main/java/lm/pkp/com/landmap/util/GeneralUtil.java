package lm.pkp.com.landmap.util;

/**
 * Created by USER on 11/11/2017.
 */
public class GeneralUtil {

    private GeneralUtil(){

    }

    public final static boolean isValidEmail(CharSequence target) {
        if (target == null) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }
}
