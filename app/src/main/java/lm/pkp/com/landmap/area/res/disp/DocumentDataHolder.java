package lm.pkp.com.landmap.area.res.disp;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import lm.pkp.com.landmap.area.AreaContext;
import lm.pkp.com.landmap.area.AreaElement;
import lm.pkp.com.landmap.drive.DriveResource;

/**
 * Created by USER on 11/6/2017.
 */

final class DocumentDataHolder {

    public static final DocumentDataHolder INSTANCE = new DocumentDataHolder();

    public ArrayList<DocumentDisplayElement> getData() {
        ArrayList<DocumentDisplayElement> docItems = new ArrayList<>();
        AreaContext ac = AreaContext.INSTANCE;

        AreaElement ae = ac.getAreaElement();
        List<DriveResource> driveResources = ae.getMediaResources();
        String rootPath = ac.getAreaLocalDocumentRoot(ae.getUniqueId()).getAbsolutePath() + File.separatorChar;
        for (int i = 0; i < driveResources.size(); i++) {
            DriveResource resource = driveResources.get(i);
            if (resource.getType().equals("file")) {
                if (resource.getContentType().equals("Document")) {
                    DocumentDisplayElement docDisplayElement = new DocumentDisplayElement();
                    docDisplayElement.setName(resource.getName());
                    docDisplayElement.setAbsPath(rootPath + resource.getName());
                    docDisplayElement.setResourceId(resource.getResourceId());
                    docItems.add(docDisplayElement);
                }
            }
        }
        return docItems;
    }
}
