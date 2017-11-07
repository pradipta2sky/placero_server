package lm.pkp.com.landmap.custom;

import java.util.ArrayList;

import lm.pkp.com.landmap.area.res.disp.FileDisplayItem;
import lm.pkp.com.landmap.area.res.disp.FileListItem;

/**
 * Created by USER on 11/7/2017.
 */
public class PermittedFileArrayList<E> extends ArrayList<E> {

    @Override
    public boolean add(E object) {
        if(object instanceof FileDisplayItem){
            FileDisplayItem item = (FileDisplayItem) object;
            final String itemName = item.getName();
            if(!itemName.contains(".")){
                return super.add(object);
            }
            if(itemName.endsWith(".pdf")){
                return super.add(object);
            }
            if(itemName.endsWith(".jpg")){
                return super.add(object);
            }
            if(itemName.endsWith(".mp4")){
                return super.add(object);
            }
        }
        return false;
    }
}
