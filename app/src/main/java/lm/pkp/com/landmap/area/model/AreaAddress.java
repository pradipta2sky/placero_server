package lm.pkp.com.landmap.area.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by USER on 11/27/2017.
 */
public class AreaAddress {

    private Map<String, String> address = new HashMap<>();

    public Map<String, String> getAddress() {
        return this.address;
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        Collection<String> values = address.values();
        Iterator<String> valuesIter = values.iterator();
        while (valuesIter.hasNext()){
            String value = valuesIter.next();
            buf.append(value);
            if(valuesIter.hasNext()){
                buf.append("@$");
            }
        }
        return buf.toString();
    }
}
