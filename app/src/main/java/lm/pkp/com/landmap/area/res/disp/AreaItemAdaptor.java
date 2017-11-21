package lm.pkp.com.landmap.area.res.disp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import java.util.ArrayList;
import java.util.List;

import lm.pkp.com.landmap.R;
import lm.pkp.com.landmap.R.layout;
import lm.pkp.com.landmap.area.AreaElement;
import lm.pkp.com.landmap.util.AreaPopulationUtil;

/**
 * Created by USER on 10/20/2017.
 */
public class AreaItemAdaptor extends ArrayAdapter {

    private ArrayList<AreaElement> items;
    private final ArrayList<AreaElement> fixedItems = new ArrayList<>();
    private final Context context;

    public AreaItemAdaptor(Context context, int textViewResourceId, ArrayList<AreaElement> items) {
        super(context, textViewResourceId, items);
        this.context = context;
        this.items = items;
        fixedItems.addAll(items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(layout.area_element_row, null);
        }
        AreaPopulationUtil.INSTANCE.populateAreaElement(v, this.items.get(position));
        return v;
    }

    public int getCount() {
        return this.items.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, Filter.FilterResults results) {
                AreaItemAdaptor.this.items = (ArrayList<AreaElement>) results.values;
                AreaItemAdaptor.this.notifyDataSetChanged();
            }

            @Override
            protected Filter.FilterResults performFiltering(CharSequence constraint) {
                List<AreaElement> filteredResults = getFilteredResults(constraint);

                Filter.FilterResults results = new Filter.FilterResults();
                results.values = filteredResults;

                return results;
            }

            private List<AreaElement> getFilteredResults(CharSequence constraint) {
                List<AreaElement> results = new ArrayList<>();
                for (int i = 0; i < AreaItemAdaptor.this.fixedItems.size(); i++) {
                    AreaElement areaElement = AreaItemAdaptor.this.fixedItems.get(i);
                    String areaName = areaElement.getName().toLowerCase();
                    String description = areaElement.getDescription().toLowerCase();
                    String address = areaElement.getAddress().toLowerCase();
                    String cons = constraint.toString().toLowerCase();
                    if (areaName.contains(cons) || description.contains(constraint) || address.contains(constraint)) {
                        results.add(areaElement);
                    }
                }
                return results;
            }
        };
    }
}
