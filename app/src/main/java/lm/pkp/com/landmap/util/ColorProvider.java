package lm.pkp.com.landmap.util;

import android.graphics.Color;

import lm.pkp.com.landmap.area.AreaElement;

/**
 * Created by USER on 11/12/2017.
 */
public class ColorProvider {

    private ColorProvider() {
    }

    public static final int DEFAULT_TOOLBAR_COLOR = Color.parseColor("#07063D");

    public static final int BUFF_YELLOW_TOOLBAR = Color.parseColor("#556E01");
    public static final int BUFF_BLUE_TOOLBAR = Color.parseColor("#07063D");
    public static final int BUFF_ORANGE_TOOLBAR = Color.parseColor("#622807");

    public static final int BUFF_YELLOW_AREA_DISPLAY = Color.parseColor("#CCEA69");
    public static final int BUFF_BLUE_AREA_DISPLAY = Color.parseColor("#C5D2F7");
    public static final int BUFF_ORANGE_AREA_DISPLAY = Color.parseColor("#F7C7AC");

    public static final int getAreaToolBarColor(AreaElement areaElement) {
        if (areaElement.getType().equalsIgnoreCase("shared")) {
            return ColorProvider.BUFF_YELLOW_TOOLBAR;
        } else if (areaElement.getType().equalsIgnoreCase("public")) {
            return ColorProvider.BUFF_ORANGE_TOOLBAR;
        } else {
            return ColorProvider.BUFF_BLUE_TOOLBAR;
        }
    }

    public static final int getAreaDetailsColor(AreaElement areaElement) {
        if (areaElement.getType().equalsIgnoreCase("shared")) {
            return ColorProvider.BUFF_YELLOW_AREA_DISPLAY;
        } else if (areaElement.getType().equalsIgnoreCase("public")) {
            return ColorProvider.BUFF_ORANGE_AREA_DISPLAY;
        } else {
            return ColorProvider.BUFF_BLUE_AREA_DISPLAY;
        }
    }

    public static int getDefaultToolBarColor() {
        return ColorProvider.DEFAULT_TOOLBAR_COLOR;
    }
}
