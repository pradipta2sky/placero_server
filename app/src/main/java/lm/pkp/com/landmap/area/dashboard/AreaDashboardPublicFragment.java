package lm.pkp.com.landmap.area.dashboard;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.ArrayList;

import lm.pkp.com.landmap.AreaDetailsActivity;
import lm.pkp.com.landmap.R;
import lm.pkp.com.landmap.R.id;
import lm.pkp.com.landmap.R.layout;
import lm.pkp.com.landmap.area.AreaContext;
import lm.pkp.com.landmap.area.AreaDashboardDisplayMetaStore;
import lm.pkp.com.landmap.area.AreaElement;
import lm.pkp.com.landmap.area.db.AreaDBHelper;
import lm.pkp.com.landmap.area.res.disp.AreaItemAdaptor;
import lm.pkp.com.landmap.custom.AsyncTaskCallback;
import lm.pkp.com.landmap.custom.FragmentIdentificationHandler;
import lm.pkp.com.landmap.sync.LocalDataRefresher;

/**
 * Created by USER on 11/4/2017.
 */
public class AreaDashboardPublicFragment extends Fragment implements FragmentIdentificationHandler{

    private Activity mActivity = null;
    private View mView = null;

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
        mView = view;
        mActivity = getActivity();
        if(getUserVisibleHint()){
            loadFragment();
        }
    }

    @Override
    public void setUserVisibleHint(boolean visible) {
        super.setUserVisibleHint(visible);
        if (visible && (mView != null) && (mActivity != null)) {
            AreaDashboardDisplayMetaStore.INSTANCE.setActiveTab(AreaDashboardDisplayMetaStore.TAB_PUBLIC_SEQ);
            this.loadFragment();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void loadFragment() {
        AreaContext.INSTANCE.setDisplayBMap(null);
        mView.findViewById(id.splash_panel).setVisibility(View.VISIBLE);

        final EditText inputSearch = (EditText) mActivity.findViewById(id.dashboard_search_box);
        String availableKey = inputSearch.getText().toString();
        inputSearch.addTextChangedListener(new UserInputWatcher());

        if (availableKey.trim().equalsIgnoreCase("")) {
            LocalDataRefresher dataRefresher = new LocalDataRefresher(mActivity,new DataReloadCallback());
            dataRefresher.refreshPublicAreas();
        } else {
            LocalDataRefresher dataRefresher = new LocalDataRefresher(mActivity, new DataReloadCallback(availableKey.trim()));
            dataRefresher.refreshPublicAreas(availableKey.trim());
        }

        ImageView refreshAreaView = (ImageView) mActivity.findViewById(id.action_area_refresh);
        refreshAreaView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mView.findViewById(id.splash_panel).setVisibility(View.VISIBLE);
                String key = inputSearch.getText().toString();
                if (key.trim().equalsIgnoreCase("")) {
                    LocalDataRefresher dataRefresher = new LocalDataRefresher(mActivity, new DataReloadCallback());
                    dataRefresher.refreshPublicAreas();
                } else {
                    LocalDataRefresher dataRefresher = new LocalDataRefresher(mActivity,new DataReloadCallback(key.trim()));
                    dataRefresher.refreshPublicAreas(key.trim());
                }
            }
        });

        Button seachClearButton = (Button) mActivity.findViewById(id.dashboard_search_clear);
        seachClearButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText inputSearch = (EditText) mActivity.findViewById(id.dashboard_search_box);
                inputSearch.setText("");
            }
        });

    }

    @Override
    public String getFragmentTitle() {
        return "Public";
    }

    private class DataReloadCallback implements AsyncTaskCallback {

        private final String filterStr;

        public DataReloadCallback(String searchKey) {
            filterStr = searchKey;
        }

        public DataReloadCallback() {
            filterStr = "";
        }

        @Override
        public void taskCompleted(Object result) {
            AreaDBHelper adh = new AreaDBHelper(mActivity);

            final ArrayList<AreaElement> publicAreas = adh.getAreas("public");
            if(publicAreas.size() > 0){
                mView.findViewById(id.area_display_list).setVisibility(View.VISIBLE);
                mView.findViewById(id.public_area_empty_layout).setVisibility(View.GONE);
            }else {
                mView.findViewById(id.area_display_list).setVisibility(View.GONE);
                mView.findViewById(id.public_area_empty_layout).setVisibility(View.VISIBLE);
            }

            AreaItemAdaptor adaptor = new AreaItemAdaptor(mView.getContext(), layout.area_element_row, publicAreas);
            ListView areaListView = (ListView) mView.findViewById(id.area_display_list);
            areaListView.setAdapter(adaptor);

            if(!filterStr.equalsIgnoreCase("")){
                adaptor.getFilter().filter(filterStr);
            }
            adaptor.notifyDataSetChanged();

            areaListView.setDescendantFocusability(ListView.FOCUS_BLOCK_DESCENDANTS);
            areaListView.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapter, View v, int position,
                                        long arg3) {
                    AreaElement ae = (AreaElement) adapter.getItemAtPosition(position);
                    AreaContext.INSTANCE.setAreaElement(ae, mActivity);
                    Intent intent = new Intent(mActivity, AreaDetailsActivity.class);
                    startActivity(intent);
                    mActivity.finish();
                }
            });

            mView.findViewById(id.splash_panel).setVisibility(View.INVISIBLE);
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
            if(AreaDashboardDisplayMetaStore.INSTANCE.getActiveTab() == AreaDashboardDisplayMetaStore.TAB_PUBLIC_SEQ){
                mView.findViewById(id.splash_panel).setVisibility(View.VISIBLE);
                String filterStr = editable.toString().trim();
                if (!filterStr.equalsIgnoreCase("")) {
                    LocalDataRefresher dataRefresher = new LocalDataRefresher(mActivity, new DataReloadCallback(filterStr));
                    dataRefresher.refreshPublicAreas(filterStr);
                } else {
                    LocalDataRefresher dataRefresher = new LocalDataRefresher(mActivity, new DataReloadCallback());
                    dataRefresher.refreshPublicAreas();
                }
            }
        }
    }

}
