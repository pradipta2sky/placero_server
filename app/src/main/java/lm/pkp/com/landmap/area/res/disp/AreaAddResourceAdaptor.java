package lm.pkp.com.landmap.area.res.disp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

import lm.pkp.com.landmap.R;
import lm.pkp.com.landmap.R.id;
import lm.pkp.com.landmap.R.layout;
import lm.pkp.com.landmap.area.AreaContext;
import lm.pkp.com.landmap.drive.DriveResource;

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
            v = vi.inflate(layout.file_element_row, null);
        }

        final DriveResource dr = this.items.get(position);

        TextView nameText = (TextView) v.findViewById(id.ar_file_name);
        nameText.setText(dr.getName());

        TextView filePathText = (TextView) v.findViewById(id.ar_file_path);
        String message = "";
        String resLat = dr.getLatitude();
        String resLong = dr.getLongitude();
        if (!resLat.trim().equalsIgnoreCase("")) {
            message = "Position: " + resLat + ", " + resLong;
            filePathText.setText(message);
        } else {
            filePathText.setText(dr.getPath());
        }

        Button removeButton = (Button) v.findViewById(id.remove_upload_resource);
        removeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                AreaContext.INSTANCE.getUploadedQueue().remove(dr);
                if (AreaAddResourceAdaptor.this.items.contains(dr)) {
                    AreaAddResourceAdaptor.this.items.remove(dr);
                } else {
                    System.out.println("Resource not found");
                }
                AreaAddResourceAdaptor.this.notifyDataSetChanged();
            }
        });
        return v;
    }
}
