package lm.pkp.com.landmap.util;

import android.graphics.Color;

import lm.pkp.com.landmap.area.AreaContext;
import lm.pkp.com.landmap.area.AreaElement;

/**
 * Created by USER on 11/12/2017.
 */
public class ColorConstants {

    private ColorConstants(){
    }

    public static final int BUFF_YELLOW_TOOLBAR = Color.parseColor("#556E01");
    public static final int BUFF_BLUE_TOOLBAR = Color.parseColor("#4A5C8F");
    public static final int DEFAULT_TOOLBAR_COLOR = Color.parseColor("#07063D");

    public static final int BUFF_YELLOW_AREA_DISPLAY = Color.parseColor("#CCEA69");
    public static final int BUFF_BLUE_AREA_DISPLAY = Color.parseColor("#C5D2F7");

    public static final int getToolBarColorForShare(){
        final AreaElement areaElement = AreaContext.getInstance().getAreaElement();
        if(!areaElement.getType().equalsIgnoreCase("shared")){
            return BUFF_BLUE_TOOLBAR;
        }else {
            return BUFF_YELLOW_TOOLBAR;
        }
    }

    public static final int getAreaColorForShare(){
        final AreaElement areaElement = AreaContext.getInstance().getAreaElement();
        if(!areaElement.getType().equalsIgnoreCase("shared")){
            return BUFF_BLUE_AREA_DISPLAY;
        }else {
            return BUFF_YELLOW_AREA_DISPLAY;
        }
    }

    public static int getDefaultToolBarColor() {
        return DEFAULT_TOOLBAR_COLOR;
    }
}
