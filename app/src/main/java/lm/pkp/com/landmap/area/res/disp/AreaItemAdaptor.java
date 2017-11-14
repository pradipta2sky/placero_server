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
import lm.pkp.com.landmap.area.AreaElement;
import lm.pkp.com.landmap.util.AreaPopulationUtil;

/**
 * Created by USER on 10/20/2017.
 */
public class AreaItemAdaptor extends ArrayAdapter implements Filterable{

    private ArrayList<AreaElement> items;
    private ArrayList<AreaElement> fixedItems = new ArrayList<>();
    private Context context;

    public AreaItemAdaptor(Context context, int textViewResourceId, ArrayList<AreaElement> items) {
        super(context, textViewResourceId, items);
        this.context = context;
        this.items = items;
        this.fixedItems.addAll(items);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.area_element_row, null);
        }
        AreaPopulationUtil.INSTANCE.populateAreaElement(v, items.get(position));
        return v;
    }

    public int getCount () {
        return items.size ();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                items = (ArrayList<AreaElement>) results.values;
                notifyDataSetChanged();
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<AreaElement> filteredResults = getFilteredResults(constraint);

                FilterResults results = new FilterResults();
                results.values = filteredResults;

                return results;
            }

            private List<AreaElement> getFilteredResults(CharSequence constraint) {
                List<AreaElement> results = new ArrayList<>();
                for (int i = 0; i < fixedItems.size(); i++) {
                    final AreaElement areaElement = fixedItems.get(i);
                    String areaName = areaElement.getName().toLowerCase();
                    String description = areaElement.getDescription().toLowerCase();
                    String address = areaElement.getAddress().toLowerCase();
                    String cons = constraint.toString().toLowerCase();
                    if(areaName.contains(cons) || description.contains(constraint) || address.contains(constraint)){
                        results.add(areaElement);
                    }
                }
                return results;
            }
        };
    }
}
