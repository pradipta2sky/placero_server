package lm.pkp.com.landmap.area;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by USER on 11/23/2017.
 */
public class AreaDisplayMetaStore {

    private AreaDisplayMetaStore(){
        tabMeta.put("self", 0);
        tabMeta.put("shared", 1);
        tabMeta.put("public", 2);
    }

    public static final AreaDisplayMetaStore INSTANCE = new AreaDisplayMetaStore();

    private Map<String, Integer> tabMeta = new HashMap<>();

    public Integer getTabPositionByAreaType(String type){
        return tabMeta.get(type);
    }

}
