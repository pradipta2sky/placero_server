package lm.pkp.com.landmap.position;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

import lm.pkp.com.landmap.AreaDetailsActivity;
import lm.pkp.com.landmap.R;
import lm.pkp.com.landmap.R.id;
import lm.pkp.com.landmap.R.layout;
import lm.pkp.com.landmap.area.AreaContext;
import lm.pkp.com.landmap.permission.PermissionConstants;
import lm.pkp.com.landmap.permission.PermissionManager;

/**
 * Created by USER on 10/16/2017.
 */
public class PositionListAdaptor extends ArrayAdapter<PositionElement> {

    private final ArrayList<PositionElement> items;
    private final Context context;
    private PositionsDBHelper pdh;

    public PositionListAdaptor(Context context, int textViewResourceId, ArrayList<PositionElement> items) {
        super(context, textViewResourceId, items);
        this.context = context;
        this.items = items;
        pdh = new PositionsDBHelper(context);
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.position_element_row, null);
        }

        final PositionElement pe = items.get(position);
        TextView nameText = (TextView) v.findViewById(R.id.pos_name);
        nameText.setText(pe.getName() + ", " + StringUtils.capitalize(pe.getType()));

        TextView latLongText = (TextView) v.findViewById(R.id.pos_latlng);
        latLongText.setText("Lat: " + pe.getLat() + ", " + "Long: " + pe.getLon());

        ImageView editButton = (ImageView) v.findViewById(R.id.edit_row);
        editButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PermissionManager.INSTANCE.hasAccess(PermissionConstants.MARK_POSITION)) {
                    ((AreaDetailsActivity)context).showPositionEdit(pe);
                }
            }
        });

        ImageView deleteButton = (ImageView) v.findViewById(R.id.del_row);
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
