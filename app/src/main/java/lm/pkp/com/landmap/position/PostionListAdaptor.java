package lm.pkp.com.landmap.position;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import lm.pkp.com.landmap.R.id;
import lm.pkp.com.landmap.R.layout;
import lm.pkp.com.landmap.area.AreaContext;
import lm.pkp.com.landmap.permission.PermissionConstants;
import lm.pkp.com.landmap.permission.PermissionManager;

/**
 * Created by USER on 10/16/2017.
 */
public class PostionListAdaptor extends ArrayAdapter<PositionElement> {

    private final ArrayList<PositionElement> items;
    private final Context context;
    private PositionsDBHelper pdh;

    public PostionListAdaptor(Context context, int textViewResourceId, ArrayList<PositionElement> items) {
        super(context, textViewResourceId, items);
        this.context = context;
        this.items = items;
        this.pdh = new PositionsDBHelper(context);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(layout.position_element_row, null);
        } else {
            return v;
        }

        final PositionElement pe = this.items.get(position);
        TextView nameText = (TextView) v.findViewById(id.pos_name);
        String pName = pe.getName();
        if (pName.length() > 25) {
            pName = pName.substring(0, 22).concat("...");
        }
        nameText.setText(pName);

        TextView latLongText = (TextView) v.findViewById(id.pos_latlng);
        latLongText.setText("Lat: " + pe.getLat() + ", " + "Long: " + pe.getLon());

        ImageView deleteButton = (ImageView) v.findViewById(id.del_row);
        deleteButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(PermissionManager.INSTANCE.hasAccess(PermissionConstants.UPDATE_AREA)){
                    items.remove(position);
                    pdh.deletePositionGlobally(pe);
                    AreaContext.INSTANCE.getAreaElement().getPositions().remove(pe);
                    notifyDataSetChanged();
                }
            }
        });
        return v;
    }
}
