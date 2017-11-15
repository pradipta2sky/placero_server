package lm.pkp.com.landmap.user;

import lm.pkp.com.landmap.custom.UserUnavailableException;

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
        if (userElement == null) {
            throw new UserUnavailableException();
        }
        return userElement;
    }

    public void setUserElement(UserElement userElement) {
        this.userElement = userElement;
    }
}
