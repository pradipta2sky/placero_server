package lm.pkp.com.landmap.user;

/**
 * Created by USER on 10/24/2017.
 */
public class UserElement {

    private String displayName;
    private String email;
    private String familyName;
    private String givenName;
    private String photoUrl;
    private String authSystemId;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getAuthSystemId() {
        return authSystemId;
    }

    public void setAuthSystemId(String authSystemId) {
        this.authSystemId = authSystemId;
    }

}
