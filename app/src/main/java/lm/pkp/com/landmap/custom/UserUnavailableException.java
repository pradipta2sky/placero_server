package lm.pkp.com.landmap.custom;

import com.google.api.services.drive.model.User;

/**
 * Created by USER on 11/11/2017.
 */
public class UserUnavailableException extends RuntimeException {

    public UserUnavailableException(){
        super();
    }

    public  UserUnavailableException(Exception e){
        super(e);
    }

    public UserUnavailableException(String message){
        super(message);
    }
}
