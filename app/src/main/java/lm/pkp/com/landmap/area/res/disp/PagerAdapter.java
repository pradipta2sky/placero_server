package lm.pkp.com.landmap.area.res.disp;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Created by USER on 11/4/2017.
 */
public class PagerAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs;

    public PagerAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                ImageDisplayFragment idf = new ImageDisplayFragment();
                return idf;
            case 1:
                VideoDisplayFragment vdf = new VideoDisplayFragment();
                return vdf;
            case 2:
                DocumentDisplayFragment ddf = new DocumentDisplayFragment();
                return ddf;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}