package lm.pkp.com.landmap.custom;

/**
 * Created by USER on 11/11/2017.
 */
public class UserUnavailableException extends RuntimeException {

    public UserUnavailableException() {
        super();
    }

    public UserUnavailableException(Exception e) {
        super(e);
    }

    public UserUnavailableException(String message) {
        super(message);
    }
}
