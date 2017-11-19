package lm.pkp.com.landmap.area.res.disp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import lm.pkp.com.landmap.R;

/**
 * Created by USER on 11/4/2017.
 */
public class AreaPictureDisplayFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_picture_display, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        GridView gridView = (GridView) getView().findViewById(R.id.gridView);
        gridView.setAdapter(new AreaPictureDisplayAdaptor(this.getContext(), this, 0));
    }

}
