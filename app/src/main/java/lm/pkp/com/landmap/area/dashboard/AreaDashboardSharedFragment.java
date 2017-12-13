package lm.pkp.com.landmap.area.dashboard;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.ArrayList;

import lm.pkp.com.landmap.R;
import lm.pkp.com.landmap.R.id;
import lm.pkp.com.landmap.R.layout;
import lm.pkp.com.landmap.area.AreaContext;
import lm.pkp.com.landmap.area.AreaDashboardDisplayMetaStore;
import lm.pkp.com.landmap.area.model.AreaElement;
import lm.pkp.com.landmap.area.db.AreaDBHelper;
import lm.pkp.com.landmap.area.res.disp.AreaItemAdaptor;
import lm.pkp.com.landmap.custom.AsyncTaskCallback;
import lm.pkp.com.landmap.custom.FragmentIdentificationHandler;
import lm.pkp.com.landmap.sync.LocalDataRefresher;

/**
 * Created by USER on 11/4/2017.
 */
public class AreaDashboardSharedFragment extends Fragment implements FragmentIdentificationHandler{

    private Activity mActivity = null;
    private View mView = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(layout.fragment_shared_areas, container, false);
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
            AreaDashboardDisplayMetaStore.INSTANCE.setActiveTab(AreaDashboardDisplayMetaStore.TAB_SHARED_SEQ);
            this.loadFragment();
        }
    }

    private void loadFragment() {
        AreaContext.INSTANCE.setDisplayBMap(null);
        mView.findViewById(id.splash_panel).setVisibility(View.VISIBLE);

        ImageView refreshAreaView = (ImageView) mActivity.findViewById(id.action_area_refresh);
        refreshAreaView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mView.findViewById(id.splash_panel).setVisibility(View.VISIBLE);
                new LocalDataRefresher(mActivity, new DataReloadCallback()).refreshLocalData();
            }
        });

        EditText inputSearch = (EditText) mActivity.findViewById(id.dashboard_search_box);
        inputSearch.addTextChangedListener(new UserInputWatcher());

        Button seachClearButton = (Button) mActivity.findViewById(id.dashboard_search_clear);
        seachClearButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText inputSearch = (EditText) mActivity.findViewById(id.dashboard_search_box);
                inputSearch.setText("");
            }
        });

        AreaDBHelper adh = new AreaDBHelper(mView.getContext());
        ListView areaListView = (ListView) mView.findViewById(id.area_display_list);
        ArrayList<AreaElement> sharedAreas = adh.getAreas("shared");
        AreaItemAdaptor adaptor = new AreaItemAdaptor(mView.getContext(), layout.area_element_row, sharedAreas);

        if (sharedAreas.size() > 0) {
            mView.findViewById(id.shared_area_empty_layout).setVisibility(View.GONE);
            areaListView.setVisibility(View.VISIBLE);

            areaListView.setAdapter(adaptor);
            areaListView.setDescendantFocusability(ListView.FOCUS_BLOCK_DESCENDANTS);
        } else {
            areaListView.setVisibility(View.GONE);
            mView.findViewById(id.shared_area_empty_layout).setVisibility(View.VISIBLE);
        }
        mView.findViewById(id.splash_panel).setVisibility(View.GONE);
    }

    @Override
    public String getFragmentTitle() {
        return "Shared";
    }

    private class DataReloadCallback implements AsyncTaskCallback {

        @Override
        public void taskCompleted(Object result) {
            ArrayList<AreaElement> areas = new AreaDBHelper(mView.getContext()).getAreas("shared");

            ListView areaListView = (ListView) mView.findViewById(id.area_display_list);
            AreaItemAdaptor adaptor = new AreaItemAdaptor(mView.getContext(), layout.area_element_row, areas);
            areaListView.setAdapter(adaptor);

            EditText inputSearch = (EditText) mActivity.findViewById(id.dashboard_search_box);
            String filterStr = inputSearch.getText().toString().trim();
            if (!filterStr.equalsIgnoreCase("")) {
                adaptor.getFilter().filter(filterStr);
            }

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
            if(AreaDashboardDisplayMetaStore.INSTANCE.getActiveTab() == AreaDashboardDisplayMetaStore.TAB_SHARED_SEQ){
                if(editable.toString().equalsIgnoreCase("")){
                    return;
                }else {
                    mView.findViewById(id.splash_panel).setVisibility(View.VISIBLE);

                    ListView areaListView = (ListView) mView.findViewById(id.area_display_list);
                    ArrayAdapter<AreaElement> adapter = (ArrayAdapter<AreaElement>) areaListView.getAdapter();
                    adapter.getFilter().filter(editable.toString());

                    mView.findViewById(id.splash_panel).setVisibility(View.GONE);
                }
            }
        }

    }

}