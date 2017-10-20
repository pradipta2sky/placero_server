package lm.pkp.com.landmap;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by USER on 10/20/2017.
 */
public class AreaItemDisplayAdaptor extends ArrayAdapter {
    private ArrayList<AreaElement> items;
    private Context context;
    private AreaDBHelper adh = null;

    public AreaItemDisplayAdaptor(Context context, int textViewResourceId, ArrayList<AreaElement> items) {
        super(context, textViewResourceId, items);
        this.context = context;
        this.items = items;
        adh = new AreaDBHelper(context);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.area_element_row, null);
        }
        final AreaElement ae = items.get(position);

        TextView areaNameView = (TextView) v.findViewById(R.id.area_name_text);
        String areaName = ae.getName();
        if(areaName.length() > 25){
            areaNameView.setText(areaName.substring(0,22).concat("..."));
        }else {
            areaNameView.setText(areaName);
        }

        TextView descText = (TextView) v.findViewById(R.id.area_desc_text);
        String desc = ae.getDescription();
        if(desc.trim().equals("")){
            desc = "No description.";
        }
        descText.setText(desc);

        ImageView deleteButton = (ImageView) v.findViewById(R.id.del_row);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                items.remove(position);
                adh.deleteArea(items.get(position).getId());
                notifyDataSetChanged();
            }
        });
        return v;
    }
}
