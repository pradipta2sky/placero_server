package lm.pkp.com.landmap.util;

import android.net.Uri;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import lm.pkp.com.landmap.user.UserElement;

/**
 * Created by USER on 10/24/2017.
 */
public class UserMappingUtil {

    public static final UserElement convertGoogleAccountToLocalAccount(GoogleSignInAccount acct) {
        UserElement ue = new UserElement();
        ue.setDisplayName(acct.getDisplayName());
        ue.setEmail(acct.getEmail());
        ue.setFamilyName(acct.getFamilyName());
        ue.setGivenName(acct.getGivenName());
        ue.setAuthSystemId(acct.getServerAuthCode());
        Uri photoUri = acct.getPhotoUrl();
        if (photoUri != null) {
            ue.setPhotoUrl(photoUri.toString());
        } else {
            ue.setPhotoUrl("");
        }
        return ue;
    }
}
