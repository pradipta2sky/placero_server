package lm.pkp.com.landmap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by USER on 10/17/2017.
 */
public class PlotterContext {

    private static PlotterContext ourInstance = new PlotterContext();

    public static PlotterContext getInstance() {
        return ourInstance;
    }

    private PlotterContext() {
    }

    private Map<String, List<PositionElement>> areaMap = new HashMap<String, List<PositionElement>>();

    public void putArea(String areaName, List<PositionElement> positions){
        areaMap.put(areaName,positions);
    }

    public List<PositionElement> getPositionsForArea(String areaName){
        return areaMap.get(areaName);
    }

    public void removeArea(String areaName){
        areaMap.remove(areaName);
    }
}
