package lm.pkp.com.landmap.area.res.disp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

import lm.pkp.com.landmap.R;
import lm.pkp.com.landmap.area.AreaContext;
import lm.pkp.com.landmap.drive.DriveResource;

/**
 * Created by USER on 10/16/2017.
 */
public class AreaAddResourceAdaptor extends ArrayAdapter<DriveResource> {

    private ArrayList<DriveResource> items;
    private Context context;

    public AreaAddResourceAdaptor(Context context, int textViewResourceId, ArrayList<DriveResource> items) {
        super(context, textViewResourceId, items);
        this.context = context;
        this.items = items;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.file_element_row, null);
        }

        final DriveResource dr = items.get(position);

        TextView nameText = (TextView) v.findViewById(R.id.ar_file_name);
        nameText.setText(dr.getName());

        TextView filePathText = (TextView) v.findViewById(R.id.ar_file_path);
        String message = "";
        String resLat = dr.getLatitude();
        String resLong = dr.getLongitude();
        if(!resLat.trim().equalsIgnoreCase("")){
            message = "Position: " + resLat + ", " + resLong;
            filePathText.setText(message);
        }else {
            filePathText.setText(dr.getPath());
        }

        Button removeButton = (Button) v.findViewById(R.id.remove_upload_resource);
        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AreaContext.INSTANCE.getUploadedQueue().remove(dr);
                if(items.contains(dr)){
                    items.remove(dr);
                }else {
                    System.out.println("Resource not found");
                }
                notifyDataSetChanged();
            }
        });
        return v;
    }
}
