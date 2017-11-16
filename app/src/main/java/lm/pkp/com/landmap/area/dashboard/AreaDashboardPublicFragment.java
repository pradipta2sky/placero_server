package lm.pkp.com.landmap.area.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import lm.pkp.com.landmap.AreaDetailsActivity;
import lm.pkp.com.landmap.R;
import lm.pkp.com.landmap.area.AreaContext;
import lm.pkp.com.landmap.area.AreaElement;
import lm.pkp.com.landmap.area.db.AreaDBHelper;
import lm.pkp.com.landmap.area.res.disp.AreaItemAdaptor;
import lm.pkp.com.landmap.custom.AsyncTaskCallback;
import lm.pkp.com.landmap.sync.LocalDataRefresher;

/**
 * Created by USER on 11/4/2017.
 */
public class AreaDashboardPublicFragment extends Fragment {

    private AreaItemAdaptor areaDisplayAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_public_areas, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void setUserVisibleHint(boolean visible) {
        super.setUserVisibleHint(visible);
        if (visible && isResumed()) {
            loadFragment();
        }
    }

    private void loadFragment() {
        View view = getView();
        view.findViewById(R.id.splash_panel).setVisibility(View.VISIBLE);

        final EditText inputSearch = (EditText) getActivity().findViewById(R.id.dashboard_search_box);
        final String availableKey = inputSearch.getText().toString();
        inputSearch.addTextChangedListener(new UserInputWatcher());

        if (availableKey.trim().equalsIgnoreCase("")) {
            LocalDataRefresher dataRefresher = new LocalDataRefresher(getContext(), new DataReloadCallback());
            dataRefresher.refreshPublicAreas();
        } else {
            LocalDataRefresher dataRefresher = new LocalDataRefresher(getContext(), new DataReloadCallback(availableKey.trim()));
            dataRefresher.refreshPublicAreas(availableKey.trim());
        }

        ImageView refreshAreaView = (ImageView) getActivity().findViewById(R.id.action_area_refresh);
        refreshAreaView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getView().findViewById(R.id.splash_panel).setVisibility(View.VISIBLE);
                final String key = inputSearch.getText().toString();
                if (key.trim().equalsIgnoreCase("")) {
                    LocalDataRefresher dataRefresher = new LocalDataRefresher(getContext(), new DataReloadCallback());
                    dataRefresher.refreshPublicAreas();
                } else {
                    LocalDataRefresher dataRefresher = new LocalDataRefresher(getContext(), new DataReloadCallback(key.trim()));
                    dataRefresher.refreshPublicAreas(key.trim());
                }
            }
        });

        final Button seachClearButton = (Button) getActivity().findViewById(R.id.dashboard_search_clear);
        seachClearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText inputSearch = (EditText) getActivity().findViewById(R.id.dashboard_search_box);
                inputSearch.setText("");
            }
        });

    }

    private class DataReloadCallback implements AsyncTaskCallback {

        private String filterStr;

        public DataReloadCallback(String searchKey) {
            this.filterStr = searchKey;
        }

        public DataReloadCallback() {
            this.filterStr = "";
        }

        @Override
        public void taskCompleted(Object result) {
            AreaDBHelper adh = new AreaDBHelper(getContext());

            AreaItemAdaptor adaptor = new AreaItemAdaptor(getContext(), R.layout.area_element_row, adh.getAreas("public"));
            adaptor.notifyDataSetChanged();
            ListView areaListView = (ListView) getView().findViewById(R.id.area_display_list);
            areaListView.setAdapter(adaptor);

            if (!filterStr.equalsIgnoreCase("")) {
                adaptor.getFilter().filter(filterStr);
            }

            areaListView.setDescendantFocusability(ListView.FOCUS_BLOCK_DESCENDANTS);
            areaListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapter, View v, int position,
                                        long arg3) {
                    getActivity().finish();

                    AreaElement ae = (AreaElement) adapter.getItemAtPosition(position);
                    AreaContext.INSTANCE.setAreaElement(ae, getContext());
                    Intent intent = new Intent(getContext(), AreaDetailsActivity.class);
                    startActivity(intent);
                }
            });

            getView().findViewById(R.id.splash_panel).setVisibility(View.INVISIBLE);
        }
    }

    private class UserInputWatcher implements TextWatcher {
        @Override
        public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
        }

        @Override
        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
            getView().findViewById(R.id.splash_panel).setVisibility(View.VISIBLE);
            String filterStr = editable.toString().trim();
            if (!filterStr.equalsIgnoreCase("")) {
                LocalDataRefresher dataRefresher = new LocalDataRefresher(getContext(), new DataReloadCallback(filterStr));
                dataRefresher.refreshPublicAreas(filterStr);
            } else {
                LocalDataRefresher dataRefresher = new LocalDataRefresher(getContext(), new DataReloadCallback());
                dataRefresher.refreshPublicAreas();
            }
        }
    }

}
