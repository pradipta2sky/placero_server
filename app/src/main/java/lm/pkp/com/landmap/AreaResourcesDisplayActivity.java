package lm.pkp.com.landmap;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import lm.pkp.com.landmap.area.res.disp.AreaDocumentDisplayFragment;
import lm.pkp.com.landmap.area.res.disp.AreaPictureDisplayFragment;
import lm.pkp.com.landmap.area.res.disp.AreaVideoDisplayFragment;
import lm.pkp.com.landmap.custom.GenericActivityExceptionHandler;

/**
 * Created by USER on 11/4/2017.
 */
public class AreaResourcesDisplayActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new GenericActivityExceptionHandler(this);

        setContentView(R.layout.activity_area_res_display);

        // Setup Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_ar_res_disp);
        setSupportActionBar(toolbar);

        ViewPager viewPager = (ViewPager) findViewById(R.id.area_tab_pager);
        // Assign created adapter to viewPager
        viewPager.setAdapter(new TabsExamplePagerAdapter(getSupportFragmentManager()));

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        // This method setup all required method for TabLayout with Viewpager
        tabLayout.setupWithViewPager(viewPager);
    }

    public static class TabsExamplePagerAdapter extends FragmentPagerAdapter {
        // As we are implementing two tabs
        private static final int NUM_ITEMS = 3;

        public TabsExamplePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        // For each tab different fragment is returned
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new AreaPictureDisplayFragment();
                case 1:
                    return new AreaVideoDisplayFragment();
                case 2:
                    return new AreaDocumentDisplayFragment();
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return NUM_ITEMS;

        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Images";
                case 1:
                    return "Videos";
                case 2:
                    return "Documents";
                default:
                    return null;
            }
        }
    }
}
