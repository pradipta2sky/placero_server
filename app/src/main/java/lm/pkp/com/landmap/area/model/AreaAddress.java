package lm.pkp.com.landmap.area.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by USER on 11/27/2017.
 */
public class AreaAddress {

    private String premises;
    private String subThoroughFare;
    private String thoroughFare;
    private String locality;
    private String subLocality;
    private String adminArea;
    private String subAdminArea;
    private String featureName;
    private String postalCode;
    private String country;

    public String getStorableAddress() {
        StringBuffer buf = new StringBuffer();

        // Add featureName.
        if(featureName != null){
            buf.append(featureName);
        }
        buf.append("@$");

        // Add premises.
        if(premises != null){
            if(featureName != null){
                if(!featureName.equalsIgnoreCase(premises)){
                    buf.append(premises);
                }
            }
        }
        buf.append("@$");

        // Add thoroughFare.
        if(thoroughFare != null){
            if(premises != null){
                if(!premises.equalsIgnoreCase(thoroughFare)){
                    buf.append(thoroughFare);
                }
            }
        }
        buf.append("@$");

        // Add subThoroughFare.
        if(subThoroughFare != null){
            buf.append(subThoroughFare);
        }
        buf.append("@$");

        // Add subLocality.
        if(subLocality != null){
            buf.append(subLocality);
        }
        buf.append("@$");

        // Add locality.
        if(locality != null){
            buf.append(locality);
        }
        buf.append("@$");

        // Add subAdminArea.
        if(subAdminArea != null){
            buf.append(subAdminArea);
        }
        buf.append("@$");

        // Add adminArea.
        if(adminArea != null){
            buf.append(adminArea);
            buf.append(",");
        }
        buf.append("@$");

        // Add postalCode.
        if(postalCode != null){
            buf.append(postalCode);
        }
        buf.append("@$");

        // Add country.
        if(country != null){
            buf.append(country);
        }
        buf.append("@$");

        return buf.toString();
    }

    public String getDisplaybleAddress(){
        String addressText = getStorableAddress();
        addressText = addressText.replaceAll(Pattern.quote("@$"), ",");
        addressText = addressText.replaceAll(",{2,}", ",");
        addressText = addressText.trim();
        addressText = addressText.replaceAll(",$", "");
        addressText = addressText.replaceFirst("^,", "");
        return addressText;
    }

    public static final AreaAddress fromStoredAddress(String addressText){
        String[] splittedAddress = addressText.split(Pattern.quote("@$"));
        if(splittedAddress.length != 10){
            return null;
        }
        AreaAddress address = new AreaAddress();
        address.setPremises(splittedAddress[0]);
        address.setFeatureName(splittedAddress[1]);
        address.setSubThoroughFare(splittedAddress[2]);
        address.setThoroughFare(splittedAddress[3]);
        address.setSubLocality(splittedAddress[4]);
        address.setLocality(splittedAddress[5]);
        address.setSubAdminArea(splittedAddress[6]);
        address.setAdminArea(splittedAddress[7]);
        address.setPostalCode(splittedAddress[8]);
        address.setCountry(splittedAddress[9]);
        return address;
    }

    public List<String> getTags(){
        List<String> tags = new ArrayList<>();
        if(premises != null){
            tags.add(premises);
        }
        if(subLocality != null){
            tags.add(subLocality);
        }
        if(locality != null){
            tags.add(locality);
        }
        if(subAdminArea != null){
            tags.add(subAdminArea);
        }
        if(adminArea != null){
            tags.add(adminArea);
        }
        if(postalCode != null){
            tags.add(postalCode);
        }
        if(country != null){
            tags.add(country);
        }
        return tags;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }

    public void setSubLocality(String subLocality) {
        this.subLocality = subLocality;
    }

    public void setAdminArea(String adminArea) {
        this.adminArea = adminArea;
    }

    public void setSubAdminArea(String subAdminArea) {
        this.subAdminArea = subAdminArea;
    }

    public void setFeatureName(String featureName) {
        this.featureName = featureName;
    }

    public void setPremises(String premises) {
        this.premises = premises;
    }

    public void setSubThoroughFare(String subThoroughFare) {
        this.subThoroughFare = subThoroughFare;
    }

    public void setThoroughFare(String thoroughFare) {
        this.thoroughFare = thoroughFare;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
