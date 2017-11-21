package lm.pkp.com.landmap.area.res.disp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import lm.pkp.com.landmap.R;
import lm.pkp.com.landmap.R.id;
import lm.pkp.com.landmap.R.layout;

/**
 * Created by USER on 11/4/2017.
 */
public class AreaDocumentDisplayFragment extends Fragment {

    private GridView gridView;
    private AreaDocumentDisplayAdaptor adaptor;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(layout.fragment_document_display, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.gridView = (GridView) this.getView().findViewById(id.gridView);
        this.adaptor = new AreaDocumentDisplayAdaptor(this.getContext(), this, 2);
        this.gridView.setAdapter(this.adaptor);
    }

    @Override
    public void setUserVisibleHint(boolean visible) {
        super.setUserVisibleHint(visible);
        if (visible && this.isResumed()) {
            this.loadFragment();
        }
    }

    private void loadFragment() {
    }
}
