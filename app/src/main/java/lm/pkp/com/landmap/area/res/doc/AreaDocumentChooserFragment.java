package lm.pkp.com.landmap.area.res.doc;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.Files;
import android.provider.MediaStore.Files.FileColumns;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.UUID;

import lm.pkp.com.landmap.AreaAddResourcesActivity;
import lm.pkp.com.landmap.R;
import lm.pkp.com.landmap.area.AreaContext;
import lm.pkp.com.landmap.area.AreaElement;
import lm.pkp.com.landmap.area.res.disp.DocumentChooserAdaptor;
import lm.pkp.com.landmap.area.res.disp.FileDisplayElement;
import lm.pkp.com.landmap.custom.PermittedFileArrayList;
import lm.pkp.com.landmap.drive.DriveResource;
import lm.pkp.com.landmap.user.UserContext;
import lm.pkp.com.landmap.util.FileUtil;

public class AreaDocumentChooserFragment extends Fragment {

    private View fragmentView;
    private ListView listView;
    private DocumentChooserAdaptor listAdapter;
    private PermittedFileArrayList<FileDisplayElement> items = new PermittedFileArrayList<FileDisplayElement>();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (fragmentView == null) {
            fragmentView = inflater.inflate(R.layout.document_select_layout, container, false);

            listAdapter = new DocumentChooserAdaptor(getContext(), items);
            TextView emptyView = (TextView) fragmentView.findViewById(R.id.searchEmptyView);
            listView = (ListView) fragmentView.findViewById(R.id.document_list_view);
            listView.setEmptyView(emptyView);
            listView.setAdapter(listAdapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    FileDisplayElement item = items.get(i);
                    File documentFile = new File(item.getPath());
                    if (!documentFile.canRead()) {
                        showErrorBox("Error: File cannot be read. Probably corrupt / locked.");
                        return;
                    }
                    if (documentFile.length() == 0) {
                        showErrorBox("Error: File does not have any contents.");
                        return;
                    }

                    final AreaContext areaContext = AreaContext.INSTANCE;
                    AreaElement ae = areaContext.getAreaElement();

                    File loadFile = new File(areaContext.getAreaLocalDocumentRoot().getAbsolutePath()
                            + File.separatorChar + documentFile.getName());
                    try {
                        FileUtils.copyFile(documentFile, loadFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    DriveResource resource = new DriveResource();
                    resource.setName(loadFile.getName());
                    resource.setPath(loadFile.getAbsolutePath());
                    resource.setType("file");
                    resource.setUserId(UserContext.getInstance().getUserElement().getEmail());
                    resource.setSize(loadFile.length() + "");
                    resource.setUniqueId(UUID.randomUUID().toString());
                    resource.setAreaId(ae.getUniqueId());
                    resource.setMimeType(FileUtil.getMimeType(loadFile));
                    resource.setContentType("Document");
                    resource.setContainerId(areaContext.getDocumentRootDriveResource().getResourceId());

                    areaContext.addResourceToQueue(resource);

                    getActivity().finish();

                    Intent intent = new Intent(getActivity(), AreaAddResourcesActivity.class);
                    startActivity(intent);
                }
            });

            PermittedFileArrayList<FileDisplayElement> files = new PermittedFileArrayList<>();
            files.addAll(findFiles(getContext(), "external", "application/pdf"));
            files.addAll(findFiles(getContext(), "internal", "application/pdf"));

            items.addAll(files);
            listAdapter.notifyDataSetChanged();
        } else {
            ViewGroup parent = (ViewGroup) fragmentView.getParent();
            if (parent != null) {
                parent.removeView(fragmentView);
            }
        }
        return fragmentView;
    }

    private PermittedFileArrayList<FileDisplayElement> findFiles(Context context, String location, String mimeType) {
        PermittedFileArrayList<FileDisplayElement> searchedFiles = new PermittedFileArrayList<>();
        ContentResolver cr = context.getContentResolver();
        Uri uri = Files.getContentUri(location);

        String mimeTypeCriteria = FileColumns.MIME_TYPE + "=?";
        String[] mimeTypeArgs = new String[]{mimeType};

        Cursor cursor = cr.query(uri, null, mimeTypeCriteria, mimeTypeArgs, null);

        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            while (cursor.isAfterLast() == false) {
                String fileTitle = cursor.getString(cursor.getColumnIndex(FileColumns.TITLE));
                String fileMime = cursor.getString(cursor.getColumnIndex(FileColumns.MIME_TYPE));
                String fileSize = cursor.getString(cursor.getColumnIndex(FileColumns.SIZE));
                String filePath = cursor.getString(cursor.getColumnIndex(FileColumns.DATA));
                String fileLastModifed = cursor.getString(cursor.getColumnIndex(FileColumns.DATE_MODIFIED));
                String fileCreated = cursor.getString(cursor.getColumnIndex(FileColumns.DATE_ADDED));

                FileDisplayElement fileDisplayItem = new FileDisplayElement();
                fileDisplayItem.setIcon(R.drawable.pdf_icon);
                fileDisplayItem.setName(fileTitle);
                fileDisplayItem.setDesc(createDescriptionText(fileSize, fileLastModifed));
                fileDisplayItem.setMimeType(fileMime);
                fileDisplayItem.setPath(filePath);
                fileDisplayItem.setCreated(fileCreated);
                fileDisplayItem.setLastModified(fileLastModifed);
                fileDisplayItem.setSizeBytes(fileSize);

                searchedFiles.add(fileDisplayItem);
                cursor.moveToNext();
            }
            cursor.close();
        }
        return searchedFiles;
    }

    private String createDescriptionText(String fileSize, String fileLastModifed) {
        // Prepare the size desc.
        Long size = new Long(fileSize);
        int sizeKB = (int) (((float) size) / 1024);
        int sizeMB = (int) (((float) sizeKB) / 1024);
        StringBuffer descBuffer = new StringBuffer();
        if (sizeKB > 1024) {
            descBuffer.append("Size " + sizeMB + "MBs, ");
        } else {
            descBuffer.append("Size " + sizeKB + "KBs, ");
        }

        // Prepare the last modified
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        final Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Calcutta"));
        calendar.setTimeInMillis(new Long(fileLastModifed) * 1000);
        final String formattedDate = format.format(calendar.getTime());
        descBuffer.append(formattedDate);

        return descBuffer.toString();
    }


    public void showErrorBox(String error) {
        if (getActivity() == null) {
            return;
        }
        new AlertDialog.Builder(getActivity())
                .setTitle("Document Chooser Error")
                .setMessage(error).setPositiveButton("OK", null).show();
    }
}
