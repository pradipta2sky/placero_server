package lm.pkp.com.landmap.user;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import lm.pkp.com.landmap.custom.LandmapApplication;
import lm.pkp.com.landmap.custom.UserUnavailableException;
import lm.pkp.com.landmap.google.signin.SignInActivity;

/**
 * Created by USER on 10/24/2017.
 */
public class UserContext {

    private static UserContext ourInstance = new UserContext();

    public static UserContext getInstance() {
        return ourInstance;
    }

    private UserContext() {
    }

    private UserElement userElement = null;

    public UserElement getUserElement() {
        if(userElement == null){
           throw new UserUnavailableException();
        }
        return userElement;
    }

    public void setUserElement(UserElement userElement) {
        this.userElement = userElement;
    }
}
