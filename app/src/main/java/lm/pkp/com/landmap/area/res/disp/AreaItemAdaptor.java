package lm.pkp.com.landmap.area.res.disp;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;

import lm.pkp.com.landmap.R;
import lm.pkp.com.landmap.area.db.AreaDBHelper;
import lm.pkp.com.landmap.area.AreaElement;
import lm.pkp.com.landmap.user.UserDBHelper;
import lm.pkp.com.landmap.util.AreaActivityUtil;

/**
 * Created by USER on 10/20/2017.
 */
public class AreaItemAdaptor extends ArrayAdapter {
    private ArrayList<AreaElement> items;
    private Context context;

    public AreaItemAdaptor(Context context, int textViewResourceId, ArrayList<AreaElement> items) {
        super(context, textViewResourceId, items);
        this.context = context;
        this.items = items;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.area_element_row, null);
        }else {
            return v;
        }

        AreaElement ae = items.get(position);
        AreaActivityUtil.INSTANCE.populateAreaElement(v, ae);

        return v;
    }
}
