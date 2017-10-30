package lm.pkp.com.landmap.position;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import lm.pkp.com.landmap.R;

/**
 * Created by USER on 10/16/2017.
 */
public class PostionListAdaptor extends ArrayAdapter<PositionElement> {

    private ArrayList<PositionElement> items;
    private Context context;
    private PositionsDBHelper pdh = null;

    public PostionListAdaptor(Context context, int textViewResourceId, ArrayList<PositionElement> items) {
        super(context, textViewResourceId, items);
        this.context = context;
        this.items = items;
        pdh = new PositionsDBHelper(context);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.position_element_row, null);
        }
        final PositionElement pe = items.get(position);
        TextView nameText = (TextView) v.findViewById(R.id.pos_name);
        String pName = pe.getName();
        if(pName.length() > 25){
            pName = pName.substring(0,22).concat("...");
        }
        nameText.setText(pName);

        TextView latLongText = (TextView) v.findViewById(R.id.pos_latlng);
        latLongText.setText("Lat: " + pe.getLat() + "\n" + "Long: " + pe.getLon());

        ImageView deleteButton = (ImageView) v.findViewById(R.id.del_row);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                items.remove(position);
                pdh.deletePosition(pe);
                notifyDataSetChanged();
            }
        });
        return v;
    }
}
