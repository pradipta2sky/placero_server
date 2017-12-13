package lm.pkp.com.landmap.area.res.disp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import lm.pkp.com.landmap.AreaDetailsActivity;
import lm.pkp.com.landmap.R;
import lm.pkp.com.landmap.area.AreaContext;
import lm.pkp.com.landmap.area.model.AreaElement;
import lm.pkp.com.landmap.area.reporting.AreaReportingService;
import lm.pkp.com.landmap.area.reporting.ReportingContext;
import lm.pkp.com.landmap.util.AreaPopulationUtil;
import lm.pkp.com.landmap.util.ColorProvider;

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
    public View getView(final int position, View convertView, final ViewGroup parent) {
        View itemView = convertView;
        if (itemView == null) {
            LayoutInflater vi = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            itemView = vi.inflate(R.layout.area_element_row, null);
        }

        AreaContext.INSTANCE.setDisplayBMap(null);

        final AreaElement areaElement = items.get(position);
        AreaPopulationUtil.INSTANCE.populateAreaElement(itemView, areaElement);

        itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int siblingCount = parent.getChildCount();
                for (int i = 0; i < siblingCount; i++) {
                    View child = parent.getChildAt(i);
                    child.setBackgroundResource(0);
                    child.setBackgroundColor(ColorProvider.getAreaDetailsColor(areaElement));
                }
                v.setBackgroundResource(R.drawable.image_border);
                AreaContext.INSTANCE.setAreaElement(areaElement, context);
                return false;
            }
        });

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Activity activity = (Activity) context;
                AreaContext.INSTANCE.setAreaElement(areaElement, activity);
                Intent intent = new Intent(activity, AreaDetailsActivity.class);
                activity.startActivity(intent);
                activity.finish();
            }

        });

        return itemView;
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
                items = (ArrayList<AreaElement>) results.values;
                notifyDataSetChanged();
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
                for (int i = 0; i < fixedItems.size(); i++) {
                    AreaElement areaElement = fixedItems.get(i);
                    String areaName = areaElement.getName().toLowerCase();
                    String description = areaElement.getDescription().toLowerCase();
                    String address = areaElement.getAddress().getDisplaybleAddress();
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
