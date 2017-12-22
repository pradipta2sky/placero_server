package lm.pkp.com.landmap.area.res.disp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

import lm.pkp.com.landmap.R;
import lm.pkp.com.landmap.R.id;
import lm.pkp.com.landmap.R.layout;
import lm.pkp.com.landmap.area.AreaContext;
import lm.pkp.com.landmap.area.FileStorageConstants;
import lm.pkp.com.landmap.area.model.AreaElement;
import lm.pkp.com.landmap.drive.DriveResource;
import lm.pkp.com.landmap.position.PositionElement;

/**
 * Created by USER on 10/16/2017.
 */
public class AreaAddResourceAdaptor extends ArrayAdapter<DriveResource> {

    private final ArrayList<DriveResource> items;
    private final Context context;

    public AreaAddResourceAdaptor(Context context, int textViewResourceId, ArrayList<DriveResource> items) {
        super(context, textViewResourceId, items);
        this.context = context;
        this.items = items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.file_element_row, null);
        }

        final AreaContext areaContext = AreaContext.INSTANCE;
        final DriveResource resource = this.items.get(position);

        TextView nameText = (TextView) v.findViewById(id.ar_file_name);
        nameText.setText(resource.getName());

        TextView filePathText = (TextView) v.findViewById(id.ar_file_path);
        String message = "";

        PositionElement resourcePosition = resource.getPosition();
        String resLat = "";
        String resLong = "";
        if(resourcePosition != null){
            message = "Position: " + resourcePosition.getLat() + ", " + resourcePosition.getLon();
            filePathText.setText(message);
        }else {
            filePathText.setText(resource.getPath());
        }

        Button removeButton = (Button) v.findViewById(id.remove_upload_resource);
        removeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                areaContext.getUploadedQueue().remove(resource);
                if (items.contains(resource)) {
                    items.remove(resource);
                } else {
                    System.out.println("Resource not found");
                }
                notifyDataSetChanged();
            }
        });
        return v;
    }
}
