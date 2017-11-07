package lm.pkp.com.landmap.area.res.disp;

/**
 * Created by USER on 11/7/2017.
 */
import android.content.Context;
import android.widget.AbsListView;

import com.squareup.picasso.Picasso;

public class SampleScrollListener implements AbsListView.OnScrollListener {
    private final Context context;

    public SampleScrollListener(Context context) {
        this.context = context;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        final Picasso picasso = Picasso.with(view.getContext());
        if (scrollState == SCROLL_STATE_IDLE || scrollState == SCROLL_STATE_TOUCH_SCROLL) {
            picasso.resumeTag(context);
        } else {
            picasso.pauseTag(context);
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                         int totalItemCount) {
        // Do nothing.
    }
}