package lm.pkp.com.landmap.area;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import java.util.ArrayList;

import lm.pkp.com.landmap.R;
import lm.pkp.com.landmap.drive.DriveResource;

/**
 * Created by USER on 10/16/2017.
 */
public class AreaDriveResourceAdaptor extends ArrayAdapter<DriveResource> {

    private ArrayList<DriveResource> items;
    private Context context;

    public AreaDriveResourceAdaptor(Context context, int textViewResourceId, ArrayList<DriveResource> items) {
        super(context, textViewResourceId, items);
        this.context = context;
        this.items = items;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.file_element_row, null);
        }
        final DriveResource dr = items.get(position);
        TextView nameText = (TextView) v.findViewById(R.id.ar_file_name);
        nameText.setText(dr.getName());

        TextView filePathText = (TextView) v.findViewById(R.id.ar_file_path);
        filePathText.setText(dr.getPath());

        return v;
    }
}
